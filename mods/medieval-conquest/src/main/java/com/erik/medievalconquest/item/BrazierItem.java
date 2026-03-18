package com.erik.medievalconquest.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Brazier — a primitive tool for harvesting wood.
 * Trees cannot be broken by hand; you must right-click logs with this tool.
 * Crafted from: 2 sticks + 1 flint + 1 sand.
 */
public class BrazierItem extends Item {

	public BrazierItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);

		if (state.is(BlockTags.LOGS)) {
			if (!level.isClientSide()) {
				// Drop the log as an item
				Block.dropResources(state, level, pos);
				// Remove the log block
				level.destroyBlock(pos, false, context.getPlayer(), 512);
				// Play chopping sound
				level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
				// Damage the tool
				context.getItemInHand().hurtAndBreak(1, context.getPlayer(), context.getHand());
			}
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}
}
