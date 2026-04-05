package com.erik.bestiary.entity.ai.behavior;

import net.minecraft.world.entity.Mob;

import java.util.List;

/**
 * ★ JAVA 21 FEATURE: Generic Records + Stream API Composition ★
 *
 * Sequence is the AND-node of a behavior tree.
 * It ticks children LEFT-TO-RIGHT and stops at the first FAILURE.
 *
 * Semantics: "Do A, THEN B, THEN C — but only if each step succeeds."
 * If any child returns false, Sequence immediately returns false.
 * Only if ALL children succeed does Sequence return true.
 *
 * Analogy: a recipe. Step 1: chop vegetables. Step 2: heat pan.
 * If you can't chop (no knife), skip the whole recipe.
 *
 * ─── Record + Generics Combination ───────────────────────────────────────────
 * This is a GENERIC RECORD — a record parameterized by T.
 * record Sequence<T extends Mob>(List<BehaviorNode<T>> children)
 *
 * The type parameter flows through:
 * - children: List<BehaviorNode<T>> — each child operates on the same mob type T
 * - tick(T mob) — the mob passed to Sequence is the SAME TYPE as the children expect
 *
 * ─── Stream API: allMatch ─────────────────────────────────────────────────────
 * children.stream().allMatch(child -> child.tick(mob))
 *
 * allMatch is SHORT-CIRCUIT: it stops processing as soon as any child fails.
 * This is the CORRECT behavior for Sequence — don't bother running later steps
 * if an earlier step already failed.
 *
 * @param <T>      the mob type
 * @param children list of child nodes to execute in order
 */
public record Sequence<T extends Mob>(List<BehaviorNode<T>> children) implements BehaviorNode<T> {

    /**
     * ★ JAVA 21 FEATURE: Records as @FunctionalInterface carriers ★
     *
     * Compact constructor validates that children is non-null.
     * The List is used as-is (not defensively copied) for performance.
     */
    public Sequence {
        if (children == null) throw new IllegalArgumentException("Sequence children cannot be null");
    }

    /**
     * ★ JAVA CONCEPT: Short-Circuit Evaluation with Stream.allMatch ★
     *
     * allMatch returns:
     * - true  if ALL elements satisfy the predicate (or list is empty)
     * - false as soon as ONE element fails the predicate (stops early)
     *
     * The predicate here is `child -> child.tick(mob)` — execute the child.
     *
     * Compare to manual loop:
     *   for (var child : children) {
     *       if (!child.tick(mob)) return false;  // early exit
     *   }
     *   return true;
     *
     * Stream.allMatch expresses the SAME logic more declaratively.
     *
     * @param mob the mob entity to pass to each child
     * @return true if all children succeeded, false if any failed
     */
    @Override
    public boolean tick(T mob) {
        // allMatch is short-circuit: stops at first false return
        // This correctly models "do ALL steps, fail fast" behavior
        return children.stream().allMatch(child -> child.tick(mob));
    }
}
