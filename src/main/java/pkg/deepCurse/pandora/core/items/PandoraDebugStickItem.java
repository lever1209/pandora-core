package pkg.deepCurse.pandora.core.items;

import java.util.*;

import org.slf4j.*;

import com.mojang.brigadier.exceptions.*;

import net.minecraft.block.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.network.message.*;
import net.minecraft.server.network.*;
import net.minecraft.state.property.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import pkg.deepCurse.pandora.core.util.exceptions.*;

public class PandoraDebugStickItem extends Item {

	Logger log = LoggerFactory.getLogger(PandoraDebugStickItem.class);

	public PandoraDebugStickItem(Item.Settings settings) {
		super(settings);
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return true;
	}

	@Override
	public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
		if (!world.isClient) {
			this.use(miner, state, world, pos, false, miner.getStackInHand(Hand.MAIN_HAND));
		}
		return false;
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		BlockPos blockPos;
		PlayerEntity playerEntity = context.getPlayer();
		World world = context.getWorld();
		if (!world.isClient && playerEntity != null && !this.use(playerEntity,
				world.getBlockState(blockPos = context.getBlockPos()), world, blockPos, true, context.getStack())) {
			return ActionResult.FAIL;
		}
		return ActionResult.success(world.isClient);
	}

	private boolean use(PlayerEntity player, BlockState state, WorldAccess world, BlockPos pos, boolean update,
			ItemStack stack) {
		if (!player.hasPermissionLevel(2)) {
			return false;
		}

		NbtCompound nbtState = NbtHelper.fromBlockState(state);
		
		log.info("{}, {}", nbtState, ((NbtCompound) nbtState.get("Properties")));
		
		try {
			NbtCompound compound = NbtHelper.fromNbtProviderString(nbtState.asString());
			log.info("Compound: {}", compound);
			
		} catch (CommandSyntaxException e) {
			throw new PandoraConfigParseException("Failed to parse config.", e);
		}
		Collection<Property<?>> props = state.getProperties();
		log.info("props:\n{}", props);
		
		for (Property<?> prop : props) {
			log.info("{}",prop.getName());
		}
		
		
		return true;
	}

	// private static <T extends Comparable<T>> BlockState cycle(BlockState state, Property<T> property, boolean inverse) {
	// 	return (BlockState) state.with(property,
	// 			PandoraDebugStickItem.cycle(property.getValues(), state.get(property), inverse));
	// }

	// private static <T> T cycle(Iterable<T> elements, @Nullable T current, boolean inverse) {
	// 	return inverse ? Util.previous(elements, current) : Util.next(elements, current);
	// }

	private void deserialize(BlockState state) {

		log.info("Whole state: {}", state);
		for (Property<?> i : state.getProperties()) {
			log.info("Properties iteration:\n\tName: {}\n\tValue: {}", i.getName(), state.get(i));
		}
		

		

	}

	private static void sendMessage(PlayerEntity player, Text message) {
		((ServerPlayerEntity) player).sendMessage(message, MessageType.CHAT);
	}

	private static <T extends Comparable<T>> String getValueString(BlockState state, Property<T> property) {
		return property.name(state.get(property));
	}
}
