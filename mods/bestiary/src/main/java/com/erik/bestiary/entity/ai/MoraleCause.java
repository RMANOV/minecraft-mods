package com.erik.bestiary.entity.ai;

/**
 * ★ JAVA 21 FEATURE: Sealed Interfaces for Exhaustive Cause Modeling ★
 *
 * MoraleCause represents WHY a wolf's morale changed. Like a discriminated union
 * in functional languages (Haskell's Either, Rust's enum with data).
 *
 * Each cause carries the EXACT data needed to reason about it:
 * - Injury carries damage amount AND health percent (context for severity)
 * - AllyDeath carries which role died (losing an alpha is catastrophic)
 * - Outnumbered carries both enemy and ally counts (ratio matters, not absolutes)
 *
 * The sealed constraint means MoraleSystem's pattern matching switch is
 * COMPILER-VERIFIED exhaustive — add a new cause? The switch won't compile
 * until you handle it. This is "making illegal states unrepresentable."
 */
public sealed interface MoraleCause
        permits MoraleCause.Injury,
                MoraleCause.AllyDeath,
                MoraleCause.Outnumbered,
                MoraleCause.PlayerArmor,
                MoraleCause.PackPresence {

    /**
     * Wolf was directly injured.
     *
     * @param damage      how much damage was taken this tick
     * @param healthPercent current HP / maxHP in [0.0, 1.0]
     */
    record Injury(float damage, float healthPercent) implements MoraleCause {}

    /**
     * A packmate was killed.
     *
     * Losing an Alpha is catastrophic (triggers FLEE in most cases).
     * Losing a Scout is less dire but reduces detection capability.
     *
     * @param lostRole the role of the wolf that died
     */
    record AllyDeath(PackRole lostRole) implements MoraleCause {}

    /**
     * The pack is outnumbered by enemies.
     *
     * The ratio (enemyCount / allyCount) is more important than
     * absolute numbers — 2 vs 1 is as scary as 10 vs 5.
     *
     * @param enemyCount number of visible enemy entities
     * @param allyCount  number of living packmates in range
     */
    record Outnumbered(int enemyCount, int allyCount) implements MoraleCause {}

    /**
     * The player/target is wearing heavy armor.
     *
     * A heavily armored target reduces the wolf's confidence — their bites
     * seem to bounce off. High armorValue → more fear.
     *
     * @param armorValue the target's effective armor points (0-30 range)
     */
    record PlayerArmor(float armorValue) implements MoraleCause {}

    /**
     * Pack presence affects morale positively.
     *
     * Wolves are social — more living packmates nearby → more courage.
     * This is the primary RALLY trigger.
     *
     * @param alivePackmates count of living packmates within rally range
     */
    record PackPresence(int alivePackmates) implements MoraleCause {}
}
