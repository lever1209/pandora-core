package pkg.deepCurse.pandora.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import pkg.deepCurse.pandora.client.callbacks.ClientEndTickCallback;

@Environment(EnvType.CLIENT)
public class ClientInitialization {

	public static KeyBinding openScreen0 = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"pandora.menu.debug.key.open.screen.0", InputUtil.GLFW_KEY_F7, "pandora.key.category.debug"));
	public static KeyBinding openScreen1 = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"pandora.menu.debug.key.open.screen.1", InputUtil.GLFW_KEY_F8, "pandora.key.category.debug"));
	public static KeyBinding openScreen2 = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"pandora.menu.debug.key.open.screen.2", InputUtil.GLFW_KEY_F9, "pandora.key.category.debug"));
	public static KeyBinding openScreen3 = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"pandora.menu.debug.key.open.screen.3", InputUtil.GLFW_KEY_F10, "pandora.key.category.debug"));

	public static void registerCallbacks() {
		ClientTickEvents.END_CLIENT_TICK.register(ClientEndTickCallback::endClientTick);
	}

}
