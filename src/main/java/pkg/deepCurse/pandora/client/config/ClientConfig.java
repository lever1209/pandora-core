package pkg.deepCurse.pandora.client.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import pkg.deepCurse.pandora.common.config.CommonConfig;
import pkg.deepCurse.pandora.common.util.ConfigUtils;

@Environment(EnvType.CLIENT)
public class ClientConfig {

	private static Logger log = LoggerFactory.getLogger(ClientConfig.class);

	public static ClientConfig CLIENT;

	// TODO re implement?
	public boolean ResetGamma;
	public float GammaValue;

	public LinkedHashMap<Identifier, ClientDimensionConfig> clientDimensionConfigMap;

	public static class ClientDimensionConfig {
		private float fogLevel;
		private boolean isDark;
		private boolean ignoreSkyLight;
		private boolean lockMoonPhase;
		private int targetMoonPhase;
		private boolean doCaveFogEffects;
		private float caveFogTransitionSpeed;

		public float getFogLevel() {
			return fogLevel;
		}

		public ClientDimensionConfig setFogLevel(float fogLevel) {
			this.fogLevel = fogLevel;
			return this;
		}

		public boolean isDark() {
			return isDark;
		}

		public ClientDimensionConfig setDark(boolean isDark) {
			this.isDark = isDark;
			return this;
		}

		public boolean isIgnoreSkyLight() {
			return ignoreSkyLight;
		}

		public ClientDimensionConfig setIgnoreSkyLight(boolean ignoreSkyLight) {
			this.ignoreSkyLight = ignoreSkyLight;
			return this;
		}

		public boolean isLockMoonPhase() {
			return lockMoonPhase;
		}

		public ClientDimensionConfig setLockMoonPhase(boolean lockMoonPhase) {
			this.lockMoonPhase = lockMoonPhase;
			return this;
		}

		public int getTargetMoonPhase() {
			return targetMoonPhase;
		}

		public ClientDimensionConfig setTargetMoonPhase(int targetMoonPhase) {
			this.targetMoonPhase = targetMoonPhase;
			return this;
		}

		public boolean doCaveFogEffects() {
			return doCaveFogEffects;
		}

		public ClientDimensionConfig setDoCaveFogEffects(boolean doCaveFogEffects) {
			this.doCaveFogEffects = doCaveFogEffects;
			return this;
		}

		public float getCaveFogTransitionSpeed() {
			return caveFogTransitionSpeed;
		}

		public ClientDimensionConfig setCaveFogTransitionSpeed(float caveFogTransitionSpeed) {
			this.caveFogTransitionSpeed = caveFogTransitionSpeed;
			return this;
		}

		public ClientDimensionConfig(float fogLevel, boolean isDark, boolean ignoreSkyLight, boolean lockMoonPhase,
				int targetMoonPhase, boolean doCaveFogEffects, float caveFogTransitionSpeed) {
			this.fogLevel = fogLevel;
			this.isDark = isDark;
			this.ignoreSkyLight = ignoreSkyLight;
			this.lockMoonPhase = lockMoonPhase;
			this.targetMoonPhase = targetMoonPhase;
			this.doCaveFogEffects = doCaveFogEffects;
			this.caveFogTransitionSpeed = caveFogTransitionSpeed;
		}

		@Override
		public String toString() {
			return String.format(
					"DimensionSetting{fogLevel=%s, isDark=%s, ignoreSkyLight=%s, lockMoonPhase=%s, targetMoonPhase=%s, doCaveFogEffects=%s, caveFogTransitionSpeed=%s}",
					fogLevel, isDark, ignoreSkyLight, lockMoonPhase, targetMoonPhase, doCaveFogEffects,
					caveFogTransitionSpeed);
		}
	}

	public HashSet<Identifier> PotionEffects;

