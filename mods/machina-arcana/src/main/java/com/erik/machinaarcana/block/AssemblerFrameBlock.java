package com.erik.machinaarcana.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * ★ JAVA CONCEPT: Simple Block (No Logic) ★
 *
 * AssemblerFrameBlock is a purely structural block — it carries no logic,
 * no block entity, and no special interactions. Its only purpose is to occupy
 * space in the 3×3×3 multi-block structure and look good doing it.
 *
 * Why extend Block directly instead of BaseEntityBlock?
 *   - BaseEntityBlock adds overhead for BlockEntity management.
 *   - If a block doesn't need to store data or tick, plain Block is correct.
 *   - Lean classes = better performance at scale.
 *
 * ★ MapCodec requirement (Minecraft 1.21+) ★
 *
 * Every custom block must provide a MapCodec for serialization.
 * {@code simpleCodec(Class::new)} creates a codec that reconstructs the block
 * from its Properties — sufficient for blocks with no extra state.
 */
public class AssemblerFrameBlock extends Block {

    /**
     * The required MapCodec for this block type.
     *
     * {@code simpleCodec} is a static helper from Block that creates a codec
     * knowing only the constructor. Since AssemblerFrameBlock is stateless
     * (beyond standard block properties), this is all we need.
     */
    public static final MapCodec<AssemblerFrameBlock> CODEC =
            simpleCodec(AssemblerFrameBlock::new);

    /**
     * Constructs an AssemblerFrameBlock with the given properties.
     * The properties include ID (required in 1.21+), hardness, etc.
     *
     * @param properties block behaviour properties (set via BlockBehaviour.Properties)
     */
    public AssemblerFrameBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Returns the serialization codec for this block class.
     * Must match the static CODEC field above.
     *
     * @return the MapCodec for AssemblerFrameBlock
     */
    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
