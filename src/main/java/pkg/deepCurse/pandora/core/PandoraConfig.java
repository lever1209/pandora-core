package pkg.deepCurse.pandora.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.particle.BlockLeakParticle;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import pkg.deepCurse.pandora.core.PandoraConfig.General.BlockLightLevelSettings;
import pkg.deepCurse.pandora.core.mixins.shared.StateMixin;
import pkg.deepCurse.pandora.core.util.exceptions.PandoraConfigParseException;
import pkg.deepCurse.pandora.core.util.interfaces.ConditionalToIntFunction;
import pkg.deepCurse.pandora.core.util.interfaces.PropertyMapPrinterAccess;
import pkg.deepCurse.pandora.core.util.tools.CalculateFogFunction;

public class PandoraConfig {

	public static class General {
		public static class Enabled {
			public static boolean EnablePandora = true;
			public static boolean EnableCustomFog = true;
			public static boolean EnableCustomAI = true;
			public static boolean EnableCustomLightmap = true;
			public static boolean EnableGrueWards = true;
		}

		public static boolean IgnoreSkyLight = false;
		public static boolean IgnoreMoonPhase = false;

		public static int MinimumSafeLightLevel = 5;
		public static int MinimumFadeLightLevel = 3;

		public static boolean HardcoreAffectsOtherMobs = true;

		public static boolean ResetGamma = true;
		public static double GammaValue = 1.0f;

		public static boolean GruesAttackInWater = false;
		public static boolean GruesEatItems = true;

		public static HashMap<Identifier, BlockLightLevelSettings> BlockLightLevelSettings = null;

		public static class BlockLightLevelSettings {

			public BlockLightLevelSettings(ConditionalToIntFunction<BlockState> level) {
				this.LightLevel = level;
			}

			public ConditionalToIntFunction<BlockState> LightLevel;
		}

		public static HashMap<Identifier, DimensionSettings> DimensionSettings = null;

		public static class DimensionSettings {

			public DimensionSettings(CalculateFogFunction fog, boolean infested) {
				this.FogLevel = fog;
				this.Infested = infested;
			}

			public CalculateFogFunction FogLevel;
			public boolean Infested;
		}

		public static HashMap<Identifier, GrueWardSettings> GrueWards = null;

		public static class GrueWardSettings {
			public double Potency;

			public GrueWardSettings(double potency) {
				this.Potency = potency;
			}
		}

		public static HashMap<Identifier, MobSettings> MobSettings = null;

		public static class MobSettings {
			public double DamageMultiplier;
			public boolean FearDarkness; // TODO look into making this a float for the ai weight?

			public MobSettings(double damageMultiplier, boolean fearsDarkness) {
				this.DamageMultiplier = damageMultiplier;
				this.FearDarkness = fearsDarkness;
			}
		}
	}

	public class Debug {
		// TODO add support for configs that do not get added, or at least shuffled to
		// the bottom
		public static double FlameLightSourceDecayRate = 1.0f;
		public static boolean ForceGruesAlwaysAttack = false;
		public static int GrueAttackInterval = 1; // TODO finish this
	}

	private static Logger log = LoggerFactory.getLogger(PandoraConfig.class);

	public static File getConfigFile() {
		return new File(FabricLoader.getInstance().getConfigDir().toFile(), "pandora.yaml");
	}

	private static LoadSettings settings = LoadSettings.builder().setLabel("Config Reader Settings").build();

	public static void loadConfig() { // TODO write yaml parser to get spans and line numbers, and to add more user
										// friendly errors

		log.info("start");

		General.DimensionSettings = new HashMap<>();
		General.MobSettings = new HashMap<>();
		General.BlockLightLevelSettings = new HashMap<>();
		General.GrueWards = new HashMap<>();

		Load load = new Load(settings);
		HashMap<?, ?> yamlHashMap;

		try {
			yamlHashMap = (HashMap<?, ?>) load.loadFromInputStream(new FileInputStream(getConfigFile()));
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
				yamlHashMap = (HashMap<?, ?>) load
						.loadFromInputStream(PandoraConfig.class.getResourceAsStream("/assets/pandora/pandora.yaml"));
			}
		}

