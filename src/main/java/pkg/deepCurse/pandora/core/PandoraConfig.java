package pkg.deepCurse.pandora.core;

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.slf4j.*;
import org.snakeyaml.engine.v2.api.*;

import net.fabricmc.loader.api.*;
import net.minecraft.block.*;
import net.minecraft.nbt.*;
import net.minecraft.state.property.*;
import net.minecraft.util.*;
import net.minecraft.util.registry.*;
import pkg.deepCurse.pandora.core.util.exceptions.*;
import pkg.deepCurse.pandora.core.util.tools.*;

public class PandoraConfig {

	private static Logger log = LoggerFactory.getLogger(PandoraConfig.class);

	public static File getConfigFile() {
		return new File(FabricLoader.getInstance().getConfigDir().toFile(),
				"pandora.yaml");
	}

	public static boolean IGNORE_SKY_LIGHT = false;
	public static boolean IGNORE_MOON_PHASE = false;
	public static int MINIMUM_SAFE_LIGHT_LEVEL = 5;
	public static int MINIMUM_FADE_LIGHT_LEVEL = 3;
	public static boolean HARDCORE_AFFECTS_OTHER_MOBS = false;
	public static boolean RESET_GAMMA = true;
	public static double GAMMA_VALUE = 1.0F;
	public static boolean GRUES_ATTACK_IN_WATER = false;
	public static boolean GRUES_EAT_ITEMS = true;

	public static boolean ENABLE_PANDORA = true;
	public static boolean ENABLE_CUSTOM_FOG = true;
	public static boolean ENABLE_PANDORA_AI = true;
	public static boolean ENABLE_LIGHT_MODIFICATION = true;
	public static boolean ENABLE_GRUE_WARDS = true;

	public static HashMap<Identifier, ToIntFunction<BlockState>> BLOCK_LIGHT_LEVEL_FUNCTIONS = new HashMap<>();
	public static HashMap<Identifier, CalculateFogFunction> DIMENSION_SETTINGS = new HashMap<>();

	public static ArrayList<Pair<ArrayList<Identifier>, Double>> GRUE_WARDS = new ArrayList<>();

	public static ArrayList<Identifier> ANIMALS = null;
	public static ArrayList<Identifier> BOSS_MOBS = null;
	public static ArrayList<Identifier> VILLAGERS = null;
	public static ArrayList<Identifier> HOSTILE_MOBS = null;
	public static ArrayList<Identifier> MISC_MOBS = null;

	public static boolean GRUES_ATTACK_ANIMALS = false;
	public static boolean GRUES_ATTACK_BOSS_MOBS = false;
	public static boolean GRUES_ATTACK_VILLAGERS = true;
	public static boolean GRUES_ATTACK_HOSTILE_MOBS = false;
	public static boolean GRUES_ATTACK_MISC_MOBS = true;
	public static boolean GRUES_ATTACK_PLAYERS = true; // players dont have ids or ai, so they are present only here

	public static boolean ANIMALS_FEAR_DARKNESS = false;
	public static boolean BOSS_MOBS_FEAR_DARKNESS = false;
	public static boolean VILLAGERS_FEAR_DARKNESS = true;
	public static boolean HOSTILE_MOBS_FEAR_DARKNESS = false;
	public static boolean MISC_MOBS_FEAR_DARKNESS = true;

	// TODO add support for configs that do not get added, or at least shuffled to the bottom
	public static double FLAME_LIGHT_SOURCE_DECAY_RATE = 1.0F;
	public static boolean FORCE_GRUES_ALWAYS_ATTACK = false;

	private static LoadSettings settings = LoadSettings.builder().setLabel("Config Reader Settings").build();

