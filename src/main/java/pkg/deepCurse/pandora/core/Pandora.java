package pkg.deepCurse.pandora.core;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.block.Block;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import pkg.deepCurse.pandora.core.callbacks.EndServerTickCallback;
import pkg.deepCurse.pandora.tools.PandoraTools;

public class Pandora implements ModInitializer {

	Logger log = LoggerFactory.getLogger(Pandora.class);

	@Override
	public void onInitialize() {

		log.info("[Pandora] Initializing. . .");

		ServerTickEvents.END_WORLD_TICK.register((world) -> {
			EndServerTickCallback.run(world);
		});
		
		for (Entry<RegistryKey<Block>, Block> entry : Registry.BLOCK.getEntrySet()) {
			PandoraTools.overrideLuminance(entry.getKey().getValue(), entry.getValue());
		}
		RegistryEntryAddedCallback.event(Registry.BLOCK).register((rawId, id, block) -> {
			PandoraTools.overrideLuminance(id, block);
		});
		
		log.info("[Pandora] Finished Initializing");

	}

}