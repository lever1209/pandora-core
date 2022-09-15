package pkg.deepCurse.pandora.core.integrations;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.minecraft.client.gui.screen.Screen;
import pkg.deepCurse.pandora.core.screens.DebugScreen;

public class ModMenu implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> createNewConfigScreen(screen);
	}

	public Screen createNewConfigScreen(Screen parent) {
		// return new PandoraSpruceUIScreen(parent);
		return new DebugScreen(parent);

	}
}
