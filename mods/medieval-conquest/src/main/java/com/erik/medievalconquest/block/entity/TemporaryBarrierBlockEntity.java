package com.erik.medievalconquest.block.entity;

import com.erik.medievalconquest.registry.ModAnomalyBlocks;
import com.erik.medievalconquest.policy.ExpiryPolicy;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Persisted wall-clock lifetime; only the current entity may expire its block. */
public final class TemporaryBarrierBlockEntity extends BlockEntity {
    private boolean initializationAttempted;
    private boolean initialized;
    private long placed;
    private long deadline;
    private long maxSeen;
    public TemporaryBarrierBlockEntity(BlockPos pos, BlockState state) {
        super(ModAnomalyBlocks.TEMPORARY_BARRIER_ENTITY, pos, state);
    }
    public boolean initializeAt(long placedAtMillis) {
        if (initializationAttempted) return false;
        long exactDeadline = ExpiryPolicy.deadlineMillis(placedAtMillis, false);
        initializationAttempted = true;
        initialized = true;
        placed = placedAtMillis;
        deadline = exactDeadline;
        maxSeen = placedAtMillis;
        setChanged();
        return true;
    }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("version", 1);
        output.putBoolean("initialized", initialized);
        output.putLong("placed", placed);
        output.putLong("deadline", deadline);
        output.putLong("maxSeen", maxSeen);
    }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        initializationAttempted = true;
        initialized = false;
        placed = input.getLong("placed").orElse(-1L);
        deadline = input.getLong("deadline").orElse(-1L);
        maxSeen = input.getLong("maxSeen").orElse(-1L);
        if (input.getInt("version").orElse(-1) != 1
                || !input.read("initialized", Codec.BOOL).orElse(false)) return;
        initialized = validLifetime();
    }
    private boolean validLifetime() {
        if (placed < 0L || deadline < 0L || maxSeen < placed) return false;
        try {
            return deadline == ExpiryPolicy.deadlineMillis(placed, false);
        } catch (IllegalArgumentException invalid) {
            return false;
        }
    }
    public static void serverTick(Level level, BlockPos pos, BlockState state,
            TemporaryBarrierBlockEntity entity) {
        serverTickAt(level, pos, state, entity, System.currentTimeMillis());
    }
    public static void serverTickAt(Level level, BlockPos pos, BlockState state,
            TemporaryBarrierBlockEntity entity, long nowMillis) {
        if (level.isClientSide()
                || !level.getBlockState(pos).is(ModAnomalyBlocks.TEMPORARY_BARRIER)
                || level.getBlockEntity(pos) != entity) return;
        if (!entity.initialized || !entity.validLifetime() || nowMillis < 0L) {
            level.removeBlock(pos, false);
            return;
        }
        if (nowMillis > entity.maxSeen) {
            entity.maxSeen = nowMillis;
            entity.setChanged();
        }
        if (ExpiryPolicy.expired(entity.deadline, false, nowMillis, entity.maxSeen)) {
            level.removeBlock(pos, false);
        }
    }
}
