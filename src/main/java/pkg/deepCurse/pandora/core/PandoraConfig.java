package pkg.deepCurse.pandora.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.function.ToIntFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import pkg.deepCurse.pandora.core.util.tools.CalculateFogFunction;

public class PandoraConfig {

	public enum PandoraConfigEnum {
		
		animalsFearDarkness(false),
		blockLightOnly(false),
		bossMobsFearDarkness(false),
		defaultGammaResetValue(1.0F),
		// dimensionFogFactors,
		// effectiveDimensions,
		flameLightSourceDecayRate(1.0F),
		gruesAttackAnimals(false),
		gruesAttackBossMobs(false),
		gruesAttackInWater(false),
		gruesAttackPlayers(true),
		gruesAttackVillagers(true),
		gruesCanAttackHostileMobs(false),
		gruesEatItems(true),
		hardcoreAffectsOtherMobs(false),
		hostileMobsFearDarkness(false),
		ignoreMoonPhase(false),
		isEnabled(true),
		minimumSafeLightLevel(3),
		resetGammaOnLaunch(true),
		villagersFearDarkness(true),
		isDarknessEnabled(true);
		
		private Object defaultValue;
		
		// public boolean getBoolean() {
		// 	return (boolean) defaultValue;
		// }
		
		// public String getString() {
		// 	return (String) defaultValue;
		// }
		
		// public int getInt() {
		// 	return (int) defaultValue;
		// }
		
		// public float getFloat() {
		// 	return (float) defaultValue;
		// }
		
		public Object getObject() {
			return defaultValue;
		}
		
		PandoraConfigEnum(Object defaultValue) {
			this.defaultValue = defaultValue;
		}
		
	}

	private static Logger log = LoggerFactory.getLogger(PandoraConfig.class);
	private static CommentedFileConfig config = CommentedFileConfig // may change in future, is only used for serialization for now
			.builder(getConfigFile()).autosave().preserveInsertionOrder()
			// .defaultResource("/assets/pandora/pandora.toml") // manual override for now
			.build();
	private static EnumMap<PandoraConfigEnum, Object> pandoraConfigMap = new EnumMap<>(PandoraConfigEnum.class);

	public static ArrayList<String> blacklistedEntityType = new ArrayList<>();
	public static ArrayList<String> grueWards = new ArrayList<>();
	public static HashMap<Identifier, CalculateFogFunction> effectiveDimensions = new HashMap<>();
	public static HashMap<Identifier, Float> dimensionFogFactors = new HashMap<>();
	public static HashMap<Identifier, ToIntFunction<BlockState>> lightLevelBlockPairs = new HashMap<>();

	// debug config
	public static boolean forceGruesAlwaysAttack = false;

	public static boolean getBoolean(PandoraConfigEnum enu) {
		return (boolean) pandoraConfigMap.get(enu);
	}

	public static void setBoolean(PandoraConfigEnum key, boolean b) {
		pandoraConfigMap.put(key, b);
	}

	public static String getString(PandoraConfigEnum enu) {
		return (String) pandoraConfigMap.get(enu);
	}

	public static void setString(PandoraConfigEnum key, String s) {
		pandoraConfigMap.put(key, s);
	}

	public static int getInt(PandoraConfigEnum enu) {
		return (int) pandoraConfigMap.get(enu);
	}

	public static void setInt(PandoraConfigEnum key, int i) {
		pandoraConfigMap.put(key, i);
	}

	public static float getFloat(PandoraConfigEnum enu) {
		return (float) pandoraConfigMap.get(enu);
	}

	public static void setFloat(PandoraConfigEnum key, float f) {
		pandoraConfigMap.put(key, f);
	}

	static {
		lightLevelBlockPairs.put(new Identifier("minecraft:torch"), (state) -> 6);
		lightLevelBlockPairs.put(new Identifier("minecraft:wall_torch"), (state) -> 6);

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

	public static File getConfigFile() {
		return new File(FabricLoader.getInstance().getConfigDir().toFile(),
				"pandora.toml");
	}

	public static void loadConfig() {
		config.load();

		// enabled = config.getOrElse("general.isEnabled", true);

		for (PandoraConfigEnum i : PandoraConfigEnum.values()) {
			var result = config.get(i.name());
			if (result == null)
				result = i.getObject();
			pandoraConfigMap.put(i, result);
			log.info("configEnum {}, value {}", i, result);
		}

	}
	
	public static void saveConfigs() {
		// for (PandoraConfigEnum i : PandoraConfigEnum.values()) {
		// 	var result = config.get(i.name());
		// 	if (result == null)
		// 		result = i.getObject();
		// 	pandoraConfigMap.put(i, result);
		// 	log.info("configEnum {}, value {}", i, result);
		// }
		config.save();
	}

	// public static void newConfig() {
	// 	config = CommentedFileConfig.builder(getConfigFile()).autosave()
	// 			.preserveInsertionOrder().build();
	// }

	public static void unpackageConfig() throws IOException {

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

	public static void deleteConfig() {
		getConfigFile().delete();
	}

	public static boolean gruesOnlyAttackPlayers() {
		return !(getBoolean(PandoraConfigEnum.gruesAttackAnimals) && getBoolean(PandoraConfigEnum.gruesAttackBossMobs)
				&& getBoolean(PandoraConfigEnum.hostileMobsFearDarkness)
				&& getBoolean(PandoraConfigEnum.gruesAttackVillagers))
				&& getBoolean(PandoraConfigEnum.gruesAttackPlayers);
	}

	public static boolean wardsEnabled() {
		return grueWards.size() > 0;
	}

}