		HashMap<?, ?> general = (HashMap<?, ?>) yamlHashMap.get("general");
		HashMap<?, ?> enabled = (HashMap<?, ?>) general.get("enabled");
		ArrayList<?> blockLightSettings = (ArrayList<?>) general.get("block light settings");
		ArrayList<?> dimensionSettings = (ArrayList<?>) general.get("dimension settings");
		ArrayList<?> grueWards = (ArrayList<?>) general.get("grue wards");
		ArrayList<?> mobGroups = (ArrayList<?>) general.get("mob group settings");
		HashMap<?, ?> debug = (HashMap<?, ?>) yamlHashMap.get("debug settings");

		General.Enabled.EnablePandora = (boolean) enabled.get("enable pandora");
		General.Enabled.EnableCustomFog = (boolean) enabled.get("enable custom fog");
		General.Enabled.EnableCustomAI = (boolean) enabled.get("enable pandora ai");
		General.Enabled.EnableCustomLightmap = (boolean) enabled.get("enable light modifications");
		General.Enabled.EnableGrueWards = (boolean) enabled.get("enable grue wards");

		General.IgnoreSkyLight = (boolean) general.get("ignore sky light");
		General.IgnoreMoonPhase = (boolean) general.get("ignore moon phase");
		General.MinimumSafeLightLevel = (int) general.get("minimum safe light level");
		General.MinimumFadeLightLevel = (int) general.get("minimum fade light level");
		General.HardcoreAffectsOtherMobs = (boolean) general.get("hardcore affects other mobs");
		General.ResetGamma = (boolean) general.get("reset gamma");
		General.GammaValue = (double) general.get("gamma value");
		General.GruesAttackInWater = (boolean) general.get("grues attack in water");
		General.GruesEatItems = (boolean) general.get("grues eat items");

		General.BlockLightLevelSettings = new HashMap<>();
		for (HashMap<String, ?> i : (ArrayList<HashMap<String, ?>>) blockLightSettings) {

			if (!i.containsKey("light level")) {
				throw new PandoraConfigParseException(
						"Value for required key \"light level\" not found or invalid. type required: int, example key value pair: \"light level: 7\"");
			}

			Identifier id = new Identifier((String) i.get("id"));
			int lightLevel = (int) i.get("light level");
			ConditionalToIntFunction<BlockState> conditional_to_int_function;

			LinkedHashMap<String, ?> configPropertyHashMap = (LinkedHashMap<String, ?>) i.get("properties");
			conditional_to_int_function = (argBlockstate, oldValue) -> {
				if (!argBlockstate.getEntries().isEmpty() && configPropertyHashMap != null) {
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

			General.BlockLightLevelSettings.put(id, new BlockLightLevelSettings(conditional_to_int_function));
		}

		for (HashMap<String, ?> dim : (ArrayList<HashMap<String, ?>>) dimensionSettings) {

			var fogFactor = (double) dim.get("fog factor");
			var identifiers = (ArrayList<String>) dim.get("ids");
			var infested = (boolean) dim.get("infested");

//			if (fogFactor == null) {
//				throw new PandoraConfigParseException(
//						"Element does not contain required key \"fog factor\", please add it to the element. (" + dim
//								+ ")");
//			}
			if (identifiers == null) {
				throw new PandoraConfigParseException(
						"Element does not contain required key \"ids\", please add it to the element. (" + dim + ")");
			}
//			if (infested == null) {
//				throw new PandoraConfigParseException(
//						"Element does not contain required key \"infested\", please add it to the element. (" + dim
//								+ ")");
//			}
			for (var id : identifiers) {
				General.DimensionSettings.put(new Identifier(id), new General.DimensionSettings(
						(owner, color, sun_angle, oldValue, world, biome_access, sun_height, i, j, k) -> {
							final double MIN = 0;
							Vec3d result = oldValue;
							result = new Vec3d(Math.max(MIN, result.x * fogFactor), Math.max(MIN, result.y * fogFactor),
									Math.max(MIN, result.z * fogFactor));
							return result;
						}, infested));
			}
		}

//		General.DimensionSettings.put(new Identifier("minecraft:overworld"), new General.DimensionSettings(
//				(owner, color, sun_angle, oldValue, world, biome_access, sun_height, i, j, k) -> {
//					final double factor = 0.7d;
//					final double MIN = 0;
//
//					{
//						Vec3d result = oldValue;
//						result = new Vec3d(Math.max(MIN, result.x * factor), Math.max(MIN, result.y * factor),
//								Math.max(MIN, result.z * factor));
//
//						return result;
//					}
//				}, true));

		for (HashMap<String, ?> i : (ArrayList<HashMap<String, ?>>) grueWards) {
			var potency = (double) i.get("potency");
			var ids = (ArrayList<String>) i.get("ids");
			for (var id : ids) {
				General.GrueWards.put(new Identifier(id), new General.GrueWardSettings(potency));
			}
		}

		for (HashMap<String, ?> i : (ArrayList<HashMap<String, ?>>) mobGroups) {
			var ids = (ArrayList<String>) i.get("ids");
			var damageMultiplier = (double) i.get("damage multiplier");
			var fearsDarkness = (boolean) i.get("fears darkness");

			for (var id : ids) {
				// log.info("{} {} {}", id, damageMultiplier, fearsDarkness);
				General.MobSettings.put(new Identifier(id), new General.MobSettings(damageMultiplier, fearsDarkness));
			}
		}

		Debug.FlameLightSourceDecayRate = (double) debug.get("flameLightSourceDecayRate");
		Debug.ForceGruesAlwaysAttack = (boolean) debug.get("forceGruesAlwaysAttack");

//		log.info("CONFIG:\n{}\n{}\n{}\n{}\n{}\n{}\n{}\n{}\n{}\n{}\n{}\n{}\n{}\n{}\n{}\n{}\n{}\n{}\n{}\n{}",
//				General.Enabled.EnablePandora, General.Enabled.EnableCustomFog, General.Enabled.EnableCustomAI,
//				General.Enabled.EnableCustomLightmap, General.Enabled.EnableGrueWards, General.IgnoreSkyLight,
//				General.IgnoreMoonPhase, General.MinimumSafeLightLevel, General.MinimumFadeLightLevel,
//				General.HardcoreAffectsOtherMobs, General.ResetGamma, General.GammaValue, General.GruesAttackInWater,
//				General.GruesEatItems, General.BlockLightLevelSettings, General.DimensionSettings, General.GrueWards,
//				General.MobSettings, Debug.FlameLightSourceDecayRate, Debug.ForceGruesAlwaysAttack);

	}

	public static void saveConfigs() { // TODO finish save configs

	}

	public static void unpackageConfig() throws IOException {

		FileWriter writer = new FileWriter(getConfigFile());
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(PandoraConfig.class.getResourceAsStream("/assets/pandora/pandora.yaml")));

		for (int t; (t = reader.read()) != -1;) {
			writer.write(t);
		}

		writer.close();
		reader.close();
	}

