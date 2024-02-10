
package pkg.deepCurse.pandora.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import pkg.deepCurse.pandora.client.config.ClientConfig;
import pkg.deepCurse.pandora.common.CommonTools;
import pkg.deepCurse.pandora.common.Pandora;
import pkg.deepCurse.pandora.common.config.CommonConfig;
import pkg.deepCurse.pandora.common.config.DebugConfig;

/**
 * I dont know how or why, but whenever i touch anything in this entire class it
 * breaks silently
 * 
 * so far ive wasted 12 hours debugging it and trying to fix it
 */
@Environment(EnvType.CLIENT)
public class ClientTools {

	// this should probably be moved to where its used instead of bloating up the client tools class
	public static boolean isBlockRayMarchTarget(Vec3d cPos) {
		var client = MinecraftClient.getInstance();

		if (client != null) {

			var cPosBlockPos = new BlockPos(cPos);

			var contactPositionBlockState = client.world.getBlockState(cPosBlockPos);

			if (Block.isShapeFullCube(contactPositionBlockState.getOutlineShape(client.world, cPosBlockPos))) {
				return true;
			}

//			client.player.sendCommand(String.format("setblock %s %s %s minecraft:glowstone keep", cPosBlockPos.getX(), cPosBlockPos.getY(), cPosBlockPos.getZ()));

		}

		return false;
	}

	/**
	 * The following functions are authored by grondag, and can be found at
	 * https://github.com/grondag/darkness I have been given permission to use this
	 * file under BSD-3 by grondag <3
	 */

	private static float skyFactor(World world) {
		var dimSettings = ClientConfig.CLIENT.clientDimensionConfigMap.get(world.getRegistryKey().getValue());

		if (!world.getDimension().hasSkyLight()) {
			return 0;
		}

		if (world.getDimension().hasSkyLight() && dimSettings.isIgnoreSkyLight()) {
			return 1;
		}

		final float angle = world.getSkyAngle(0);

		if (angle > 0.25f && angle < 0.75f) { // TODO fine tune these angles
			final float oldWeight = Math.max(0, (Math.abs(angle - 0.5f) - 0.2f)) * 20;
			// {1.0f, 0.75f, 0.5f, 0.25f, 0.0f, 0.25f, 0.5f, 0.75f}
			final float moon = DimensionType.MOON_SIZES[dimSettings.isLockMoonPhase() ? dimSettings.getTargetMoonPhase()
					: world.getDimension().getMoonPhase(world.getLunarTime())];

			return MathHelper.lerp(oldWeight * oldWeight * oldWeight, moon * moon, 1f);
		}

		return 1;
	}

	private static final float[][] LUMINANCE = new float[16][16];

	public static int darken(int c, int blockIndex, int skyIndex) {
		final float lTarget = LUMINANCE[blockIndex][skyIndex];
		final float r = (c & 0xFF) / 255f;
		final float g = ((c >> 8) & 0xFF) / 255f;
		final float b = ((c >> 16) & 0xFF) / 255f;
		final float l = luminance(r, g, b);
		final float f = l > 0 ? Math.min(1, lTarget / l) : 0;

		if (DebugConfig.DEBUG.PaintLightValues) {
			var client = MinecraftClient.getInstance();
			client.getProfiler().push("pandora_paintingLightValues");
			var commonDimSettings = CommonConfig.COMMON.DimensionSettings
					.get(client.world.getDimensionKey().getValue());
			if (commonDimSettings != null) {
				if (skyIndex <= commonDimSettings.minimumFadeLightLevel
						&& blockIndex <= commonDimSettings.minimumFadeLightLevel) {
					client.getProfiler().pop();
					return f == 1f ? c : 0xFF000000 | 255 | (0 << 8) | (0 << 16);
				} else if (skyIndex <= commonDimSettings.minimumSafeLightLevel
						&& blockIndex <= commonDimSettings.minimumSafeLightLevel) {
					client.getProfiler().pop();
					return f == 1f ? c : 0xFF000000 | 0 | (0 << 8) | (255 << 16);
				} else {
					client.getProfiler().pop();
					return f == 1f ? c : 0xFF000000 | 0 | (255 << 8) | (0 << 16);
				}
			}
			client.getProfiler().pop();
		}

		// may have found a way to make areas darker even in the light or increase the
		// range of lighting in minecraft without affecting its strict 16 light level
		// limit
		// return f == 1f ? c : 0xFF000000 | 0 | (0 << 8) | (0 << 16);

		return f == 1f ? c
				: 0xFF000000 | Math.round(f * r * 255) | (Math.round(f * g * 255) << 8)
						| (Math.round(f * b * 255) << 16);
	}

	public static float luminance(float r, float g, float b) {
		return r * 0.2126f + g * 0.7152f + b * 0.0722f;
	}

	public static boolean ENABLE_WORKSPACE_DARKNESS = true;

