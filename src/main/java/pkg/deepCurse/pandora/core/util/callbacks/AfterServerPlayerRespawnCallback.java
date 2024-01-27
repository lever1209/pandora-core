package pkg.deepCurse.pandora.core.util.callbacks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.AfterRespawn;
import net.minecraft.server.network.ServerPlayerEntity;

public class AfterServerPlayerRespawnCallback implements AfterRespawn {

	private static Logger log = LoggerFactory.getLogger(AfterServerPlayerRespawnCallback.class);
	
	@Override
	public void afterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
		log.info("{} {} {}", oldPlayer,newPlayer,alive);
	}
	
}
