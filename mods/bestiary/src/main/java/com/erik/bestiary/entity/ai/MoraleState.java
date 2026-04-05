package com.erik.bestiary.entity.ai;

/**
 * ★ JAVA 21 FEATURE: Records as Immutable Value Objects ★
 *
 * A record is a special class whose sole purpose is to hold data.
 * The compiler generates: constructor, accessors, equals, hashCode, toString.
 *
 * Key property: records are IMMUTABLE by design (all fields are final).
 * Instead of mutating state, we return a NEW record with changed values.
 * This is the "persistent data structures" pattern from functional programming.
 *
 * Example flow:
 *   MoraleState initial = new MoraleState(0.0f, 0.5f, new MoraleCause.Injury(5f, 0.8f));
 *   MoraleState injured = initial.withFear(0.6f);   // new object!
 *   MoraleState rallied = injured.withCourage(0.8f); // new object!
 *
 * Benefits:
 * 1. Thread-safe by default (no shared mutable state)
 * 2. Easy to snapshot and compare (equals works correctly)
 * 3. Pattern matching deconstructs cleanly: MoraleState(var f, var c, var cause)
 *
 * ─── Record Component Names ──────────────────────────────────────────────────
 * - fear:    [0.0, 1.0] — how scared the wolf is (1.0 = maximum terror)
 * - courage: [0.0, 1.0] — how brave the wolf is (1.0 = fearless charge)
 * - cause:   WHY this morale state exists (used in pattern matching decisions)
 *
 * Note: fear + courage do NOT need to sum to 1.0. A wolf can be both scared AND
 * courageous — that's a wolf that fights despite being afraid (wolf pack morale).
 *
 * @param fear    current fear level [0.0 = calm, 1.0 = panicked]
 * @param courage current courage level [0.0 = cowering, 1.0 = charging]
 * @param cause   the most recent reason morale changed
 */
public record MoraleState(float fear, float courage, MoraleCause cause) {

    /**
     * ★ JAVA 21 FEATURE: Compact Record Constructor with Validation ★
     *
     * The compact constructor runs before the auto-generated one.
     * We use it to clamp values into valid ranges, ensuring invariants hold.
     * This is "design by contract" — invalid states cannot be constructed.
     */
    public MoraleState {
        // Clamp fear and courage to [0.0, 1.0]
        fear    = Math.max(0.0f, Math.min(1.0f, fear));
        courage = Math.max(0.0f, Math.min(1.0f, courage));
        // cause must not be null
        if (cause == null) throw new IllegalArgumentException("MoraleCause cannot be null");
    }

    /**
     * ★ JAVA 21 PATTERN: Wither Methods (Record Copy-with-Change) ★
     *
     * Because records are immutable, "mutation" creates a new record.
     * These "wither" methods are the idiomatic way to update a single field
     * while keeping the others unchanged — similar to Kotlin's data class copy().
     *
     * @param newFear the updated fear value (will be clamped)
     * @return a new MoraleState with updated fear, same courage and cause
     */
    public MoraleState withFear(float newFear) {
        return new MoraleState(newFear, this.courage, this.cause);
    }

    /**
     * Creates a copy with updated courage, keeping fear and cause.
     *
     * @param newCourage the updated courage value (will be clamped)
     * @return a new MoraleState with updated courage
     */
    public MoraleState withCourage(float newCourage) {
        return new MoraleState(this.fear, newCourage, this.cause);
    }

    /**
     * Creates a copy with a new cause, keeping fear and courage.
     * Used when the cause of morale change updates without the values changing.
     *
     * @param newCause the new reason for this morale state
     * @return a new MoraleState with updated cause
     */
    public MoraleState withCause(MoraleCause newCause) {
        return new MoraleState(this.fear, this.courage, newCause);
    }

    /**
     * Convenience factory: create the default "neutral" morale state.
     * Used when a wolf spawns before any events have occurred.
     */
    public static MoraleState neutral() {
        return new MoraleState(0.0f, 0.5f, new MoraleCause.PackPresence(1));
    }

    /**
     * Convenience: check if this wolf is in a panic state.
     * High fear + low courage = fleeing behavior expected.
     */
    public boolean isPanicking() {
        return fear > 0.8f && courage < 0.2f;
    }

    /**
     * Convenience: check if this wolf is rallied (high pack presence felt).
     */
    public boolean isRallied() {
        return courage > 0.7f;
    }
}
