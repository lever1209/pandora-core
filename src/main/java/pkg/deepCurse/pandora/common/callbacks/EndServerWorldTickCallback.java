package pkg.deepCurse.pandora.common.callbacks;

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
import pkg.deepCurse.pandora.common.CommonTools;
import pkg.deepCurse.pandora.common.config.CommonConfig;
import pkg.deepCurse.pandora.common.config.CommonConfig.DifficultySettingsEnum;
import pkg.deepCurse.pandora.common.config.DebugConfig;
import pkg.deepCurse.pandora.common.content.GrueDamageSource;
import pkg.deepCurse.pandora.common.interfaces.PlayerGrueDataInterface;
import pkg.deepCurse.pandora.common.util.CooldownTracker;

public class EndServerWorldTickCallback {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(EndServerWorldTickCallback.class);

	private static HashMap<Identifier, CooldownTracker<Entity>> dimensionalCooldownManagerHashMap = new HashMap<>();

	// TODO prevent villagers from walking into the darkness willingly, optimize
	// their pathfinding for light

	// TODO bottle o ghast tears
	// TODO glow squids

	public static void onEndTick(ServerWorld world) {

		var dimensionKey = world.getDimensionKey().getValue();
		var dimensionalCooldownManager = dimensionalCooldownManagerHashMap.get(dimensionKey);
		if (dimensionalCooldownManager == null) {
			dimensionalCooldownManagerHashMap.put(dimensionKey, new CooldownTracker<Entity>());
			return;
		}

		world.getProfiler().push("pandoraEndTickCallback");

		var commonSettings = CommonConfig.COMMON;
		var dimensionSettings = commonSettings.DimensionSettings.get(dimensionKey);
		var difficultySettingsEnum = CommonConfig.DifficultySettingsEnum.getFromDifficulty(world.getDifficulty());
		var difficultySettings = commonSettings.DifficultySettingsMap.get(difficultySettingsEnum);

		if (dimensionSettings.infested && difficultySettings.damageAmount != 0f) {
			Iterator<Entity> entities = world.iterateEntities().iterator();
			while (entities.hasNext()) {
				var worldRandomFloat = world.random.nextFloat();
				Entity entity = entities.next();

				float wardPotency = 0f;
				if (DebugConfig.DEBUG.forceGruesAlwaysAttack) {
					worldRandomFloat = 1;
					wardPotency = 0f;
				}

				if (!(entity instanceof LivingEntity)) {
					if (entity.getType() == EntityType.ITEM && commonSettings.GruesEatItems
							&& worldRandomFloat < commonSettings.GruesEatItemsChance) {
						entity.kill();
					}
					continue;
				}

				LivingEntity livingEntity = (LivingEntity) entity;

				float modifiedDamageAmount = difficultySettings.damageAmount;

				if (livingEntity.getType() == EntityType.PLAYER) {
					var playerEntity = (PlayerEntity) livingEntity;
					if (playerEntity.isCreative() || playerEntity.isSpectator()) {
						continue;
					}
					if (world.getServer().isHardcore() && commonSettings.Player_UsesHardcoreDifficulty)
						difficultySettings = commonSettings.DifficultySettingsMap.get(DifficultySettingsEnum.Hardcore);
					modifiedDamageAmount *= commonSettings.Player_DamageMultiplier;
				}

				if (livingEntity.getType() != EntityType.PLAYER) {
					var mobSettings = commonSettings.MobSettings.get(Registry.ENTITY_TYPE.getId(entity.getType()));
					if (mobSettings == null) {
						continue;
					}
					if (worldRandomFloat < mobSettings.attackChance) {
						continue;
					}
					if (world.getServer().isHardcore() && mobSettings.isHardcore)
						difficultySettings = commonSettings.DifficultySettingsMap.get(DifficultySettingsEnum.Hardcore);
					modifiedDamageAmount *= mobSettings.damageMultiplier;
				}

				if (livingEntity.isOnFire()) {
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

				if (CommonTools.isNearLight(world, entityLocation, dimensionSettings.minimumSafeLightLevel)) {
					continue;
				}

				// TODO config this attack chance in difficulty settings
				if (CommonTools.isNearLight(world, entityLocation, dimensionSettings.minimumFadeLightLevel)
						&& worldRandomFloat < dimensionSettings.fadeLightLevelAttackChance) {
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
				Iterator<ItemStack> itemStack = entity.getItemsEquipped().iterator();
				while (itemStack.hasNext()) {
					var item = itemStack.next().getItem();
					var wardSettings = CommonConfig.COMMON.GrueWardSettings.get(Registry.ITEM.getId(item));
					if (wardSettings != null) {
						if (wardPotency < wardSettings.potency) {
							wardPotency = wardSettings.potency;
						}
					}
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
								&& (playerDataInterface.getLastTutorialEncounterTime()
										+ difficultySettings.grueTutorialGracePeriod < world.getTime()
										|| playerDataInterface.skipTimeCheck())) { // the default time check could
																					// instead be set to a negative
																					// number, but this feels more solid

							hasTutorial = true;

							playerDataInterface.setTutorialEncountersLeft(
									(short) (playerDataInterface.getTutorialEncountersLeft() - 1));
							playerDataInterface.setLastTutorialEncounterTime(world.getTime());
							playerDataInterface.setSkipTimeCheck(false);

							playerEntity.sendMessage(Text.translatable("pandora.grue.tutorial"));
							dimensionalCooldownManager.set(entity, cooldownTime + 200);
						}

					}

					if (!hasTutorial) {
						entity.damage(new GrueDamageSource(difficultySettings.gruesBypassArmor,
								difficultySettings.gruesBypassProtection), modifiedDamageAmount);

						dimensionalCooldownManager.set(entity, cooldownTime);
					}
				}
				dimensionalCooldownManager.update(entity);
			}
		}
		world.getProfiler().pop();
	}
}
