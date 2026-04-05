package com.erik.machinaarcana.hud;

import com.erik.machinaarcana.mana.ManaType;

/**
 * ★ JAVA 16+ FEATURE: Records as Data Transfer Objects (DTOs) ★
 *
 * A <b>record</b> is a special class for immutable data carriers.
 * Declaring {@code record ManaDisplay(int current, int max, ManaType type)}
 * automatically generates:
 *   - A canonical constructor: {@code new ManaDisplay(current, max, type)}
 *   - Accessor methods: {@code current()}, {@code max()}, {@code type()}
 *   - {@code equals()}, {@code hashCode()}, {@code toString()}
 *   - The record is implicitly {@code final} — cannot be subclassed
 *
 * Why use a record here instead of a plain class?
 *   - Concise: 1 line replaces ~30 lines of boilerplate
 *   - Immutable: no setters, fields are final → thread-safe by default
 *   - Value semantics: two ManaDisplay with same fields are equal
 *   - Clear intent: "this is a data snapshot, not a stateful object"
 *
 * Records can also have additional methods (like percentage() and color() below).
 * They cannot have mutable fields or non-static instance fields beyond the header.
 *
 * @param current  how much mana is stored right now
 * @param max      maximum mana capacity
 * @param type     which type of mana this display represents
 */
public record ManaDisplay(int current, int max, ManaType type) {

    // ── Compact Constructor (validation) ──────────────────────────────────

    /**
     * ★ Compact constructor ★ (records-specific syntax)
     *
     * In a record, you can add a "compact constructor" that runs before
     * the generated canonical constructor assigns fields. This is the place
     * for validation, without having to redeclare the parameters.
     *
     * Here we ensure values are in valid ranges.
     */
    public ManaDisplay {
        if (max <= 0) throw new IllegalArgumentException("max must be positive, got: " + max);
        current = Math.max(0, Math.min(current, max)); // clamp to [0, max]
    }

    // ── Derived Properties ─────────────────────────────────────────────────

    /**
     * Returns mana as a percentage of maximum capacity.
     *
     * ★ Records can have additional methods ★
     * These are computed properties derived from the record's fields.
     * They don't add mutable state — they just compute and return values.
     *
     * @return 0.0 (empty) to 1.0 (full)
     */
    public float percentage() {
        return (float) current / max;
    }

    /**
     * Returns the display color for the mana bar.
     *
     * Delegates to the ManaType enum's color field — demonstrating
     * how enums and records work together cleanly.
     *
     * @return RGB integer (0xRRGGBB) from the ManaType
     */
    public int color() {
        return type.getColor();
    }

    /**
     * Returns a display string like "250 / 1000 Arcane Mana".
     *
     * @return formatted display text
     */
    public String displayText() {
        return current + " / " + max + " " + type.getDisplayName() + " Mana";
    }

    /**
     * Whether the mana is critically low (below 20%).
     * Could be used to pulse the HUD bar red.
     *
     * @return true if below 20%
     */
    public boolean isCriticallyLow() {
        return percentage() < 0.2f;
    }

    /**
     * Whether mana is completely full.
     *
     * @return true if current equals max
     */
    public boolean isFull() {
        return current >= max;
    }
}
