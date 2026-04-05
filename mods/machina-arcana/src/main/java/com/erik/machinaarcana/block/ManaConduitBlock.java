package com.erik.machinaarcana.block;

import com.erik.machinaarcana.block.entity.ManaConduitBlockEntity;
import com.erik.machinaarcana.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * ★ JAVA CONCEPT: BaseEntityBlock for Network Nodes ★
 *
 * A Mana Conduit connects mana-producing and mana-consuming blocks.
 * It acts as a relay in the mana network — storing a small mana buffer
 * and distributing to adjacent receivers each tick.
 *
 * Because conduits need to tick (to poll for neighbors, push mana, etc.)
 * and store state (buffered mana, network membership), they use a BlockEntity.
 *
 * When a conduit is placed or broken, it triggers a network rebuild in
 * all nearby conduits — ensuring the network topology stays accurate.
 *
 * ★ Interaction with Generics ★
 * ManaConduitBlockEntity implements both ManaReceiver AND ManaProvider,
 * making it usable as T in {@code ManaNetwork<T extends BlockEntity & ManaReceiver>}.
 */
public class ManaConduitBlock extends BaseEntityBlock {

    public static final MapCodec<ManaConduitBlock> CODEC =
            simpleCodec(ManaConduitBlock::new);

    public ManaConduitBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManaConduitBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // ── Ticking ────────────────────────────────────────────────────────────

    /**
     * Returns the tick function for this block's entity.
     * The ManaConduitBlockEntity ticks to distribute mana to neighbors.
     */
    @Nullable
    @Override
    public <E extends BlockEntity> BlockEntityTicker<E> getTicker(
            Level level, BlockState state, BlockEntityType<E> type) {
        return createTickerHelper(type, ModBlocks.MANA_CONDUIT_ENTITY,
                ManaConduitBlockEntity::tick);
    }

    // ── Network Rebuild on Place/Break ─────────────────────────────────────

    /**
     * Called when this block is placed. Triggers network rebuild in neighbors.
     *
     * @param level     the world
     * @param pos       where this block was placed
     * @param state     the new block state
     * @param placer    the entity that placed it (may be null for pistons etc.)
     * @param itemStack the item that was placed
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable net.minecraft.world.entity.LivingEntity placer,
                            net.minecraft.world.item.ItemStack itemStack) {
        super.setPlacedBy(level, pos, state, placer, itemStack);
        if (!level.isClientSide()) {
            notifyNeighborConduits(level, pos);
        }
    }

    /**
     * Called when this block is destroyed. Triggers network rebuild in neighbors
     * so they know to remove this conduit from their networks.
     */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state,
                                       net.minecraft.world.entity.player.Player player) {
        if (!level.isClientSide()) {
            notifyNeighborConduits(level, pos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    /**
     * Tells all adjacent ManaConduit block entities to rebuild their networks.
     * This ensures connectivity changes propagate immediately.
     *
     * @param level  the world
     * @param center the changed position
     */
    private void notifyNeighborConduits(Level level, BlockPos center) {
        for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            BlockPos neighbor = center.relative(dir);
            BlockEntity be = level.getBlockEntity(neighbor);
            if (be instanceof ManaConduitBlockEntity conduit) {
                conduit.markNetworkDirty();
            }
        }
    }
}
