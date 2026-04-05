package com.erik.bestiary.entity.ai.behavior;

import net.minecraft.world.entity.Mob;

/**
 * ★ JAVA CONCEPT: Composition Root — Wiring a Behavior Tree ★
 *
 * BehaviorTree is the EXECUTOR that holds the root node and provides
 * a clean entry point for ticking the entire tree.
 *
 * It is NOT a sealed interface or record — it's a plain class because:
 * - It holds mutable execution state (lastResult, tickCount)
 * - It needs a constructor that takes a root node
 * - It's the "public API" of the tree, not a data type
 *
 * ─── Generic Class: BehaviorTree<T extends Mob> ───────────────────────────────
 *
 * The generic parameter ensures type safety from root to leaves.
 * A BehaviorTree<PackWolfEntity> can ONLY be ticked with a PackWolfEntity.
 * You cannot accidentally pass a Zombie to a wolf behavior tree.
 *
 * ─── Example Usage ───────────────────────────────────────────────────────────
 *
 * BehaviorTree<PackWolfEntity> tree = new BehaviorTree<>(
 *     new Selector<>(List.of(
 *         new Sequence<>(List.of(
 *             new Condition<>(wolf -> wolf.getTarget() != null),
 *             new Condition<>(wolf -> wolf.getHealth() > 5.0f),
 *             new Action<>(wolf -> { wolf.chargeTarget(); return true; })
 *         )),
 *         new Action<>(wolf -> { wolf.patrol(); return true; })
 *     ))
 * );
 *
 * // In the wolf's tick():
 * tree.tick(this);
 *
 * @param <T> the mob type this tree executes on
 */
public class BehaviorTree<T extends Mob> {

    /**
     * The root node of the behavior tree.
     * Can be any BehaviorNode<T> — typically a Selector or Sequence.
     */
    private final BehaviorNode<T> root;

    /**
     * Tracks how many times this tree has been ticked (useful for debugging).
     */
    private long tickCount = 0L;

    /**
     * The result of the most recent tick (true = success, false = failure).
     */
    private boolean lastResult = false;

    /**
     * @param root the root node; typically a Selector with high-priority behaviors
     */
    public BehaviorTree(BehaviorNode<T> root) {
        if (root == null) throw new IllegalArgumentException("BehaviorTree root cannot be null");
        this.root = root;
    }

    /**
     * ★ JAVA CONCEPT: Delegating Execution Pattern ★
     *
     * BehaviorTree.tick() delegates entirely to the root node's tick().
     * This thin wrapper provides:
     * 1. Instrumentation (tickCount, lastResult)
     * 2. Error boundary (exceptions don't crash the mob)
     * 3. Clean public API
     *
     * The actual logic lives in the tree structure, not here.
     * BehaviorTree is the "executor" — it runs the tree but doesn't define behavior.
     *
     * @param mob the mob entity to tick the tree against
     * @return true if the root node succeeded, false otherwise
     */
    public boolean tick(T mob) {
        tickCount++;
        try {
            lastResult = root.tick(mob);
            return lastResult;
        } catch (Exception e) {
            // ★ EDUCATIONAL NOTE: Behavior trees should be resilient.
            // A bug in one branch shouldn't crash the mob entirely.
            // In production: log the error, continue with fallback behavior.
            lastResult = false;
            return false;
        }
    }

    /**
     * @return the root BehaviorNode (useful for tree inspection/debugging)
     */
    public BehaviorNode<T> getRoot() {
        return root;
    }

    /**
     * @return total number of times this tree has been ticked
     */
    public long getTickCount() {
        return tickCount;
    }

    /**
     * @return the result of the most recent tick
     */
    public boolean getLastResult() {
        return lastResult;
    }
}
