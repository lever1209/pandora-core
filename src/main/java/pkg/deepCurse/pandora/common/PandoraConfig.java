package pkg.deepCurse.pandora.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import pkg.deepCurse.pandora.common.util.interfaces.ConditionalToIntFunction;

public class PandoraConfig {

	// TODO remove constructors?

	public static class Enabled {
		public static PandoraConfig.Enabled ENABLED;

		public boolean EnablePandora = true;
		public boolean EnableCustomFog = true;
		public boolean EnableCustomAI = true;
		public boolean EnableCustomLightmap = true;
		public boolean EnableGrueWards = true;
	}

	@Environment(EnvType.CLIENT)
	public static class Client {
		public static PandoraConfig.Client CLIENT;

		// TODO re implement?
		public boolean ResetGamma;
		public float GammaValue;

		public LinkedHashMap<Identifier, DimensionSetting> DimensionSettings = null;

		public static class DimensionSetting {
			public float fogLevel;
			public boolean isDark;

			public DimensionSetting(float fogLevel, boolean isDark) {
				this.fogLevel = fogLevel;
				this.isDark = isDark;
			}

			@Override
			public String toString() {
				return String.format("DimensionSetting{fogLevel=%s, isDark=%s}", fogLevel, isDark);
			}
		}

	}

//	@Environment(EnvType.SERVER)
	public static class Server {

		public static PandoraConfig.Server SERVER;

		public boolean GruesEatItems = true;
		public float GruesEatItemsChance = 0.0f; // TODO fix this value

		public float Player_DamageMultiplier = 1.0f;
		public boolean Player_UsesHardcoreDifficulty = true;
		public boolean Player_CanUseGrueWards = true;

		public LinkedHashMap<DifficultySettingsEnum, DifficultySettings> DifficultySettingsMap = null;
		public LinkedHashMap<Identifier, BlockLightLevelSetting> BlockLightLevelSettings = null;
		public LinkedHashMap<Identifier, DimensionSetting> DimensionSettings = null;
		public LinkedHashMap<Identifier, GrueWardSetting> GrueWardSettings = null;
		public LinkedHashMap<Identifier, MobSetting> MobSettings = null;

		public static enum DifficultySettingsEnum {
			Peaceful, Easy, Normal, Hard, Hardcore;

			public static DifficultySettingsEnum getFromWorld(ServerWorld world) {
//				if (world.getServer().isHardcore()) {
//					return Hardcore;
//				}

				switch (world.getDifficulty()) {
				case HARD:
					return Hard;
				case NORMAL:
					return Normal;
				case EASY:
					return Easy;
				case PEACEFUL:
					return Peaceful;
				default: // WTF java, this default should not be required at all
					return Normal;
				}
			}
		}

		// TODO simplify by ditching constructors?

		public static class DifficultySettings {
			public float damageAmount;
			public short grueTutorialCount;
			public short grueTutorialGracePeriod;
			public short grueMaxTickWait;
			public short grueMinTickWait;
			public boolean gruesIgnoreArmor;

			public DifficultySettings(float damageAmount, short grueTutorialCount, short grueTutorialGracePeriod,
					short grueMaxTickWait, short grueMinTickWait, boolean gruesIgnoreArmor) {
				this.damageAmount = damageAmount;
				this.grueTutorialCount = grueTutorialCount;
				this.grueTutorialGracePeriod = grueTutorialGracePeriod;
				this.grueMaxTickWait = grueMaxTickWait;
				this.grueMinTickWait = grueMinTickWait;
				this.gruesIgnoreArmor = gruesIgnoreArmor;
			}

			@Override
			public String toString() {
				return String.format(
						"BlockLightSetting{damageAmount=%s, grueTutorialCount=%s, grueTutorialGracePeriod=%s, grueMaxTickWait=%s, grueMinTickWait=%s, gruesIgnoreArmor=%s}",
						damageAmount, grueTutorialCount, grueTutorialGracePeriod, grueMaxTickWait, grueMinTickWait,
						gruesIgnoreArmor);
			}

		}

		public static class BlockLightLevelSetting {
			public ConditionalToIntFunction<BlockState> LightLevel;

			public BlockLightLevelSetting(ConditionalToIntFunction<BlockState> level) {
				this.LightLevel = level;
			}
		}

