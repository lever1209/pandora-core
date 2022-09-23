package pkg.deepCurse.pandora.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import pkg.deepCurse.pandora.core.items.RevenantFlintAndSteelItem;

public class PandoraRegistry {

	private static boolean hasInitialized = false;
	private static Logger log = LoggerFactory.getLogger(PandoraRegistry.class);
	
	public static final Item REVENANT_FLINT_AND_STEEL = new RevenantFlintAndSteelItem(
			new FabricItemSettings().fireproof().maxCount(1).rarity(Rarity.RARE).group(ItemGroup.TOOLS));

	// public static final Block SOUL_FIRE = new SoulFireBlock(FabricBlockSettings.copy(Blocks.SOUL_FIRE));

	public static void init() {
		
		if (hasInitialized) {
			log.info("[Pandora] Items already registered. Aborting.");
			return;
		}
		
		Registry.register(Registry.ITEM, new Identifier("pandora", "revenant_flint_and_steel"),
				REVENANT_FLINT_AND_STEEL);
		// Registry.register(Registry.BLOCK, new Identifier("pandora", "soul_fire"), SOUL_FIRE);
		
		hasInitialized = true;
		
	}

}
