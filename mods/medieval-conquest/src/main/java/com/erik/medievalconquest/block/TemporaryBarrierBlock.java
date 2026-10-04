package com.erik.medievalconquest.block;

import com.erik.medievalconquest.block.entity.TemporaryBarrierBlockEntity;
import com.erik.medievalconquest.registry.ModAnomalyBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class TemporaryBarrierBlock extends BaseEntityBlock {
    public static final MapCodec<TemporaryBarrierBlock> CODEC = simpleCodec(TemporaryBarrierBlock::new);
    public TemporaryBarrierBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TemporaryBarrierBlockEntity(pos, state);
    }
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()
                || !state.is(ModAnomalyBlocks.TEMPORARY_BARRIER)
                || !level.getBlockState(pos).is(ModAnomalyBlocks.TEMPORARY_BARRIER)) return;
        if (level.getBlockEntity(pos) instanceof TemporaryBarrierBlockEntity entity
                && entity.getType() == ModAnomalyBlocks.TEMPORARY_BARRIER_ENTITY) {
            entity.initializeAt(System.currentTimeMillis());
        }
    }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type,
                ModAnomalyBlocks.TEMPORARY_BARRIER_ENTITY, TemporaryBarrierBlockEntity::serverTick);
    }
}
