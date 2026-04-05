package com.erik.bestiary.morale;

import com.erik.bestiary.entity.ai.MoraleCause;
import com.erik.bestiary.entity.ai.MoraleState;
import com.erik.bestiary.entity.ai.PackRole;

/**
 * ★ JAVA 21 FEATURE: Pattern Matching with Guarded Patterns ★
 *
 * MoraleSystem is the decision engine that converts a MoraleState into
 * a concrete action. It uses the most advanced pattern matching Java 21 offers:
 * deconstruction patterns with 'when' guards.
 *
 * ─── How Guarded Pattern Matching Works ─────────────────────────────────────
 *
 * A standard pattern match checks the TYPE:
 *   case MoraleState s -> ...
 *
 * A deconstruction pattern also UNPACKS the record:
 *   case MoraleState(var f, var c, var cause) -> ...
 *   // Now f, c, cause are in scope as local variables
 *
 * A GUARDED pattern adds a boolean condition with 'when':
 *   case MoraleState(var f, var c, _) when f > 0.8f && c < 0.2f -> FLEE
 *   // Only matches if the structural pattern matches AND the guard is true
 *
 * The '_' wildcard means "I don't need this component's value."
 *
 * ─── Why This Is Better Than if/else ─────────────────────────────────────────
 *
 * Traditional approach:
 *   if (state.fear() > 0.8f && state.courage() < 0.2f) return FLEE;
 *   else if (state.courage() > 0.7f) return FIGHT;
 *   ...
 *
 * With pattern matching, the intent is structural and EXHAUSTIVE.
 * The compiler ensures you've handled every case combination you care about.
 * The code reads like a specification, not imperative instructions.
 */
public class MoraleSystem {

    /**
     * The four possible morale decisions.
     *
     * FIGHT  — charge the enemy, maximize aggression
     * FLEE   — run away, escape combat
     * RALLY  — regroup with packmates, coordinate
     * HOLD   — maintain position, wait for better conditions
     */
    public enum MoraleAction {
        FIGHT, FLEE, RALLY, HOLD
    }

