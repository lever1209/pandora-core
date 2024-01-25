package pkg.deepCurse.pandora.core.util.integrations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.minecraft.client.gui.screen.Screen;
import pkg.deepCurse.pandora.core.util.screens.DebugScreen;

public class ModMenuIntegration implements ModMenuApi {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(ModMenuIntegration.class);

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> createNewConfigScreen(screen);
	}

	public Screen createNewConfigScreen(Screen parent) {
		// return new PandoraSpruceUIScreen(parent);
		return new DebugScreen(parent);

	}
}
