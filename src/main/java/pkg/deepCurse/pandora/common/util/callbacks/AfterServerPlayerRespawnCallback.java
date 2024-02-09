package pkg.deepCurse.pandora.common.util.callbacks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import pkg.deepCurse.pandora.common.util.interfaces.PlayerGrueDataInterface;

public class AfterServerPlayerRespawnCallback {

	private static Logger log = LoggerFactory.getLogger(AfterServerPlayerRespawnCallback.class);

	public static void afterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
		var oldPlayerGrueDataInterface = (PlayerGrueDataInterface) (PlayerEntity) oldPlayer;
		var newPlayerGrueDataInterface = (PlayerGrueDataInterface) (PlayerEntity) newPlayer;

		newPlayerGrueDataInterface
				.setLastTutorialEncounterTime(oldPlayerGrueDataInterface.getLastTutorialEncounterTime());
		newPlayerGrueDataInterface.setSkipTimeCheck(oldPlayerGrueDataInterface.skipTimeCheck());
		newPlayerGrueDataInterface.setTutorialEncountersLeft(oldPlayerGrueDataInterface.getTutorialEncountersLeft());

	}

}
