/**
 * This file is authored by grondag, and can be found at https://github.com/grondag/darkness
 * I have been given permission to use this file under BSD-3 by grondag <3
 * 
 * This file has been heavily modified by me for integration with Pandora
 */

package pkg.deepCurse.pandora.tools;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import pkg.deepCurse.pandora.core.PandoraConfig;
import pkg.deepCurse.pandora.core.PandoraConfig.PandoraConfigEnum;

public class DarknessTools {

	// public static boolean darkOverworld;
	// public static boolean darkNether;
	// public static boolean darkEnd;
	// public static boolean darkSkyless;
	// public static boolean enableModdedDarkDimensions;

	// public static double darkNetherFogConfigured;
	// public static double darkEndFogConfigured;

	// public static double darkNetherFogEffective;
	// public static double darkEndFogEffective;

	static {

		// for (DimensionConfigInfoObject i : dimensionMap.values()) {
		// try {
		// dimensionMap.put(i.getIdentifier(),
		// new DimensionConfigInfoObject(i.getId(), i.isEnabled(),
		// MathHelper.clamp(i.getIntensity(), 0.0, 1.0)));
		// } catch (final Exception e) {
		// darkNetherFogConfigured = 0.5;
		// LOG.warn(
		// "[Darkness] Invalid configuration value for '{}'. Disabling dimension.
		// Accepted values: 0.0 - 1.0, current value: {}",
		// i.getId(), i.getIntensity());
		// }
		// }

		// computeConfigValues();

	}

	// private static void computeConfigValues() {
	// darkNetherFogEffective = darkNether ? darkNetherFogConfigured : 1.0;
	// darkEndFogEffective = darkEnd ? darkEndFogConfigured : 1.0;
	// }

	// public static boolean blockLightOnly() {
	// return blockLightOnly;
	// }

	private static boolean isDark(World world) {
		return PandoraConfig.effectiveDimensions.containsKey(world.getRegistryKey().getValue());
	}

	private static float skyFactor(World world) {
		if (!PandoraConfig.getBoolean(PandoraConfigEnum.blockLightOnly) && isDark(world)) {
			if (world.getDimension().hasSkyLight()) {
				final float angle = world.getSkyAngle(0);

				if (angle > 0.25f && angle < 0.75f) {
					final float oldWeight = Math.max(0, (Math.abs(angle - 0.5f) - 0.2f)) * 20;
					final float moon = PandoraConfig.getBoolean(PandoraConfigEnum.ignoreMoonPhase) ? 0
							: world.getMoonSize();
					return MathHelper.lerp(oldWeight * oldWeight * oldWeight, moon * moon, 1f);
				} else {
					return 1;
				}
			} else {
				return 0;
			}
		} else {
			return 1;
		}
	}

	private static final float[][] LUMINANCE = new float[16][16];

	public static int darken(int c, int blockIndex, int skyIndex) {
		final float lTarget = LUMINANCE[blockIndex][skyIndex];
		final float r = (c & 0xFF) / 255f;
		final float g = ((c >> 8) & 0xFF) / 255f;
		final float b = ((c >> 16) & 0xFF) / 255f;
		final float l = luminance(r, g, b);
		final float f = l > 0 ? Math.min(1, lTarget / l) : 0;

		return f == 1f ? c
				: 0xFF000000 | Math.round(f * r * 255) | (Math.round(f * g * 255) << 8)
						| (Math.round(f * b * 255) << 16);
	}

	public static float luminance(float r, float g, float b) {
		return r * 0.2126f + g * 0.7152f + b * 0.0722f;
	}