	public static void deleteConfig() {
		getConfigFile().delete();
	}

	// public static boolean moblistContains(Identifier identifier) {
	// return ANIMALS.contains(identifier) || VILLAGERS.contains(identifier)
	// || HOSTILE_MOBS.contains(identifier) || BOSS_MOBS.contains(identifier)
	// || MISC_MOBS.contains(identifier);
	// }

	// HashMap<?,?> load_linked_hashmap_from_config() {

	// }

}

// public enum PandoraConfigEnum {

// animalsFearDarkness(false, "Should animals avoid staying in the dark too
// long"),
// blockLightOnly(false,
// "Should the calculation for whether you are in darkness or not only use block
// light, ignoring sky light"),
// bossMobsFearDarkness(false, "Should boss mobs avoid staying in the dark too
// long"),
// defaultGammaResetValue(1.0F,
// "The value pandora will reset your gamma to on boot, resetting your gamma can
// be disabled too"),

//
// dimensionFogFactors(0, "What fog factor should a dimension use"),
// effectiveDimensions(0, "What dimensions should be afflicted by grues"),
// lightLevelBlockPairs(0,
// "What blocks should have what light levels, supports block states (API NOTE:
// an api is available for this value)"),

// flameLightSourceDecayRate(1.0F,
// "The decay rate of torches and similar, 1.0 is 1x the decay rate, 2.0 is 2x,
// 0.5 is half the decay rate, any value can be entered"),
// gruesAttackAnimals(false, "Should grues eat animals"),
// gruesAttackBossMobs(false, "Should grues eat boss monsters"),
// gruesAttackInWater(false, "Should grues eat mobs in dark water"),
// gruesAttackPlayers(true, "Should grues eat players"),
// gruesAttackVillagers(true, "Should grues eat villagers"),
// gruesCanAttackHostileMobs(false, "Should grues attack hostile mobs"),
// gruesEatItems(true, "Should grues eat items (since items do not have health,
// the grue can eat it in one go)"),
// hardcoreAffectsOtherMobs(false,
// "Does being in hardcore mode affect other mobs, in hardcore mode grues will 1
// hit ko"),
// hostileMobsFearDarkness(false, "Should hostile mobs avoid staying in the dark
// too long"),
// ignoreMoonPhase(false,
// "Should the calculation for whether you are in darkness or not use the moon
// phases, full moon you are safe under the moonlight"),
// isEnabled(true, "Is the whole mod enabled"),
// minimumSafeLightLevel(3, "The minimum light level you need to be in to stay
// safe from grues"),
// resetGammaOnLaunch(true, "Should pandora reset your gamma value at launch"),
// villagersFearDarkness(true, "Should villagers avoid staying in the dark too
// long"),
// grueWardsEnabled(true, "Should grue wards be enabled (items you can hold to
// be mostly immune to grues)"),
// isDarknessEnabled(true, "Should the world darken significantly (client side
// only)"),

