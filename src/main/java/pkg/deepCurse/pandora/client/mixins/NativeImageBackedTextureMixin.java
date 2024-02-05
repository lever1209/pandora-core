/**
 * This file is authored by grondag, and can be found at https://github.com/grondag/darkness
 * I have been given permission to use this file under BSD-3 by grondag <3
 */

package pkg.deepCurse.pandora.client.mixins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import pkg.deepCurse.pandora.common.util.interfaces.TextureAccess;
import pkg.deepCurse.pandora.common.util.tools.DarknessTools;

@Mixin(NativeImageBackedTexture.class)
public class NativeImageBackedTextureMixin implements TextureAccess {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(NativeImageBackedTextureMixin.class);

	@Shadow
	@Mutable
	NativeImage image;

	private boolean enableHook = false;

	@Inject(method = "upload", at = @At(value = "HEAD"))
	private void onUpload(CallbackInfo ci) {
		
//		log.info("ON{}",enableHook && DarknessTools.ENABLE_WORKSPACE_DARKNESS && image != null);
		
		if (enableHook && DarknessTools.ENABLE_WORKSPACE_DARKNESS && image != null) {
//			final NativeImage img = image;

			for (int b = 0; b < 16; b++) {
				for (int s = 0; s < 16; s++) {
					int color = DarknessTools.darken(image.getColor(b, s), b, s);
					
					image.setColor(b, s, color);
				}
			}
		}
	}

	@Override
	public void darkness_enableUploadHook() {
		enableHook = true;
	}
}
