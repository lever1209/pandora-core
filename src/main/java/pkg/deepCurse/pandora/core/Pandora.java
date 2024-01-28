package pkg.deepCurse.pandora.core;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.block.Block;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import pkg.deepCurse.pandora.core.util.callbacks.AfterServerPlayerRespawnCallback;
import pkg.deepCurse.pandora.core.util.callbacks.BlockRegisterCallback;
import pkg.deepCurse.pandora.core.util.callbacks.EndServerWorldTickCallback;

public class Pandora implements ModInitializer, PreLaunchEntrypoint {

	public static Logger log = LoggerFactory.getLogger(Pandora.class);

	@Override
	public void onPreLaunch() {
		log.info("[Pandora] Running pre launch tasks. . .");

		MixinExtrasBootstrap.init();

		log.info("[Pandora] Finished pre launch tasks.");
	}

	@Override
	public void onInitialize() {

		log.info("[Pandora] Initializing mod. . .");

		PandoraConfig.loadConfig();

		registerCallbacks();

		log.info("[Pandora] Finished initializing mod.");

	}

	public static void registerCallbacks() {
		log.info("[Pandora] Registering callbacks.");

		ServerTickEvents.END_WORLD_TICK.register(new EndServerWorldTickCallback());
		ServerPlayerEvents.AFTER_RESPAWN.register(new AfterServerPlayerRespawnCallback());

		// we need both because the mod is not guaranteed to load before other mods, and
		// the vanilla registries do not use fabric api
		for (Entry<RegistryKey<Block>, Block> entry : Registry.BLOCK.getEntrySet()) {
			BlockRegisterCallback.overrideLuminance(entry.getKey().getValue(), entry.getValue());
		}
		RegistryEntryAddedCallback.event(Registry.BLOCK).register(new BlockRegisterCallback());

		log.info("[Pandora] Finished registering callbacks.");
	}

}