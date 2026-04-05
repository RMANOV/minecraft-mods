package com.erik.bestiary.entity.ai;

import net.minecraft.world.phys.Vec3;

/**
 * ★ JAVA 21 FEATURE: Records as Immutable Data Carriers ★
 *
 * Signal represents a single communication event between pack wolves.
 * It is IMMUTABLE — when it propagates, a new Signal is returned with
 * reduced urgency and increased hop count.
 *
 * This models real signal attenuation:
 * - A wolf shouts "DANGER!" at full urgency (1.0)
 * - Nearby wolf hears it, re-broadcasts at 0.8 urgency (hop 1)
 * - Farther wolf hears 0.64 urgency (hop 2)
 * - Beyond 5 hops or urgency < 0.1, signal dies
 *
 * The record + propagate() method demonstrates:
 * 1. Value semantics: propagate() returns a new Signal, never mutates
 * 2. Functional style: transformation without side effects
 * 3. Self-documenting: the record parameters ARE the documentation
 *
 * @param type    what kind of signal this is (DANGER, FOOD, RALLY)
 * @param origin  where in the world this signal originated
 * @param urgency current signal strength [0.0, 1.0]
 * @param hops    how many wolves have re-broadcast this signal
 */
public record Signal(SignalType type, Vec3 origin, float urgency, int hops) {

    /**
     * ★ JAVA 21 FEATURE: Compact Constructor for Invariant Enforcement ★
     *
     * Validate urgency is in [0.0, 1.0] at construction time.
     * Hops must be non-negative.
     */
    public Signal {
        urgency = Math.max(0.0f, Math.min(1.0f, urgency));
        if (hops < 0) throw new IllegalArgumentException("Hops cannot be negative");
        if (origin == null) throw new IllegalArgumentException("Signal origin cannot be null");
    }

    /**
     * ★ JAVA 21 PATTERN: Immutable Transformation (Record Wither) ★
     *
     * Propagation models how signals weaken over distance.
     *
     * Algorithm:
     * 1. Calculate distance from origin to receiver's position
     * 2. Apply distance attenuation: urgency *= (1 - distance/MAX_RANGE) * DECAY_FACTOR
     * 3. Increment hop counter
     * 4. Return NEW Signal — never mutate the original
     *
     * By keeping origin unchanged, any wolf can calculate "where did this come from?"
     * even after 4 hops. This is crucial for the Scout's prey-location reporting.
     *
     * @param receiverPos the Vec3 position of the wolf receiving and re-broadcasting
     * @return a new Signal with attenuated urgency and incremented hops
     */
    public Signal propagate(Vec3 receiverPos) {
        // Distance from signal origin to this receiver
        double distance = this.origin.distanceTo(receiverPos);

        // Signals die at 32 blocks from origin
        double maxRange = 32.0;

        // Urgency decays by distance + a fixed per-hop factor
        float distanceFactor = (float) Math.max(0.0, 1.0 - distance / maxRange);
        float hopDecay = 0.85f; // Each re-broadcast loses 15% urgency
        float newUrgency = this.urgency * distanceFactor * hopDecay;

        return new Signal(this.type, this.origin, newUrgency, this.hops + 1);
    }

    /**
     * A signal is "alive" if it still has enough urgency to matter
     * AND hasn't been relayed too many times (prevents infinite loops).
     *
     * MAX_HOPS = 5 means a signal can cross at most 5 wolves in a chain.
     * At urgency < 0.1 the signal is too weak to act upon.
     *
     * @return true if this signal should still be propagated
     */
    public boolean isAlive() {
        return this.urgency >= 0.1f && this.hops < 5;
    }

    /**
     * Factory: create a fresh signal at full urgency from a source position.
     * Use this when a wolf ORIGINATES a signal (not re-broadcasting).
     */
    public static Signal create(SignalType type, Vec3 origin, float urgency) {
        return new Signal(type, origin, urgency, 0);
    }
}
