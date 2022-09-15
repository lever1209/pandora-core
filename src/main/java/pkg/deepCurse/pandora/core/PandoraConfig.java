package pkg.deepCurse.pandora.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.ToIntFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;

public class PandoraConfig {

	@SuppressWarnings("unused")
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
	public static boolean isDarknessEnabled;
	ArrayList<Identifier> effectiveDimensions;
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
	public static double resetGammaValue = config
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

}
