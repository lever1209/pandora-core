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
import net.minecraft.client.texture.NativeImageBackedTexture;
import pkg.deepCurse.pandora.common.interfaces.LightmapAccess;
import pkg.deepCurse.pandora.common.interfaces.TextureAccess;

@Environment(EnvType.CLIENT)
@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin implements LightmapAccess {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(LightmapTextureManagerMixin.class);

	@Shadow
	private NativeImageBackedTexture texture;
	@Shadow
	private float flickerIntensity;
	@Shadow
	private boolean dirty;

	@Inject(method = "<init>*", at = @At(value = "RETURN"))
	private void afterInit(GameRenderer gameRenderer, MinecraftClient minecraftClient, CallbackInfo ci) {
		((TextureAccess) texture).darkness_enableUploadHook();
	}

	@Override
	public float darkness_prevFlicker() {
		return flickerIntensity;
	}

	@Override
	public boolean darkness_isDirty() {
		return dirty;
	}
}
