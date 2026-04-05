package com.erik.medievalconquest.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Invisible light-emitting block used for dynamic lighting.
 * Placed at the player's position when they hold a torch/lantern.
 * Has no collision, no visual model, but emits light level 14.
 */
public class LightSourceBlock extends Block {
	public static final MapCodec<LightSourceBlock> CODEC = simpleCodec(LightSourceBlock::new);

	public LightSourceBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		return true;
	}
}
