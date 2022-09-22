package pkg.deepCurse.pandora.core;

import java.io.IOException;
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

	private static Logger log = LoggerFactory.getLogger(Pandora.class);

	// TODO hip lantern
	
	// TODO light enchantment
	
	// TODO torches burn out and can only be relit twice, after that they turn to ash
	
	// TODO refil lanterns with phantom membrane, and have 3x slower burn rate
	
	// TODO glowstone paste of some kind to augment equipment with light, vanishes on repair and grindstone
	
	/* TODO "tutorial" message
	 * first time player gets "Its pitch black, you are likely to be eaten by a grue."
	 * then cooldown, and 50% every time you enter darkness, until you get it, then cooldown
	 * then chance reduces to 15%, until message has appeared 10 times, then disable message
	 */
	
	@Override
	public void onPreLaunch() {
		log.info("[Pandora] Running pre launch initializers. . .");
		MixinExtrasBootstrap.init();
		log.info("[Pandora] Finished pre launch initializers.");
	}

	@Override
	public void onInitialize() {

		log.info("[Pandora] Initializing mod. . .");

		ServerTickEvents.END_WORLD_TICK.register((world) -> {
			EndServerTickCallback.run(world);
		});

		for (Entry<RegistryKey<Block>, Block> entry : Registry.BLOCK.getEntrySet()) {
			PandoraTools.overrideLuminance(entry.getKey().getValue(), entry.getValue());
		}
		RegistryEntryAddedCallback.event(Registry.BLOCK).register((rawId, id, block) -> {
			PandoraTools.overrideLuminance(id, block);
		});

		log.info("[Pandora] Loading and applying config. . .");
		if (!PandoraConfig.getConfigFile().exists()) {
			try {
				log.info("[Pandora] Config not found, extracting sample config. . .");
				PandoraConfig.unpackageConfig();
				log.info("[Pandora] Example config extracted.");
				PandoraConfig.loadConfig();
				log.info("[Pandora] Loaded and applied config.");
			} catch (IOException e) {
				log.error("[Pandora] Failed to extract example config.");
				e.printStackTrace();
				log.error("[Pandora] using internal defaults for now, please look into this issue using the above stack trace.");
			}
		} else {
			PandoraConfig.loadConfig();
			log.info("[Pandora] Loaded and applied config.");
		}
 
		log.info("[Pandora] Finished initializing mod.");

	}

}