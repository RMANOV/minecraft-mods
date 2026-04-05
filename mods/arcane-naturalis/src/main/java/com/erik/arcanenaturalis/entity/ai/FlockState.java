package com.erik.arcanenaturalis.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * ★ JAVA 21 FEATURE: Sealed Interfaces + Records ★
 *
 * A SEALED INTERFACE restricts which classes can implement it via 'permits'.
 * Every permitted subtype is listed here — the compiler knows ALL subtypes
 * at compile time. This enables:
 *
 *   1. EXHAUSTIVE pattern matching in switch expressions:
 *      No 'default' branch needed — the compiler guarantees coverage.
 *
 *   2. ALGEBRAIC DATA TYPES (Sum types):
 *      FlockState is exactly one of: Idle | Flocking | Feeding | Fleeing
 *      Each variant carries different payload data — like a tagged union in C.
 *
 *   3. BETTER TYPE SAFETY than plain enums:
 *      Idle carries an int, Fleeing carries a Vec3 AND a float urgency.
 *      Enums can't do this without messy Optional fields.
 *
 * Combined with RECORDS (also Java 16+, matured in 21):
 *   - Records are immutable data carriers
 *   - Auto-generate constructor, equals(), hashCode(), toString()
 *   - 'record Idle(int ticksRemaining)' is a complete class definition
 *
 * Think of sealed interfaces as "enums that can hold different data per variant".
 */
public sealed interface FlockState
        permits FlockState.Idle, FlockState.Flocking, FlockState.Feeding, FlockState.Fleeing {

    /**
     * The butterfly is resting — waiting before choosing a new behavior.
     *
     * @param ticksRemaining how many more ticks to stay idle
     */
    record Idle(int ticksRemaining) implements FlockState {
        /** Returns a new Idle with one fewer tick remaining. */
        public Idle tick() {
            return new Idle(ticksRemaining - 1);
        }

        /** True when the idle period has expired. */
        public boolean isDone() {
            return ticksRemaining <= 0;
        }
    }

    /**
     * The butterfly is actively participating in a flock.
     * Carries the current composite boid force being applied.
     *
     * @param currentForce the composite separation+alignment+cohesion force
     */
    record Flocking(BoidForce currentForce) implements FlockState {}

    /**
     * The butterfly has spotted a flower and is flying toward it to feed.
     *
     * @param flowerPos the block position of the target flower
     */
    record Feeding(BlockPos flowerPos) implements FlockState {}

    /**
     * The butterfly is fleeing from danger (player approached, explosion, etc.).
     *
     * @param dangerSource the world position of the threat
     * @param urgency      0.0 = mild concern, 1.0 = maximum panic speed
     */
    record Fleeing(Vec3 dangerSource, float urgency) implements FlockState {}
}