	public static void loadConfig() { // TODO test every cast

		log.info("start");

		GRUE_WARDS.clear();

		DIMENSION_SETTINGS.clear();
		BLOCK_LIGHT_LEVEL_FUNCTIONS.clear();

		Load load = new Load(settings);
		LinkedHashMap<?, ?> yamlLinkedHashMap = null;

		try {

			yamlLinkedHashMap = (LinkedHashMap<?, ?>) load
					.loadFromInputStream(new FileInputStream(getConfigFile()));

		} catch (FileNotFoundException e) {
			try {
				log.info("[Pandora] Config not found, extracting sample config. . .");
				unpackageConfig();
				log.info("[Pandora] Example config extracted.");

				loadConfig();
				return;

			} catch (IOException e2) {
				log.error("[Pandora] Failed to extract example config.");
				e2.printStackTrace();
				log.error(
						"[Pandora] using internal defaults for now, please look into this issue using the above stack trace.");
				yamlLinkedHashMap = (LinkedHashMap<?, ?>) load.loadFromInputStream(PandoraConfig.class
						.getResourceAsStream("/assets/pandora/pandora.yaml"));
			}
		}

		LinkedHashMap<?, ?> general;
		try {
			general = (LinkedHashMap<?, ?>) yamlLinkedHashMap.get("general");
		} catch (ClassCastException e) {
			log.error("[Pandora Help] Incorrect data type for general in config file.");
			throw new PandoraConfigParseException("Incorrect data type for general in config file", e);
		}

		LinkedHashMap<?, ?> enabled;
		try {
			enabled = (LinkedHashMap<?, ?>) general.get("enabled");
		} catch (ClassCastException e) {
			log.error("[Pandora Help] Incorrect data type for enabled in config file.");
			throw new PandoraConfigParseException("Incorrect data type for enabled in config file", e);
		}

		ArrayList<?> blockLightSettings;
		try {
			blockLightSettings = (ArrayList<?>) general.get("block light settings");
		} catch (ClassCastException e) {
			log.error("[Pandora Help] Incorrect data type for block light settings in config file.");
			throw new PandoraConfigParseException("Incorrect data type for block light settings in config file", e);
		}

		ArrayList<?> dimensionSettings;
		try {
			dimensionSettings = (ArrayList<?>) general.get("dimension settings");
		} catch (ClassCastException e) {
			log.error("[Pandora Help] Incorrect data type for dimension settings in config file.");
			throw new PandoraConfigParseException("Incorrect data type for dimension settings in config file", e);
		}

		ArrayList<?> grueWards;
		try {
			grueWards = (ArrayList<?>) general.get("grue wards");
		} catch (ClassCastException e) {
			log.error("[Pandora Help] Incorrect data type for grue wards in config file.");
			throw new PandoraConfigParseException("Incorrect data type for grue wards in config file", e);
		}

		LinkedHashMap<?, ?> mobGroup;
		try {
			mobGroup = (LinkedHashMap<?, ?>) general.get("mob group settings");
		} catch (ClassCastException e) {
			log.error("[Pandora Help] Incorrect data type for mob group in config file.");
			throw new PandoraConfigParseException("Incorrect data type for mob group in config file", e);
		}

		LinkedHashMap<?, ?> debug;
		try {
			debug = (LinkedHashMap<?, ?>) yamlLinkedHashMap.get("debug settings");
		} catch (ClassCastException e) {
			log.error("[Pandora Help] Incorrect data type for debug in config file.");
			throw new PandoraConfigParseException("Incorrect data type for debug in config file", e);
		}

		IGNORE_SKY_LIGHT = (boolean) general.get("ignore sky light");
		IGNORE_MOON_PHASE = (boolean) general.get("ignore moon phase");

		MINIMUM_SAFE_LIGHT_LEVEL = (int) general.get("minimum safe light level");
		MINIMUM_FADE_LIGHT_LEVEL = (int) general.get("minimum fade light level");

		HARDCORE_AFFECTS_OTHER_MOBS = (boolean) general.get("hardcore affects other mobs");

		RESET_GAMMA = (boolean) general.get("reset gamma");
		GAMMA_VALUE = (double) general.get("gamma value");

		GRUES_ATTACK_IN_WATER = (boolean) general.get("grues attack in water");
		GRUES_EAT_ITEMS = (boolean) general.get("grues eat items");

		ENABLE_PANDORA = (boolean) enabled.get("enable pandora");
		ENABLE_CUSTOM_FOG = (boolean) enabled.get("enable custom fog");
		ENABLE_PANDORA_AI = (boolean) enabled.get("enable pandora ai");

		for (LinkedHashMap<?, ?> i : (ArrayList<LinkedHashMap<?, ?>>) blockLightSettings) {

			final Identifier id;
			int lightLevel;
			ToIntFunction<BlockState> tif;

			if (!i.containsKey("light level")) {
				throw new PandoraConfigParseException(
						"Value for required key \"light level\" not found or invalid. type required: int, example key value pair: \"light level: 7\"");
			}
			lightLevel = (int) i.get("light level");

			if (i.containsKey("id") && !i.containsKey("state")) {
				id = new Identifier((String) i.get("id"));

				tif = (state) -> {
					return lightLevel;
				};

				if (BLOCK_LIGHT_LEVEL_FUNCTIONS.containsKey(id)) {
					throw new PandoraConfigParseException("block light settings already contains key " + id);
				}

				BLOCK_LIGHT_LEVEL_FUNCTIONS.put(id, tif);

			} else if (i.containsKey("state") && !i.containsKey("id")) {
				final LinkedHashMap<?, ?> state = (LinkedHashMap<?, ?>) i.get("state");
				final LinkedHashMap<?, ?> properties = (LinkedHashMap<?, ?>) state.get("properties");
				id = new Identifier((String) state.get("name"));

				tif = (blockstate) -> {

					for (Property<?> prop : blockstate.getProperties()) {
						if (!prop.getName().equals(id.getPath())) {
							
						}
					}
					
					return 0;
				};

			} else if (i.containsKey("state") && i.containsKey("id")) {
				throw new PandoraConfigParseException(
						"Element contains two identifier keys, please remove either \"state\" or \"id\" from the element. ("
								+ i + ")");
			} else if (!i.containsKey("state") && !i.containsKey("id"))
				throw new PandoraConfigParseException(
						"Element contains no identifier keys, please add either \"state\" or \"id\" to the element. ("
								+ i + ")");
		}

		log.info("block level light settings: {}", BLOCK_LIGHT_LEVEL_FUNCTIONS);
		
		for (LinkedHashMap<?, ?> i : (ArrayList<LinkedHashMap<?, ?>>) dimensionSettings) {
			
			
			
		}
		
		for (LinkedHashMap<?, ?> i : (ArrayList<LinkedHashMap<?, ?>>) grueWards) {

			// String dep = (String) (i.get("depends") == null ? "minecraft" : i.get("depends")); // introduces questionable pairity for network sessions

			// if (FabricLoader.getInstance().isModLoaded(dep)) {

			ArrayList<Identifier> items = new ArrayList<>();

			for (String id : (ArrayList<String>) i.get("items"))
				items.add(new Identifier(id));
			GRUE_WARDS.add(new Pair(items, i.get("potency")));
			// }
		}

		log.info("wards:");

		for (Pair<ArrayList<Identifier>, Double> i : GRUE_WARDS) {
			log.info("id[]: {}, double: {}", i.getLeft(), i.getRight());
		}

		ANIMALS = (ArrayList<Identifier>) mobGroup.get("animals");
		BOSS_MOBS = (ArrayList<Identifier>) mobGroup.get("boss mobs");
		VILLAGERS = (ArrayList<Identifier>) mobGroup.get("villagers");
		HOSTILE_MOBS = (ArrayList<Identifier>) mobGroup.get("hostile mobs");
		MISC_MOBS = (ArrayList<Identifier>) mobGroup.get("misc");

		GRUES_ATTACK_ANIMALS = (boolean) mobGroup.get("grues attack animals");
		GRUES_ATTACK_BOSS_MOBS = (boolean) mobGroup.get("grues attack boss mobs");
		GRUES_ATTACK_VILLAGERS = (boolean) mobGroup.get("grues attack villagers");
		GRUES_ATTACK_HOSTILE_MOBS = (boolean) mobGroup.get("grues attack hostile mobs");
		GRUES_ATTACK_MISC_MOBS = (boolean) mobGroup.get("grues attack misc mobs");
		GRUES_ATTACK_PLAYERS = (boolean) mobGroup.get("grues attack players");

		ANIMALS_FEAR_DARKNESS = (boolean) mobGroup.get("animals fear darkness");
		BOSS_MOBS_FEAR_DARKNESS = (boolean) mobGroup.get("boss mobs fear darkness");
		VILLAGERS_FEAR_DARKNESS = (boolean) mobGroup.get("villagers fear darkness");
		HOSTILE_MOBS_FEAR_DARKNESS = (boolean) mobGroup.get("hostile mobs fear darkness");
		MISC_MOBS_FEAR_DARKNESS = (boolean) mobGroup.get("misc mobs fear darkness");

		FLAME_LIGHT_SOURCE_DECAY_RATE = (double) debug.get("flameLightSourceDecayRate");
		FORCE_GRUES_ALWAYS_ATTACK = (boolean) debug.get("forceGruesAlwaysAttack");

		log.info("end"); // TODO remove later

	}