	public static void loadClientConfig() {
		try {
			var config = ConfigUtils.getConfigFile("pandora.client.yaml");

			if (!config.exists()) {
				log.debug("Unpacking client config. . .");
				ConfigUtils.unpackageFile("pandora.client.yaml");
				log.debug("Client config unpacked.");
			}

			@SuppressWarnings("resource")
			InputStream fis = new FileInputStream(config);

			if (!config.canRead()) {
				log.warn("Cannot read client config file! Using internal defaults.");
				fis.close();
				fis = CommonConfig.class.getResourceAsStream("/assets/pandora/pandora.client.yaml");
				log.warn("Internal defaults loaded.");
			}

			loadClientConfigFromInputStream(fis);
			fis.close();
		} catch (IOException e) {
			log.error("[Pandora] There was an IOException while loading the client yaml file.");
			log.error("{}", e);
			throw new RuntimeException("Unable to continue.");
		}

	}

	public static void loadClientConfigFromInputStream(InputStream is) {
		log.info("[Pandora] Loading client config. . .");

		Yaml yaml = new Yaml();

		LinkedHashMap<String, Object> rootMap = yaml.load(is);

		ClientConfig client = new ClientConfig();

		client.clientDimensionConfigMap = new LinkedHashMap<>();
		client.PotionEffects = new HashSet<>();

		client.ResetGamma = Boolean.parseBoolean(rootMap.getOrDefault("reset gamma", client.ResetGamma).toString());
		client.GammaValue = Float.parseFloat(rootMap.getOrDefault("gamma value", client.GammaValue).toString());

		@SuppressWarnings("unchecked") // if i could do this without casting i would
		ArrayList<HashMap<String, Object>> dimensionSettings = (ArrayList<HashMap<String, Object>>) rootMap
				.get("dimension settings");

		for (HashMap<String, ?> dim : dimensionSettings) {

			@SuppressWarnings("unchecked") // if i could do this without casting i would
			ArrayList<String> identifiers = (ArrayList<String>) dim.get("ids");

			if (identifiers == null) {
				throw new IllegalArgumentException(
						"Element does not contain required key \"ids\", please add it to the element. (" + dim + ")");
			}

			for (var cid : identifiers) {
				// TODO replace clamps with errors? not on the client config
				var id = new Identifier(cid);
				var fogFactor = MathHelper.clamp(Float.parseFloat(dim.get("fog factor").toString()), 0f, 1f);
				var isDark = Boolean.parseBoolean(dim.get("is dark").toString());
				var ignoreSkyLight = Boolean.parseBoolean(dim.get("ignore sky light").toString());
				var lockMoonPhase = Boolean.parseBoolean(dim.get("lock moon phase").toString());
				var targetMoonPhase = MathHelper.clamp(Integer.parseInt(dim.get("target moon phase").toString()), 0, 7);
				var doCaveFogEffects = Boolean.parseBoolean(dim.get("do cave fog effects").toString());
				var caveFogTransitionSpeed = Float.parseFloat(dim.get("cave fog transition speed").toString());
				client.clientDimensionConfigMap.put(id, new ClientDimensionConfig(fogFactor, isDark, ignoreSkyLight,
						lockMoonPhase, targetMoonPhase, doCaveFogEffects, caveFogTransitionSpeed));
			}
		}

		@SuppressWarnings("unchecked") // if i could do this without casting i would
		ArrayList<String> potionEffects = (ArrayList<String>) rootMap.get("potion effects");

		for (String stringId : potionEffects) {
			client.PotionEffects.add(new Identifier(stringId));
		}

		ClientConfig.CLIENT = client;
		log.info("[Pandora] Client config loaded.");
	}

	public static void saveClientConfig() {
		var config = ConfigUtils.getConfigFile("pandora.client.yaml");

		if (!config.canWrite()) {
			log.warn("Cannot write to client config file!");
		}
		saveClientConfigToFile(config);
	}

	public static void saveClientConfigToFile(File file) {
		log.info("[Pandora] Saving client config. . .");

		log.info("[Pandora] Client config saved.");
	}
}
