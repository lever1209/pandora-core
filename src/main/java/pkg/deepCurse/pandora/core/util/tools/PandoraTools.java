package pkg.deepCurse.pandora.core.util.tools;

import org.slf4j.*;

import net.minecraft.block.*;
import net.minecraft.entity.mob.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import pkg.deepCurse.pandora.core.*;
import pkg.deepCurse.pandora.core.mixins.shared.accessors.*;

public class PandoraTools {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(PandoraTools.class);

	public static boolean isNearLight(World world, BlockPos pos) {
		return isNearLight(world, pos, PandoraConfig.MINIMUM_SAFE_LIGHT_LEVEL); // TODO fade support
	}

	public static boolean isNearLight(World world, BlockPos pos,
			int minimumSafeLightLevel) {
		int blockLightLevel = world.getLightLevel(LightType.BLOCK, pos);
		int skyLightLevel = world.getLightLevel(LightType.SKY, pos);
		if (blockLightLevel >= minimumSafeLightLevel) {
			return true;
		} else {
			if (world.getDimension().hasSkyLight()
					&& skyLightLevel >= minimumSafeLightLevel) {
				float angle = world.getDimension()
						.getSkyAngle(world.getLunarTime());
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

	public static void overrideLuminance(Identifier identifier, Block block) {
		if (PandoraConfig.BLOCK_LIGHT_LEVEL_FUNCTIONS.containsKey(identifier)) {
			// log.info("[Pandora] Changing luminance of {} from {} to {}", identifier,
			// block.getDefaultState().getLuminance(),
			// PandoraConfig.lightLevelBlockPairs.get(identifier).applyAsInt(
			// block.getDefaultState()));
			for (BlockState state : block.getStateManager().getStates()) {
				((LuminanceOverride) state)
						.setLuminance(PandoraConfig.BLOCK_LIGHT_LEVEL_FUNCTIONS.get(identifier).applyAsInt(state));
			}
		}
	}

	public static boolean shouldFearDarkness(MobEntity self) {
		return true; // TODO fix
	}
}