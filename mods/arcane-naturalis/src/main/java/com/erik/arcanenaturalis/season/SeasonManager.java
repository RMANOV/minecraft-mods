package com.erik.arcanenaturalis.season;

/**
 * ★ JAVA 21 FEATURE: Switch Expression + Sealed Interface exhaustive matching ★
 *
 * SeasonManager converts Minecraft's game time to the current Season.
 *
 * Minecraft time:
 *   - 1 MC day = 24000 ticks
 *   - Day 0-7 = Spring (8 MC days = 192000 ticks)
 *   - Day 8-15 = Summer
 *   - Day 16-23 = Autumn
 *   - Day 24-31 = Winter
 *   - Day 32 → back to Spring (full cycle = 32 days = 768000 ticks)
 *
 * SWITCH EXPRESSION returns a Season:
 *   getCurrentSeason() uses a switch expression that returns one of the 4 season
 *   sealed variants. The compiler verifies all 4 cases (0,1,2,3) are covered.
 *
 * The season system demonstrates:
 *   - Sealed interface as an algebraic type: only 4 valid states
 *   - Records as data carriers: each season holds its own color values
 *   - Switch expression: concise, no fall-through, returns a value
 */
public class SeasonManager {

    /** Number of Minecraft days per season. */
    public static final int DAYS_PER_SEASON = 8;

    /** Ticks per MC day. */
    public static final long TICKS_PER_DAY = 24000L;

    /** Ticks per season. */
    public static final long TICKS_PER_SEASON = DAYS_PER_SEASON * TICKS_PER_DAY; // 192000

    /** Full cycle: 4 seasons × TICKS_PER_SEASON. */
    public static final long TICKS_PER_CYCLE = 4 * TICKS_PER_SEASON; // 768000

    /**
     * Returns the current season based on the world's total game time.
     *
     * ★ SWITCH EXPRESSION: returns a Season value ★
     *   - Arrow '->' syntax: no fall-through
     *   - Exhaustive: all 4 remainders (0,1,2,3) covered → no default needed
     *     (compiler would warn if a case was missing)
     *
     * @param dayTime the world's getDayTime() or getGameTime()
     * @return the Season currently active
     */
    public static Season getCurrentSeason(long dayTime) {
        // Which season slot (0-3) are we in?
        int seasonIndex = (int) ((dayTime % TICKS_PER_CYCLE) / TICKS_PER_SEASON);

        // ★ Switch expression: concise and exhaustive over all 4 slots ★
        return switch (seasonIndex) {
            case 0 -> new Season.Spring();
            case 1 -> new Season.Summer();
            case 2 -> new Season.Autumn();
            case 3 -> new Season.Winter();
            // This default is unreachable by logic (seasonIndex ∈ {0,1,2,3})
            // but Java requires it because 'int' is not a sealed type
            default -> new Season.Spring();
        };
    }

    /**
     * Returns how many ticks remain in the current season.
     * Useful for fade effects and seasonal weather transitions.
     */
    public static long ticksRemainingInSeason(long dayTime) {
        long positionInCycle = dayTime % TICKS_PER_CYCLE;
        long positionInSeason = positionInCycle % TICKS_PER_SEASON;
        return TICKS_PER_SEASON - positionInSeason;
    }

    /**
     * Returns a 0.0–1.0 progress through the current season.
     * 0.0 = just started, 1.0 = about to change.
     */
    public static double seasonProgress(long dayTime) {
        long positionInCycle = dayTime % TICKS_PER_CYCLE;
        long positionInSeason = positionInCycle % TICKS_PER_SEASON;
        return (double) positionInSeason / TICKS_PER_SEASON;
    }

    /**
     * ★ Pattern Matching in a switch to display season info ★
     *
     * This demonstrates how sealed interface + pattern matching enables
     * type-safe dispatch — each variant is handled specifically.
     */
    public static String getSeasonDescription(Season season) {
        // Pattern matching switch over sealed Season
        return switch (season) {
            case Season.Spring s -> "Spring — new growth awakens, flowers bloom";
            case Season.Summer s -> "Summer — lush full canopy, peak life";
            case Season.Autumn s -> "Autumn — leaves turn gold and orange";
            case Season.Winter s -> "Winter — bare branches, frost on the grass";
        };
    }
}
