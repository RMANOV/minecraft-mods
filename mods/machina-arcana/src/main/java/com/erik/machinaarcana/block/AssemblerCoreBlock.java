package com.erik.machinaarcana.block;

import com.erik.machinaarcana.block.entity.ArcaneAssemblerBlockEntity;
import com.erik.machinaarcana.multiblock.AssemblerBuilder;
import com.erik.machinaarcana.multiblock.MultiBlockValidator;
import com.erik.machinaarcana.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * ★ JAVA CONCEPT: BaseEntityBlock — Block with State Storage ★
 *
 * AssemblerCoreBlock is the "brain" of the 3×3×3 Arcane Assembler multi-block.
 * Because it needs persistent state (crafting progress, inventory, mana buffer),
 * it uses a BlockEntity: {@link ArcaneAssemblerBlockEntity}.
 *
 * Contrast with AssemblerFrameBlock which extends plain Block:
 *   - Plain Block        → no state, no tick, no storage
 *   - BaseEntityBlock   → has a linked BlockEntity for storage and ticking
 *
 * ★ MapCodec + Builder Pattern interaction ★
 *
 * When a player right-clicks the core, we use the Builder pattern:
 * {@link AssemblerBuilder} constructs and validates the multi-block structure
 * in a readable fluent chain.
 *
 * ★ Generics in getBlockEntityTicker ★
 *
 * The ticker method uses bounded generics to safely return a ticker only when
 * the block entity type matches — avoiding unsafe casts.
 */
public class AssemblerCoreBlock extends BaseEntityBlock {

    /** Codec required by Minecraft 1.21+ for all block subclasses. */
    public static final MapCodec<AssemblerCoreBlock> CODEC =
            simpleCodec(AssemblerCoreBlock::new);

    public AssemblerCoreBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // ── Block Entity Creation ──────────────────────────────────────────────

    /**
     * Creates the BlockEntity for this block.
     * Called once when the block is placed in the world.
     *
     * @param pos   world position
     * @param state current block state
     * @return a new ArcaneAssemblerBlockEntity
     */
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneAssemblerBlockEntity(pos, state);
    }

    /**
     * Specifies that this block should render using a model (not invisible).
     */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // ── Ticking ────────────────────────────────────────────────────────────

    /**
     * ★ JAVA GENERICS: Type-safe ticker retrieval ★
     *
     * Returns a BlockEntityTicker only if the given type matches our block entity.
     * The {@code <E>} generic lets the method work for any BlockEntityType
     * without the caller needing to cast.
     *
     * {@code createTickerHelper} is a static utility that does the type check:
     * it returns null if {@code type != ModBlocks.ASSEMBLER_CORE_ENTITY},
     * or the ArcaneAssemblerBlockEntity's static tick method otherwise.
     *
     * @param level the world
     * @param state the block state
     * @param type  the block entity type being requested
     * @param <E>   the block entity type parameter
     * @return a ticker, or null if type doesn't match
     */
    @Nullable
    @Override
    public <E extends BlockEntity> BlockEntityTicker<E> getTicker(
            Level level, BlockState state, BlockEntityType<E> type) {
        // ★ createTickerHelper performs the generic type check safely ★
        return createTickerHelper(type, ModBlocks.ASSEMBLER_CORE_ENTITY,
                ArcaneAssemblerBlockEntity::tick);
    }

    // ── Player Interaction ─────────────────────────────────────────────────

    /**
     * Called when a player right-clicks this block (without holding a special item).
     *
     * ★ Builder Pattern in action ★
     * We use AssemblerBuilder's fluent API to validate the multi-block structure.
     * The chain reads almost like English:
     *   "Create a builder, in this world, with this core position,
     *    then validate, then build — and if it worked, activate."
     *
     * If the player holds an item (main hand not empty), we try inserting that item
     * into the assembler. This consolidates all interaction into one method,
     * following the v3 pattern exactly.
     *
     * @param state     current block state
     * @param level     the world
     * @param pos       block position
     * @param player    the interacting player
     * @param hitResult where the player hit the block
     * @return interaction result
     */
    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hitResult) {

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ArcaneAssemblerBlockEntity entity)) {
            return InteractionResult.PASS;
        }

        // If holding an item, try to insert it
        ItemStack heldItem = player.getMainHandItem();
        if (!heldItem.isEmpty()) {
            return entity.tryInsertItem(player, InteractionHand.MAIN_HAND, heldItem);
        }

        // No item in hand — try to validate and activate the multi-block structure
        // ★ Fluent Builder chain ★
        Optional<AssemblerBuilder> assembler = AssemblerBuilder.create()
                .inWorld(level)
                .withCore(pos)
                .withFrames(MultiBlockValidator.getFramePositions(pos))
                .validate()
                .build();

        if (assembler.isPresent()) {
            entity.onStructureFormed();
            player.displayClientMessage(
                    Component.literal("Arcane Assembler activated! Insert items to begin crafting."),
                    true);
        } else {
            player.displayClientMessage(
                    Component.literal("Incomplete structure! Build a 3x3x3 cube of Assembler Frames around the Core."),
                    true);
        }

        return InteractionResult.SUCCESS;
    }
}
