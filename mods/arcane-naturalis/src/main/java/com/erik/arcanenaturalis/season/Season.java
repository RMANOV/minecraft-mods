package com.erik.arcanenaturalis.season;

/**
 * ★ JAVA 21 FEATURE: Sealed Interface as a State Machine ★
 *
 * Season is a sealed interface with 4 record permits, forming a complete
 * algebraic state machine for the mod's seasonal system.
 *
 * WHY SEALED INTERFACE INSTEAD OF ENUM?
 *   - Enums can't carry different data per variant
 *   - This sealed interface lets each season define its OWN foliage and grass colors
 *     as part of the type definition — no switch/case lookup needed
 *   - The compiler knows all 4 seasons exist — pattern matching is exhaustive
 *
 * RECORDS as permits:
 *   Each season is a record — immutable, with auto-generated constructor/equals.
 *   'record Spring() implements Season' — no fields = like enum constant,
 *   but it's a record so it could hold data if needed.
 *
 * COLOR ENCODING:
 *   Colors are ARGB integers where 0xFF000000 = opaque, 0x00RRGGBB bits.
 *   These are passed to Minecraft's foliage/grass color system.
 *   foliageColor() → applied to all leaf blocks in the world
 *   grassColor()   → applied to grass/fern blocks
 */
public sealed interface Season
        permits Season.Spring, Season.Summer, Season.Autumn, Season.Winter {

    /** The foliage (leaf) color for this season as an ARGB integer. */
    int foliageColor();

    /** The grass color for this season as an ARGB integer. */
    int grassColor();

    /** Human-readable name for UI/debugging. */
    String displayName();

    // ── The four seasons ─────────────────────────────────────────────────────

    /**
     * Spring: fresh light green — new growth, soft colors.
     * Foliage: pale yellow-green (0xA8C050)
     * Grass: light green (0x79C05A)
     */
    record Spring() implements Season {
        @Override public int foliageColor() { return 0x59AE30; }
        @Override public int grassColor()   { return 0x79C05A; }
        @Override public String displayName() { return "Spring"; }
    }

    /**
     * Summer: deep rich green — full leaf cover, lush.
     * Foliage: dark green (0x4B9A2A)
     * Grass: vibrant green (0x55C93F)
     */
    record Summer() implements Season {
        @Override public int foliageColor() { return 0x3C9C1C; }
        @Override public int grassColor()   { return 0x55C93F; }
        @Override public String displayName() { return "Summer"; }
    }

    /**
     * Autumn: warm orange and gold tones — deciduous trees turning.
     * Foliage: amber orange (0xE06C1E)
     * Grass: dry yellow-green (0xA2A84B)
     */
    record Autumn() implements Season {
        @Override public int foliageColor() { return 0xD06418; }
        @Override public int grassColor()   { return 0x929A3C; }
        @Override public String displayName() { return "Autumn"; }
    }

    /**
     * Winter: grey-brown bare branches, pale dead grass.
     * Foliage: muted grey (0x8B8B6A) — conifer needles remain
     * Grass: pale straw (0x9E9E5A)
     */
    record Winter() implements Season {
        @Override public int foliageColor() { return 0x7A8060; }
        @Override public int grassColor()   { return 0x8C8C50; }
        @Override public String displayName() { return "Winter"; }
    }
}