	public static void saveConfigs() { // TODO finish save configs

	}

	public static void unpackageConfig() throws IOException {

		FileWriter writer = new FileWriter(getConfigFile());
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(PandoraConfig.class
						.getResourceAsStream("/assets/pandora/pandora.yaml")));

		for (int t; (t = reader.read()) != -1;) {
			writer.write(t);
		}

		writer.close();
		reader.close();
	}

	public static void deleteConfig() {
		getConfigFile().delete();
	}

	public static boolean moblistContains(Identifier identifier) {
		return ANIMALS.contains(identifier) || BOSS_MOBS.contains(identifier) || VILLAGERS.contains(identifier)
				|| HOSTILE_MOBS.contains(identifier) || MISC_MOBS.contains(identifier);
	}
}

// public enum PandoraConfigEnum {

// 	animalsFearDarkness(false, "Should animals avoid staying in the dark too long"),
// 	blockLightOnly(false,
// 			"Should the calculation for whether you are in darkness or not only use block light, ignoring sky light"),
// 	bossMobsFearDarkness(false, "Should boss mobs avoid staying in the dark too long"),
// 	defaultGammaResetValue(1.0F,
// 			"The value pandora will reset your gamma to on boot, resetting your gamma can be disabled too"),

// 
// 	dimensionFogFactors(0, "What fog factor should a dimension use"),
// 	effectiveDimensions(0, "What dimensions should be afflicted by grues"),
// 	lightLevelBlockPairs(0,
// 			"What blocks should have what light levels, supports block states (API NOTE: an api is available for this value)"),