		public static class DimensionSetting {
			public boolean infested;
			public boolean ignoreSkyLight;
			public boolean lockMoonPhase;
			public int targetMoonPhase;
			public int minimumSafeLightLevel;
			public int minimumFadeLightLevel;
			public boolean gruesAttackInWater;

			public DimensionSetting(boolean infested, boolean ignoreSkyLight, boolean lockMoonPhase,
					int targetMoonPhase, int minimumSafeLightLevel, int minimumFadeLightLevel,
					boolean gruesAttackInWater) {
				this.infested = infested;
				this.minimumSafeLightLevel = minimumSafeLightLevel;
				this.minimumFadeLightLevel = minimumFadeLightLevel;
				this.gruesAttackInWater = gruesAttackInWater;
				this.ignoreSkyLight = ignoreSkyLight;
				this.lockMoonPhase = lockMoonPhase;
				this.targetMoonPhase = targetMoonPhase;
			}

			@Override
			public String toString() {
				return String.format(
						"DimensionSetting{infested=%s, minimumSafeLightLevel=%s, minimumFadeLightLevel=%s, gruesAttackInWater=%s, ignoreSkyLight=%s, lockMoonPhase=%s, targetMoonPhase=%s}",
						infested, minimumSafeLightLevel, minimumFadeLightLevel, gruesAttackInWater, ignoreSkyLight,
						lockMoonPhase, targetMoonPhase);
			}
		}

		public static class GrueWardSetting {
			public float potency;
			public int lightLevel;

			public GrueWardSetting(float potency, int lightLevel) {
				this.potency = potency;
				this.lightLevel = lightLevel;
			}

			@Override
			public String toString() {
				return String.format("GrueWardSetting{potency=%s, lightLevel=%s}", potency, lightLevel);
			}
		}

		public static class MobSetting {
			public float damageMultiplier;
			public boolean fearDarkness;
			// TODO setup fear weight
			public float fearWeight;
			public boolean isHardcore;

			public MobSetting(float damageMultiplier, boolean fearsDarkness, float fearWeight, boolean isHardcore) {
				this.damageMultiplier = damageMultiplier;
				this.fearDarkness = fearsDarkness;
				this.fearWeight = fearWeight;
				this.isHardcore = isHardcore;
			}

			@Override
			public String toString() {
				return String.format("MobSetting{damageMultiplier=%s, fearDarkness=%s, fearWeight=%s, isHardcore=%s}",
						damageMultiplier, fearDarkness, fearWeight, isHardcore);
			}

		}

	}

	public static class Debug {
		public static PandoraConfig.Debug DEBUG;

		public float flameLightSourceDecayRate = 1.0f;
		public boolean forceGruesAlwaysAttack = false;

		public boolean deleteConfigsOnLoad = false;
		public boolean useDebugMenuForModMenu = false;
		public boolean enableSavingConfigFile;
	}

	private static Logger log = LoggerFactory.getLogger(PandoraConfig.class);

	public static File getConfigFile(String path) {
		return new File(FabricLoader.getInstance().getConfigDir().toFile(), path);
	}

	public static void loadConfigs() { // TODO write yaml parser to get spans and line numbers, and to add more user
										// friendly errors, make it path based

		log.info("[Pandora] Loading and applying configs. . .");

		loadDebugConfig();

		if (Debug.DEBUG.deleteConfigsOnLoad) {
			getConfigFile("pandora.client.yaml").delete();
			getConfigFile("pandora.server.yaml").delete();
		}

		loadClientConfig();

		loadServerConfig();

		log.info("[Pandora] Loaded and applied configs.");
	}

