package pkg.deepCurse.pandora.client.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.source.BiomeAccess;
import pkg.deepCurse.pandora.common.PandoraConfig.Client;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {

	@WrapOperation(method = "method_24873", at = @At(value = "INVOKE", target = "net/minecraft/client/render/DimensionEffects.adjustFogColor(Lnet/minecraft/util/math/Vec3d;F)Lnet/minecraft/util/math/Vec3d;"))
	private static Vec3d overrideFog(DimensionEffects effects, Vec3d color, float f, Operation<Vec3d> operation,
			ClientWorld world, BiomeAccess access, float sunHeight, int i, int j, int k) {

		var settings = Client.CLIENT.DimensionSettings.get(world.getDimensionKey().getValue());

		var result = operation.call(effects, color, f);

		if (settings != null) {
			final float MIN = 0;
			return new Vec3d(Math.max(MIN, result.x * settings.fogLevel), Math.max(MIN, result.y * settings.fogLevel),
					Math.max(MIN, result.z * settings.fogLevel));
		}

		return result;
	}
}
