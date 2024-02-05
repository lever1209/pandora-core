package pkg.deepCurse.pandora.common;

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
import pkg.deepCurse.pandora.common.util.callbacks.AfterServerPlayerRespawnCallback;
import pkg.deepCurse.pandora.common.util.callbacks.BlockRegisterCallback;
import pkg.deepCurse.pandora.common.util.callbacks.EndServerWorldTickCallback;

public class Pandora implements ModInitializer, PreLaunchEntrypoint {

	// ASAP environment types on all classes

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

		PandoraConfig.loadConfigs();

		modifyRegistries();

		registerCallbacks();

		log.info("[Pandora] Finished initializing mod.");

	}

	public static void modifyRegistries() {
		log.info("[Pandora] Modifying registries. . .");

		for (Entry<RegistryKey<Block>, Block> entry : Registry.BLOCK.getEntrySet()) {
			BlockRegisterCallback.overrideLuminance(entry.getKey().getValue(), entry.getValue());
		}

		log.info("[Pandora] Registries modified.");
	}

	private static boolean callbacksRegistered = false;

	public static void registerCallbacks() {
		log.info("[Pandora] Registering callbacks. . .");

		if (callbacksRegistered) {
			log.warn("[Pandora] Aborting callback registration as they have already been registered.");
			return;
		}

		ServerTickEvents.END_WORLD_TICK.register(EndServerWorldTickCallback::onEndTick);
		ServerPlayerEvents.AFTER_RESPAWN.register(AfterServerPlayerRespawnCallback::afterRespawn);

		RegistryEntryAddedCallback.event(Registry.BLOCK).register(BlockRegisterCallback::onEntryAdded);

		callbacksRegistered = true;
		
		log.info("[Pandora] Finished registering callbacks.");
	}

}