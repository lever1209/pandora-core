package pkg.deepCurse.pandora.common.mixins;

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
import pkg.deepCurse.pandora.common.config.CommonConfig;
import pkg.deepCurse.pandora.common.content.entities.ai.EscapeGrueGoal;

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
															// the goal will stay if an entity clears its goals somehow
	private void pandora_constructorReturn(EntityType<? extends MobEntity> entityType, World world, CallbackInfo ci) {
		MobEntity self = (MobEntity) (Object) this;
		// TODO give a slight regen effect when near light?
		var type = self.getType();
		var identifier = Registry.ENTITY_TYPE.getId(type);
		var mobSettings = CommonConfig.COMMON.MobSettings.get(identifier);
		var dimensionSettings = CommonConfig.COMMON.DimensionSettings.get(world.getDimensionKey().getValue());

		// TODO add support for prioritizing safe over fade, but allow fade
		// ASAP come back here later
		if (mobSettings != null && mobSettings.fearDarkness && dimensionSettings != null
				&& dimensionSettings.infested) {
			goalSelector.add(0, new EscapeGrueGoal(self, 1D/* TODO patch this speed value to load from config? */,
					dimensionSettings.minimumSafeLightLevel));
		}
	}

	// This method was supposed to prevent entities from walking into the darkness
	// unless absolutely necessary
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
