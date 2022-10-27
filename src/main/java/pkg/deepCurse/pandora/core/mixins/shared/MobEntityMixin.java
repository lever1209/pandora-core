package pkg.deepCurse.pandora.core.mixins.shared;

import org.slf4j.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.mob.*;
import net.minecraft.util.math.*;
import pkg.deepCurse.pandora.core.*;
import pkg.deepCurse.pandora.core.util.ai.goals.*;
import pkg.deepCurse.pandora.core.util.tools.*;

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
					PandoraConfig.MINIMUM_SAFE_LIGHT_LEVEL)); // TODO add support for prioritizing safe over fade, but allow fade
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
