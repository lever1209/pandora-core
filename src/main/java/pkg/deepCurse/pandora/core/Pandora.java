package pkg.deepCurse.pandora.core;

import java.util.Map.*;

import org.slf4j.*;

import com.llamalad7.mixinextras.*;

import net.fabricmc.api.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.event.registry.*;
import net.fabricmc.loader.api.entrypoint.*;
import net.minecraft.block.*;
import net.minecraft.util.registry.*;
import pkg.deepCurse.pandora.core.util.callbacks.*;
import pkg.deepCurse.pandora.core.util.tools.*;

public class Pandora implements ModInitializer, PreLaunchEntrypoint {

	private static Logger log = LoggerFactory.getLogger(Pandora.class);

	@Override
	public void onPreLaunch() {
		log.info("[Pandora] Running pre launch initializers. . .");
		MixinExtrasBootstrap.init();
		log.info("[Pandora] Finished pre launch initializers.");
	}

	@Override
	public void onInitialize() {

		log.info("[Pandora] Initializing mod. . .");

//		PandoraRegistry.init();

		log.info("[Pandora] Loading and applying config. . .");
		PandoraConfig.loadConfig();
		log.info("[Pandora] Loaded and applied config.");

		registerHooks();

		log.info("[Pandora] Finished initializing mod.");

	}

	public static void registerHooks() {
		ServerTickEvents.END_WORLD_TICK.register((world) -> {
			EndServerTickCallback.run(world);
		});
		for (Entry<RegistryKey<Block>, Block> entry : Registry.BLOCK.getEntrySet()) {
			PandoraTools.overrideLuminance(entry.getKey().getValue(), entry.getValue());
		}
		RegistryEntryAddedCallback.event(Registry.BLOCK).register((rawId, id, block) -> {
			PandoraTools.overrideLuminance(id, block);
		});
	}

}