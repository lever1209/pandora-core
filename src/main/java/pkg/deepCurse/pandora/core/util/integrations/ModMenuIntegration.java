package pkg.deepCurse.pandora.core.util.integrations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.minecraft.client.gui.screen.Screen;
import pkg.deepCurse.pandora.core.util.screens.DebugScreen;

public class ModMenuIntegration implements ModMenuApi {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(ModMenuIntegration.class);

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> createNewConfigScreen(screen);
	}

	public Screen createNewConfigScreen(Screen parent) {
		return new DebugScreen(parent);
	}
	
//	public Screen createNewConfigScreenClothConfig(Screen parent) {
//		// return new PandoraSpruceUIScreen(parent);
//		ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent)
//				.setTitle(Text.translatable("pandora.config.menu.title"))
//				.setSavingRunnable(() -> PandoraConfig.saveConfigs());
//
//		ConfigEntryBuilder entryBuilder = builder.entryBuilder();
////		ConfigEntryBuilder entryBuilder2 = builder.;
//
//		ConfigCategory general = builder.getOrCreateCategory(Text.translatable("pandora.config.menu.general"));
//		{
//			// ASAP ignore skylight seems to ignore it in favor of sight!
//			// ASAP ai and damager does not use this value!
//			general.addEntry(entryBuilder
//					.startBooleanToggle(Text.translatable("pandora.config.menu.ignore.sky.light"),
//							PandoraConfig.General.IgnoreSkyLight)
//					.setDefaultValue(false).setSaveConsumer(newValue -> PandoraConfig.General.IgnoreSkyLight = newValue)
//					.build());
//			general.addEntry(entryBuilder
//					.startBooleanToggle(Text.translatable("pandora.config.menu.ignore.moon.phase"),
//							PandoraConfig.General.IgnoreMoonPhase)
//					.setDefaultValue(false)
//					.setSaveConsumer(newValue -> PandoraConfig.General.IgnoreMoonPhase = newValue).build());
//
//			general.addEntry(entryBuilder
//					.startIntField(Text.translatable("pandora.config.menu.minimum.safe.level"),
//							PandoraConfig.General.MinimumSafeLightLevel)
//					.setDefaultValue(5).setMin(0).setMax(15) // TODO do the config file too
//					.setSaveConsumer((newVal) -> PandoraConfig.General.MinimumSafeLightLevel = newVal).build());
//			general.addEntry(entryBuilder
//					.startIntField(Text.translatable("pandora.config.menu.minimum.fade.level"),
//							PandoraConfig.General.MinimumFadeLightLevel)
//					.setMin(0).setMax(15) // TODO do the config file too
//					.setTooltip(Text.translatable("pandora.config.menu.minimum.fade.level.tooltip")).setDefaultValue(3)
//					.setSaveConsumer((newVal) -> PandoraConfig.General.MinimumFadeLightLevel = newVal).build());
//
//			general.addEntry(entryBuilder
//					.startBooleanToggle(Text.translatable("pandora.config.menu.reset.gamma"),
//							PandoraConfig.General.ResetGamma)
//					.setTooltip(Text.translatable("pandora.config.menu.reset.gamma.tooltip")).setDefaultValue(true)
//					.setSaveConsumer(newValue -> PandoraConfig.General.ResetGamma = newValue).build());
//			general.addEntry(entryBuilder
//					.startDoubleField(Text.translatable("pandora.config.menu.reset.gamma.value"),
//							PandoraConfig.General.GammaValue)
//					.setMax(1).setMin(0)// TODO do the config file too
//					.setTooltip(Text.translatable("pandora.config.menu.reset.gamma.value.tooltip"))
//					.setDefaultValue(1.0d).setSaveConsumer(newValue -> PandoraConfig.General.GammaValue = newValue)
//					.build());
//
//			general.addEntry(entryBuilder
//					.startBooleanToggle(Text.translatable("pandora.config.menu.grues.attack.in.water"),
//							PandoraConfig.General.GruesAttackInWater)
//					.setDefaultValue(false)
//					.setSaveConsumer(newValue -> PandoraConfig.General.GruesAttackInWater = newValue).build());
//		}
//
//		ConfigCategory enabled = builder.getOrCreateCategory(Text.translatable("pandora.config.menu.enable"));
//		{
//			enabled.addEntry(entryBuilder
//					.startBooleanToggle(Text.translatable("pandora.config.menu.enable.pandora"),
//							PandoraConfig.Enabled.EnablePandora)
//					.setDefaultValue(true)
//					.setSaveConsumer((newVal) -> PandoraConfig.Enabled.EnablePandora = newVal).build());
//			enabled.addEntry(entryBuilder
//					.startBooleanToggle(Text.translatable("pandora.config.menu.enable.custom.fog"),
//							PandoraConfig.Enabled.EnableCustomFog)
//					.setDefaultValue(true)
//					.setSaveConsumer((newVal) -> PandoraConfig.Enabled.EnableCustomFog = newVal).build());
//			enabled.addEntry(entryBuilder
//					.startBooleanToggle(Text.translatable("pandora.config.menu.enable.custom.ai"),
//							PandoraConfig.Enabled.EnableCustomAI)
//					.setDefaultValue(true)
//					.setSaveConsumer((newVal) -> PandoraConfig.Enabled.EnableCustomAI = newVal).build());
//			enabled.addEntry(entryBuilder
//					.startBooleanToggle(Text.translatable("pandora.config.menu.enable.custom.block.light"),
//							PandoraConfig.Enabled.EnableCustomLightmap)
//					.setDefaultValue(true)
//					.setSaveConsumer((newVal) -> PandoraConfig.Enabled.EnableCustomLightmap = newVal).build());
//			enabled.addEntry(entryBuilder
//					.startBooleanToggle(Text.translatable("pandora.config.menu.enable.grue.wards"),
//							PandoraConfig.Enabled.EnablePandora)
//					.setDefaultValue(true)
//					.setSaveConsumer((newVal) -> PandoraConfig.Enabled.EnableGrueWards = newVal).build());
//		}
//
//		ConfigCategory difficultySettings = builder
//				.getOrCreateCategory(Text.translatable("pandora.config.menu.difficulty.settings"));
//		
//		
////		difficultySettings.addEntry(
////				new NestedListListEntry<PandoraConfig.General.DimensionSettings, MultiElementListEntry<PandoraConfig.General.DimensionSettings>>(
////						Text.translatable("title"),
////						PandoraConfig.General.DimensionSettings.CONFIG, true,
////						Optional::empty, list -> {
////							PandoraConfig.saveSimpleDimensionList(list);
////							System.out.println("saving: " + list);
////						}, () -> PandoraConfig.getSimpleDimensions(),
////						Text.translatable("Reset"), true, true,
////						(element, nestedEntry) -> {
////
////							// default values in case element is null
////							// (on item creation)
////							String dimensionID = "minecraft:overworld";
////							Boolean isEnabled = true;
////							Double intensity = 1.0;
////
////							if (element != null) {
////								dimensionID = element.getId();
////								isEnabled = element.isEnabled();
////								intensity = element.getIntensity();
////							}
////
////							System.out.println("on element creation: "
////									+ element.toString());
////
////							MultiElementListEntry<DimensionConfigInfoObject> entry = new MultiElementListEntry<DimensionConfigInfoObject>(
////									new TranslatableText("Dimension"), element,
////									Lists.newArrayList(
////											entryBuilder.startStrField(
////													new TranslatableText(
////															"Dimension ID"),
////													dimensionID).build(),
////											entryBuilder.startBooleanToggle(
////													new TranslatableText(
////															"Is Enabled"),
////													isEnabled).build(),
////											entryBuilder.startDoubleField(
////													new TranslatableText(
////															"Intensity"),
////													intensity).build()),
////									true);
////							System.out.println("final entry to return: "
////									+ entry.getValue().toString());
////							return entry;
////						}));
//		
//		ConfigCategory blockLightSettings = builder
//				.getOrCreateCategory(Text.translatable("pandora.config.menu.block.light.settings"));
//
//		ConfigCategory dimensionSettings = builder
//				.getOrCreateCategory(Text.translatable("pandora.config.menu.dimension.settings"));
//
//		ConfigCategory grueWards = builder.getOrCreateCategory(Text.translatable("pandora.config.menu.grue.wards"));
//
//		ConfigCategory mobGroupSettings = builder
//				.getOrCreateCategory(Text.translatable("pandora.config.menu.mob.group.settings"));
//
//		ConfigCategory debug = builder.getOrCreateCategory(Text.translatable("pandora.config.menu.debug"));
//
//		debug.addEntry(entryBuilder
//				.startDoubleField(Text.translatable("pandora.config.menu.debug.FlameLightSourceDecayRate"),
//						PandoraConfig.Debug.FlameLightSourceDecayRate)
//				.setDefaultValue(1.0d).setMin(0) // TODO do the config file too
//				.setSaveConsumer(newValue -> PandoraConfig.Debug.FlameLightSourceDecayRate = newValue).build());
//
//		debug.addEntry(entryBuilder
//				.startBooleanToggle(Text.translatable("pandora.config.menu.debug.ForceGruesAlwaysAttack"),
//						PandoraConfig.Debug.ForceGruesAlwaysAttack)
//				.setDefaultValue(false)
//				.setSaveConsumer(newValue -> PandoraConfig.Debug.ForceGruesAlwaysAttack = newValue).build());
//
//		return builder.build();
//
//	}

}
