package pkg.deepCurse.pandora.core.mixins.shared;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoulFireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

@Mixin(SoulFireBlock.class)
public class SoulFireBlockMixin {

	@Inject(method = "canPlaceAt", at = @At(value = "RETURN"), cancellable = true)
	private void canPlaceAtOverride(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
	}

}
