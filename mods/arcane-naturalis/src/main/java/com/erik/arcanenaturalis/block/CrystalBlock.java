package com.erik.arcanenaturalis.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * CrystalBlock — the decorative crystal block placed by the L-System grower.
 *
 * ★ IMPORTANT: MapCodec is REQUIRED in 1.21.11 for all custom blocks ★
 *
 * MapCodec is part of Minecraft's serialization system (DFU — DataFixerUpper).
 * It handles saving/loading block states to/from JSON and NBT.
 * Every block must override codec() since 1.21.
 *
 * The codec() method must return the block's own MapCodec so the game can:
 *   1. Save the block state to the world file (chunk NBT)
 *   2. Load it back on world open
 *   3. Support data packs that reference this block type
 *
 * CODEC — BLOCK.CODEC is the standard pattern:
 *   'CODEC' is a static field of type MapCodec<CrystalBlock>
 *   It tells DFU: "to serialize a CrystalBlock, just use BlockBehaviour.Properties"
 *   simpleCodec(CrystalBlock::new) = a codec that constructs the block from properties
 *
 * Light level 7 (set in ModBlocks.register() via lightLevel(state -> 7)) makes
 * crystal clusters glow — visible in caves and at night.
 */
public class CrystalBlock extends Block {

    // ★ MapCodec: required for all custom blocks in 1.21.11 ★
    public static final MapCodec<CrystalBlock> CODEC = simpleCodec(CrystalBlock::new);

    public CrystalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /** Returns this block's codec for the DFU serialization system. */
    @Override
    public MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