	// TODO we should be able to run this substantially less than what it currently
	// runs at
	public static void updateLuminance(float tickDelta, MinecraftClient client, GameRenderer worldRenderer,
			float prevFlicker) {
		final ClientWorld world = client.world;

		final var dimSettings = ClientConfig.CLIENT.clientDimensionConfigMap.get(world.getRegistryKey().getValue());

		if (world != null) {

			if (dimSettings == null) {
				ENABLE_WORKSPACE_DARKNESS = false;
				return;
			}

			final boolean isDark = dimSettings.isDark();

			if (!isDark) {
				ENABLE_WORKSPACE_DARKNESS = false;
				return;
			}


			var statusEffects = client.player.getActiveStatusEffects();

			for (var e : statusEffects.keySet()) {
				Pandora.log.info("{}", Registry.STATUS_EFFECT.getId(e));
				if (ClientConfig.CLIENT.PotionEffects.contains(Registry.STATUS_EFFECT.getId(e))) {
					ENABLE_WORKSPACE_DARKNESS = false;
					return;
				}
			}

//			if (client.player.hasStatusEffect(StatusEffects.CONDUIT_POWER)
//					&& client.player.getUnderwaterVisibility() > 0) {
//				ENABLE_WORKSPACE_DARKNESS = false;
//				return;
//			}
			
			if (world.getLightningTicksLeft() > 0) {
				ENABLE_WORKSPACE_DARKNESS = false;
				return;
			}

			ENABLE_WORKSPACE_DARKNESS = true;

			final float dimSkyFactor = skyFactor(world);
			final float ambient = world.getStarBrightness(1.0F);
			final DimensionType dim = world.getDimension();

			for (int skyIndex = 0; skyIndex < 16; ++skyIndex) {
				float skyFactor = 1f - skyIndex / 15f;
				skyFactor = 1 - skyFactor * skyFactor * skyFactor * skyFactor;
				skyFactor *= dimSkyFactor;

				float min = skyFactor * 0.05f;
				final float rawAmbient = ambient * skyFactor;
				final float minAmbient = rawAmbient * (1 - min) + min;
				final float skyBase = LightmapTextureManager.getBrightness(dim, skyIndex) * minAmbient;

				min = 0.35f * skyFactor;
				float skyRed = skyBase * (rawAmbient * (1 - min) + min);
				float skyGreen = skyBase * (rawAmbient * (1 - min) + min);
				float skyBlue = skyBase;

				if (worldRenderer.getSkyDarkness(tickDelta) > 0.0F) {
					final float skyDarkness = worldRenderer.getSkyDarkness(tickDelta);
					skyRed = skyRed * (1.0F - skyDarkness) + skyRed * 0.7F * skyDarkness;
					skyGreen = skyGreen * (1.0F - skyDarkness) + skyGreen * 0.6F * skyDarkness;
					skyBlue = skyBlue * (1.0F - skyDarkness) + skyBlue * 0.6F * skyDarkness;
				}

				for (int blockIndex = 0; blockIndex < 16; ++blockIndex) {
					float blockFactor = 1f;

					if (isDark) {
						blockFactor = 1f - blockIndex / 15f;
						blockFactor = 1 - blockFactor * blockFactor * blockFactor * blockFactor;
					}

					final float blockBase = blockFactor * LightmapTextureManager.getBrightness(dim, blockIndex)
							* (prevFlicker * 0.1F + 1.5F);
					min = 0.4f * blockFactor;
					final float blockGreen = blockBase * ((blockBase * (1 - min) + min) * (1 - min) + min);
					final float blockBlue = blockBase * (blockBase * blockBase * (1 - min) + min);

					float red = skyRed + blockBase;
					float green = skyGreen + blockGreen;
					float blue = skyBlue + blockBlue;

					final float f = Math.max(skyFactor, blockFactor);
					min = 0.03f * f;
					red = red * (0.99F - min) + min;
					green = green * (0.99F - min) + min;
					blue = blue * (0.99F - min) + min;

//					if (world.getRegistryKey() == World.END) {
//						red = skyFactor * 0.22F + blockBase * 0.75f;
//						green = skyFactor * 0.28F + blockGreen * 0.75f;
//						blue = skyFactor * 0.25F + blockBlue * 0.75f;
//					}

					red = Math.min(red, 1);
					green = Math.min(green, 1);
					blue = Math.min(blue, 1);

					final float gamma = (float) (client.options.getGamma().getValue() * f);
					float invRed = 1.0F - red;
					float invGreen = 1.0F - green;
					float invBlue = 1.0F - blue;
					invRed = 1.0F - invRed * invRed * invRed * invRed;
					invGreen = 1.0F - invGreen * invGreen * invGreen * invGreen;
					invBlue = 1.0F - invBlue * invBlue * invBlue * invBlue;
					red = red * (1.0F - gamma) + invRed * gamma;
					green = green * (1.0F - gamma) + invGreen * gamma;
					blue = blue * (1.0F - gamma) + invBlue * gamma;

					min = 0.03f * f;
					red = red * (0.99F - min) + min;
					green = green * (0.99F - min) + min;
					blue = blue * (0.99F - min) + min;

					LUMINANCE[blockIndex][skyIndex] = luminance(CommonTools.clamp(red, 0, 1),
							CommonTools.clamp(green, 0, 1), CommonTools.clamp(blue, 0, 1));
				}
			}
		}
	}
}
