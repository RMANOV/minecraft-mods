package com.erik.bestiary.entity.ai.behavior;

import net.minecraft.world.entity.Mob;

import java.util.function.Predicate;

/**
 * ★ JAVA 21 FEATURE: Condition Nodes — Pure Predicates in the Tree ★
 *
 * Condition is a LEAF node that ONLY checks — it never changes world state.
 * This distinction from Action is important:
 *
 * Action:    performs work + reports success/failure (has side effects)
 * Condition: queries state + reports true/false (pure predicate, no side effects)
 *
 * ─── Why Separate Condition from Action? ──────────────────────────────────────
 *
 * In behavior tree design, PURE CONDITIONS should not cause side effects.
 * Separating them makes trees READABLE and DEBUGGABLE:
 *
 *   Sequence[
 *     Condition[hasTarget],      // pure check: does wolf have a target?
 *     Condition[isHealthAbove50%], // pure check: is wolf healthy enough?
 *     Action[chargeTarget]        // side effect: move toward target
 *   ]
 *
 * If the wolf fails the health condition, we KNOW why without running chargeTarget.
 * If chargeTarget fails, we know the conditions passed but the action itself failed.
 *
 * ─── Predicate<T> as a First-Class Value ──────────────────────────────────────
 *
 * Predicate<T> is @FunctionalInterface — compatible with lambdas:
 *   new Condition<>(wolf -> wolf.getTarget() != null)
 *   new Condition<>(wolf -> wolf.getHealth() > wolf.getMaxHealth() * 0.5f)
 *   new Condition<>(PackWolfEntity::hasPackmates)  // method reference!
 *
 * Predicates can also be COMPOSED using and(), or(), negate():
 *   Predicate<PackWolfEntity> complex =
 *       hasTarget.and(isHealthy).or(isAlpha.and(mustFight));
 *   new Condition<>(complex)
 *
 * This is predicate algebra — combining conditions without if/else chains.
 *
 * @param <T>       the mob type this condition checks
 * @param predicate the condition lambda: (mob) -> is condition met?
 */
public record Condition<T extends Mob>(Predicate<T> predicate) implements BehaviorNode<T> {

    public Condition {
        if (predicate == null) throw new IllegalArgumentException("Condition predicate cannot be null");
    }

    /**
     * ★ JAVA CONCEPT: Pure Evaluation — No Side Effects ★
     *
     * A Condition node ONLY calls predicate.test(mob) — no modifications.
     * The tick() method is completely pure: same mob → same result.
     *
     * This is the foundation of testable AI: conditions can be unit-tested
     * independently by constructing a mock mob and checking the result.
     *
     * @param mob the mob to evaluate the condition against
     * @return true if the condition is satisfied, false otherwise
     */
    @Override
    public boolean tick(T mob) {
        return predicate.test(mob);
    }
}
