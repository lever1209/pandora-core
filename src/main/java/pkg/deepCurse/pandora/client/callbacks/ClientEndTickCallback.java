package pkg.deepCurse.pandora.client.callbacks;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import pkg.deepCurse.pandora.client.ClientInitialization;
import pkg.deepCurse.pandora.client.ClientTools;
import pkg.deepCurse.pandora.client.config.ClientConfig;
import pkg.deepCurse.pandora.client.gui.screens.DebugScreen;
import pkg.deepCurse.pandora.common.CommonTools;

public class ClientEndTickCallback {

	public static float EffectStrength = 1;
	/**
	 * This is the distance that ray marches from the center of the player head will
	 * travel in 45 degree increments in two layers around the head
	 * <p>
	 * 16 positions will then be found, either a block or the position at the end of
	 * the distance, if it is the end of the distance, the world heightmap will then
	 * be used to calculate how much space above the player is covered up, and
	 * whether they can see the sky from there
	 * <p>
	 * these values will then be averaged out with weights, horizontal 0, vertical
	 * 0.75
	 */
	private static int coveringDistance = 5;
	/**
	 * This is the distance that ray marches from the center of the players head
	 * will take along the path the player is looking, it will then either use the
	 * intersected block position or the position at the end of the distance to
	 * calculate how dark that position is and adjust the fog accordingly
	 * <p>
	 * This will be used to change the fog coloring if you are staring into a cave
	 * and it should be dark, or if you are inside a cave staring out and it should
	 * be bright
	 */
	private static int playerFacingDist = 64; // TODO chunk distance?

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

		if (client.world != null) {
			var clientSettings = ClientConfig.CLIENT.clientDimensionConfigMap
					.get(client.world.getDimensionKey().getValue());

			if (clientSettings != null) {
				if (clientSettings.doCaveFogEffects()) {

					// TODO line trace so many units ahead of player and use that location instead?
					// assuming directly down is 0 degreees, the player must be looking under 90
					// degrees to run the above

					// CHANGE OF PLANS, DO PORCUPINE ON BOTH PLAYER AND EYE CAST LOCATION

					client.getProfiler().push("pandora_caveFogEffects");
					var originPositionBlockPos = client.player.getBlockPos().add(0,
							client.player.getEyeHeight(client.player.getPose()), 0);
					var originPosition = new Vec3d(originPositionBlockPos.getX(), originPositionBlockPos.getY(),
							originPositionBlockPos.getZ());
					Vec3d eyeTargetContactPosition;
					{
						// mul -1 because the axis are backwards for some reason
						var headPitch = MathHelper.wrapDegrees(client.player.getPitch()) * -1;
						// mul -1 because the axis are backwards for some reason
						var headYaw = MathHelper.wrapDegrees(client.player.getHeadYaw()) * -1;

						var targetRelativePosition = CommonTools.getPosFromSphericalPosition(headPitch, headYaw,
								playerFacingDist * 0.8);
						Vec3d targetAbsolutePosition = originPosition.add(targetRelativePosition.x,
								targetRelativePosition.y, targetRelativePosition.z);

						var distOriginToTarget = originPosition.distanceTo(targetAbsolutePosition);

						eyeTargetContactPosition = CommonTools.rayMarch(originPosition, targetAbsolutePosition,
								(float) (distOriginToTarget * 1.15f), ClientTools::isBlockRayMarchTarget);

						if (eyeTargetContactPosition == null) {
							eyeTargetContactPosition = targetAbsolutePosition;
						}

					}

					// TODO mirror and translate to avoid calculating the same thing that you can
					// just invert

					var mulValsTotal = 0;
					var mulValsMix = 0f;

					if (ClientTools.isBlockRayMarchTarget(eyeTargetContactPosition)) {
						mulValsMix--;
						mulValsTotal++;
					}
					// TODO better collision handling for eyes only
					mulValsMix -= lotus(eyeTargetContactPosition, client);
					mulValsTotal += 32;

					mulValsMix -= lotus(originPosition, client);
					mulValsTotal += 32;

					mulValsMix = Math.abs(mulValsMix + mulValsTotal);

//					Pandora.log.info("{}/{}={}", mulValsMix, mulValsTotal, (mulValsMix / mulValsTotal));

					client.player.sendMessage(
							Text.literal(
									String.format("%s/%s=%s", mulValsMix, mulValsTotal, (mulValsMix / mulValsTotal))),
							true);

					// ease into the new fog value
//					EffectStrength = MathHelper.clamp(EffectStrength
//							+ (EffectStrength < mulValsMix / mulValsTotal ? clientSettings.caveFogTransitionSpeed
//									: (clientSettings.caveFogTransitionSpeed * -1)),
//							0, 1);

					EffectStrength = mulValsMix / mulValsTotal;

					client.player.sendMessage(Text.literal("ES" + EffectStrength), true);

					client.getProfiler().pop();
				} else {
					EffectStrength = 1;
				}
			}
		}
	}

	private static float lotus(Vec3d originPosition, MinecraftClient client) {

		var mulValsMix = 0f;

		for (float yawMod = 0; yawMod < 16; yawMod++) {
			for (float pitchMod = 1; pitchMod <= 2; pitchMod++) {

				// TODO add random variance of 30 degrees and re test
				float yaw = yawMod * 22.5f; // 90f / 4f
				float pitch = pitchMod * 22.5f; // 90f / 4f

				var targetRelativePosition = CommonTools.getPosFromSphericalPosition(pitch, yaw,
						pitchMod == 1 ? coveringDistance * 2 : coveringDistance);
				Vec3d targetAbsolutePosition = originPosition.add(targetRelativePosition.x, targetRelativePosition.y,
						targetRelativePosition.z);

				var distOriginToTarget = originPosition.distanceTo(targetAbsolutePosition);

				var targetContactPosition = CommonTools.rayMarch(
						new Vec3d(originPosition.getX(), originPosition.getY(), originPosition.getZ()),
						targetAbsolutePosition, (float) (distOriginToTarget * 1.15f),
						ClientTools::isBlockRayMarchTarget);
				if (targetContactPosition == null) {
					targetContactPosition = targetAbsolutePosition;
				}

				if (ClientTools.isBlockRayMarchTarget(targetContactPosition)) {
					mulValsMix++;
				} else {

					targetAbsolutePosition = new Vec3d(targetAbsolutePosition.x, client.world.getTopY(),
							targetAbsolutePosition.z);

					var distOriginToSkyTarget = targetContactPosition.distanceTo(targetAbsolutePosition);

					var verticalCheckPosition = CommonTools.rayMarch(
							new Vec3d(targetContactPosition.getX(), targetContactPosition.getY(),
									targetContactPosition.getZ()),
							targetAbsolutePosition, (float) (distOriginToSkyTarget * 1.15f),
							ClientTools::isBlockRayMarchTarget);
					if (verticalCheckPosition == null) {
						verticalCheckPosition = targetAbsolutePosition;
					}
					if (ClientTools.isBlockRayMarchTarget(verticalCheckPosition)) {
						mulValsMix++;
					}
				}
			}
		}

		return mulValsMix;
	}

}
