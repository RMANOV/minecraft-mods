package com.erik.medievalconquest.event;

import com.erik.medievalconquest.MedievalConquestMod;
import com.erik.medievalconquest.item.BrazierItem;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
 * 1. Logs CANNOT be broken by hand — must use the Brazier tool (right-click)
 * 2. Leaves drop extra sticks when broken
 *
 * The mixin LogBreakSpeedMixin prevents mining progress on logs.
 * This handler is a safety net that also cancels the break event.
 */
public class TreeMechanicsHandler {

	public static void register() {
		// Safety net: cancel log breaking even if mixin fails
		PlayerBlockBreakEvents.BEFORE.register(TreeMechanicsHandler::onBlockBreak);

		// Leaves drop extra sticks
		PlayerBlockBreakEvents.AFTER.register(TreeMechanicsHandler::onBlockBroken);

		MedievalConquestMod.LOGGER.info("Tree mechanics registered!");
	}

	private static boolean onBlockBreak(Level level, Player player,
			BlockPos pos, BlockState state, BlockEntity blockEntity) {
		// Block log breaking unless player is in creative mode
		if (state.is(BlockTags.LOGS) && !player.isCreative()) {
			if (!level.isClientSide()) {
				player.displayClientMessage(
						Component.literal("Use a Brazier (right-click) to harvest wood!"),
						true);
			}
			return false; // Cancel the break
		}
		return true; // Allow all other blocks
	}

	private static void onBlockBroken(Level level, Player player,
			BlockPos pos, BlockState state, BlockEntity blockEntity) {
		// Leaves drop 1-3 extra sticks
		if (state.is(BlockTags.LEAVES) && !level.isClientSide()) {
			int stickCount = 1 + level.getRandom().nextInt(3); // 1-3 sticks
			Block.popResource(level, pos,
					new ItemStack(Items.STICK, stickCount));
		}
	}
}
