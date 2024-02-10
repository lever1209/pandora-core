package pkg.deepCurse.pandora.common.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import pkg.deepCurse.pandora.common.content.items.PandoraDebugStickItem;
import pkg.deepCurse.pandora.common.content.items.RevenantFlintAndSteelItem;

public class PandoraRegistry {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(PandoraRegistry.class);

	public static final ItemGroup PANDORA_ITEM_GROUP = FabricItemGroupBuilder
			.create(new Identifier("pandora", "pandora.item.group.default"))
			.icon(() -> new ItemStack(Items.REINFORCED_DEEPSLATE)) // TODO figure out how to set this to the revenant
																	// flint, circular dependency issue
			.build();

	public static final Item REVENANT_FLINT_AND_STEEL = Registry.register(Registry.ITEM,
			new Identifier("pandora", "revenant_flint_and_steel"), new RevenantFlintAndSteelItem(
					new FabricItemSettings().fireproof().maxCount(1).rarity(Rarity.RARE).group(PANDORA_ITEM_GROUP)));

	public static final Item DEBUG_STICK = Registry.register(Registry.ITEM, new Identifier("pandora", "debug_stick"),
			new PandoraDebugStickItem(
					new Item.Settings().fireproof().group(PANDORA_ITEM_GROUP).maxCount(1).rarity(Rarity.EPIC)));

}
