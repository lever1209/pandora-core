package pkg.deepCurse.pandora.core.util.callbacks;

import java.util.*;

import org.slf4j.*;

import net.minecraft.entity.*;
import net.minecraft.entity.effect.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.server.world.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.*;
import pkg.deepCurse.pandora.core.*;
import pkg.deepCurse.pandora.core.util.managers.*;
import pkg.deepCurse.pandora.core.util.tools.*;

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
				// FIXME re test all values here because i heavily messed with some variables and changed logic

		if (!(entity instanceof LivingEntity)) {
			return;
		}

		Double wardPotency = 0D;
		float nonPlayerAttackChance = world.getRandom().nextFloat();

		if (PandoraConfig.ENABLE_GRUE_WARDS) {
			Iterator<ItemStack> itemStack = entity.getItemsEquipped().iterator();

			while (itemStack.hasNext()) {
				for (Pair<ArrayList<Identifier>, Double> i : PandoraConfig.GRUE_WARDS) {
					for (Identifier j : i.getLeft()) {
						if (itemStack.next().getRegistryEntry().matchesId(j)) {
							wardPotency = i.getRight();
						}
					}
				}
			}
		}

		if (PandoraConfig.FORCE_GRUES_ALWAYS_ATTACK) {
			nonPlayerAttackChance = 1;
			wardPotency = 0D;
		}

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

		if (!world.getBlockState(entityLocation).isAir()) {
			entityLocation = entityLocation.up();
		} // patch for soul sand

		if (entity instanceof PlayerEntity) {
			if (((PlayerEntity) entity).isCreative()) {
				return;
			}
		} else {
			if (PandoraConfig.ANIMALS.contains(Registry.ENTITY_TYPE.getId(entity.getType())) && !PandoraConfig.GRUES_ATTACK_HOSTILE_MOBS) {
				damageAmount /= 2.0F;
				return;
			}

			if (PandoraConfig.ANIMALS.contains(Registry.ENTITY_TYPE.getId(entity.getType())) && !PandoraConfig.GRUES_ATTACK_VILLAGERS) {
				return;
			}

			if (PandoraConfig.ANIMALS.contains(Registry.ENTITY_TYPE.getId(entity.getType())) && !PandoraConfig.GRUES_ATTACK_ANIMALS) {
				return;
			}

			if (entity.getType() == EntityType.ITEM) {
				if (nonPlayerAttackChance <= 0.003D // 0.3% chance
						&& PandoraConfig.GRUES_EAT_ITEMS) {
					entity.kill();
					return;
				}
				return;
			}

			if (nonPlayerAttackChance < 0.90D) { // 90% chance
				return;
			}
		}

		if (((LivingEntity) entity).getActiveStatusEffects()
				.containsKey(StatusEffects.NIGHT_VISION))
			return;

		if (entity.isSubmergedInWater() && !PandoraConfig.GRUES_ATTACK_IN_WATER)
			return;

		if (!PandoraConfig.moblistContains(Registry.ENTITY_TYPE.getId(entity.getType())))
			return;

		if (world.getServer().isHardcore()
				&& (PandoraConfig.HARDCORE_AFFECTS_OTHER_MOBS
						|| entity instanceof PlayerEntity)) {
			damageAmount = Float.MAX_VALUE;

		}

		if (wardPotency == 0 || (world.random.nextFloat() > wardPotency)) {
			entity.damage(CustomDamageSources.GRUE,
					damageAmount);
		}
	}
}
