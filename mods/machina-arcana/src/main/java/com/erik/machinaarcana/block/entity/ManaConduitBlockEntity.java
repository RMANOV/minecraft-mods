package com.erik.machinaarcana.block.entity;

import com.erik.machinaarcana.mana.ManaNetwork;
import com.erik.machinaarcana.mana.ManaProvider;
import com.erik.machinaarcana.mana.ManaReceiver;
import com.erik.machinaarcana.mana.ManaType;
import com.erik.machinaarcana.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * ★ JAVA CONCEPT: Implementing Multiple Interfaces — the Relay Node ★
 *
 * ManaConduitBlockEntity implements BOTH:
 *   - {@link ManaReceiver}  — can accept mana pushed from providers
 *   - {@link ManaProvider}  — can supply stored mana to receivers
 *
 * This "dual role" makes it a relay: it receives mana from one side and
 * distributes to the other. The combined type bound:
 *   {@code T extends BlockEntity & ManaReceiver}
 * is satisfied by this class because it extends BlockEntity AND implements ManaReceiver.
 *
 * ★ ManaNetwork Generic Type ★
 * The network stored here:
 *   {@code ManaNetwork<ManaConduitBlockEntity>}
 * This is valid because ManaConduitBlockEntity extends BlockEntity & ManaReceiver.
 *
 * ★ Lazy network rebuild ★
 * The network is not rebuilt immediately on change — instead a dirty flag is set
 * and the rebuild happens on the next tick. This prevents cascading rebuilds
 * when multiple conduits are placed at once.
 */
public class ManaConduitBlockEntity extends BlockEntity implements ManaReceiver, ManaProvider {

    // ── Constants ──────────────────────────────────────────────────────────

    private static final int MAX_BUFFER      = 500;
    private static final int MAX_RECEIVE     = 20;
    private static final int DISTRIBUTE_RATE = 10;
    private static final int TICK_INTERVAL   = 4; // distribute every 4 ticks (5 times/sec)

    // ── Fields ─────────────────────────────────────────────────────────────

    /** Mana buffer indexed by type ordinal. */
    private final int[] manaBuffer = new int[ManaType.values().length];

    /**
     * The local mana network managed by this conduit.
     *
     * ★ Generic instantiation: ManaNetwork parameterized with our own class ★
     * Because ManaConduitBlockEntity extends BlockEntity & ManaReceiver,
     * this compiles — the type bounds are satisfied.
     */
    private ManaNetwork<ManaConduitBlockEntity> network;

    /** Flag: rebuild network on next tick. */
    private boolean networkDirty = true;

    /** Tick counter for rate limiting distribution. */
    private int tickCounter = 0;

    // ── Constructor ────────────────────────────────────────────────────────

    public ManaConduitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.MANA_CONDUIT_ENTITY, pos, state);
    }

    // ── Ticking ────────────────────────────────────────────────────────────

    /**
     * Static tick method called by ManaConduitBlock's getTicker().
     */
    public static void tick(Level level, BlockPos pos, BlockState state,
                            ManaConduitBlockEntity entity) {
        if (level.isClientSide()) return;
        entity.serverTick(level, pos);
    }

    private void serverTick(Level level, BlockPos pos) {
        // Lazy network rebuild
        if (networkDirty) {
            rebuildNetwork(level, pos);
            networkDirty = false;
        }

        // Rate-limit distribution
        tickCounter++;
        if (tickCounter < TICK_INTERVAL) return;
        tickCounter = 0;

        // Distribute mana in the buffer to connected receivers
        if (network != null && !network.isEmpty()) {
            for (ManaType type : ManaType.values()) {
                if (canProvide(type)) {
                    network.distributeToReceivers(this, type);
                }
            }
        }
    }

    /**
     * Rebuilds the network by BFS from this conduit's position.
     *
     * ★ Generic method call: rebuild(Class<T>) ★
     * We pass ManaConduitBlockEntity.class as the runtime type token because
     * Java erases generics — you can't write {@code rebuild(ManaConduitBlockEntity)}
     * without the .class token.
     */
    private void rebuildNetwork(Level level, BlockPos pos) {
        network = new ManaNetwork<>(level, pos);
        network.rebuild(ManaConduitBlockEntity.class);
    }

    // ── ManaReceiver ───────────────────────────────────────────────────────

    @Override
    public int receiveMana(ManaType type, int amount) {
        if (!canReceive(type)) return 0;
        int idx = type.ordinal();
        int space = MAX_BUFFER - manaBuffer[idx];
        int stored = Math.min(amount, space);
        manaBuffer[idx] += stored;
        if (stored > 0) setChanged();
        return stored;
    }

    @Override
    public int getMaxReceive() {
        return MAX_RECEIVE;
    }

    @Override
    public boolean canReceive(ManaType type) {
        return manaBuffer[type.ordinal()] < MAX_BUFFER;
    }

    // ── ManaProvider ───────────────────────────────────────────────────────

    @Override
    public int extractMana(ManaType type, int amount) {
        int idx = type.ordinal();
        int available = manaBuffer[idx];
        int extracted = Math.min(amount, Math.min(available, DISTRIBUTE_RATE));
        manaBuffer[idx] -= extracted;
        if (extracted > 0) setChanged();
        return extracted;
    }

    @Override
    public int getStoredMana(ManaType type) {
        return manaBuffer[type.ordinal()];
    }

    @Override
    public boolean canProvide(ManaType type) {
        return manaBuffer[type.ordinal()] > 0;
    }

    // ── Network Control ────────────────────────────────────────────────────

    /**
     * Marks this conduit's network as needing a rebuild.
     * Called when nearby conduits are placed or broken.
     */
    public void markNetworkDirty() {
        networkDirty = true;
    }

    public int getNetworkSize() {
        return network != null ? network.size() : 0;
    }

    // ── Serialization ──────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (ManaType type : ManaType.values()) {
            output.putInt("Mana_" + type.name(), manaBuffer[type.ordinal()]);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (ManaType type : ManaType.values()) {
            manaBuffer[type.ordinal()] = input.getIntOr("Mana_" + type.name(), 0);
        }
        // Force network rebuild after load
        networkDirty = true;
    }
}
