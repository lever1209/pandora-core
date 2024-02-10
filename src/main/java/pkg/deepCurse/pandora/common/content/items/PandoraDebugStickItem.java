package pkg.deepCurse.pandora.common.content.items;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

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

		// log.info("{}, {}", nbtState, ((NbtCompound) nbtState.get("Properties")));

		try {
			NbtCompound compound = NbtHelper.fromNbtProviderString(nbtState.asString());
			log.info("Compound: {}", compound);
		} catch (CommandSyntaxException e) {
			log.info("Could not log compund, {}", e.getMessage());
		}

		Collection<Property<?>> props = state.getProperties();
		log.info("props:\n{}", props);

		for (Property<?> prop : props) {
			log.info("{}", prop.getName());
		}

		return true;
	}

	// private static <T extends Comparable<T>> BlockState cycle(BlockState state,
	// Property<T> property, boolean inverse) {
	// return (BlockState) state.with(property,
	// PandoraDebugStickItem.cycle(property.getValues(), state.get(property),
	// inverse));
	// }

	// private static <T> T cycle(Iterable<T> elements, @Nullable T current, boolean
	// inverse) {
	// return inverse ? Util.previous(elements, current) : Util.next(elements,
	// current);
	// }
//
//	private void deserialize(BlockState state) {
//
//		log.info("Whole state: {}", state);
//		for (Property<?> i : state.getProperties()) {
//			log.info("Properties iteration:\n\tName: {}\n\tValue: {}", i.getName(), state.get(i));
//		}
//
//	}
//
//	private static void sendMessage(PlayerEntity player, Text message) {
//		((ServerPlayerEntity) player).sendMessage(message, MessageType.CHAT);
//	}
//
//	private static <T extends Comparable<T>> String getValueString(BlockState state, Property<T> property) {
//		return property.name(state.get(property));
//	}
}