    /**
     * ★ JAVA 21 CORE FEATURE: Pattern Matching switch with Guarded Patterns ★
     *
     * This method is the heart of the educational showcase.
     * Each case demonstrates a different level of pattern complexity:
     *
     * Level 1 — Simple guard on record:
     *   case MoraleState(var f, var c, var cause1) when f > 0.8f &amp;&amp; c < 0.2f -> FLEE
     *   NOTE: In Java 22+, unused vars can use '_': MoraleState(var f, var c, _)
     *
     * Level 2 — Guard on courageous state:
     *   case MoraleState(var f3, var c3, var cause3) when c3 > 0.7f -> FIGHT
     *
     * Level 3 — Nested pattern: guard depends on CAUSE TYPE and its value:
     *   case MoraleState(var f2, var c2, MoraleCause.PackPresence(var n)) when n >= 3 -> RALLY
     *   Here we deconstruct TWO levels: the MoraleState AND the PackPresence inside it!
     *   NOTE: f2, c2 are bound but unused — Java 22+ '_' would be cleaner here
     *
     * Level 4 — Cause type check with value guard:
     *   case MoraleState(var f4, var c4, MoraleCause.AllyDeath(var lostRole)) when f4 > 0.5f -> FLEE
     *
     * Level 5 — Two-level deconstruction with ratio check:
     *   case MoraleState(var f5, var c5, MoraleCause.Outnumbered(var e, var a)) when e > a * 2 -> FLEE
     *
     * ─── Case Ordering Matters! ───────────────────────────────────────────────
     * Cases are tried TOP-TO-BOTTOM. The first matching case wins.
     * Panic (fear > 0.8) is checked before courage — panic overrides bravery.
     *
     * @param state the current morale state of the wolf
     * @return the recommended action for this tick
     */
    public static MoraleAction decide(MoraleState state) {
        return switch (state) {

            // ─── CASE 1: Full panic — maximum fear, minimum courage ────────────
            // Deconstruct MoraleState into f (fear), c (courage), ignored_cause
            // Guard: fear must be critically high AND courage critically low
            // → The wolf has given up fighting; survival instinct takes over
            // NOTE: 'var cause1' bound but unused — we only care about f and c here
            case MoraleState(var f, var c, var cause1) when f > 0.8f && c < 0.2f -> MoraleAction.FLEE;

            // ─── CASE 2: Pack rally — enough packmates present ─────────────────
            // Deeply nested deconstruction: unpack MoraleState, then PackPresence inside it
            // This is TWO levels of deconstruction in one pattern!
            // Guard: at least 3 living packmates → pack instinct overrides individual fear
            // NOTE: f2, c2 are bound but unused — only n (packmate count) matters
            case MoraleState(var f2, var c2, MoraleCause.PackPresence(var n)) when n >= 3 -> MoraleAction.RALLY;

            // ─── CASE 3: High courage → aggressive attack ─────────────────────
            // Simple guard: if courage is dominant, the wolf fights
            // Note: this comes AFTER panic check — a panicking wolf won't fight
            // NOTE: f3, cause3 bound but unused — only c3 (courage) drives this check
            case MoraleState(var f3, var c3, var cause3) when c3 > 0.7f -> MoraleAction.FIGHT;

            // ─── CASE 4: Ally death with significant fear ─────────────────────
            // Matches on CAUSE TYPE (AllyDeath) AND checks fear level via guard
            // The AllyDeath role is bound but we only need to know it IS an ally death
            // → Witnessing pack member death + already scared = flee
            case MoraleState(var f4, var c4, MoraleCause.AllyDeath(var lostRole)) when f4 > 0.5f -> MoraleAction.FLEE;

            // ─── CASE 5: Heavily outnumbered ─────────────────────────────────
            // Deconstruct Outnumbered to get enemy count (e) and ally count (a)
            // Guard: enemies outnumber allies by MORE THAN 2:1
            // → Even a brave wolf recognizes impossible odds
            case MoraleState(var f5, var c5, MoraleCause.Outnumbered(var e, var a)) when e > a * 2 -> MoraleAction.FLEE;

            // ─── CASE 6: Player in heavy armor ────────────────────────────────
            // Pattern matches on PlayerArmor cause type, extracts armorValue
            // If the target seems invincible, hold position and wait
            case MoraleState(var f6, var c6, MoraleCause.PlayerArmor(var armor)) when armor > 20.0f -> MoraleAction.HOLD;

            // ─── DEFAULT: All other states → hold position ────────────────────
            // This handles all remaining combinations not covered above
            // A 'default' is needed here because the switch is on a non-sealed type
            // (MoraleState is a record, not a sealed interface)
            default -> MoraleAction.HOLD;
        };
    }

    /**
     * ★ JAVA 21 FEATURE: Pattern Matching for Type Checks ★
     *
     * Secondary analysis: determine the urgency/priority of the morale state.
     * Returns a float in [0.0, 1.0] where 1.0 = act immediately.
     *
     * This demonstrates using pattern matching NOT for control flow but for
     * data extraction — reading information out of nested structures.
     */
    public static float getUrgency(MoraleState state) {
        // Extract cause-specific urgency
        return switch (state.cause()) {
            // Injury urgency scales with damage amount
            case MoraleCause.Injury(var damage, var hp) -> Math.min(1.0f, damage / 10.0f);

            // Ally death urgency depends on which role was lost
            // Alpha death = maximum urgency (pack loses its coordinator)
            // PackRole.Alpha alpha: bind the alpha record, test it IS an Alpha type
            case MoraleCause.AllyDeath(PackRole.Alpha alphaRole) -> 0.9f;
            // Other role death = high but not maximum urgency (Scout, Flanker, Guard)
            case MoraleCause.AllyDeath(var otherRole) -> 0.7f;

            // Outnumbered urgency = ratio of enemies to allies
            case MoraleCause.Outnumbered(var enemies, var allies) ->
                    allies > 0 ? Math.min(1.0f, (float) enemies / (allies * 2)) : 1.0f;

            // Heavy armor = medium urgency (dangerous but not immediately lethal)
            case MoraleCause.PlayerArmor(var armor) -> armor / 30.0f;

            // Pack presence reduces urgency (safety in numbers)
            case MoraleCause.PackPresence(var count) -> Math.max(0.0f, 1.0f - count * 0.2f);
        };
    }
}
