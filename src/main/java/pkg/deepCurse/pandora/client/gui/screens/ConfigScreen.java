package pkg.deepCurse.pandora.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ConfigScreen extends Screen {

	private Screen parent;

	public ConfigScreen(Screen parent) {
		super(Text.translatable("pandora.config.menu.title"));
		this.parent = parent;
	}
}