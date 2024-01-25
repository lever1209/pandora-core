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

	// TODO hip lantern?

	// TODO light enchantment? glowstone ash and ghast tear paste to become an armor trim instead?

	// TODO torches burn out and can only be relit twice, after that they turn to
	// ash

	// TODO refil lanterns with phantom membrane, and have 3x slower burn rate

	// TODO glowstone paste of some kind to augment equipment with light, vanishes
	// on repair and grindstone

	/*
	 * TODO "tutorial" message first time player gets
	 * "Its pitch black, you are likely to be eaten by a grue." then cooldown, and
	 * 50% every time you enter darkness, until you get it, then cooldown then
	 * chance reduces to 15%, until message has appeared 10 times, then disable
	 * message
	 */

	// TODO setup gamerules for select config options

	// TODO fade light level

	// TODO fix readme, that last line is kinda cringe

	// TODO add button on worlds menu to recalculate all blocks lighting,
	// potentially dangerous with non vanilla, so throw up a warning, this "solves"
	// the lighting artifacts when changing lighting values without needing too much
	// cpu power in game, should only be used when editing these values or when
	// converting world to pandora since all lighting values are cached

	// TODO spawn a light generating whisp when respawning, it lasts 2 minutes and
	// follows the player, or until the player enters the light
	@Override
	public void onPreLaunch() {
		log.info("[Pandora] Running pre launch initializers. . .");
		MixinExtrasBootstrap.init();
		log.info("[Pandora] Finished pre launch initializers.");
	}

	@Override
	public void onInitialize() {

		log.info("[Pandora] Initializing mod. . .");

		PandoraRegistry.init();

		PandoraConfig.deleteConfig(); // FIXME remove before producton compile
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