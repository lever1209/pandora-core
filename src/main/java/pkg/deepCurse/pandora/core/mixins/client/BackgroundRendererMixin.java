package pkg.deepCurse.pandora.core.mixins.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.source.BiomeAccess;
import pkg.deepCurse.pandora.core.screens.DebugScreen;
import pkg.deepCurse.pandora.tools.PandoraTools;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {

	private static Logger log = LoggerFactory.getLogger(BackgroundRendererMixin.class);

	private static int tickCount = 0;
	private static double MIN = 0.029999999329447746D;

	@WrapOperation(method = "method_24873", at = @At(value = "INVOKE", target = "net/minecraft/client/render/DimensionEffects.adjustFogColor(Lnet/minecraft/util/math/Vec3d;F)Lnet/minecraft/util/math/Vec3d;"))
	private static Vec3d overrideFog(DimensionEffects effects, Vec3d color, float f, Operation<Vec3d> operation,
			ClientWorld world, BiomeAccess access,
			float sunHeight, int i, int j, int k) {

		// boolean isNearLight = PandoraTools.isNearLight(world,
		// MinecraftClient.getInstance().player.getBlockPos(), 5); use to darken fog
		// while not near light

		Vec3d originalValue = operation.call(effects, color, f);
		Vec3d hasSkyLightValue = color.multiply(sunHeight * 0.94f + 0.06f, sunHeight * 0.94f + 0.06f,
				sunHeight * 0.91f + 0.09f);
		Vec3d noSkyLightValue = new Vec3d(color.x * DebugScreen.factor, color.y * DebugScreen.factor,
				color.z * DebugScreen.factor);
		
		tickCount++;
		if (tickCount > 10000) {
			log.info(
					"[Pandora] color? {}, world {}, access {}, sunHeight? {}, i {}, j {}, k {}, dimensionKey {}, factor {}\nnoSkyLightReturnValue {}, skyLightReturnValue {}, originalValue {}",
					color, world, access, sunHeight, i, j, k, world.getDimensionKey().getValue(), DebugScreen.factor,
					noSkyLightValue, hasSkyLightValue, originalValue);
			tickCount = 0;
		}

		if (DebugScreen.factor != 1.0) {
			if (!world.getDimension().hasSkyLight()) {
				return noSkyLightValue;
			} else { // sunHeight represents 0 when the sun is not up at all, 1 is the max value when
						// it has passed dusk/dawn and everything in between is dusk/dawn
				return hasSkyLightValue;
			}
		}
		return originalValue;
	}
}