// 	flameLightSourceDecayRate(1.0F,
// 			"The decay rate of torches and similar, 1.0 is 1x the decay rate, 2.0 is 2x, 0.5 is half the decay rate, any value can be entered"),
// 	gruesAttackAnimals(false, "Should grues eat animals"),
// 	gruesAttackBossMobs(false, "Should grues eat boss monsters"),
// 	gruesAttackInWater(false, "Should grues eat mobs in dark water"),
// 	gruesAttackPlayers(true, "Should grues eat players"),
// 	gruesAttackVillagers(true, "Should grues eat villagers"),
// 	gruesCanAttackHostileMobs(false, "Should grues attack hostile mobs"),
// 	gruesEatItems(true, "Should grues eat items (since items do not have health, the grue can eat it in one go)"),
// 	hardcoreAffectsOtherMobs(false,
// 			"Does being in hardcore mode affect other mobs, in hardcore mode grues will 1 hit ko"),
// 	hostileMobsFearDarkness(false, "Should hostile mobs avoid staying in the dark too long"),
// 	ignoreMoonPhase(false,
// 			"Should the calculation for whether you are in darkness or not use the moon phases, full moon you are safe under the moonlight"),
// 	isEnabled(true, "Is the whole mod enabled"),
// 	minimumSafeLightLevel(3, "The minimum light level you need to be in to stay safe from grues"),
// 	resetGammaOnLaunch(true, "Should pandora reset your gamma value at launch"),
// 	villagersFearDarkness(true, "Should villagers avoid staying in the dark too long"),
// 	grueWardsEnabled(true, "Should grue wards be enabled (items you can hold to be mostly immune to grues)"),
// 	isDarknessEnabled(true, "Should the world darken significantly (client side only)"),

