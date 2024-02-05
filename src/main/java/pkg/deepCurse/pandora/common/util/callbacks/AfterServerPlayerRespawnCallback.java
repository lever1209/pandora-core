package pkg.deepCurse.pandora.common.util.callbacks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.server.network.ServerPlayerEntity;

public class AfterServerPlayerRespawnCallback {

	private static Logger log = LoggerFactory.getLogger(AfterServerPlayerRespawnCallback.class);

	public static void afterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
		log.info("{} {} {}", oldPlayer, newPlayer, alive);
	}

}
