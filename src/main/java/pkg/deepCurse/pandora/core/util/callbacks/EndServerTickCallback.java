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
import pkg.deepCurse.pandora.core.PandoraConfig.General;
import pkg.deepCurse.pandora.core.util.managers.*;
import pkg.deepCurse.pandora.core.util.tools.*;

public class EndServerTickCallback {

	private static Logger log = LoggerFactory.getLogger(EndServerTickCallback.class);

	private static EntityCooldownManager cooldownManager = new EntityCooldownManager();

	public static void run(ServerWorld world) {
		Iterator<Entity> entities = world.iterateEntities().iterator();
		while (entities.hasNext()) {
			Entity entity = entities.next();
			
			// log.info("{} {}", Registry.ENTITY_TYPE.getId(entity.getType()), cooldownManager.getCooldownProgress(entity, 0));
			
			if (!cooldownManager.isCoolingDown(entity)) {
				doDarknessDamage(entity, 0.0F, world);
				cooldownManager.set(entity, world.getRandom().nextInt(100) + 40);
			}

			cooldownManager.update(); // ASAP COOLDOWN MANAGER BROKEY
		}
	}

	private static void doDarknessDamage(Entity entity, float damageAmount, ServerWorld world) { // TODO optimize a bit
																									// more at some
																									// point
		// FIXME re test all values here because i heavily messed with some variables
		// and changed logic

		if (!(entity instanceof LivingEntity)) {
			return;
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
		
		if (!General.DimensionSettings.get(world.getRegistryKey().getValue()).Infested) {
			return;
		}
		
		BlockPos entityLocation = entity.getBlockPos();
		if (PandoraTools.isNearLight(world, entityLocation, PandoraConfig.General.MinimumSafeLightLevel))
			return;
		if (PandoraTools.isNearLight(world, entityLocation, PandoraConfig.General.MinimumFadeLightLevel)
				&& world.random.nextFloat() > 0.85f) { // TODO use cooldown manager here
			return;
		}

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

		if (!world.getBlockState(entityLocation).isAir()) { // TODO prevent villagers from walking into the darkness
															// willingly, optimize their pathfinding for light
			entityLocation = entityLocation.up();
		} // patch for soul sand since the poll location is the center of the feet, which
			// sinks into the soul sand, meaning if you stand on soul sand you are always in
			// 0 light

		// TODO bottle o ghast tears

		if (entity instanceof PlayerEntity) {
			if (((PlayerEntity) entity).isCreative())
				return;
		} else {
			if (entity.getType() == EntityType.ITEM) {
				if (nonPlayerAttackChance <= 0.003D // 0.3% chance
						&& PandoraConfig.General.GruesEatItems) {
					entity.kill();
					return;
				}

			} else {
				var mob_settings = PandoraConfig.General.MobSettings.get(Registry.ENTITY_TYPE.getId(entity.getType()));

				if (mob_settings == null)
					return;

				damageAmount *= mob_settings.DamageMultiplier;

			}
			
			if (nonPlayerAttackChance < 0.90D) { // 90% chance
				return;
			}
		}

		if (((LivingEntity) entity).getActiveStatusEffects().containsKey(StatusEffects.NIGHT_VISION)) // TODO make this
																										// configurable
			return;

		if (entity.isSubmergedInWater() && !PandoraConfig.General.GruesAttackInWater)
			return;

		if (world.getServer().isHardcore()
				&& (PandoraConfig.General.HardcoreAffectsOtherMobs || entity instanceof PlayerEntity))
			damageAmount = Float.MAX_VALUE;

		if (world.random.nextFloat() > wardPotency && damageAmount != 0f) {
			entity.damage(CustomDamageSources.GRUE, damageAmount); // TODO glow squids
		}
	}
}
