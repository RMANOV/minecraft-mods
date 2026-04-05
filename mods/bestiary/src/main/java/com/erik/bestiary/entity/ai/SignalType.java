package com.erik.bestiary.entity.ai;

/**
 * ★ JAVA CONCEPT: Enums as Named Constants with Behavior ★
 *
 * SignalType defines WHAT a signal means. Wolves communicate via signals —
 * like a biological telegraph system where urgency and distance determine
 * whether the signal reaches the intended recipient.
 *
 * Enum choice over sealed interface here because:
 * - No per-variant state needed (all variants carry no extra data)
 * - Ordinal comparison (DANGER > FOOD in priority) is useful
 * - Can be stored/compared efficiently
 *
 * The ACTUAL signal data (origin, urgency, hops) lives in the Signal record.
 */
public enum SignalType {

    /**
     * DANGER — predator or hostile entity detected.
     * Highest urgency. Triggers RALLY or FLEE based on morale.
     * Propagated by Scout wolves when they detect enemies.
     */
    DANGER,

    /**
     * FOOD — prey spotted.
     * Medium urgency. Triggers pack coordination toward food source.
     * Propagated by any wolf that senses prey.
     */
    FOOD,

    /**
     * RALLY — alpha calling pack to consolidate position.
     * Medium-high urgency. Overrides individual FLEE states.
     * Only Alpha wolves can emit RALLY signals.
     */
    RALLY
}
