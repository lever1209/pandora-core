package pkg.deepCurse.pandora.client.callbacks;

import net.minecraft.client.MinecraftClient;
import pkg.deepCurse.pandora.client.ClientInitialization;
import pkg.deepCurse.pandora.client.gui.screens.DebugScreen;

public class ClientEndTickCallback {

	public static float EffectStrength = 1;
	private static int headDist = 24;
	private static int eyeDist = 64; // TODO chunk distance?

	public static void endClientTick(MinecraftClient client) {

		while (ClientInitialization.openScreen0.wasPressed()) {
			client.setScreen(new DebugScreen(client.currentScreen, (short) 0));
		}
		while (ClientInitialization.openScreen1.wasPressed()) {
			client.setScreen(new DebugScreen(client.currentScreen, (short) 1));
		}
		while (ClientInitialization.openScreen2.wasPressed()) {
			client.setScreen(new DebugScreen(client.currentScreen, (short) 2));
		}
		while (ClientInitialization.openScreen3.wasPressed()) {
			client.setScreen(new DebugScreen(client.currentScreen, (short) 3));
		}

//		client.player.sendMessage(Text.literal("Key 1 was pressed!"), false);

//		if (client.world != null) {
//			var settings = ClientConfig.CLIENT.DimensionSettings.get(client.world.getDimensionKey().getValue());
//
//			if (settings != null) {
//				
//				// TODO mirror and translate to avoid calculating the same thing that you can just invert
//				
//				if (settings.doCaveFogEffects) {
//
//					// TODO line trace so many units ahead of player and use that location instead?
//					// assuming directly down is 0 degreees, the player must be looking under 90
//					// degrees to run the above
//
//					client.getProfiler().push("pandora_caveFogEffects");
//
//					var playerFeetBlockPos = client.player.getBlockPos();
//					var eyePosY = client.player.getPos().y + client.player.getEyeHeight(client.player.getPose());
//					var mulValsAverage = 1f;
//
//					var headPitch = MathHelper.wrapDegrees(client.player.getPitch()) * -1;
//					var headYaw = MathHelper.wrapDegrees(client.player.getHeadYaw()) * -1;
//
//
//
////					Vec3d newPos = new Vec3d(Math.round(playerFeetBlockPos.getX() + posX), Math.round(eyePosY + posY),
////							Math.round(playerFeetBlockPos.getZ() + posZ));
//					
//					
//					
//					// ease into the new fog value mulVal
//					// TODO head position vals multiplicates against this since its the characters
//					// focal point
//					float speed = 0.0002f; // transition speed // TODO config this
//					EffectStrength = MathHelper
//							.clamp(EffectStrength + (EffectStrength < mulValsAverage ? speed : (speed * -1)), 0, 1);
//					client.getProfiler().pop();
//				} else {
//					EffectStrength = 1;
//				}
//			}
//		}
	}
}
