package pkg.deepCurse.pandora.core.util.callbacks;

import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import pkg.deepCurse.pandora.core.PandoraConfig.General.BlockLightLevelSettings;
import pkg.deepCurse.pandora.core.mixins.shared.accessors.LuminanceOverride;

public class BlockRegisterCallback implements RegistryEntryAddedCallback<Block> {

	public static void overrideLuminance(Identifier id, Block block) {
		if (BlockLightLevelSettings.CONFIG.containsKey(id)) {
			for (BlockState state : block.getStateManager().getStates()) {
				((LuminanceOverride) state)
						.setLuminance(BlockLightLevelSettings.CONFIG.get(id).LightLevel.applyAsInt(state,
								state.getLuminance()));
			}
		}
	}

	@Override
	public void onEntryAdded(int rawId, Identifier id, Block block) {
		overrideLuminance(id, block);
	}
}
