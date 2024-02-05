package pkg.deepCurse.pandora.common.util.callbacks;

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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import pkg.deepCurse.pandora.common.GrueDamageSource;
import pkg.deepCurse.pandora.common.PandoraConfig;
import pkg.deepCurse.pandora.common.PandoraConfig.Debug;
import pkg.deepCurse.pandora.common.PandoraConfig.Server;
import pkg.deepCurse.pandora.common.PandoraConfig.Server.DifficultySettingsEnum;
import pkg.deepCurse.pandora.common.util.interfaces.PlayerGrueDataInterface;
import pkg.deepCurse.pandora.common.util.managers.EntityCooldownManager;
import pkg.deepCurse.pandora.common.util.tools.PandoraTools;

public class EndServerWorldTickCallback {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(EndServerWorldTickCallback.class);

	private static HashMap<Identifier, EntityCooldownManager> dimensionalCooldownManagerHashMap = new HashMap<>();

	// TODO prevent villagers from walking into the darkness willingly, optimize
	// their pathfinding for light

	// TODO bottle o ghast tears
	// TODO glow squids

	public static void onEndTick(ServerWorld world) {
		world.getProfiler().push("pandoraEndTickCallback");

		var dimensionKey = world.getDimensionKey().getValue();
		var dimensionalCooldownManager = dimensionalCooldownManagerHashMap.get(dimensionKey);
		if (dimensionalCooldownManager == null) {
			dimensionalCooldownManagerHashMap.put(dimensionKey, new EntityCooldownManager());
			return;
		}

		var serverSettings = PandoraConfig.Server.SERVER;
		var dimensionSettings = serverSettings.DimensionSettings.get(dimensionKey);
		var difficultySettingsEnum = PandoraConfig.Server.DifficultySettingsEnum.getFromWorld(world);
		var difficultySettings = serverSettings.DifficultySettingsMap.get(difficultySettingsEnum);

		if (dimensionSettings.infested && difficultySettings.damageAmount != 0f) {
			Iterator<Entity> entities = world.iterateEntities().iterator();
			while (entities.hasNext()) {
				var worldRandomFloat = world.random.nextFloat();
				Entity entity = entities.next();

				if (!(entity instanceof LivingEntity)) {
					if (entity.getType() == EntityType.ITEM && serverSettings.GruesEatItems
							&& worldRandomFloat < serverSettings.GruesEatItemsChance) {
						entity.kill();
					}
					continue;
				}

				LivingEntity livingEntity = (LivingEntity) entity;

				if (livingEntity.getType() == EntityType.PLAYER) {
					var playerEntity = (PlayerEntity) livingEntity;
					if (playerEntity.isCreative()) {
						continue;
					}
					if (world.getServer().isHardcore() && serverSettings.Player_UsesHardcoreDifficulty)
						difficultySettings = serverSettings.DifficultySettingsMap.get(DifficultySettingsEnum.Hardcore);
					difficultySettings.damageAmount *= serverSettings.Player_DamageMultiplier;
				}

				if (livingEntity.getType() != EntityType.PLAYER) {
					// TODO config this
					if (worldRandomFloat < 0.9f /* 90% chance to not attack */) {
						continue;
					}
					var mob_settings = serverSettings.MobSettings.get(Registry.ENTITY_TYPE.getId(entity.getType()));
					if (mob_settings == null) {
						continue;
					}
					if (world.getServer().isHardcore() && mob_settings.isHardcore)
						difficultySettings = serverSettings.DifficultySettingsMap.get(DifficultySettingsEnum.Hardcore);
					difficultySettings.damageAmount *= mob_settings.damageMultiplier;
				}

				if (livingEntity.isOnFire()) {
					log.info("on fire");
					continue;
				}
				
				// if uses fully submerged it can get annoying while swimming
				if (livingEntity.isTouchingWater() && !dimensionSettings.gruesAttackInWater) {
					continue;
				}

				// TODO make this configurable
				if (livingEntity.getActiveStatusEffects().containsKey(StatusEffects.NIGHT_VISION)) {
					continue;
				}

				BlockPos entityLocation = livingEntity.getBlockPos();

				// soul sand patch
				if (!world.getBlockState(entityLocation).isAir()) {
					entityLocation = entityLocation.up();
				}

				// suffocation patch
				if (livingEntity.isInsideWall()) {
					continue;
				}

				if (PandoraTools.isNearLight(world, entityLocation, dimensionSettings.minimumSafeLightLevel)) {
					continue;
				}

				// TODO config this attack chance in difficulty settings
				if (PandoraTools.isNearLight(world, entityLocation, dimensionSettings.minimumFadeLightLevel)
						&& worldRandomFloat < 0.60f/* 60% chance to fail */) {
					continue;
				}

				// if a block does not exist
				// within a centered 3x3x5 rectangle below the player dont do damage
				{
					boolean blockFound = false;
					var locationArray = new BlockPos[] { entityLocation.north().east(), entityLocation.north(),
							entityLocation.north().west(), entityLocation.east(), entityLocation, entityLocation.west(),
							entityLocation.south().east(), entityLocation.south(), entityLocation.south().west(), };
					for (BlockPos blockLocation : locationArray) {
						/* TODO config value "height immunity" or something */
						for (short ij = 5; ij > 0; ij--) {
							blockLocation = blockLocation.down();
							var location = world.getBlockState(blockLocation);
							if (!location.isAir()) {
								blockFound = true;
								break;
							}
							if (blockFound) {
								break;
							}
						}
					}
					if (!blockFound) {
						continue;
					}
				}

				// TODO enchantments etc, nbt tags?
				// get strongest ward on player
				// perhaps change this to average out all valid wards?
				// idk, do something so it cant be broken by some golden shoes or something
				float wardPotency = 0f;
				Iterator<ItemStack> itemStack = entity.getItemsEquipped().iterator();
				while (itemStack.hasNext()) {
					var item = itemStack.next().getItem();
					var wardSettings = Server.SERVER.GrueWardSettings.get(Registry.ITEM.getId(item));
					if (wardSettings != null) {
						if (wardPotency < wardSettings.potency) {
							wardPotency = wardSettings.potency;
						}
					}
				}

				if (Debug.DEBUG.forceGruesAlwaysAttack) {
					worldRandomFloat = 1;
					wardPotency = 0f;
				}

				if (worldRandomFloat < wardPotency) {
					continue;
				}

				if (!dimensionalCooldownManager.isCoolingDown(entity)) {
					// get random number between minTickWait and maxTickWait, but we cant do 0 to 0
					// with nextInt there so we need to check if 0 to 0 will happen and just give 0
					// instead of calling random
					var maxCooldownTime = difficultySettings.grueMaxTickWait - difficultySettings.grueMinTickWait;
					var cooldownTime = (maxCooldownTime == 0 ? 0 : world.getRandom().nextInt(maxCooldownTime))
							+ difficultySettings.grueMinTickWait;

					// if player has a tutorial and the grace period has passed since the last
					// tutorial, skip damage and give tutorial

					boolean hasTutorial = false;
					if (entity instanceof PlayerEntity) {
						var playerEntity = (PlayerEntity) entity;
						PlayerGrueDataInterface playerDataInterface = (PlayerGrueDataInterface) playerEntity;

						// TODO gamerule for grue tutorials?
						if (playerDataInterface.getTutorialEncountersLeft() > 0
								&& playerDataInterface.getLastTutorialEncounterTime()
										+ difficultySettings.grueTutorialGracePeriod < world.getTime()) {

							hasTutorial = true;
							playerDataInterface.setTutorialEncountersLeft(
									(short) (playerDataInterface.getTutorialEncountersLeft() - 1));
							playerDataInterface.setLastTutorialEncounterTime(world.getTime());
							playerEntity.sendMessage(Text.translatable("pandora.grue.tutorial"));
							dimensionalCooldownManager.set(entity, cooldownTime + 200);
						}

					}

					if (!hasTutorial) {
						entity.damage(GrueDamageSource.GRUE, difficultySettings.damageAmount);

						dimensionalCooldownManager.set(entity, cooldownTime);
					}
				}
				dimensionalCooldownManager.update(entity);
			}
		}
		world.getProfiler().pop();
	}
}
