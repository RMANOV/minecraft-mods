package com.erik.bestiary.entity.ai;

import com.erik.bestiary.entity.PackWolfEntity;

import java.util.List;

/**
 * ★ JAVA 21 FEATURE: Sealed Interfaces as Algebraic Data Types ★
 *
 * A sealed interface restricts which classes can implement it.
 * Combined with records, this creates a closed set of types — exactly
 * what algebraic data types (ADTs) offer in functional languages.
 *
 * Think of PackRole as an "enum with data":
 * - Traditional enum: ALPHA, SCOUT, FLANKER, GUARD (no per-variant state)
 * - Sealed interface: each variant is a record with its OWN fields
 *
 * The 'permits' clause is the key — no outside class may implement PackRole.
 * This lets the compiler verify exhaustive pattern matching (no default needed).
 *
 * ─── Design Choice: Why sealed interface vs enum? ───────────────────────────
 * Alpha holds a List<PackWolfEntity>. Scout holds detectionRange (double).
 * These can't fit in a plain enum constant — they need instance state.
 * Sealed interface + records solves this cleanly with zero boilerplate.
 */
public sealed interface PackRole
        permits PackRole.Alpha, PackRole.Scout, PackRole.Flanker, PackRole.Guard {

    /**
     * ALPHA — the pack leader.
     *
     * An Alpha wolf holds references to all its pack members and coordinates
     * their movement. Alpha has the highest HP bonus.
     *
     * Using a record here auto-generates: constructor, getters, equals, hashCode, toString.
     *
     * @param packMembers mutable list of wolves this alpha commands
     */
    record Alpha(List<PackWolfEntity> packMembers) implements PackRole {}

    /**
     * SCOUT — the fastest pack member.
     *
     * Scouts range ahead and detect prey, then signal the pack via the
     * SignalPropagator. Their wider detection range is encoded directly in the type.
     *
     * @param detectionRange how far (in blocks) the scout detects prey
     */
    record Scout(double detectionRange) implements PackRole {}

    /**
     * FLANKER — the surrounding specialist.
     *
     * Flankers circle the target at a given angle offset, making it harder
     * for the prey to escape. Two flankers at 90° and 270° = true surround.
     *
     * @param flankAngle the offset angle (radians) from the direct attack vector
     */
    record Flanker(double flankAngle) implements PackRole {}

    /**
     * GUARD — protects an injured alpha.
     *
     * Guards stay close to the alpha and intercept attackers. They only
     * activate when the alpha's health drops below 50%.
     *
     * @param protectedAlpha the alpha wolf this guard is assigned to protect
     */
    record Guard(PackWolfEntity protectedAlpha) implements PackRole {}

    // ─── Static utility methods using Pattern Matching ───────────────────────

    /**
     * ★ JAVA 21 FEATURE: Pattern Matching in switch Expressions ★
     *
     * This switch expression exhaustively handles ALL variants of PackRole
     * without a default case, because the compiler knows the sealed set.
     *
     * Notice how each arm can directly ACCESS the record components:
     * - Alpha(var members) → we have 'members' in scope
     * - Scout(var range)   → we have 'range' in scope
     *
     * This is "deconstruction pattern matching" — the record is
     * automatically unpacked into its components.
     *
     * @param role the wolf's current role
     * @return flat HP bonus to apply when this role is assigned
     */
    static float getHealthBonus(PackRole role) {
        return switch (role) {
            // Alpha commands the pack — toughest, deserves +10 HP
            case Alpha(var members) -> 10.0f;

            // Scout must stay alive to report — no HP bonus, but fastest
            case Scout(var range) -> 0.0f;

            // Flanker takes hits from ambush angles — moderate HP buffer
            case Flanker(var angle) -> 4.0f;

            // Guard must survive to protect — second highest HP bonus
            case Guard(var alpha) -> 8.0f;
        };
    }

    /**
     * Returns a human-readable name for the role, useful for tooltips and debug.
     *
     * Again: exhaustive switch — no default needed because the sealed interface
     * guarantees only these four cases can ever exist.
     */
    static String getRoleName(PackRole role) {
        return switch (role) {
            case Alpha alpha   -> "Alpha";
            case Scout scout   -> "Scout";
            case Flanker f     -> "Flanker";
            case Guard g       -> "Guard";
        };
    }
}
