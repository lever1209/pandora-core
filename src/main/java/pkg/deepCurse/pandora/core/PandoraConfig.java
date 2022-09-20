package pkg.deepCurse.pandora.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.ToIntFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import pkg.deepCurse.pandora.tools.CalculateFogFunction;

public class PandoraConfig {

	private static Logger log = LoggerFactory.getLogger(PandoraConfig.class);

	public static File getConfigFile() {
		return new File(FabricLoader.getInstance().getConfigDir().toFile(),
				"pandora.toml");
	}

	public static CommentedFileConfig config = CommentedFileConfig
			.builder(getConfigFile()).autosave().preserveInsertionOrder()
			.defaultResource("/assets/pandora/pandora.toml").build();

	// integration
	public static boolean lambDynLightsIsPresent = FabricLoader.getInstance()
			.isModLoaded("lambdynlights");
	// grondags darkness
	public static boolean isEnabled;
	public static HashMap<Identifier, CalculateFogFunction> effectiveDimensions = new HashMap<>();
	public static HashMap<Identifier, Float> dimensionFogFactors = new HashMap<>();

	// grues
	public static boolean gruesOnlyAttackPlayers() {
		return !(gruesCanAttackAnimals && gruesCanAttackBossMobs && gruesCanAttackHostileMobs
				&& gruesCanAttackVillagers) && gruesCanAttackPlayers;
	}

	public static boolean gruesCanAttackPlayers;
	public static boolean gruesCanAttackAnimals;
	public static boolean gruesCanAttackVillagers;
	public static boolean gruesCanAttackHostileMobs;
	public static boolean gruesCanAttackBossMobs;

	public static boolean gruesCanEatItems;
	public static boolean gruesCanAttackInWater;
	public static boolean hardcoreAffectsOtherMobs;

	public static int grueAttackLightLevelMaximum;
	// darkness
	public static boolean villagersFearDarkness;
	public static boolean animalsFearDarkness;
	public static boolean hostileMobsFearDarkness;
	public static boolean bossMobsFearDarkness;

	public static HashMap<Identifier, ToIntFunction<BlockState>> lightLevelBlockPairs = new HashMap<>();

	static {

		lightLevelBlockPairs.put(new Identifier("minecraft:torch"), (state) -> 6);
		lightLevelBlockPairs.put(new Identifier("minecraft:wall_torch"), (state) -> 6);

		// boolean isNearLight = PandoraTools.isNearLight(world,
		// MinecraftClient.getInstance().player.getBlockPos(), 5);
		// TODO use to darken fog while not near light

		dimensionFogFactors.put(new Identifier("minecraft:overworld"), 1.0F);
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
				});
	}

	// gamma
	public static boolean resetGamma;
	public static double defaultGammaResetValue = config
			.getOrElse("darkness.defaultGammaValue", 1.0);
	// debug
	public static float torchDecayRate;
	public static boolean resetGrueAttackChance;

	public static ArrayList<String> grueWards;
	public static ArrayList<String> blacklistedEntityType;

	public static boolean ignoreMoonPhase;

	public static boolean blockLightOnly;

	public static boolean isDynamicLightingEnabled() {
		return lambDynLightsIsPresent;
	}

	public static void loadConfig() {

		config.load();

		isEnabled = config.getOrElse("general.isEnabled", true);
		// effectiveDimensions = config.getOrElse("general.effectiveDimensions",
		// List.of(new Identifier("minecraft:overworld"), new
		// Identifier("minecraft:the_nether"),
		// new Identifier("minecraft:the_end")));

	}

	public static void saveConfigs() {

		config.set("general.isEnabled", isEnabled);
		config.set("general.effectiveDimensions", effectiveDimensions);
		config.set("general.ignoreMoonPhase", ignoreMoonPhase);
		config.set("general.blockLightOnly", blockLightOnly);

		config.set("grues.canAttackPlayers", gruesCanAttackPlayers);
		config.set("grues.canAttackAnimals", gruesCanAttackAnimals);
		config.set("grues.canAttackVillagers", gruesCanAttackVillagers);
		config.set("grues.canAttackHostileMobs", gruesCanAttackHostileMobs);
		config.set("grues.canAttackBossMobs", gruesCanAttackBossMobs);

		config.set("grues.canEatItems", gruesCanEatItems);
		config.set("grues.canGetWet", gruesCanAttackInWater);
		config.set("grues.hardcoreAffectsOtherMobs", hardcoreAffectsOtherMobs);

		config.set("grues.attackLightLevelMaximum", grueAttackLightLevelMaximum);

		config.set("fear.villagersFearDarkness", villagersFearDarkness);
		config.set("fear.animalsFearDarkness", animalsFearDarkness);
		config.set("fear.hostileMobsFearDarkness", hostileMobsFearDarkness);
		config.set("fear.bossMobsFearDarkness", bossMobsFearDarkness);

		config.set("gamma.resetGammaOnLaunch", resetGamma);
		config.set("gamma.resetValue", defaultGammaResetValue);

		config.set("grue.wards", grueWards);
		config.set("grue.entityBlacklist", blacklistedEntityType);

		config.save();
	}

	public static void newConfig() {
		config = CommentedFileConfig.builder(getConfigFile()).autosave()
				.preserveInsertionOrder().build();
	}

	public static void unpackageConfig() throws IOException {
		log.info("upackaging config");

		FileWriter writer = new FileWriter(getConfigFile());
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(PandoraConfig.class
						.getResourceAsStream("/assets/pandora/pandora.toml")));

		for (int t; (t = reader.read()) != -1;) {
			writer.write(t);
		}

		writer.close();
		reader.close();
	}

	public static boolean deleteConfig() {
		try {
			getConfigFile().delete();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	// public static ArrayList<Pair<Identifier, CalculateFogFunction>>
	// registerDimensionFog() { // example implementation
	// ArrayList<Pair<Identifier, CalculateFogFunction>> arr = new ArrayList<>();

	// arr.add(Pair.of(new Identifier("modID", "dimensionID"),
	// (effects, color, f, oldValue, world, access, sunHeight, i, j, k) -> {
	// return 4F;
	// }));

	// return arr;

	// }

}
