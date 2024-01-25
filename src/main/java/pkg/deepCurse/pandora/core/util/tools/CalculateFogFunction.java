package pkg.deepCurse.pandora.core.util.tools;

import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.source.BiomeAccess;

@FunctionalInterface
public interface CalculateFogFunction { // interface for darkness api? will be used internally

	Vec3d calculate(DimensionEffects owner, Vec3d color, float sun_angle, Vec3d oldValue, ClientWorld world,
			BiomeAccess access, float sunHeight, int i, int j, int k);

}
