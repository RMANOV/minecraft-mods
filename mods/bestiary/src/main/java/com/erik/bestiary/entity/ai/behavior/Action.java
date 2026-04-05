package com.erik.bestiary.entity.ai.behavior;

import net.minecraft.world.entity.Mob;

import java.util.function.Predicate;

/**
 * ★ JAVA 21 FEATURE: Functional Records — Records that Hold Lambda Functions ★
 *
 * Action is a LEAF node in the behavior tree — it has no children.
 * It wraps a Predicate<T> that:
 * - PERFORMS the action on the mob
 * - Returns true if the action completed successfully
 * - Returns false if the action could not be performed
 *
 * ─── Records + Functional Interfaces ─────────────────────────────────────────
 *
 * Predicate<T> is a functional interface: (T) -> boolean
 * By storing it in a record, we make actions FIRST-CLASS VALUES:
 *
 *   var chargeAction = new Action<PackWolfEntity>(wolf -> {
 *       wolf.getMoveControl().setWantedPosition(...);
 *       return true;
 *   });
 *
 * Actions can be:
 * - Stored in collections
 * - Passed as parameters
 * - Combined with other behavior nodes
 * - Created with method references: new Action<>(wolf::chargeTarget)
 *
 * ─── Why Predicate<T> and not Runnable? ──────────────────────────────────────
 * Runnable returns void — no way to signal success/failure.
 * Predicate<T> returns boolean — we can propagate failure up the tree.
 *
 * This enables the tree to make decisions based on whether actions succeeded:
 *   Sequence[canSeeTarget, moveToTarget, attackTarget]
 * If moveToTarget fails (path blocked), attackTarget is skipped.
 *
 * @param <T>    the mob type this action operates on
 * @param action the action lambda: (mob) -> did it succeed?
 */
public record Action<T extends Mob>(Predicate<T> action) implements BehaviorNode<T> {

    public Action {
        if (action == null) throw new IllegalArgumentException("Action predicate cannot be null");
    }

    /**
     * ★ JAVA CONCEPT: Predicate.test() — Execute a Lambda ★
     *
     * action.test(mob) invokes the stored lambda with the mob as argument.
     * The result (true/false) is propagated up to parent composite nodes.
     *
     * This is functional application: apply a function to its argument.
     * In Haskell notation: action mob
     * In Java: action.test(mob)
     *
     * @param mob the mob to perform the action on
     * @return true if the action succeeded, false otherwise
     */
    @Override
    public boolean tick(T mob) {
        return action.test(mob);
    }
}
