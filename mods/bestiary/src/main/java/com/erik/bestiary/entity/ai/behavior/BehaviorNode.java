package com.erik.bestiary.entity.ai.behavior;

import net.minecraft.world.entity.Mob;

/**
 * ★ JAVA 21 FEATURE: Generic Sealed Interfaces for Type-Safe Tree Composition ★
 *
 * BehaviorNode is the foundation of a composable behavior tree.
 * It combines two powerful Java 21 features:
 *
 * 1. GENERICS: BehaviorNode<T extends Mob>
 *    Every node is parameterized by the mob type it operates on.
 *    A BehaviorNode<PackWolfEntity> can ONLY be used with PackWolfEntity.
 *    This prevents accidentally mixing wolf behaviors with dragon behaviors.
 *
 * 2. SEALED INTERFACE: restricts the tree node types to exactly 4:
 *    Sequence, Selector, Action, Condition
 *    This makes the tree a closed algebra — known, composable, exhaustive.
 *
 * ─── Behavior Tree Concepts ──────────────────────────────────────────────────
 *
 * A behavior tree is an AI architecture used in games (Unreal Engine, many AAA games).
 * It represents decision-making as a TREE of nodes:
 *
 *   Root (Selector)
 *   ├── Sequence [attack if target nearby]
 *   │   ├── Condition [has target?]
 *   │   └── Action [charge!]
 *   └── Sequence [patrol]
 *       ├── Condition [no target?]
 *       └── Action [wander]
 *
 * vs. Minecraft's GoalSelector: fixed priority list, not composable.
 * Behavior trees can express complex logic (if A and B, do C or else D) cleanly.
 *
 * ─── The Generic Sealed Interface Pattern ─────────────────────────────────────
 * sealed interface BehaviorNode<T extends Mob> permits Sequence<T>, ...
 *
 * Each permitted class MUST also be generic:
 * - record Sequence<T extends Mob>(...) implements BehaviorNode<T>
 * This ensures type safety flows through the entire tree.
 *
 * @param <T> the mob type this behavior tree operates on
 */
public sealed interface BehaviorNode<T extends Mob>
        permits Sequence, Selector, Action, Condition {

    /**
     * Execute this node against the given mob entity.
     *
     * Convention (standard behavior tree semantics):
     * - Returns true  = SUCCESS (this node completed its task)
     * - Returns false = FAILURE (this node could not execute)
     *
     * Composite nodes (Sequence, Selector) interpret children's results
     * to decide their own result.
     *
     * @param mob the mob entity to operate on
     * @return true if this node succeeded, false if it failed
     */
    boolean tick(T mob);
}
