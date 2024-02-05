package pkg.deepCurse.pandora.common.util.callbacks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import pkg.deepCurse.pandora.common.PandoraConfig.Server;
import pkg.deepCurse.pandora.common.mixins.accessors.LuminanceOverride;

public class BlockRegisterCallback {

	public static void overrideLuminance(Identifier id, Block block) {
		var lightLevelSetting = Server.SERVER.BlockLightLevelSettings.get(id);
		if (lightLevelSetting != null) {
			for (BlockState state : block.getStateManager().getStates()) {
				((LuminanceOverride) state)
						.setLuminance(lightLevelSetting.LightLevel.applyAsInt(state, state.getLuminance()));
			}
		}
	}

	public static void onEntryAdded(int rawId, Identifier id, Block block) {
		overrideLuminance(id, block);
	}
}
