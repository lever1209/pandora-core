package pkg.deepCurse.pandora.common.callbacks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import pkg.deepCurse.pandora.common.interfaces.PlayerGrueDataInterface;

public class AfterServerPlayerRespawnCallback {

	public static void afterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
		var oldPlayerGrueDataInterface = (PlayerGrueDataInterface) (PlayerEntity) oldPlayer;
		var newPlayerGrueDataInterface = (PlayerGrueDataInterface) (PlayerEntity) newPlayer;

		newPlayerGrueDataInterface
				.setLastTutorialEncounterTime(oldPlayerGrueDataInterface.getLastTutorialEncounterTime());
		newPlayerGrueDataInterface.setSkipTimeCheck(oldPlayerGrueDataInterface.skipTimeCheck());
		newPlayerGrueDataInterface.setTutorialEncountersLeft(oldPlayerGrueDataInterface.getTutorialEncountersLeft());

	}

}
