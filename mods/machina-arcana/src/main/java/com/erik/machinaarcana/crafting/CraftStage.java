package com.erik.machinaarcana.crafting;

import net.minecraft.world.item.ItemStack;

/**
 * ★ JAVA 21 FEATURE: Sealed Interfaces with Pattern-Matched Records ★
 *
 * A <b>sealed interface</b> restricts which classes can implement it.
 * The {@code permits} clause lists the ONLY allowed subtypes.
 *
 * Why seal an interface?
 *   1. Exhaustive switches — the compiler can verify you handled every case.
 *   2. Domain modeling — "a craft is EXACTLY one of these 4 states, nothing else."
 *   3. Eliminates need for "default" cases when you've covered every subtype.
 *
 * Combined with <b>records</b> (another Java 16+ feature), each state carries
 * its own data compactly. Records are immutable data carriers:
 *   - Automatic constructor, equals(), hashCode(), toString()
 *   - Fields are declared in the header: {@code record Foo(int x, String y)}
 *
 * This models a State Machine for crafting:
 *   Input → Processing → Output
 *       ↘              ↗
 *         Failed (error)
 *
 * Pattern matching switch on this sealed type in {@link CraftingPipeline} is
 * exhaustive — no default needed — because the compiler knows all 4 subtypes.
 */
public sealed interface CraftStage permits
        CraftStage.Input,
        CraftStage.Processing,
        CraftStage.Output,
        CraftStage.Failed {

    // ── Permitted subtypes ─────────────────────────────────────────────────

    /**
     * An item has been inserted and is waiting for crafting to begin.
     * Transitions to: {@code Processing} when mana is available,
     *                 {@code Failed}     if the item is not craftable.
     *
     * @param item the input item stack
     */
    record Input(ItemStack item) implements CraftStage {}

    /**
     * Crafting is actively in progress.
     *
     * @param item           the item being processed
     * @param ticksRemaining how many more ticks until complete
     * @param progress       0.0 to 1.0, used to drive the progress bar
     */
    record Processing(ItemStack item, int ticksRemaining, float progress) implements CraftStage {}

    /**
     * Crafting completed successfully. The result is ready for collection.
     *
     * @param result the crafted output item stack
     */
    record Output(ItemStack result) implements CraftStage {}

    /**
     * Crafting failed. The original item is returned with an error message.
     *
     * @param item   the original input item (returned to player)
     * @param reason human-readable failure description
     */
    record Failed(ItemStack item, String reason) implements CraftStage {}

    // ── Default interface methods ──────────────────────────────────────────

    /**
     * ★ Pattern matching instanceof (Java 16+) ★
     *
     * Returns the relevant ItemStack for this stage, regardless of which subtype.
     * Uses pattern matching: {@code instanceof Input i} both checks the type AND
     * binds the variable {@code i} in one expression — no explicit cast needed.
     *
     * @return the primary item for this stage
     */
    default ItemStack getRelevantItem() {
        // ★ Sealed interface + pattern matching = exhaustive, no default needed ★
        if (this instanceof Input i)           return i.item();
        if (this instanceof Processing p)      return p.item();
        if (this instanceof Output o)          return o.result();
        if (this instanceof Failed f)          return f.item();
        // This line is unreachable because the interface is sealed and all
        // subtypes are handled above — the compiler verifies this.
        throw new AssertionError("Unreachable: unknown CraftStage subtype");
    }

    /**
     * Convenience check: is crafting currently active (not idle)?
     *
     * @return true if in Processing state
     */
    default boolean isProcessing() {
        return this instanceof Processing;
    }

    /**
     * Convenience check: is there a result ready to collect?
     *
     * @return true if in Output state
     */
    default boolean hasOutput() {
        return this instanceof Output;
    }
}
