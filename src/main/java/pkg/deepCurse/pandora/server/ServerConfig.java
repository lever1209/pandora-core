package pkg.deepCurse.pandora.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import pkg.deepCurse.pandora.common.ConfigUtils;

@Environment(EnvType.SERVER)
public class ServerConfig {

	private static Logger log = LoggerFactory.getLogger(ServerConfig.class);
	public static ServerConfig SERVER;

	public static void loadServerConfig() {
		try {
			var config = ConfigUtils.getConfigFile("pandora.server.yaml");

			if (!config.exists()) {
				log.debug("Unpacking server config. . .");
				ConfigUtils.unpackageFile("pandora.server.yaml");
				log.debug("Server config unpacked.");
			}

			@SuppressWarnings("resource")
			InputStream fis = new FileInputStream(config);

			if (!config.canRead()) {
				log.warn("Cannot read server config file! Using internal defaults.");
				fis.close();
				fis = ServerConfig.class.getResourceAsStream("/assets/pandora/pandora.server.yaml");
				log.warn("Internal defaults loaded.");
			}

			loadServerConfigFromInputStream(fis);
			fis.close();
		} catch (IOException e) {
			log.error("[Pandora] There was an IOException while loading the server yaml file.");
			log.error("{}", e);
			throw new RuntimeException("Unable to continue.");
		}

	}

	@SuppressWarnings("unused")
	public static void loadServerConfigFromInputStream(InputStream is) {
		log.info("[Pandora] Loading server config. . .");

		Yaml yaml = new Yaml();

		LinkedHashMap<String, Object> rootMap = yaml.load(is);

		ServerConfig server = new ServerConfig();

		ServerConfig.SERVER = server;
		log.info("[Pandora] Server config loaded.");
	}

	public static void saveServerConfig() {
		var config = ConfigUtils.getConfigFile("pandora.server.yaml");

		if (!config.canWrite()) {
			log.warn("Cannot write to server config file!");
		}
		saveServerConfigToFile(config);
	}

	public static void saveServerConfigToFile(File file) {
		log.info("[Pandora] Saving server config. . .");

		log.info("[Pandora] Server config saved.");
	}
}
