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

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import pkg.deepCurse.pandora.core.PandoraConfig;
import pkg.deepCurse.pandora.core.PandoraConfig.PandoraConfigEnum;
import pkg.deepCurse.pandora.core.interfaces.TextureAccess;
import pkg.deepCurse.pandora.tools.DarknessTools;

@Mixin(NativeImageBackedTexture.class)
public class NativeImageBackedTextureMixin implements TextureAccess {
	@Shadow
	NativeImage image;

	private boolean enableHook = false;

	@Inject(method = "upload", at = @At(value = "HEAD"))
	private void onUpload(CallbackInfo ci) {
		if (enableHook && PandoraConfig.getBoolean(PandoraConfigEnum.isDarknessEnabled) && image != null) {
			final NativeImage img = image;

			for (int b = 0; b < 16; b++) {
				for (int s = 0; s < 16; s++) {
					final int color = DarknessTools.darken(img.getColor(b, s), b, s);
					img.setColor(b, s, color);
				}
			}
		}
	}

	@Override
	public void darkness_enableUploadHook() {
		enableHook = true;
	}
}
