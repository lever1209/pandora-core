package pkg.deepCurse.pandora.client.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.source.BiomeAccess;
import pkg.deepCurse.pandora.client.callbacks.ClientEndTickCallback;
import pkg.deepCurse.pandora.client.config.ClientConfig;

@Environment(EnvType.CLIENT)
@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {

	// TODO change how the dark fog works, instead multiplying, change the max
	// darkness to the config value, maybe keep both?

	// TODO lerp input tick delta, min min, max max

	@WrapOperation(method = "method_24873", at = @At(value = "INVOKE", target = "net/minecraft/client/render/DimensionEffects.adjustFogColor(Lnet/minecraft/util/math/Vec3d;F)Lnet/minecraft/util/math/Vec3d;"))
	private static Vec3d pandora_overrideFog(DimensionEffects effects, Vec3d color, float f, Operation<Vec3d> operation,
			ClientWorld world, BiomeAccess access, float sunHeight, int i, int j, int k) {

		var settings = ClientConfig.CLIENT.clientDimensionConfigMap.get(world.getDimensionKey().getValue());

		var result = operation.call(effects, color, f);

		if (settings != null) {
			return new Vec3d(result.x * settings.getFogLevel() * ClientEndTickCallback.EffectStrength,
					result.y * settings.getFogLevel() * ClientEndTickCallback.EffectStrength,
					result.z * settings.getFogLevel() * ClientEndTickCallback.EffectStrength);
		}

		return result;
	}
}
