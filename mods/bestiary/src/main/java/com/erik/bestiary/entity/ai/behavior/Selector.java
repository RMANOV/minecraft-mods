package com.erik.bestiary.entity.ai.behavior;

import net.minecraft.world.entity.Mob;

import java.util.List;

/**
 * ★ JAVA 21 FEATURE: Generic Records + Stream API (anyMatch) ★
 *
 * Selector is the OR-node of a behavior tree.
 * It ticks children LEFT-TO-RIGHT and stops at the first SUCCESS.
 *
 * Semantics: "Try A. If it fails, try B. If that fails, try C."
 * This is the fallback / priority-selection pattern.
 *
 * If ANY child returns true, Selector immediately returns true (success).
 * Only if ALL children fail does Selector return false.
 *
 * Analogy: planning a trip. Option 1: fly. If no flights, Option 2: drive.
 * If no car, Option 3: train. First option that works wins.
 *
 * ─── Selector vs Sequence ─────────────────────────────────────────────────────
 *
 * Sequence (AND): ALL children must succeed
 *   → "Do A AND B AND C" — stops at first failure
 *
 * Selector (OR): ONE child must succeed
 *   → "Try A OR B OR C" — stops at first success
 *
 * Together they form a COMPLETE behavior logic system:
 *   Selector[             // Try attack OR flee
 *     Sequence[           // Attack: requires target AND enough health
 *       hasTarget,
 *       hasEnoughHP,
 *       chargeAttack
 *     ],
 *     Sequence[           // Flee: always possible
 *       flee
 *     ]
 *   ]
 *
 * ─── Stream API: anyMatch ─────────────────────────────────────────────────────
 * children.stream().anyMatch(child -> child.tick(mob))
 *
 * anyMatch is SHORT-CIRCUIT: stops as soon as one child succeeds.
 * This is CORRECT for Selector — once we find a working option, stop looking.
 *
 * @param <T>      the mob type
 * @param children list of alternative child nodes
 */
public record Selector<T extends Mob>(List<BehaviorNode<T>> children) implements BehaviorNode<T> {

    public Selector {
        if (children == null) throw new IllegalArgumentException("Selector children cannot be null");
    }

    /**
     * ★ JAVA CONCEPT: Short-Circuit Evaluation with Stream.anyMatch ★
     *
     * anyMatch returns:
     * - true  as soon as ONE element satisfies the predicate (stops early)
     * - false if NO elements satisfy the predicate (exhausted all options)
     *
     * The side effect (child.tick(mob)) executes the child as a predicate.
     *
     * WARNING: using stream().anyMatch() with side effects is acceptable here
     * because the "side effect" IS the intended operation — ticking the behavior.
     * In pure functional code, prefer explicit loops for side-effect clarity.
     * In this educational context, anyMatch expresses the selection semantics perfectly.
     *
     * @param mob the mob entity to pass to each child
     * @return true if any child succeeded, false if all failed
     */
    @Override
    public boolean tick(T mob) {
        // anyMatch is short-circuit: stops at first true return
        // This correctly models "try alternatives, use first that works" behavior
        return children.stream().anyMatch(child -> child.tick(mob));
    }
}
