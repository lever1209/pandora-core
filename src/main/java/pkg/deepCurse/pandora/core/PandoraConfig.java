package pkg.deepCurse.pandora.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.ToIntFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;

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
			.isModLoaded("lambdynlights"); // TODO smarter handling of dynamic torches
	// grondags darkness
	public static boolean isEnabled;
	public static List<Identifier> effectiveDimensions;

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
		effectiveDimensions = config.getOrElse("general.effectiveDimensions",
				List.of(new Identifier("minecraft:overworld"), new Identifier("minecraft:the_nether"),
						new Identifier("minecraft:the_end")));

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

		// TODO Calculate to int functions

		config.set("gamma.resetGammaOnLaunch", resetGamma);
		config.set("gamma.resetValue", defaultGammaResetValue);

		// TODO debug config for decay rate etc

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

}
