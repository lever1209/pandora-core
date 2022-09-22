package pkg.deepCurse.pandora.core.util.callbacks;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import pkg.deepCurse.pandora.core.PandoraConfig.PandoraConfigEnum;
import pkg.deepCurse.pandora.core.util.managers.EntityCooldownManager;
import pkg.deepCurse.pandora.core.util.tools.PandoraTools;

public class EndServerTickCallback {
	
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(EndServerTickCallback.class);

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
			ServerWorld world) { // TODO optimize a bit more at some point

		if (!(entity instanceof PlayerEntity)
				&& PandoraConfig.gruesOnlyAttackPlayers())
			return;

		if (PandoraConfig.wardsEnabled()) {
			Iterator<ItemStack> itemStack = entity.getItemsHand().iterator();
			while (itemStack.hasNext()) {
				if (PandoraConfig.grueWards.contains(
						Registry.ITEM.getId(itemStack.next().getItem())
								.toString())) {
					return;
				}
			}
		} // if wards are enabled, and the player is holding a registered ward, return
			// TODO add "low" chance to allow grue to attack anyway

		BlockPos entityLocation = entity.getBlockPos();
		if (PandoraTools.isNearLight(world, entityLocation))
			return;

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
		float resetGrueAttackChance = world.getRandom().nextFloat();
		if (PandoraConfig.forceGruesAlwaysAttack) {
			resetGrueAttackChance = 0.0F;
		}

		if (!world.getBlockState(entityLocation).isAir()) {
			entityLocation = entityLocation.up();
		} // patch for soul sand

		if (entity instanceof PlayerEntity) {
			if (((PlayerEntity) entity).isCreative()) {
				return;
			}
		} else {
			boolean skipRaceDiscovery = false;
			if (entity instanceof HostileEntity) {
				if (!PandoraConfig.getBoolean(PandoraConfigEnum.gruesCanAttackHostileMobs)) {
					return;
				}
				skipRaceDiscovery = true;
				damageAmount /= 2.0F;
			}

			if (!skipRaceDiscovery
					&& entity instanceof VillagerEntity) {
				if (!PandoraConfig.getBoolean(PandoraConfigEnum.gruesAttackVillagers)) {
					return;
				}
				skipRaceDiscovery = true;
			}

			if (!skipRaceDiscovery
					&& entity instanceof AnimalEntity) {
				if (!PandoraConfig.getBoolean(PandoraConfigEnum.gruesAttackAnimals)) {
					return;
				}

				skipRaceDiscovery = true;
			}

			if (entity.getType() == EntityType.ITEM) {
				if (resetGrueAttackChance <= 0.00015D
						&& PandoraConfig.getBoolean(PandoraConfigEnum.gruesEatItems)) {
					entity.kill();
					return;
				}
				return;
			} else if (!(entity instanceof LivingEntity)) {
				return;
			}

			if (resetGrueAttackChance > 0.045D) { // if not player, and chance is greater than 0.045, return
				return;
			}
		}

		if (((LivingEntity) entity).getActiveStatusEffects()
				.containsKey(StatusEffects.NIGHT_VISION))
			return; // TODO remove night vision when grues are entities

		if (entity.isSubmergedInWater() && !PandoraConfig.getBoolean(PandoraConfigEnum.gruesAttackInWater))
			return;

		if (PandoraConfig.blacklistedEntityType.contains(Registry.ENTITY_TYPE.getId(entity.getType()).toString()))
			return;

		if (world.getServer().isHardcore()
				&& (PandoraConfig.getBoolean(PandoraConfigEnum.hardcoreAffectsOtherMobs)
						|| entity instanceof PlayerEntity))
			damageAmount = Float.MAX_VALUE;

		entity.damage(CustomDamageSources.GRUE,
				damageAmount);
	}
}
