package pkg.deepCurse.pandora.common;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import pkg.deepCurse.pandora.common.config.CommonConfig;

public class CommonTools {

	// i hate that logical mess down there with the angles and stuff // TODO re work
	// that at some point, there may be a simpler way of doing it
	public static boolean isNearLight(World world, BlockPos pos, int targetLightLevel) {
		int blockLightLevel = world.getLightLevel(LightType.BLOCK, pos);
		int skyLightLevel = world.getLightLevel(LightType.SKY, pos);
		var dimSettings = CommonConfig.COMMON.DimensionSettings.get(world.getDimensionKey().getValue());

		if (blockLightLevel >= targetLightLevel) {
			return true;
		}

		// if there is a skylight and its bright enough
		// OR if we are ignoring the skylight
		if (world.getDimension().hasSkyLight() && skyLightLevel >= targetLightLevel || dimSettings.ignoreSkyLight) {
			float angle = world.getDimension().getSkyAngle(world.getLunarTime());
			// if its dawn or dusk or the moon is bright enough
			if ((angle < 0.26F || angle > 0.73F || world.getMoonSize() > 0.5f)) {
				return true;
			}
		}
		return false;
	}

	public static float iLerp(float min, float max, float input) {
		return (input - min) / (max - min);
	}

	public static double iLerp(double min, double max, double input) {
		return (input - min) / (max - min);
	}

	public static Vec3d rayMarch(Vec3d origin, Vec3d target, float stepDistance,
			LineTraceResultFunction lineTraceResultFunction) {

		Vec3d cPos = origin;

		for (float i = 0; i <= stepDistance; i++) {
			float t = i / stepDistance;

			if (iLerp(origin.x, target.x, cPos.x) < 1) {
				cPos = new Vec3d(MathHelper.lerp(t, origin.x, target.x), cPos.y, cPos.z);
			}

			if (iLerp(origin.y, target.y, cPos.y) < 1) {
				cPos = new Vec3d(cPos.x, MathHelper.lerp(t, origin.y, target.y), cPos.z);
			}

			if (iLerp(origin.z, target.z, cPos.z) < 1) {
				cPos = new Vec3d(cPos.x, cPos.y, MathHelper.lerp(t, origin.z, target.z));
			}

//			cPos = new Vec3d(MathHelper.lerp(t, origin.x, target.x), MathHelper.lerp(t, origin.y, target.y),
//					MathHelper.lerp(t, origin.z, target.z));

			if (lineTraceResultFunction.onPoint(cPos)) {
				return cPos;
			}
		}

		// return null instead of cpos because needing cpos on fail is specific to my
		// need and not ray marching, ill handle it myself after the ray march
		return null;
	}

	public static int clamp(int in, int min, int max) {
		return Math.max(Math.min(in, max), min);
	}

	public static float clamp(float in, float min, float max) {
		return Math.max(Math.min(in, max), min);
	}

	public static double clamp(double in, double min, double max) {
		return Math.max(Math.min(in, max), min);
	}

	public interface LineTraceResultFunction {
		public boolean onPoint(Vec3d point);
	}

//	public static Vec3d rayMarch(BlockPos origin, BlockPos target, float stepDistance,
//			LineTraceResultFunction lineTraceResultFunction) {
//		return rayMarch(new Vec3d(origin.getX(), origin.getY(), origin.getZ()),
//				new Vec3d(target.getX(), target.getY(), target.getZ()), stepDistance, lineTraceResultFunction);
//	}

	// TODO remove to radians and just hand radians to the func instead

	/**
	 * 
	 * @param pitch    The pitch used to calculate the altitude of the position
	 * @param yaw      The yaw used to calculate the position on the minecraft XZ
	 *                 plane
	 * @param distance The distance from the origin (0,0,0) at which to calculate
	 *                 the position
	 * @return The relative position that should be added to your known position
	 */
	public static Vec3d getPosFromSphericalPosition(float pitch, float yaw, float distance) {
		var phi = (float) Math.toRadians(pitch);
		var theta = (float) Math.toRadians(yaw);

		return new Vec3d(distance * MathHelper.sin(theta) * MathHelper.cos(phi), distance * MathHelper.sin(phi),
				distance * MathHelper.cos(theta) * MathHelper.cos(phi));
	}

	/**
	 * 
	 * @param pitch    The pitch used to calculate the altitude of the position
	 * @param yaw      The yaw used to calculate the position on the minecraft XZ
	 *                 plane
	 * @param distance The distance from the origin (0,0,0) at which to calculate
	 *                 the position
	 * @return The relative position that should be added to your known position
	 */
	public static Vec3d getPosFromSphericalPosition(double pitch, double yaw, double distance) {
		var phi = Math.toRadians(pitch);
		var theta = Math.toRadians(yaw);

		return new Vec3d(distance * Math.sin(theta) * Math.cos(phi), distance * Math.sin(phi),
				distance * Math.cos(theta) * Math.cos(phi));
	}

}