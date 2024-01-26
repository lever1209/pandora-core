package pkg.deepCurse.pandora.core.util.callbacks;

import java.util.*;

import org.slf4j.*;

import net.minecraft.entity.*;
import net.minecraft.entity.effect.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.server.world.*;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.Registry;
import pkg.deepCurse.pandora.core.*;
import pkg.deepCurse.pandora.core.PandoraConfig.Debug;
import pkg.deepCurse.pandora.core.PandoraConfig.General;
import pkg.deepCurse.pandora.core.util.managers.*;
import pkg.deepCurse.pandora.core.util.tools.*;

public class EndServerTickCallback {

	private static Logger log = LoggerFactory.getLogger(EndServerTickCallback.class);

	private static HashMap<ServerWorld, EntityCooldownManager> dimensionalCooldownManagerHashMap = new HashMap<>();

	public static void run(ServerWorld world) {
		var dimensionalCooldownManager = dimensionalCooldownManagerHashMap.get(world);

		if (dimensionalCooldownManager == null) {
			dimensionalCooldownManagerHashMap.put(world, new EntityCooldownManager());
			return;
		}

		Iterator<Entity> entities = world.iterateEntities().iterator();
		while (entities.hasNext()) {
			Entity entity = entities.next();

			if (shouldDoDamage(entity, world)) {
				if (!dimensionalCooldownManager.isCoolingDown(entity)) {

					var rand = PandoraConfig.Debug.GrueMaximumTickWait - Debug.GrueMinimumTickWait;

					var wait = (rand == 0 ? 0 : world.getRandom().nextInt(rand))
							+ PandoraConfig.Debug.GrueMinimumTickWait;

					if (entity instanceof PlayerEntity) {
						log.info("{} in {} for {}", entity.getEntityName(), entity.getWorld().getDimensionKey(), ((float) wait) / 20f);
					}
					doDarknessDamage(entity, 0.0F, world);
					dimensionalCooldownManager.set(entity, wait);
				}

				dimensionalCooldownManager.update(entity);
			}
//			if (entity instanceof PlayerEntity) {
//			log.info("{}", dimensionalCooldownManager.getCooldownProgress(entity, 0));
//			}
		}
	}

	private static boolean shouldDoDamage(Entity entity, ServerWorld world) {
		if (!(entity instanceof LivingEntity)) {
			return false;
		}

		if (!General.DimensionSettings.get(world.getRegistryKey().getValue()).Infested) {
			return false;
		}

		if (entity instanceof PlayerEntity) {
			if (((PlayerEntity) entity).isCreative())
				return false;
		}

		if (entity.isSubmergedInWater() && !PandoraConfig.General.GruesAttackInWater)
			return false;

		if (((LivingEntity) entity).getActiveStatusEffects().containsKey(StatusEffects.NIGHT_VISION)) // TODO make this
			// configurable
			return false;

		BlockPos entityLocation = entity.getBlockPos();

		if (!world.getBlockState(entityLocation).isAir()) { // TODO prevent villagers from walking into the darkness
															// willingly, optimize their pathfinding for light
			entityLocation = entityLocation.up();
		} // patch for soul sand since the poll location is the center of the feet, which
			// sinks into the soul sand, meaning if you stand on soul sand you are always in
			// 0 light

		if (PandoraTools.isNearLight(world, entityLocation, PandoraConfig.General.MinimumSafeLightLevel))
			return false;

		if (PandoraTools.isNearLight(world, entityLocation, PandoraConfig.General.MinimumFadeLightLevel)
				&& world.random.nextFloat() > 0.85f) { // TODO use cooldown manager here
			return false;
		}

		return true;
	}

	private static void doDarknessDamage(Entity entity, float damageAmount, ServerWorld world) {
		if (damageAmount <= 0.0F) {
			switch (world.getDifficulty()) {
			case HARD:
				damageAmount = 8.0F;
				break;
			case NORMAL:
				damageAmount = 4.0F;
				break;
			case EASY:
				damageAmount = 2.0F;
				break;
			case PEACEFUL:
				damageAmount = 1.0F;
			}
		}

		Double wardPotency = 0D;
		float nonPlayerAttackChance = world.getRandom().nextFloat();

		if (PandoraConfig.General.Enabled.EnableGrueWards) {
			Iterator<ItemStack> itemStack = entity.getItemsEquipped().iterator();

			while (itemStack.hasNext()) {
				for (var i : PandoraConfig.General.GrueWards.entrySet()) { //
					if (itemStack.next().getRegistryEntry().matchesId(i.getKey())) {
						wardPotency = i.getValue().Potency;
					}
				}
			}
		}

		if (PandoraConfig.Debug.ForceGruesAlwaysAttack) {
			nonPlayerAttackChance = 1;
			wardPotency = 0D;
		}

		// TODO bottle o ghast tears

		if (entity.getType() == EntityType.ITEM && (nonPlayerAttackChance <= 0.003D // 0.3% chance
				&& PandoraConfig.General.GruesEatItems)) {
			entity.kill();
			return;
		} else if (!(entity instanceof PlayerEntity)) {
			if (nonPlayerAttackChance < 0.90D) { // 90% chance
				return;
			}
			var mob_settings = PandoraConfig.General.MobSettings.get(Registry.ENTITY_TYPE.getId(entity.getType()));
			if (mob_settings == null)
				return;
			damageAmount *= mob_settings.DamageMultiplier;
		}

		if (world.getServer().isHardcore()
				&& (PandoraConfig.General.HardcoreAffectsOtherMobs || entity instanceof PlayerEntity))
			damageAmount = Float.MAX_VALUE;

		if (world.random.nextFloat() > wardPotency && damageAmount != 0f) {
			entity.damage(GrueDamageSource.GRUE, damageAmount); // TODO glow squids
		}
	}
}
