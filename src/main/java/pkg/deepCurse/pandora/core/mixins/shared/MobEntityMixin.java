package pkg.deepCurse.pandora.core.mixins.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import pkg.deepCurse.pandora.ai.goals.EscapeGrueGoal;
import pkg.deepCurse.pandora.core.PandoraConfig;
import pkg.deepCurse.pandora.core.PandoraConfig.PandoraConfigEnum;
import pkg.deepCurse.pandora.tools.PandoraTools;

@Mixin(MobEntity.class) // TODO change to PathAwareEntity?
public class MobEntityMixin {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(MobEntityMixin.class);

	@Shadow
	private GoalSelector goalSelector;

	@Shadow
	private BlockPos positionTarget;

	@Inject(method = "initGoals()V", at = @At(value = "RETURN"))
	private void appendGoals(CallbackInfo ci) {
		MobEntity self = (MobEntity) (Object) this;
		if (self instanceof PathAwareEntity && PandoraTools.shouldFearDarkness(self)) {
			goalSelector.add(0, new EscapeGrueGoal(self, 1.2D,
					PandoraConfig.getInt(PandoraConfigEnum.minimumSafeLightLevel)));
		}
	}

	// This method was supposed to prevent entities from walking into the darkness unless absolutely necissary
	// FIXME bug where entities spin in place after moving one block
	// @Inject(method = "getPathfindingPenalty(Lnet/minecraft/entity/ai/pathing/PathNodeType;)F", at = @At(value = "RETURN"))
	// private float getPathFindingPenaltyOverride(PathNodeType nodeType, CallbackInfoReturnable<Float> cir) {
		
	// 	if (PandoraTools.isNearLight(((Entity) (Object) this).world, positionTarget)) {
	// 		return cir.getReturnValueF();
	// 	}
	// 	return 16F;
	// }
}
