package com.erik.machinaarcana.mana;

/**
 * ★ JAVA 21 FEATURE: Enum as a Type-Safe Constant Set ★
 *
 * Enums in Java are a special class type — each constant (ARCANE, NATURE, VOID)
 * is a singleton instance of the enum class.
 *
 * Why an enum instead of int constants?
 *   - Type safety: you can't pass an invalid value
 *   - Readability: ManaType.ARCANE is clearer than int 0
 *   - Extensibility: add methods and fields per-constant
 *   - Pattern matching: Java 21 switch works beautifully with enums
 *
 * Each ManaType carries display metadata used by the HUD overlay.
 */
public enum ManaType {

    /**
     * Arcane mana — pure magical energy, purple/blue color.
     * Used by the Arcane Assembler for crafting.
     */
    ARCANE("Arcane", 0x8B6FE8),

    /**
     * Nature mana — life energy from plants and animals, green color.
     * Enhances organic processes and growth.
     */
    NATURE("Nature", 0x4CAF50),

    /**
     * Void mana — dark energy from the end, black/dark purple color.
     * Dangerous but extremely powerful for advanced recipes.
     */
    VOID("Void", 0x2D0B4E);

    // ── Instance fields ────────────────────────────────────────────────────

    /** Human-readable display name shown in the HUD. */
    private final String displayName;

    /**
     * RGB color packed as an integer (0xRRGGBB).
     * Used by ManaDisplay.color() to tint the mana bar.
     */
    private final int color;

    // ── Constructor ────────────────────────────────────────────────────────

    /**
     * Enum constructors are always private — called once per constant.
     *
     * @param displayName human-readable name
     * @param color       RGB int (0xRRGGBB)
     */
    ManaType(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }

    // ── Accessors ──────────────────────────────────────────────────────────

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }
}