	public static void loadServerConfig() {
		try {
			var config = getConfigFile("pandora.server.yaml");

			if (!config.exists()) {
				log.debug("Unpacking server config. . .");
				unpackageFile("pandora.server.yaml");
				log.debug("Server config unpacked.");
			}

			@SuppressWarnings("resource")
			InputStream fis = new FileInputStream(config);

			if (!config.canRead()) {
				log.warn("Cannot read server config file! Using internal defaults.");
				fis.close();
				fis = PandoraConfig.class.getResourceAsStream("/assets/pandora/pandora.server.yaml");
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

	public static void loadClientConfig() {
		try {
			var config = getConfigFile("pandora.client.yaml");

			if (!config.exists()) {
				log.debug("Unpacking client config. . .");
				unpackageFile("pandora.client.yaml");
				log.debug("Client config unpacked.");
			}

			@SuppressWarnings("resource")
			InputStream fis = new FileInputStream(config);

			if (!config.canRead()) {
				log.warn("Cannot read client config file! Using internal defaults.");
				fis.close();
				fis = PandoraConfig.class.getResourceAsStream("/assets/pandora/pandora.client.yaml");
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

	public static void loadDebugConfig() {
		try {
			var config = getConfigFile("pandora.debug.properties");

			if (!config.exists()) {
				log.debug("Unpacking debug config. . .");
				unpackageFile("pandora.debug.properties");
				log.debug("Debug config unpacked.");
			}

			@SuppressWarnings("resource")
			InputStream fis = new FileInputStream(config);

			if (!config.canRead()) {
				log.warn("Cannot read debug config file! Using internal defaults.");
				fis.close();
				fis = PandoraConfig.class.getResourceAsStream("/assets/pandora/pandora.debug.properties");
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

	public static void loadServerConfigFromInputStream(InputStream is) {
		log.info("[Pandora] Loading server config. . .");

		Yaml yaml = new Yaml();

		LinkedHashMap<String, Object> rootMap = yaml.load(is);

		Server server = new Server();

		server.GruesEatItems = Boolean
				.parseBoolean(rootMap.getOrDefault("grues eat items", server.GruesEatItems).toString());
		server.GruesEatItemsChance = Float
				.parseFloat(rootMap.getOrDefault("grues eat items chance", server.GruesEatItemsChance).toString());

		@SuppressWarnings("unchecked")
		HashMap<String, Object> playerSettings = (HashMap<String, Object>) rootMap.get("player settings");

		server.Player_DamageMultiplier = Float.parseFloat(playerSettings.get("damage multiplier").toString());
		server.Player_UsesHardcoreDifficulty = Boolean
				.parseBoolean(playerSettings.get("uses hardcore difficulty").toString());
		server.Player_CanUseGrueWards = Boolean.parseBoolean(playerSettings.get("can use grue wards").toString());

		server.DifficultySettingsMap = new LinkedHashMap<>();
		server.DimensionSettings = new LinkedHashMap<>();
		server.MobSettings = new LinkedHashMap<>();
		server.BlockLightLevelSettings = new LinkedHashMap<>();
		server.GrueWardSettings = new LinkedHashMap<>();

		// difficulty settings
		{
			@SuppressWarnings("unchecked") // if i could do this without casting i would
			LinkedHashMap<String, Object> difficultySettingsMapMap = (LinkedHashMap<String, Object>) rootMap
					.get("difficulty settings");

			for (var i : Server.DifficultySettingsEnum.values()) {
				@SuppressWarnings("unchecked")
				LinkedHashMap<String, Object> difficultySettingsMap = (LinkedHashMap<String, Object>) difficultySettingsMapMap
						.get(i.toString().toLowerCase());

				var damageAmount = Float.parseFloat(difficultySettingsMap.get("damage amount").toString());
				var grueTutorialCount = Short.parseShort(difficultySettingsMap.get("grue tutorial count").toString());
				var grueTutorialGracePeriod = Short
						.parseShort(difficultySettingsMap.get("grue tutorial grace period").toString());
				var grueMaxTickWait = Short.parseShort(difficultySettingsMap.get("grue maximum tick wait").toString());
				var grueMinTickWait = Short.parseShort(difficultySettingsMap.get("grue minimum tick wait").toString());
				var gruesIgnoreArmor = Boolean.parseBoolean(difficultySettingsMap.get("grues ignore armor").toString());

				server.DifficultySettingsMap.put(i, new Server.DifficultySettings(damageAmount, grueTutorialCount,
						grueTutorialGracePeriod, grueMaxTickWait, grueMinTickWait, gruesIgnoreArmor));
			}
		} // this scope exists to hopefully gc the content earlier than usual, ill have to
			// research this behavior before i sprinkle it throughout the mod

		// block light settings
		{
			@SuppressWarnings("unchecked") // if i could do this without casting i would
			ArrayList<HashMap<String, ?>> blockLightSettings = (ArrayList<HashMap<String, ?>>) rootMap
					.get("block light settings");

			for (HashMap<String, ?> i : blockLightSettings) {
				// ill do parsing errors another time // TODO make that custom yaml parser path
				// based to avoid making all these useless objects and checks
//						if (!i.containsKey("light level")) {
//							throw new IllegalArgumentException(
//									"Value for required key \"light level\" not found or invalid. type required: int, example key value pair: \"light level: 7\"");
//						}
//			

				@SuppressWarnings("unchecked") // if i could do this without casting i would
				LinkedHashMap<String, Object> configPropertyHashMap = (LinkedHashMap<String, Object>) i
						.get("properties");

				Identifier id = new Identifier(i.get("id").toString());
				int lightLevel = Integer.parseInt(i.get("light level").toString());
				ConditionalToIntFunction<BlockState> conditional_to_int_function =

						(argBlockstate, oldValue) -> { // TODO revisit this
							if (!argBlockstate.getEntries().isEmpty() && configPropertyHashMap != null) {

								// TODO avoid turning it into a string, create a blockstate from the config to
								// compare
								var translatedBlockState = new HashMap<String, String>();
								for (var ij : argBlockstate.getEntries().entrySet()) {
									translatedBlockState.put(ij.getKey().getName(), ij.getValue().toString());
								}
								for (var config_property : configPropertyHashMap.entrySet()) {
									if (!translatedBlockState.get(config_property.getKey())
											.contentEquals(config_property.getValue().toString())) {
										return oldValue;
									}
								}
							}
							return lightLevel;
						};

				server.BlockLightLevelSettings.put(id, new Server.BlockLightLevelSetting(conditional_to_int_function));
			}
		}

		// dimension settings
		{
			@SuppressWarnings("unchecked") // if i could do this without casting i would
			ArrayList<HashMap<String, Object>> dimensionSettings = (ArrayList<HashMap<String, Object>>) rootMap
					.get("dimension settings");

			for (HashMap<String, ?> dim : dimensionSettings) {

				@SuppressWarnings("unchecked") // if i could do this without casting i would
				ArrayList<String> identifiers = (ArrayList<String>) dim.get("ids");

				if (identifiers == null) {
					throw new IllegalArgumentException(
							"Element does not contain required key \"ids\", please add it to the element. (" + dim
									+ ")");
				}

				for (var cid : identifiers) {

					// TODO replace clamps with errors?

					var id = new Identifier(cid);
//					var fogFactor = MathHelper.clamp(Float.parseFloat(dim.get("fog factor").toString()), 0f, 1f);
					var infested = Boolean.parseBoolean(dim.get("infested").toString());
//					var isDark = Boolean.parseBoolean(dim.get("is dark").toString());
					var ignoreSkyLight = Boolean.parseBoolean(dim.get("ignore sky light").toString());
					var lockMoonPhase = Boolean.parseBoolean(dim.get("lock moon phase").toString());
					var targetMoonPhase = MathHelper.clamp(Integer.parseInt(dim.get("target moon phase").toString()), 0,
							7);
					var minimumSafeLightLevel = MathHelper
							.clamp(Integer.parseInt(dim.get("minimum safe light level").toString()), 0, 15);
					var minimumFadeLightLevel = MathHelper
							.clamp(Integer.parseInt(dim.get("minimum fade light level").toString()), 0, 15);
					var gruesAttackInWater = Boolean.parseBoolean(dim.get("grues attack in water").toString());

					server.DimensionSettings.put(id,
							new Server.DimensionSetting(infested, ignoreSkyLight, lockMoonPhase, targetMoonPhase,
									minimumSafeLightLevel, minimumFadeLightLevel, gruesAttackInWater));
				}
			}
		}
		{
			@SuppressWarnings("unchecked") // if i could do this without casting i would
			ArrayList<HashMap<String, Object>> grueWards = (ArrayList<HashMap<String, Object>>) rootMap
					.get("grue wards");

			for (HashMap<String, Object> i : grueWards) {
				@SuppressWarnings("unchecked") // if i could do this without casting i would
				var ids = (ArrayList<String>) i.get("ids");
				var potency = MathHelper.clamp(Float.parseFloat(i.get("potency").toString()), 0f, 1f);
				var lightLevel = MathHelper.clamp(Integer.parseInt(i.get("light level").toString()), 0, 15);
				for (var id : ids) {
					server.GrueWardSettings.put(new Identifier(id), new Server.GrueWardSetting(potency, lightLevel));
				}
			}
		}
		{
			@SuppressWarnings("unchecked") // if i could do this without casting i would
			ArrayList<HashMap<String, Object>> mobSettings = (ArrayList<HashMap<String, Object>>) rootMap
					.get("mob settings");

			for (HashMap<String, Object> i : (ArrayList<HashMap<String, Object>>) mobSettings) {
				@SuppressWarnings("unchecked") // if i could do this without casting i would
				var ids = (ArrayList<String>) i.get("ids");
				var damageMultiplier = Float.parseFloat(i.get("damage multiplier").toString());
				var fearsDarkness = Boolean.parseBoolean(i.get("fears darkness").toString());
				var fearWeight = Float.parseFloat(i.get("fear weight").toString());
				var usesHardcoreDifficulty = Boolean.parseBoolean(i.get("uses hardcore difficulty").toString());

				for (var id : ids) {
					server.MobSettings.put(new Identifier(id),
							new Server.MobSetting(damageMultiplier, fearsDarkness, fearWeight, usesHardcoreDifficulty));
				}
			}

			if (server.MobSettings.containsKey(new Identifier("minecraft:player"))) {
				log.error( // TODO make sure ai doesnt use contains key or anything of the like, just get
							// and is not null
						"[Pandora] Please remove the id \"minecraft:player\" from the id array, it will cause problems for the server trying to process a player entity as if its a mob and may result in slowdowns or buggy behavior");
			}
		}

		Server.SERVER = server;
		log.info("[Pandora] Server config loaded.");
	}

	public static void loadDebugConfigFromInputStream(InputStream is) throws IOException {
		log.debug("[Pandora] Loading debug config. . .");
		Properties props = new Properties();
		props.load(is);

		Debug debug = new Debug();

		debug.flameLightSourceDecayRate = Float.parseFloat(props.get("flameLightSourceDecayRate").toString());
		debug.forceGruesAlwaysAttack = Boolean.parseBoolean(props.get("forceGruesAlwaysAttack").toString());

		debug.enableSavingConfigFile = Boolean.parseBoolean(props.getProperty("enableSavingConfigFile").toString());
		debug.useDebugMenuForModMenu = Boolean.parseBoolean(props.get("useDebugMenuForModMenu").toString());
		debug.deleteConfigsOnLoad = Boolean.parseBoolean(props.get("deleteConfigsOnLoad").toString());

		Debug.DEBUG = debug;
		log.debug("[Pandora] Debug config loaded.");
	}

	public static void loadClientConfigFromInputStream(InputStream is) {
		log.info("[Pandora] Loading client config. . .");

		Yaml yaml = new Yaml();

		LinkedHashMap<String, Object> rootMap = yaml.load(is);

		Client client = new Client();

		client.DimensionSettings = new LinkedHashMap<>();

		client.ResetGamma = Boolean.parseBoolean(rootMap.getOrDefault("reset gamma", client.ResetGamma).toString());
		client.GammaValue = Float.parseFloat(rootMap.getOrDefault("gamma value", client.GammaValue).toString());

		// dimension settings
		{
			@SuppressWarnings("unchecked") // if i could do this without casting i would
			ArrayList<HashMap<String, Object>> dimensionSettings = (ArrayList<HashMap<String, Object>>) rootMap
					.get("dimension settings");

			for (HashMap<String, ?> dim : dimensionSettings) {

				@SuppressWarnings("unchecked") // if i could do this without casting i would
				ArrayList<String> identifiers = (ArrayList<String>) dim.get("ids");

				if (identifiers == null) {
					throw new IllegalArgumentException(
							"Element does not contain required key \"ids\", please add it to the element. (" + dim
									+ ")");
				}

				for (var cid : identifiers) {

					// TODO replace clamps with errors?

					var id = new Identifier(cid);
					var fogFactor = MathHelper.clamp(Float.parseFloat(dim.get("fog factor").toString()), 0f, 1f);
//							var infested = Boolean.parseBoolean(dim.get("infested").toString());
					var isDark = Boolean.parseBoolean(dim.get("is dark").toString());
//							var ignoreSkyLight = Boolean.parseBoolean(dim.get("ignore sky light").toString());
//							var lockMoonPhase = Boolean.parseBoolean(dim.get("lock moon phase").toString());
//							var targetMoonPhase = MathHelper.clamp(Integer.parseInt(dim.get("target moon phase").toString()), 0,
//									7);
//							var minimumSafeLightLevel = MathHelper
//									.clamp(Integer.parseInt(dim.get("minimum safe light level").toString()), 0, 15);
//							var minimumFadeLightLevel = MathHelper
//									.clamp(Integer.parseInt(dim.get("minimum fade light level").toString()), 0, 15);
//							var gruesAttackInWater = Boolean.parseBoolean(dim.get("grues attack in water").toString());

					client.DimensionSettings.put(id, new Client.DimensionSetting(fogFactor, isDark));
				}
			}
		}

		Client.CLIENT = client;
		log.info("[Pandora] Client config loaded.");
	}

	public static void saveServerConfig() {
//		try {
		var config = getConfigFile("pandora.server.yaml");

//			if (!config.exists()) {
//				log.debug("Unpacking server config. . .");
//				unpackageFile("pandora.server.yaml");
//				log.debug("Server config unpacked.");
//			}

//			@SuppressWarnings("resource")
//			InputStream fis = new FileInputStream(config);

		if (!config.canWrite()) {
			log.warn("Cannot write to server config file!");
//				fis.close();
//				fis = PandoraConfig.class.getResourceAsStream("/assets/pandora/pandora.server.yaml");
//				log.warn("Internal defaults loaded.");
		}

		saveServerConfigToFile(config);
//			fis.close();
//		} catch (IOException e) {
//			log.error("[Pandora] There was an IOException while loading the server yaml file.");
//			log.error("{}", e);
//			throw new RuntimeException("Unable to continue.");
//		}
	}

	public static void saveClientConfig() {
//		try {
		var config = getConfigFile("pandora.client.yaml");

//			if (!config.exists()) {
//				log.debug("Unpacking client config. . .");
//				unpackageFile("pandora.client.yaml");
//				log.debug("Client config unpacked.");
//			}

//			@SuppressWarnings("resource")
//			InputStream fis = new FileInputStream(config);

		if (!config.canWrite()) {
			log.warn("Cannot write to client config file!");
//				fis.close();
//				fis = PandoraConfig.class.getResourceAsStream("/assets/pandora/pandora.client.yaml");
//				log.warn("Internal defaults loaded.");
		}

		saveClientConfigToFile(config);

//		} catch (IOException e) {
//			log.error("[Pandora] There was an IOException while loading the client yaml file.");
//			log.error("{}", e);
//			throw new RuntimeException("Unable to continue.");
//		}

	}

	public static void saveDebugConfig() {
		var config = getConfigFile("pandora.debug.properties");

		if (!config.canWrite()) {
			log.warn("Cannot write to debug config file!");
		}

		saveDebugConfigToFile(config);
	}

	public static void saveServerConfigToFile(File file) {
		log.info("[Pandora] Saving server config. . .");

		log.info("[Pandora] Server config saved.");
	}

	public static void saveClientConfigToFile(File file) {
		log.info("[Pandora] Saving client config. . .");

		log.info("[Pandora] Client config saved.");
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

	public static void saveConfigs() { // TODO finish save configs
		log.info("[Pandora] Saving config files. . .");

		saveClientConfigToFile(getConfigFile("pandora.client.yaml"));

		saveServerConfigToFile(getConfigFile("pandora.server.yaml"));

		if (Debug.DEBUG.enableSavingConfigFile) {
			saveDebugConfigToFile(getConfigFile("pandora.debug.properties"));
		}

		log.info("[Pandora] Config files saved.");
	}

	public static void unpackageFile(String path) {

		try (FileWriter writer = new FileWriter(getConfigFile(path))) {
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(PandoraConfig.class.getResourceAsStream("/assets/pandora/" + path)))) {

				for (int t; (t = reader.read()) != -1;) {
					writer.write(t);
				}

//				writer.close();
//				reader.close();
			}
		} catch (IOException e) {
			log.error("[Pandora] There was an IOException while unpackaging a file.");
			log.error("{}", e);
			throw new RuntimeException("Unable to continue.");
		}

	}
}