	public static void updateLuminance(float tickDelta, MinecraftClient client,
			GameRenderer worldRenderer, float prevFlicker) {
		final ClientWorld world = client.world;

		if (world != null) {
			if (!isDark(world) || client.player.hasStatusEffect(StatusEffects.NIGHT_VISION)
					|| (client.player.hasStatusEffect(StatusEffects.CONDUIT_POWER)
							&& client.player.getUnderwaterVisibility() > 0)
					|| world.getLightningTicksLeft() > 0) {
				PandoraConfig.setBoolean(PandoraConfigEnum.isDarknessEnabled, false);
				return;
			} else {
				PandoraConfig.setBoolean(PandoraConfigEnum.isDarknessEnabled, true);
			}

			final float dimSkyFactor = skyFactor(world);
			final float ambient = world.getStarBrightness(1.0F);
			final DimensionType dim = world.getDimension();
			final boolean blockAmbient = !isDark(world);

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

					if (!blockAmbient) {
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

					if (world.getRegistryKey() == World.END) {
						red = skyFactor * 0.22F + blockBase * 0.75f;
						green = skyFactor * 0.28F + blockGreen * 0.75f;
						blue = skyFactor * 0.25F + blockBlue * 0.75f;
					}

					if (red > 1.0F) {
						red = 1.0F;
					}

					if (green > 1.0F) {
						green = 1.0F;
					}

					if (blue > 1.0F) {
						blue = 1.0F;
					}

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

					if (red > 1.0F) {
						red = 1.0F;
					}

					if (green > 1.0F) {
						green = 1.0F;
					}

					if (blue > 1.0F) {
						blue = 1.0F;
					}

					if (red < 0.0F) {
						red = 0.0F;
					}

					if (green < 0.0F) {
						green = 0.0F;
					}

					if (blue < 0.0F) {
						blue = 0.0F;
					}

					LUMINANCE[blockIndex][skyIndex] = luminance(red, green, blue);
				}
			}
		}
	}

	// private static float skyFactor(World world) {
	// if (!blockLightOnly && isDark(world)) {
	// if (world.getDimension().hasSkyLight()) {
	// final float angle = world.getSkyAngle(0);

	// if (angle > 0.25f && angle < 0.75f) {
	// final float oldWeight = Math.max(0,
	// (Math.abs(angle - 0.5f) - 0.2f)) * 20;
	// final float moon = ignoreMoonPhase
	// ? 0
	// : world.getMoonSize();
	// return MathHelper.lerp(oldWeight * oldWeight * oldWeight,
	// moon * moon, 1f);
	// } else {
	// return 1;
	// }
	// } else {
	// return 0;
	// }
	// } else {
	// return 1;
	// }
	// }

	// public static boolean enabled = false;

	// private static final float[][] LUMINANCE = new float[16][16];

	// public static int darken(int c, int blockIndex, int skyIndex) { // per block
	// // for block
	// // face?
	// final float lTarget = LUMINANCE[blockIndex][skyIndex];
	// final float r = (c & 0xFF) / 255f;
	// final float g = ((c >> 8) & 0xFF) / 255f;
	// final float b = ((c >> 16) & 0xFF) / 255f;
	// final float l = luminance(r, g, b);
	// final float f = l > 0 ? Math.min(1, lTarget / l) : 0;

	// return f == 1f
	// ? c
	// : 0xFF000000 | Math.round(f * r * 255)
	// | (Math.round(f * g * 255) << 8)
	// | (Math.round(f * b * 255) << 16);
	// }

	// public static float luminance(float r, float g, float b) {
	// return r * 0.2126f + g * 0.7152f + b * 0.0722f;
	// }

	// public static void updateLuminance(float tickDelta, MinecraftClient client,
	// GameRenderer worldRenderer, float prevFlicker) {
	// final ClientWorld world = client.world;

	// if (world != null) {
	// if (!isDark(world)
	// || client.player.hasStatusEffect(StatusEffects.NIGHT_VISION)
	// || (client.player
	// .hasStatusEffect(StatusEffects.CONDUIT_POWER)
	// && client.player.getUnderwaterVisibility() > 0)
	// || world.getLightningTicksLeft() > 0) {
	// enabled = false;
	// return;
	// } else {
	// enabled = true;
	// }

	// final float dimSkyFactor = skyFactor(world);
	// final float ambient = world.getStarBrightness(1.0F);
	// final DimensionType dim = world.getDimension();
	// final boolean blockAmbient = !isDark(world);

	// for (int skyIndex = 0; skyIndex < 16; ++skyIndex) {
	// float skyFactor = 1f - skyIndex / 15f;
	// skyFactor = 1 - skyFactor * skyFactor * skyFactor * skyFactor;
	// skyFactor *= dimSkyFactor;

	// float min = skyFactor * 0.05f;
	// final float rawAmbient = ambient * skyFactor;
	// final float minAmbient = rawAmbient * (1 - min) + min;
	// final float skyBase = dim.getSkyAngle(skyIndex) * minAmbient;

	// min = 0.35f * skyFactor;
	// float skyRed = skyBase * (rawAmbient * (1 - min) + min);
	// float skyGreen = skyBase * (rawAmbient * (1 - min) + min);
	// float skyBlue = skyBase;

	// if (worldRenderer.getSkyDarkness(tickDelta) > 0.0F) {
	// final float skyDarkness = worldRenderer
	// .getSkyDarkness(tickDelta);
	// skyRed = skyRed * (1.0F - skyDarkness)
	// + skyRed * 0.7F * skyDarkness;
	// skyGreen = skyGreen * (1.0F - skyDarkness)
	// + skyGreen * 0.6F * skyDarkness;
	// skyBlue = skyBlue * (1.0F - skyDarkness)
	// + skyBlue * 0.6F * skyDarkness;
	// }

	// for (int blockIndex = 0; blockIndex < 16; ++blockIndex) {
	// float blockFactor = 1f;

	// if (!blockAmbient) {
	// blockFactor = 1f - blockIndex / 15f;
	// blockFactor = 1 - blockFactor * blockFactor
	// * blockFactor * blockFactor;
	// }

	// final float blockBase = blockFactor
	// * dim.getSkyAngle(blockIndex)
	// * (prevFlicker * 0.1F + 1.5F);
	// min = 0.4f * blockFactor;
	// final float blockGreen = blockBase
	// * ((blockBase * (1 - min) + min) * (1 - min) + min);
	// final float blockBlue = blockBase
	// * (blockBase * blockBase * (1 - min) + min);

	// float red = skyRed + blockBase;
	// float green = skyGreen + blockGreen;
	// float blue = skyBlue + blockBlue;

	// final float f = Math.max(skyFactor, blockFactor);
	// min = 0.03f * f;
	// red = red * (0.99F - min) + min;
	// green = green * (0.99F - min) + min;
	// blue = blue * (0.99F - min) + min;

	// if (world.getRegistryKey() == World.END) {
	// red = skyFactor * 0.22F + blockBase * 0.75f;
	// green = skyFactor * 0.28F + blockGreen * 0.75f;
	// blue = skyFactor * 0.25F + blockBlue * 0.75f;
	// }

	// if (red > 1.0F) {
	// red = 1.0F;
	// }

	// if (green > 1.0F) {
	// green = 1.0F;
	// }

	// if (blue > 1.0F) {
	// blue = 1.0F;
	// }

	// final double gamma = client.options.getGamma().getValue() * f;
	// float invRed = 1.0F - red;
	// float invGreen = 1.0F - green;
	// float invBlue = 1.0F - blue;
	// invRed = 1.0F - invRed * invRed * invRed * invRed;
	// invGreen = 1.0F - invGreen * invGreen * invGreen * invGreen;
	// invBlue = 1.0F - invBlue * invBlue * invBlue * invBlue;
	// red = (float) (red * (1.0F - gamma) + invRed * gamma);
	// green = (float) (green * (1.0F - gamma) + invGreen * gamma);
	// blue = (float) (blue * (1.0F - gamma) + invBlue * gamma);

	// min = 0.03f * f;
	// red = red * (0.99F - min) + min;
	// green = green * (0.99F - min) + min;
	// blue = blue * (0.99F - min) + min;

	// if (red > 1.0F) {
	// red = 1.0F;
	// }

	// if (green > 1.0F) {
	// green = 1.0F;
	// }

	// if (blue > 1.0F) {
	// blue = 1.0F;
	// }

	// if (red < 0.0F) {
	// red = 0.0F;
	// }

	// if (green < 0.0F) {
	// green = 0.0F;
	// }

	// if (blue < 0.0F) {
	// blue = 0.0F;
	// }

	// LUMINANCE[blockIndex][skyIndex] = luminance(red,
	// green, blue);
	// }
	// }
	// }
	// }

	// @Override
	// public void onPreLaunch() {
	// MixinExtrasBootstrap.init();
	// }

	// @Override
	// public void onInitialize() {
	// // if (dimensionMap.isEmpty()) {
	// // dimensionMap.put(World.NETHER.getValue(),
	// // new DimensionConfigInfoObject("minecraft:the_nether", true,
	// // 0.5D));
	// // dimensionMap.put(World.OVERWORLD.getValue(),
	// // new DimensionConfigInfoObject("minecraft:overworld", false,
	// // 1.0D));
	// // dimensionMap.put(World.END.getValue(),
	// // new DimensionConfigInfoObject("minecraft:the_end", true,
	// // 0.0D));
	// // }
	// }

}
