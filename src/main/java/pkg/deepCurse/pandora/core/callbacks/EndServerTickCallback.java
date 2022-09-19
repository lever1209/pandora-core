package pkg.deepCurse.pandora.core.callbacks;

import java.util.Iterator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import pkg.deepCurse.pandora.core.CustomDamageSources;
import pkg.deepCurse.pandora.core.PandoraConfig;
import pkg.deepCurse.pandora.core.managers.EntityCooldownManager;
import pkg.deepCurse.pandora.tools.PandoraTools;

public class EndServerTickCallback {

	private static EntityCooldownManager cooldownManager = new EntityCooldownManager();

	public static void run(ServerWorld world) {

		cooldownManager.update();

		Iterator<Entity> entities = world.iterateEntities().iterator();
		while (entities.hasNext()) {
			Entity entity = entities.next();

			if (!cooldownManager.isCoolingDown(entity)) {
				doDarknessDamage(entity, 0.0F, world);
				cooldownManager.set(entity,
						world.getRandom().nextInt(60) + 30);
			}
		}
	}

	private static void doDarknessDamage(Entity entity, float damageAmount,
			ServerWorld world) { // FATAL OPTOMIZE ME ASAP

		if (entity != null) {
			if (entity instanceof PlayerEntity
					|| !PandoraConfig.gruesOnlyAttackPlayers()) {
				if (PandoraConfig.isDynamicLightingEnabled()) {
					Iterator<ItemStack> itemStack = entity.getItemsHand()
							.iterator();
					while (itemStack.hasNext()) {
						if (PandoraConfig.grueWards.contains(
								Registry.ITEM.getId(itemStack.next().getItem())
										.toString())) {
							return;
						}
					}
				}

				float trueDamageAmount = damageAmount;
				if (damageAmount <= 0.0F) {
					switch (world.getDifficulty()) {
						case HARD:
							trueDamageAmount = 8.0F;
							break;
						case NORMAL:
							trueDamageAmount = 4.0F;
							break;
						case EASY:
							trueDamageAmount = 2.0F;
							break;
						case PEACEFUL:
							trueDamageAmount = 1.0F;
					}
				}

				float resetGrueAttackChance = world.getRandom().nextFloat();
				if (PandoraConfig.resetGrueAttackChance) {
					resetGrueAttackChance = 0.0F;
				}

				BlockPos entityLocation = entity.getBlockPos();
				if (!world.getBlockState(entityLocation).isAir()) {
					entityLocation = entityLocation.up();
				}

				if (!PandoraTools.isNearLight(world, entityLocation)) {
					boolean isItem = false;
					if (!(entity instanceof PlayerEntity)) {
						boolean skipRaceDiscovery = false;
						if (!skipRaceDiscovery
								&& entity instanceof HostileEntity) {
							if (!PandoraConfig.gruesCanAttackHostileMobs) {
								return;
							}

							skipRaceDiscovery = true;
							trueDamageAmount /= 2.0F;
						}

						if (!skipRaceDiscovery
								&& entity instanceof VillagerEntity) {
							if (!PandoraConfig.gruesCanAttackVillagers) {
								return;
							}

							skipRaceDiscovery = true;
						}

						if (!skipRaceDiscovery
								&& entity instanceof AnimalEntity) {
							if (!PandoraConfig.gruesCanAttackAnimals) {
								return;
							}

							skipRaceDiscovery = true;
						}

						if (entity.getType() == EntityType.ITEM) {
							isItem = true;
						}

						if (!(entity instanceof LivingEntity) && !isItem) {
							return;
						}

						if (isItem) {
							if ((double) resetGrueAttackChance <= 0.00015D
									&& PandoraConfig.gruesCanEatItems) {
								entity.kill();
								return;
							}

							return;
						}

						if ((double) resetGrueAttackChance > 0.045D) {
							return;
						}
					} else if (entity instanceof PlayerEntity) {
						if (((PlayerEntity) entity).isCreative()) {
							return;
						}
					}

					if (!(entity instanceof LivingEntity)
							|| !((LivingEntity) entity).getActiveStatusEffects()
									.containsKey(StatusEffects.NIGHT_VISION)) { // TODO remove night vision when grues are entities
						if (!entity.isSubmergedInWater()
								|| PandoraConfig.gruesCanAttackInWater) {
							if (!PandoraConfig.blacklistedEntityType.contains(
									Registry.ENTITY_TYPE.getId(entity.getType())
											.toString())) {
								if (world.getServer().isHardcore()
										&& (PandoraConfig.hardcoreAffectsOtherMobs
												|| entity instanceof PlayerEntity)) {
									entity.damage(CustomDamageSources.GRUE,
											Float.MAX_VALUE);
								} else {
									entity.damage(CustomDamageSources.GRUE,
											trueDamageAmount);
								}
							}
						}
					}
				}
			}
		}
	}
}
