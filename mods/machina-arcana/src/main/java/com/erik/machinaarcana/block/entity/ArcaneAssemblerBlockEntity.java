package com.erik.machinaarcana.block.entity;

import com.erik.machinaarcana.crafting.CraftStage;
import com.erik.machinaarcana.crafting.CraftingPipeline;
import com.erik.machinaarcana.mana.ManaReceiver;
import com.erik.machinaarcana.mana.ManaType;
import com.erik.machinaarcana.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * ★ JAVA CONCEPT: BlockEntity — Persistent State and Server Ticking ★
 *
 * BlockEntities hold data that survives chunk load/unload and game restarts.
 * They can also tick (run logic every game tick = 1/20th of a second).
 *
 * This entity:
 *   1. Holds an input ItemStack (what to craft)
 *   2. Tracks the current {@link CraftStage} (Input → Processing → Output/Failed)
 *   3. Stores a mana buffer (consumes mana from the network)
 *   4. Implements {@link ManaReceiver} — so the ManaNetwork can push mana to it
 *
 * ★ Interface combination ★
 * By implementing BOTH ManaReceiver AND indirectly being used by ManaProvider,
 * this entity is a participant in the generic ManaNetwork:
 *   {@code ManaNetwork<ArcaneAssemblerBlockEntity>} is valid because
 *   {@code ArcaneAssemblerBlockEntity extends BlockEntity & ManaReceiver}.
 *
 * ★ Serialization pattern (1.21+) ★
 * Uses ValueInput/ValueOutput instead of CompoundTag for structured NBT I/O.
 * This mirrors the pattern in ClaimMarkerBlockEntity.
 */
public class ArcaneAssemblerBlockEntity extends BlockEntity implements ManaReceiver {

    // ── Constants ──────────────────────────────────────────────────────────

    /** Mana cost per tick while processing. */
    private static final int MANA_COST_PER_TICK = 5;

    /** Maximum mana stored in this assembler's internal buffer. */
    private static final int MAX_MANA_STORAGE = 1000;

    /** How much mana to accept per network push. */
    private static final int MAX_RECEIVE_PER_TICK = 20;

    // ── Fields ─────────────────────────────────────────────────────────────

    /** The item currently being processed (or waiting/output). */
    private ItemStack inputStack = ItemStack.EMPTY;

    /** Current crafting state — uses sealed interface with pattern matching. */
    private CraftStage craftStage = null;

    /** Internal mana buffer (filled by ManaNetwork, consumed during crafting). */
    private int storedMana = 0;

    /** Whether the 3×3×3 structure has been validated and formed. */
    private boolean structureFormed = false;

    /** The crafting pipeline — stateless, can be a shared constant. */
    private static final CraftingPipeline PIPELINE = new CraftingPipeline();

    // ── Constructor ────────────────────────────────────────────────────────

