package pkg.deepCurse.pandora.core.util.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class PandoraTools {

	private static Logger log = LoggerFactory.getLogger(PandoraTools.class);

	public static boolean isNearLight(World world, BlockPos pos, int minimumSafeLightLevel) {
		int blockLightLevel = world.getLightLevel(LightType.BLOCK, pos);
		int skyLightLevel = world.getLightLevel(LightType.SKY, pos);
		if (blockLightLevel >= minimumSafeLightLevel) {
			return true;
		} else {
			if (world.getDimension().hasSkyLight() && skyLightLevel >= minimumSafeLightLevel) {
				float angle = world.getDimension().getSkyAngle(world.getLunarTime());
				if (angle < 0.26F || angle > 0.73F) {
					return true;
				}

				if ((double) world.getMoonSize() > 0.5D) {
					return true;
				}
			}

			return false;
		}
	}

}