// // Debug options ahead, will not be stored in config, but will always be
// available in config

// forceGruesAlwaysAttack(false, false);

// public Object object;
// public String comment;
// public boolean canPutInConfig = true;

// PandoraConfigEnum(Object defaultValue, boolean canPutInConfig) {
// this.object = defaultValue;
// this.canPutInConfig = canPutInConfig;
// }

// PandoraConfigEnum(Object defaultValue, String comment) {
// this.object = defaultValue;
// this.comment = comment;
// }
// }

// public static ArrayList<String> blacklistedEntityType = new ArrayList<>();
// public static ArrayList<String> grueWards = new ArrayList<>();
// public static HashMap<Identifier, CalculateFogFunction> effectiveDimensions =
// new HashMap<>();
// public static HashMap<Identifier, Float> dimensionFogFactors = new
// HashMap<>();
// public static HashMap<Identifier, ToIntFunction<BlockState>>
// lightLevelBlockPairs = new HashMap<>();

// static {

// data values i have yet to figure out how to serialize

/*
 * dimensionFogFactors.put(new Identifier("minecraft:overworld"), 1.0F);
 * dimensionFogFactors.put(new Identifier("minecraft:the_nether"), 0.5F);
 * dimensionFogFactors.put(new Identifier("minecraft:the_end"), 0.0F);
 * 
 * double MIN = 0.029999999329447746D; // minimum brightness in grondags
 * darkness
 * 
 * effectiveDimensions.putIfAbsent(new Identifier("minecraft:overworld"),
 * (effects, color, f, oldValue, world, access, sunHeight, i, j, k) -> { float
 * factor = dimensionFogFactors.getOrDefault(new
 * Identifier("minecraft:overworld"), 1.0F); return new Vec3d(Math.max(MIN,
 * oldValue.x * factor), Math.max(MIN, oldValue.y * factor), Math.max(MIN,
 * oldValue.z * factor)); });
 * 
 * effectiveDimensions.putIfAbsent(new Identifier("minecraft:the_nether"),
 * (effects, color, f, oldValue, world, access, sunHeight, i, j, k) -> { float
 * factor = dimensionFogFactors.getOrDefault(new
 * Identifier("minecraft:the_nether"), 0.5F); return new Vec3d(Math.max(MIN,
 * oldValue.x * factor), Math.max(MIN, oldValue.y * factor), Math.max(MIN,
 * oldValue.z * factor)); });
 * 
 * effectiveDimensions.putIfAbsent(new Identifier("minecraft:the_end"),
 * (effects, color, f, oldValue, world, access, sunHeight, i, j, k) -> { float
 * factor = dimensionFogFactors.getOrDefault(new
 * Identifier("minecraft:the_end"), 0.0F); return new Vec3d(Math.max(MIN,
 * oldValue.x * factor), Math.max(MIN, oldValue.y * factor), Math.max(MIN,
 * oldValue.z * factor)); });
 */
// }

// public static void saveConfigs() {

// config.commentMap().clear();

// for (PandoraConfigEnum i : PandoraConfigEnum.values()) {
// if (i.canPutInConfig) {
// config.setComment(i.name(), i.comment);
// }
// }

// config.save();
// }