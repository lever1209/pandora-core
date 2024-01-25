/**
 * This file is authored by grondag, and can be found at https://github.com/grondag/darkness
 * I have been given permission to use this file under BSD-3 by grondag <3
 */

package pkg.deepCurse.pandora.core.mixins.client;

import org.slf4j.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import net.minecraft.client.texture.*;
import pkg.deepCurse.pandora.core.util.interfaces.*;
import pkg.deepCurse.pandora.core.util.tools.*;

@Mixin(NativeImageBackedTexture.class)
public class NativeImageBackedTextureMixin implements TextureAccess {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(NativeImageBackedTextureMixin.class);

	@Shadow
	NativeImage image;

	private boolean enableHook = false;

	@Inject(method = "upload", at = @At(value = "HEAD"))
	private void onUpload(CallbackInfo ci) {
		if (enableHook && DarknessTools.ENABLE_WORKSPACE_DARKNESS && image != null) {
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
