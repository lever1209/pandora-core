package pkg.deepCurse.pandora.core;

import org.slf4j.Logger;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import pkg.deepCurse.pandora.core.callbacks.EndServerTickCallback;

import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;

public class Pandora implements ModInitializer {

	Logger log = LoggerFactory.getLogger(Pandora.class);

	@Override
	public void onInitialize() {

		log.info("[Pandora] Initializing. . .");

		ServerTickEvents.END_WORLD_TICK.register((world) -> {
			EndServerTickCallback.run(world);
		});
		
		
		
		log.info("[Pandora] Finished Initializing");

	}

}