// 	// Debug options ahead, will not be stored in config, but will always be available in config

// 	forceGruesAlwaysAttack(false, false);

// 	public Object object;
// 	public String comment;
// 	public boolean canPutInConfig = true;

// 	PandoraConfigEnum(Object defaultValue, boolean canPutInConfig) {
// 		this.object = defaultValue;
// 		this.canPutInConfig = canPutInConfig;
// 	}

// 	PandoraConfigEnum(Object defaultValue, String comment) {
// 		this.object = defaultValue;
// 		this.comment = comment;
// 	}
// }

// public static ArrayList<String> blacklistedEntityType = new ArrayList<>();
// public static ArrayList<String> grueWards = new ArrayList<>();
// public static HashMap<Identifier, CalculateFogFunction> effectiveDimensions = new HashMap<>();
// public static HashMap<Identifier, Float> dimensionFogFactors = new HashMap<>();
// public static HashMap<Identifier, ToIntFunction<BlockState>> lightLevelBlockPairs = new HashMap<>();

// static {

// data values i have yet to figure out how to serialize

/* 		dimensionFogFactors.put(new Identifier("minecraft:overworld"), 1.0F);
		dimensionFogFactors.put(new Identifier("minecraft:the_nether"), 0.5F);
		dimensionFogFactors.put(new Identifier("minecraft:the_end"), 0.0F);

		double MIN = 0.029999999329447746D; // minimum brightness in grondags darkness

		effectiveDimensions.putIfAbsent(new Identifier("minecraft:overworld"),
				(effects, color, f, oldValue, world, access, sunHeight, i, j, k) -> {
					float factor = dimensionFogFactors.getOrDefault(new Identifier("minecraft:overworld"), 1.0F);
					return new Vec3d(Math.max(MIN, oldValue.x * factor),
							Math.max(MIN, oldValue.y * factor),
							Math.max(MIN, oldValue.z * factor));
				});

		effectiveDimensions.putIfAbsent(new Identifier("minecraft:the_nether"),
				(effects, color, f, oldValue, world, access, sunHeight, i, j, k) -> {
					float factor = dimensionFogFactors.getOrDefault(new Identifier("minecraft:the_nether"), 0.5F);
					return new Vec3d(Math.max(MIN, oldValue.x * factor),
							Math.max(MIN, oldValue.y * factor),
							Math.max(MIN, oldValue.z * factor));
				});

		effectiveDimensions.putIfAbsent(new Identifier("minecraft:the_end"),
				(effects, color, f, oldValue, world, access, sunHeight, i, j, k) -> {
					float factor = dimensionFogFactors.getOrDefault(new Identifier("minecraft:the_end"), 0.0F);
					return new Vec3d(Math.max(MIN, oldValue.x * factor),
							Math.max(MIN, oldValue.y * factor),
							Math.max(MIN, oldValue.z * factor));
				}); */
// }

// public static void saveConfigs() {

// config.commentMap().clear();

// for (PandoraConfigEnum i : PandoraConfigEnum.values()) {
// 	if (i.canPutInConfig) {
// 		config.setComment(i.name(), i.comment);
// 	}
// }

// config.save();
// }