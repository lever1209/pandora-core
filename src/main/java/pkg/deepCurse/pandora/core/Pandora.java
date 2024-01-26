package pkg.deepCurse.pandora.core;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.block.Block;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import pkg.deepCurse.pandora.core.util.callbacks.EndServerTickCallback;
import pkg.deepCurse.pandora.core.util.tools.PandoraTools;

public class Pandora implements ModInitializer, PreLaunchEntrypoint {

	public static Logger log = LoggerFactory.getLogger(Pandora.class);

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
			// try catch throwable this?
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