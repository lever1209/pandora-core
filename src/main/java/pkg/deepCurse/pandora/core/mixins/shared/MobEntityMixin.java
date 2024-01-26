package pkg.deepCurse.pandora.core.mixins.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import pkg.deepCurse.pandora.core.PandoraConfig;
import pkg.deepCurse.pandora.core.util.ai.goals.EscapeGrueGoal;

@Mixin(MobEntity.class) // TODO modify all wander goals to prioritize light over dark, but not to make
						// it a requirement
public class MobEntityMixin {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(MobEntityMixin.class);

	@Shadow
	private GoalSelector goalSelector;

	@Shadow
	private BlockPos positionTarget;

	@Inject(method = "<init>*", at = @At(value = "RETURN")) // TODO fix this, using the constructor does not guarantee
															// the goal will stay if a custom entity clears its goals
	private void pandora_constructorReturn(EntityType<? extends MobEntity> entityType, World world, CallbackInfo ci) { // portal
																														// to
																														// SheepEntity
																														// and
																														// VillagerEntity
		MobEntity self = (MobEntity) (Object) this;
		// TODO give a slight regen effect when near light?
		var type = self.getType();
		var identifier = Registry.ENTITY_TYPE.getId(type);
		var mobSettings = PandoraConfig.General.MobSettings.get(identifier);

//		log.info("initGoals: {} {} {}", type, identifier, mobSettings);

		// TODO add support for prioritizing safe over fade, but allow fade
		// self instanceof PathAwareEntity &&
		if (mobSettings != null && mobSettings.FearDarkness) {
			goalSelector.add(0, new EscapeGrueGoal(self, 1D/* TODO patch this speed value to load from config? */,
					PandoraConfig.General.MinimumSafeLightLevel));
		}
	}

	// This method was supposed to prevent entities from walking into the darkness
	// unless absolutely necissary
	// FIXME bug where entities spin in place after moving one block
	// @Inject(method =
	// "getPathfindingPenalty(Lnet/minecraft/entity/ai/pathing/PathNodeType;)F", at
	// = @At(value = "RETURN"))
	// private float getPathFindingPenaltyOverride(PathNodeType nodeType,
	// CallbackInfoReturnable<Float> cir) {

	// if (PandoraTools.isNearLight(((Entity) (Object) this).world, positionTarget))
	// {
	// return cir.getReturnValueF();
	// }
	// return 16F;
	// }
}
