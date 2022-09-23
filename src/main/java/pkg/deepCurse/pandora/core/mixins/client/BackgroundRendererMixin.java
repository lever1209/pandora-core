package pkg.deepCurse.pandora.core.mixins.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.source.BiomeAccess;
import pkg.deepCurse.pandora.core.PandoraConfig;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {
	
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(BackgroundRendererMixin.class);

	@WrapOperation(method = "method_24873", at = @At(value = "INVOKE", target = "net/minecraft/client/render/DimensionEffects.adjustFogColor(Lnet/minecraft/util/math/Vec3d;F)Lnet/minecraft/util/math/Vec3d;"))
	private static Vec3d overrideFog(DimensionEffects effects, Vec3d color, float f, Operation<Vec3d> operation,
			ClientWorld world, BiomeAccess access,
			float sunHeight, int i, int j, int k) {

		// boolean isNearLight = PandoraTools.isNearLight(world,
		// MinecraftClient.getInstance().player.getBlockPos(), 5);
		// TODO use to darken fog while not near light
				
		return PandoraConfig.effectiveDimensions.getOrDefault(world.getDimensionKey().getValue(), (e, c, f2, o, w, a, s, i2, j2, k2) -> o).calculate(effects, color, f, operation.call(effects, color, f), world, access, sunHeight, i, j, k);
	}
}
