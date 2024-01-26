package pkg.deepCurse.pandora.core.util.callbacks;

import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import pkg.deepCurse.pandora.core.GrueDamageSource;
import pkg.deepCurse.pandora.core.PandoraConfig;
import pkg.deepCurse.pandora.core.PandoraConfig.Debug;
import pkg.deepCurse.pandora.core.PandoraConfig.General;
import pkg.deepCurse.pandora.core.util.interfaces.PlayerGrueDataInterface;
import pkg.deepCurse.pandora.core.util.managers.EntityCooldownManager;
import pkg.deepCurse.pandora.core.util.tools.PandoraTools;

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
						PlayerEntity player = (PlayerEntity) entity;
						log.info("{}, {}", ((PlayerGrueDataInterface) player).getLastEncounterTime(),
								((PlayerGrueDataInterface) player).getTrainingWheelEncountersLeft());
//						log.info("{} in {} for {}", entity.getEntityName(), entity.getWorld().getDimensionKey(), ((float) wait) / 20f);
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

		boolean blockFound = false;
		for (BlockPos location : new BlockPos[] { entityLocation.north().east(), entityLocation.north(),
				entityLocation.north().west(), entityLocation.east(), entityLocation, entityLocation.west(),
				entityLocation.south().east(), entityLocation.south(), entityLocation.south().west(), }) {
			for (short i = 5 /* TODO config value "height immunity" or something */; --i > 0;) {
				location = location.down();
				if (!world.getBlockState(location).isAir()) {
					blockFound = true;
					break;
				}
			}
			if (blockFound)
				break;
		}

		if (!blockFound) {
			// dont deal damage
			return false;
		}

		// deal damage
		return true;
	}

	private static void doDarknessDamage(Entity entity, float damageAmount, ServerWorld world) {
		if (damageAmount <= 0.0F) {
			switch (world.getDifficulty()) { // TODO add difficulty settings to the config
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
			damageAmount = Float.MAX_VALUE; // TODO make this configurable

		if (world.random.nextFloat() > wardPotency && damageAmount != 0f) {
			entity.damage(GrueDamageSource.GRUE, damageAmount); // TODO glow squids
		}
	}
}
