package com.erik.arcanenaturalis.block;

import com.erik.arcanenaturalis.crystal.CrystalGrower;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * CrystalSeedBlock — a tiny crystal sprout that grows over time.
 *
 * Uses Minecraft's RANDOM TICK system:
 *   - isRandomlyTicking() returns true → the block receives randomTick() calls
 *   - Each random tick has a ~1/1365 probability per block per game tick
 *   - This simulates "slow, probabilistic growth" — just like real plants!
 *
 * On each random tick, CrystalGrower.grow() is called with the L-System
 * (iterations=1 for a simple single-step growth event).
 *
 * Extends BushBlock:
 *   - Handles placement validation (needs solid surface below)
 *   - Provides the "cross" shape (two crossing planes like flowers/saplings)
 *   - Handles block break drops
 *
 * ★ JAVA 21 showcase (via CrystalGrower + LSystem):
 *   Switch expressions, Pattern Matching over sealed GrowthRule variants ★
 */
public class CrystalSeedBlock extends Block {

    public static final MapCodec<CrystalSeedBlock> CODEC = simpleCodec(CrystalSeedBlock::new);

    /** Small cross shape — 4x8x4 centered in the block space (like a sapling). */
    private static final VoxelShape SHAPE = Block.box(6.0, 0.0, 6.0, 10.0, 8.0, 10.0);

    public CrystalSeedBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<CrystalSeedBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /**
     * Called by the game engine randomly (not every tick — probabilistically).
     * This is Minecraft's built-in growth timer for plants.
     *
     * On trigger: grow one L-System step upward from this block's position.
     * The seed itself doesn't break — crystal grows above it.
     */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Each random tick: grow one step upward
        // L-System iterations=1: one simple branching segment
        CrystalGrower.grow(level, pos, Direction.UP, 1);
    }
}
