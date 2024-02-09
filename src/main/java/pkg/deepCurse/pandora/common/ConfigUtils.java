package pkg.deepCurse.pandora.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
//import ;
//import ;

public class ConfigUtils {
	private static Logger log = LoggerFactory.getLogger(ConfigUtils.class);

	public static File getConfigFile(String path) {
		return new File(FabricLoader.getInstance().getConfigDir().toFile(), path);
	}

	public static void loadConfigs() { // TODO write yaml parser to get spans and line numbers, and to add more user
										// friendly errors, make it path based

		log.info("[Pandora] Loading and applying configs. . .");

		DebugConfig.loadDebugConfig();

		if (DebugConfig.DEBUG.deleteConfigsOnLoad) {
			if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
				getConfigFile("pandora.client.yaml").delete();
			}
			getConfigFile("pandora.common.yaml").delete();
			if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
				getConfigFile("pandora.server.yaml").delete();
			}
		}

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			pkg.deepCurse.pandora.client.ClientConfig.loadClientConfig();
		}

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
			pkg.deepCurse.pandora.server.ServerConfig.loadServerConfig();
		}

		CommonConfig.loadCommonConfig();

		log.info("[Pandora] Loaded and applied configs.");
	}

	public static void saveConfigs() { // TODO finish save configs
		log.info("[Pandora] Saving config files. . .");
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
			pkg.deepCurse.pandora.client.ClientConfig.saveClientConfigToFile(getConfigFile("pandora.client.yaml"));
		}
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
			pkg.deepCurse.pandora.server.ServerConfig.saveServerConfigToFile(getConfigFile("pandora.server.yaml"));
		}

		CommonConfig.saveCommonConfigToFile(getConfigFile("pandora.common.yaml"));

		if (DebugConfig.DEBUG.enableSavingConfigFile) {
			DebugConfig.saveDebugConfigToFile(getConfigFile("pandora.debug.properties"));
		}

		log.info("[Pandora] Config files saved.");
	}

	public static void unpackageFile(String path) {

		try (FileWriter writer = new FileWriter(getConfigFile(path))) {
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(CommonConfig.class.getResourceAsStream("/assets/pandora/" + path)))) {

				for (int t; (t = reader.read()) != -1;) {
					writer.write(t);
				}
			}
		} catch (IOException e) {
			log.error("[Pandora] There was an IOException while unpackaging a file.");
			log.error("{}", e);
			throw new RuntimeException("Unable to continue.");
		}
	}
}
