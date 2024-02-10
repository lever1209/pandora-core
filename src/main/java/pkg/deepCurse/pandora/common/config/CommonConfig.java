package pkg.deepCurse.pandora.common.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import pkg.deepCurse.pandora.common.CommonTools;
import pkg.deepCurse.pandora.common.util.ConfigUtils;

public class CommonConfig {

	private static Logger log = LoggerFactory.getLogger(CommonConfig.class);

	// TODO remove constructors?

	public static CommonConfig COMMON;

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

		public static DifficultySettingsEnum getFromDifficulty(Difficulty difficulty) {
			switch (difficulty) {
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
		public boolean gruesBypassArmor;
		public boolean gruesBypassProtection;

		public DifficultySettings(float damageAmount, short grueTutorialCount, short grueTutorialGracePeriod,
				short grueMaxTickWait, short grueMinTickWait, boolean gruesBypassArmor, boolean gruesBypassProtection) {
			this.damageAmount = damageAmount;
			this.grueTutorialCount = grueTutorialCount;
			this.grueTutorialGracePeriod = grueTutorialGracePeriod;
			this.grueMaxTickWait = grueMaxTickWait;
			this.grueMinTickWait = grueMinTickWait;
			this.gruesBypassArmor = gruesBypassArmor;
			this.gruesBypassProtection = gruesBypassProtection;
		}

		@Override
		public String toString() {
			return String.format(
					"DifficultySetting{damageAmount=%s, grueTutorialCount=%s, grueTutorialGracePeriod=%s, grueMaxTickWait=%s, grueMinTickWait=%s, gruesBypassArmor=%s, gruesBypassProtection=%s}",
					damageAmount, grueTutorialCount, grueTutorialGracePeriod, grueMaxTickWait, grueMinTickWait,
					gruesBypassArmor, gruesBypassProtection);
		}

	}

	public static class BlockLightLevelSetting {
		public ConditionalToIntFunction<BlockState> LightLevel;

		public BlockLightLevelSetting(ConditionalToIntFunction<BlockState> level) {
			this.LightLevel = level;
		}
	}

	@FunctionalInterface
	public interface ConditionalToIntFunction<T> {
		int applyAsInt(T value, int oldValue);
	}

	public static class DimensionSetting {
		public boolean infested;
		public boolean ignoreSkyLight;
		public boolean lockMoonPhase;
		public int targetMoonPhase;
		public int minimumSafeLightLevel;
		public int minimumFadeLightLevel;
		public boolean gruesAttackInWater;
		public float fadeLightLevelAttackChance;

		public DimensionSetting(boolean infested, boolean ignoreSkyLight, boolean lockMoonPhase, int targetMoonPhase,
				int minimumSafeLightLevel, int minimumFadeLightLevel, boolean gruesAttackInWater,
				float fadeLightLevelAttackChance) {
			this.infested = infested;
			this.minimumSafeLightLevel = minimumSafeLightLevel;
			this.minimumFadeLightLevel = minimumFadeLightLevel;
			this.gruesAttackInWater = gruesAttackInWater;
			this.ignoreSkyLight = ignoreSkyLight;
			this.lockMoonPhase = lockMoonPhase;
			this.targetMoonPhase = targetMoonPhase;
			this.fadeLightLevelAttackChance = fadeLightLevelAttackChance;
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
		public float attackChance;

		public MobSetting(float damageMultiplier, boolean fearsDarkness, float fearWeight, boolean isHardcore,
				float attackChance) {
			this.damageMultiplier = damageMultiplier;
			this.fearDarkness = fearsDarkness;
			this.fearWeight = fearWeight;
			this.isHardcore = isHardcore;
			this.attackChance = attackChance;
		}

		@Override
		public String toString() {
			return String.format("MobSetting{damageMultiplier=%s, fearDarkness=%s, fearWeight=%s, isHardcore=%s}",
					damageMultiplier, fearDarkness, fearWeight, isHardcore);
		}

	}

	public static void loadCommonConfigFromInputStream(InputStream is) {
		log.info("[Pandora] Loading common config. . .");

		Yaml yaml = new Yaml();

		LinkedHashMap<String, Object> rootMap = yaml.load(is);

		CommonConfig common = new CommonConfig();

		common.GruesEatItems = Boolean
				.parseBoolean(rootMap.getOrDefault("grues eat items", common.GruesEatItems).toString());
		common.GruesEatItemsChance = Float
				.parseFloat(rootMap.getOrDefault("grues eat items chance", common.GruesEatItemsChance).toString());

		@SuppressWarnings("unchecked")
		HashMap<String, Object> playerSettings = (HashMap<String, Object>) rootMap.get("player settings");

		common.Player_DamageMultiplier = Float.parseFloat(playerSettings.get("damage multiplier").toString());
		common.Player_UsesHardcoreDifficulty = Boolean
				.parseBoolean(playerSettings.get("uses hardcore difficulty").toString());
		common.Player_CanUseGrueWards = Boolean.parseBoolean(playerSettings.get("can use grue wards").toString());

		common.DifficultySettingsMap = new LinkedHashMap<>();
		common.DimensionSettings = new LinkedHashMap<>();
		common.MobSettings = new LinkedHashMap<>();
		common.BlockLightLevelSettings = new LinkedHashMap<>();
		common.GrueWardSettings = new LinkedHashMap<>();

		// difficulty settings
		{
			@SuppressWarnings("unchecked") // if i could do this without casting i would
			LinkedHashMap<String, Object> difficultySettingsMapMap = (LinkedHashMap<String, Object>) rootMap
					.get("difficulty settings");

			for (var i : CommonConfig.DifficultySettingsEnum.values()) {
				@SuppressWarnings("unchecked")
				LinkedHashMap<String, Object> difficultySettingsMap = (LinkedHashMap<String, Object>) difficultySettingsMapMap
						.get(i.toString().toLowerCase());

				var damageAmount = Float.parseFloat(difficultySettingsMap.get("damage amount").toString());
				var grueTutorialCount = Short.parseShort(difficultySettingsMap.get("grue tutorial count").toString());
				var grueTutorialGracePeriod = Short
						.parseShort(difficultySettingsMap.get("grue tutorial grace period").toString());
				var grueMaxTickWait = Short.parseShort(difficultySettingsMap.get("grue maximum tick wait").toString());
				var grueMinTickWait = Short.parseShort(difficultySettingsMap.get("grue minimum tick wait").toString());
				var gruesBypassArmor = Boolean.parseBoolean(difficultySettingsMap.get("grues bypass armor").toString());
				var gruesBypassProtection = Boolean
						.parseBoolean(difficultySettingsMap.get("grues bypass protection").toString());

				common.DifficultySettingsMap.put(i,
						new CommonConfig.DifficultySettings(damageAmount, grueTutorialCount, grueTutorialGracePeriod,
								grueMaxTickWait, grueMinTickWait, gruesBypassArmor, gruesBypassProtection));
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

				common.BlockLightLevelSettings.put(id,
						new CommonConfig.BlockLightLevelSetting(conditional_to_int_function));
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
					var infested = Boolean.parseBoolean(dim.get("infested").toString());
					var ignoreSkyLight = Boolean.parseBoolean(dim.get("ignore sky light").toString());
					var lockMoonPhase = Boolean.parseBoolean(dim.get("lock moon phase").toString());
					var targetMoonPhase = MathHelper.clamp(Integer.parseInt(dim.get("target moon phase").toString()), 0,
							7);
					var minimumSafeLightLevel = CommonTools
							.clamp(Integer.parseInt(dim.get("minimum safe light level").toString()), 0, 15);
					var minimumFadeLightLevel = CommonTools
							.clamp(Integer.parseInt(dim.get("minimum fade light level").toString()), 0, 15);
					var fadeLightLevelAttackChance = CommonTools
							.clamp(Float.parseFloat(dim.get("fade light level attack chance").toString()), 0, 1);
					var gruesAttackInWater = Boolean.parseBoolean(dim.get("grues attack in water").toString());

					common.DimensionSettings.put(id,
							new CommonConfig.DimensionSetting(infested, ignoreSkyLight, lockMoonPhase, targetMoonPhase,
									minimumSafeLightLevel, minimumFadeLightLevel, gruesAttackInWater,
									fadeLightLevelAttackChance));
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
					common.GrueWardSettings.put(new Identifier(id),
							new CommonConfig.GrueWardSetting(potency, lightLevel));
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
				var fearWeight = Integer.parseInt(i.get("fear priority").toString());
				var usesHardcoreDifficulty = Boolean.parseBoolean(i.get("uses hardcore difficulty").toString());
				var attackChance = Float.parseFloat(i.get("attack chance").toString());

				for (var id : ids) {
					common.MobSettings.put(new Identifier(id), new CommonConfig.MobSetting(damageMultiplier,
							fearsDarkness, fearWeight, usesHardcoreDifficulty, attackChance));
				}
			}

			if (common.MobSettings.containsKey(new Identifier("minecraft:player"))) {
				log.error( // TODO make sure ai doesnt use contains key or anything of the like, just get
							// and is not null
						"[Pandora] Please remove the id \"minecraft:player\" from the id array, it will cause problems for the server trying to process a player entity as if its a mob and may result in slowdowns or buggy behavior");
			}
		}

		COMMON = common;
		log.info("[Pandora] Common config loaded.");
	}

	public static void saveCommonConfig() {
		var config = ConfigUtils.getConfigFile("pandora.common.yaml");

		if (!config.canWrite()) {
			log.warn("Cannot write to common config file!");
		}
		saveCommonConfigToFile(config);
	}

	public static void saveCommonConfigToFile(File file) {
		log.info("[Pandora] Saving common config. . .");

		log.info("[Pandora] Common config saved.");
	}

	public static void loadCommonConfig() {
		try {
			var config = ConfigUtils.getConfigFile("pandora.common.yaml");

			if (!config.exists()) {
				log.debug("Unpacking common config. . .");
				ConfigUtils.unpackageFile("pandora.common.yaml");
				log.debug("Common config unpacked.");
			}

			@SuppressWarnings("resource")
			InputStream fis = new FileInputStream(config);

			if (!config.canRead()) {
				log.warn("Cannot read common config file! Using internal defaults.");
				fis.close();
				fis = CommonConfig.class.getResourceAsStream("/assets/pandora/pandora.common.yaml");
				log.warn("Internal defaults loaded.");
			}

			loadCommonConfigFromInputStream(fis);
			fis.close();
		} catch (IOException e) {
			log.error("[Pandora] There was an IOException while loading the common yaml file.");
			log.error("{}", e);
			throw new RuntimeException("Unable to continue.");
		}
	}
}