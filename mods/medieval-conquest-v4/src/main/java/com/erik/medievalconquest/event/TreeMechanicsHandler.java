package com.erik.medievalconquest.event;

import com.erik.medievalconquest.MedievalConquestMod;
import com.erik.medievalconquest.registry.ModBlocks;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Modified tree mechanics:
 * 1. Logs can be broken by hand again.
 * 2. Leaves drop extra sticks when broken.
 * 3. Lilac leaves use their loot table to drop the regeneration herb.
 */
public class TreeMechanicsHandler {

	public static void register() {
		PlayerBlockBreakEvents.AFTER.register(TreeMechanicsHandler::onBlockBroken);

		MedievalConquestMod.LOGGER.info("Tree mechanics registered!");
	}

	private static void onBlockBroken(Level level, Player player,
			BlockPos pos, BlockState state, BlockEntity blockEntity) {
		if (!state.is(BlockTags.LEAVES) || level.isClientSide()) {
			return;
		}

		if (state.is(ModBlocks.LILAC_LEAVES)) {
			return;
		}

		int stickCount = 1 + level.getRandom().nextInt(3); // 1-3 sticks
		Block.popResource(level, pos,
				new ItemStack(Items.STICK, stickCount));
	}
}
