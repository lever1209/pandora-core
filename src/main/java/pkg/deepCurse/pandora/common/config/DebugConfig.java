package pkg.deepCurse.pandora.common.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pkg.deepCurse.pandora.common.util.ConfigUtils;

public class DebugConfig {

	private static Logger log = LoggerFactory.getLogger(DebugConfig.class);

	public static DebugConfig DEBUG;

	public float flameLightSourceDecayRate;
	public boolean forceGruesAlwaysAttack;

	public boolean deleteConfigsOnLoad;
	public boolean useDebugMenuForModMenu;
	public boolean enableSavingConfigFile;

	public boolean PaintLightValues;

	public static void loadDebugConfigFromInputStream(InputStream is) throws IOException {
		log.debug("[Pandora] Loading debug config. . .");
		Properties props = new Properties();
		props.load(is);

		DebugConfig debug = new DebugConfig();

		debug.flameLightSourceDecayRate = Float.parseFloat(props.get("flameLightSourceDecayRate").toString());
		debug.forceGruesAlwaysAttack = Boolean.parseBoolean(props.get("forceGruesAlwaysAttack").toString());

		debug.deleteConfigsOnLoad = Boolean.parseBoolean(props.get("deleteConfigsOnLoad").toString());
		debug.useDebugMenuForModMenu = Boolean.parseBoolean(props.get("useDebugMenuForModMenu").toString());
		debug.enableSavingConfigFile = Boolean.parseBoolean(props.getProperty("enableSavingConfigFile").toString());

		debug.PaintLightValues = Boolean.parseBoolean(props.getProperty("PaintLightValues").toString());

		DebugConfig.DEBUG = debug;
		log.debug("[Pandora] Debug config loaded.");
	}

	public static void saveDebugConfig() {
		var config = ConfigUtils.getConfigFile("pandora.debug.properties");

		if (!config.canWrite()) {
			log.warn("Cannot write to debug config file!");
		}
		saveDebugConfigToFile(config);
	}

	/**
	 * Never call this normally, this should only be accessed from the debug menu
	 * 
	 * @param file
	 */
	public static void saveDebugConfigToFile(File file) {
		log.info("[Pandora] Saving debug config. . .");

		log.info("[Pandora] Debug config saved.");
	}

	public static void loadDebugConfig() {
		try {
			var config = ConfigUtils.getConfigFile("pandora.debug.properties");

			if (!config.exists()) {
				log.debug("Unpacking debug config. . .");
				ConfigUtils.unpackageFile("pandora.debug.properties");
				log.debug("Debug config unpacked.");
			}

			@SuppressWarnings("resource")
			InputStream fis = new FileInputStream(config);

			if (!config.canRead()) {
				log.warn("Cannot read debug config file! Using internal defaults.");
				fis.close();
				fis = CommonConfig.class.getResourceAsStream("/assets/pandora/pandora.debug.properties");
				log.warn("Internal defaults loaded.");
			}

			loadDebugConfigFromInputStream(fis);
			fis.close();
		} catch (IOException e) {
			log.error("[Pandora] There was an IOException while loading the debug properties file.");
			log.error("{}", e);
			throw new RuntimeException("Unable to continue.");
		}
	}

}
