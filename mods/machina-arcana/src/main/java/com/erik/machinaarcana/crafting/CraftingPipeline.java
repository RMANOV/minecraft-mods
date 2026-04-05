package com.erik.machinaarcana.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * ★ JAVA 21 FEATURE: Sealed Interfaces + Pattern Matching Switch + Functional Pipelines ★
 *
 * The CraftingPipeline manages state transitions through {@link CraftStage}.
 * It uses two key Java 21 features together:
 *
 * <h3>1. Pattern Matching Switch (preview in 17, finalized in 21)</h3>
 * <pre>
 *   switch (stage) {
 *       case Input i       -> ...
 *       case Processing p  -> ...
 *       case Output o      -> ...
 *       case Failed f      -> ...
 *   }
 * </pre>
 * Because {@link CraftStage} is sealed, the switch is exhaustive — the compiler
 * verifies all subtypes are handled. No {@code default} branch needed!
 * Each case arm binds the specific record type, so you can access its fields
 * directly without casting.
 *
 * <h3>2. Functional Pipelines (UnaryOperator + method chaining)</h3>
 * {@link #process(CraftStage)} demonstrates pipeline composition:
 * a series of transformation functions (validators → processors → finalizers)
 * are chained using {@code Function.andThen()}, and then applied to the initial
 * state via {@code .apply()}.
 *
 * {@code UnaryOperator<T>} is just {@code Function<T,T>} — a function from T to T.
 * Useful for transformation pipelines where type doesn't change.
 */
public class CraftingPipeline {

    // ── Constants ──────────────────────────────────────────────────────────

    /** Ticks required to complete one craft (20 ticks = 1 second). */
    public static final int CRAFT_DURATION_TICKS = 100; // 5 seconds

    // ── Pipeline Stages (as UnaryOperators) ───────────────────────────────

    /**
     * ★ UnaryOperator as a pipeline step ★
     *
     * This validator checks if the input item can actually be crafted.
     * If not, it immediately transitions to Failed.
     *
     * UnaryOperator<CraftStage> is Function<CraftStage, CraftStage>:
     *   - Input:  a CraftStage
     *   - Output: a CraftStage (possibly different subtype)
     */
    private static final UnaryOperator<CraftStage> VALIDATE_INPUT =
            stage -> {
                // Only validate if we're in Input state
                if (stage instanceof CraftStage.Input input) {
                    if (input.item().isEmpty()) {
                        return new CraftStage.Failed(ItemStack.EMPTY, "No item provided");
                    }
                    // Simple craftability check — could query a recipe registry
                    if (!isCraftable(input.item())) {
                        return new CraftStage.Failed(input.item(), "Item is not an arcane recipe");
                    }
                }
                // Pass through unchanged for other states
                return stage;
            };

    /**
     * This processor starts the crafting timer when we have a valid Input.
     */
    private static final UnaryOperator<CraftStage> START_PROCESSING =
            stage -> {
                if (stage instanceof CraftStage.Input input) {
                    // Transition from Input → Processing
                    return new CraftStage.Processing(input.item(), CRAFT_DURATION_TICKS, 0.0f);
                }
                return stage;
            };

    // ── Core Methods ───────────────────────────────────────────────────────

    /**
     * ★ FUNCTIONAL PIPELINE: stream().reduce() style composition ★
     *
     * Processes an initial {@link CraftStage} through a composed pipeline of
     * {@link UnaryOperator} transformations.
     *
     * We use {@code Function.andThen()} to compose operators:
     *   validateInput.andThen(startProcessing)
     * This creates a new function that runs both in sequence.
     *
     * Conceptually equivalent to:
     *   stage → VALIDATE_INPUT(stage) → START_PROCESSING(result)
     *
     * @param initial the initial stage (should be an Input)
     * @return the resulting stage after pipeline processing
     */
    public CraftStage process(CraftStage initial) {
        // ★ Function composition: andThen() chains transforms left-to-right ★
        // VALIDATE_INPUT runs first; its output feeds into START_PROCESSING.
        UnaryOperator<CraftStage> pipeline =
                stage -> START_PROCESSING.apply(VALIDATE_INPUT.apply(stage));

        return pipeline.apply(initial);
    }

    /**
     * ★ PATTERN MATCHING SWITCH (Java 21) ★
     *
     * Advances the crafting pipeline by one game tick.
     * The switch is EXHAUSTIVE because CraftStage is sealed:
     *   - The compiler verifies we handled all 4 subtypes.
     *   - No default branch needed (though we add one for safety).
     *   - Each arm deconstructs the record with pattern variable binding.
     *
     * @param current the current crafting state
     * @return the next crafting state
     */
    public CraftStage advanceTick(CraftStage current) {
        // ★ Pattern matching switch — Java 21 finalized syntax ★
        return switch (current) {

            // Input: shouldn't tick; call process() to start it
            case CraftStage.Input i ->
                    new CraftStage.Failed(i.item(), "Call process() before advanceTick()");

            // Processing: decrement timer, update progress, check completion
            case CraftStage.Processing p -> {
                int remaining = p.ticksRemaining() - 1;
                if (remaining <= 0) {
                    // Crafting complete! Produce the output.
                    ItemStack result = craft(p.item());
                    yield new CraftStage.Output(result);
                } else {
                    // Still processing — update progress (0.0 → 1.0)
                    float progress = 1.0f - ((float) remaining / CRAFT_DURATION_TICKS);
                    yield new CraftStage.Processing(p.item(), remaining, progress);
                }
            }

            // Output: already done, nothing to advance
            case CraftStage.Output o -> o;

            // Failed: already done (with error), nothing to advance
            case CraftStage.Failed f -> f;
        };
    }

    /**
     * ★ Pattern Matching instanceof (Java 16+) ★
     *
     * Extracts the output if available, wrapped in Optional for safe handling.
     * Uses pattern matching {@code instanceof} to avoid explicit cast.
     *
     * @param stage the current stage
     * @return Optional containing the result, or empty if not yet complete
     */
    public Optional<ItemStack> tryCollect(CraftStage stage) {
        // ★ Pattern matching instanceof: checks type AND binds variable ★
        if (stage instanceof CraftStage.Output output) {
            return Optional.of(output.result());
        }
        return Optional.empty();
    }

    // ── Private Helpers ────────────────────────────────────────────────────

    /**
     * Checks if an item can be processed by the Arcane Assembler.
     * In a real mod, this would query a recipe registry.
     * Here we use a simple whitelist for demonstration.
     *
     * @param stack the item to check
     * @return true if craftable
     */
    private static boolean isCraftable(ItemStack stack) {
        // Simple demo: iron ingots and gold ingots can be "arcane-processed"
        return stack.is(Items.IRON_INGOT)
                || stack.is(Items.GOLD_INGOT)
                || stack.is(Items.DIAMOND)
                || stack.is(Items.AMETHYST_SHARD);
    }

    /**
     * Performs the actual crafting transformation.
     * Demo: iron ingot → arcane ingot (simulated as a glowstone dust).
     * In a real mod, this would look up a recipe and return the actual result.
     *
     * @param input the input item
     * @return the crafted output
     */
    private static ItemStack craft(ItemStack input) {
        // Demo transformation: all inputs become glowstone dust (as a placeholder)
        // A real implementation would look up the recipe for the specific item.
        return new ItemStack(Items.GLOWSTONE_DUST, 1);
    }
}
