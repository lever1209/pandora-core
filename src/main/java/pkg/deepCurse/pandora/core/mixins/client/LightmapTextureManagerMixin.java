/**
 * This file is authored by grondag, and can be found at https://github.com/grondag/darkness
 * I have been given permission to use this file under BSD-3 by grondag <3
 */

package pkg.deepCurse.pandora.core.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.texture.NativeImageBackedTexture;
import pkg.deepCurse.pandora.core.interfaces.LightmapAccess;
import pkg.deepCurse.pandora.core.interfaces.TextureAccess;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin implements LightmapAccess {
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