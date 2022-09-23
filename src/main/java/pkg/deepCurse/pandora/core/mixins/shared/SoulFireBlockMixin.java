package pkg.deepCurse.pandora.core.mixins.shared;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoulFireBlock;

@Mixin(SoulFireBlock.class)
public class SoulFireBlockMixin {

	@Inject(method = "isSoulBase", at = @At(value = "RETURN"), cancellable = true)
	private static void isSoulBaseOverride(BlockState state, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(true);
	}

}
