package com.erik.medievalconquest.block;

import com.erik.medievalconquest.item.AlienFlowerItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Planted alien flower: grows on any solid top face and is smelled with right-click like the held one. */
public final class AlienFlowerBlock extends VegetationBlock {
    public static final MapCodec<AlienFlowerBlock> CODEC = simpleCodec(AlienFlowerBlock::new);
    private static final VoxelShape SHAPE = Block.column(10.0, 0.0, 15.0);

    public AlienFlowerBlock(Properties properties) { super(properties); }

    @Override
    protected MapCodec<AlienFlowerBlock> codec() { return CODEC; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.isFaceSturdy(level, pos, Direction.UP);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        return AlienFlowerItem.smell(level, player, asItem(), Vec3.atCenterOf(pos)) ? InteractionResult.SUCCESS
                : InteractionResult.PASS;
    }
}
