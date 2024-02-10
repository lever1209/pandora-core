/**
 * This file is authored by grondag, and can be found at https://github.com/grondag/darkness
 * I have been given permission to use this file under BSD-3 by grondag <3
 */

package pkg.deepCurse.pandora.client.mixins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import pkg.deepCurse.pandora.client.ClientTools;
import pkg.deepCurse.pandora.common.interfaces.LightmapAccess;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class GameRendererMixin {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(GameRendererMixin.class);

	@Shadow
	private MinecraftClient client;
	@Shadow
	private LightmapTextureManager lightmapTextureManager;

	@Inject(method = "renderWorld", at = @At(value = "HEAD"))
	private void pandora_onRenderWorld(float tickDelta, long nanos, MatrixStack matrixStack, CallbackInfo ci) {
		final LightmapAccess lightmap = (LightmapAccess) lightmapTextureManager;

		if (lightmap.darkness_isDirty()) {
			client.getProfiler().push("lightTex");
			ClientTools.updateLuminance(tickDelta, client, (GameRenderer) (Object) this,
					lightmap.darkness_prevFlicker());
			client.getProfiler().pop();
		}
	}
}