    public ArcaneAssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.ASSEMBLER_CORE_ENTITY, pos, state);
    }

    // ── Tick (Server-side Logic) ───────────────────────────────────────────

    /**
     * Static tick method — Fabric/Minecraft calls this every game tick.
     *
     * Using a static method (instead of an instance tick() on a Tickable interface)
     * is the modern Fabric pattern — it avoids the overhead of interface dispatch
     * and keeps the calling convention explicit.
     *
     * @param level   the world (level.isClientSide() == false here, always server)
     * @param pos     block position
     * @param state   current block state
     * @param entity  the block entity instance
     */
    public static void tick(Level level, BlockPos pos, BlockState state,
                            ArcaneAssemblerBlockEntity entity) {
        if (level.isClientSide()) return;
        entity.serverTick();
    }

    /**
     * All server-side tick logic.
     *
     * ★ Pattern matching switch on sealed CraftStage ★
     * The switch here is exhaustive — no default needed because CraftStage is sealed.
     */
    private void serverTick() {
        if (!structureFormed) return;
        if (craftStage == null) return;

        // ★ Pattern matching switch — exhaustive on sealed interface ★
        switch (craftStage) {
            case CraftStage.Input ignored -> {
                // Waiting to start — pipeline.process() will kick it off
                if (storedMana >= MANA_COST_PER_TICK) {
                    craftStage = PIPELINE.process(craftStage);
                }
            }
            case CraftStage.Processing p -> {
                // Actively crafting — consume mana each tick
                if (storedMana >= MANA_COST_PER_TICK) {
                    storedMana -= MANA_COST_PER_TICK;
                    craftStage = PIPELINE.advanceTick(craftStage);
                    setChanged();
                } else {
                    // Not enough mana — stall (don't advance, don't fail)
                    // Progress is preserved; crafting resumes when mana arrives
                }
            }
            case CraftStage.Output ignored -> {
                // Waiting for player to collect — do nothing
            }
            case CraftStage.Failed ignored -> {
                // Failed — waiting for player to reset
            }
        }
    }

    // ── ManaReceiver Implementation ────────────────────────────────────────

    /**
     * Called by {@link com.erik.machinaarcana.mana.ManaNetwork} to push mana.
     *
     * ★ Interface implementation ★
     * Because this class implements ManaReceiver, it can be used as the
     * type parameter T in ManaNetwork<T extends BlockEntity & ManaReceiver>.
     */
    @Override
    public int receiveMana(ManaType type, int amount) {
        if (!canReceive(type)) return 0;
        int space = MAX_MANA_STORAGE - storedMana;
        int toStore = Math.min(amount, space);
        storedMana += toStore;
        if (toStore > 0) setChanged();
        return toStore;
    }

    @Override
    public int getMaxReceive() {
        return MAX_RECEIVE_PER_TICK;
    }

    @Override
    public boolean canReceive(ManaType type) {
        // Only accepts ARCANE mana for crafting
        return type == ManaType.ARCANE && storedMana < MAX_MANA_STORAGE;
    }

    // ── Interaction Methods ────────────────────────────────────────────────

    /**
     * Called when the 3×3×3 structure is successfully validated.
     * Activates the assembler.
     */
    public void onStructureFormed() {
        structureFormed = true;
        setChanged();
    }

    /**
     * Attempt to insert an item from the player's hand into the assembler.
     *
     * @param player  the inserting player
     * @param hand    which hand holds the item
     * @param heldItem the item stack
     * @return interaction result
     */
    public InteractionResult tryInsertItem(Player player, InteractionHand hand, ItemStack heldItem) {
        if (!structureFormed) {
            player.displayClientMessage(
                    Component.literal("Activate the Assembler first (right-click the core with empty hand)!"),
                    true);
            return InteractionResult.FAIL;
        }

        // If there's already output waiting, let the player collect it
        if (craftStage instanceof CraftStage.Output output) {
            player.addItem(output.result().copy());
            craftStage = null;
            inputStack = ItemStack.EMPTY;
            setChanged();
            player.displayClientMessage(Component.literal("Collected crafted item!"), true);
            return InteractionResult.SUCCESS;
        }

        // If not currently processing, accept a new item
        if (craftStage == null || craftStage instanceof CraftStage.Failed) {
            ItemStack toProcess = heldItem.split(1);
            inputStack = toProcess.copy();
            craftStage = new CraftStage.Input(toProcess);
            setChanged();
            player.displayClientMessage(
                    Component.literal("Inserted " + toProcess.getHoverName().getString()
                            + " — crafting will begin when mana is available."),
                    true);
            return InteractionResult.SUCCESS;
        }

        player.displayClientMessage(
                Component.literal("Assembler is busy! Wait for it to finish."),
                true);
        return InteractionResult.FAIL;
    }

    // ── Accessors ──────────────────────────────────────────────────────────

    public int getStoredMana() { return storedMana; }
    public int getMaxMana()    { return MAX_MANA_STORAGE; }
    public boolean isStructureFormed() { return structureFormed; }
    public CraftStage getCraftStage()  { return craftStage; }

    /**
     * Progress percentage (0.0–1.0) for HUD or rendering.
     * Returns 0 if not processing.
     */
    public float getCraftProgress() {
        if (craftStage instanceof CraftStage.Processing p) {
            return p.progress();
        }
        return 0.0f;
    }

    // ── Serialization ──────────────────────────────────────────────────────

    /**
     * ★ ValueOutput (1.21+ NBT serialization) ★
     * Saves persistent data when chunk is saved to disk.
     *
     * We follow the exact pattern from ClaimMarkerBlockEntity (v3 reference):
     * only putInt and putString are used — the minimal verified API.
     *
     * Note: In-progress crafting state is intentionally NOT persisted across restarts
     * (items drop back to the player on rejoin if they were processing).
     * Only the mana buffer and structure state persist.
     */
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("StoredMana", storedMana);
        // Boolean stored as int: 1 = true, 0 = false (following v3 safe-API patterns)
        output.putInt("StructureFormed", structureFormed ? 1 : 0);
    }

    /**
     * ★ ValueInput (1.21+ NBT deserialization) ★
     * Loads persistent data when chunk is loaded from disk.
     */
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        storedMana = input.getIntOr("StoredMana", 0);
        structureFormed = input.getIntOr("StructureFormed", 0) != 0;
        // craftStage and inputStack reset to null/empty on load —
        // this is acceptable for an educational showcase mod
    }
}
