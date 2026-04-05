package com.erik.arcanenaturalis.crystal;

import net.minecraft.core.Direction;

/**
 * ★ JAVA 21 FEATURE: Sealed Interfaces (as a grammar for an L-System) ★
 *
 * An L-SYSTEM (Lindenmayer System, 1968) is a formal grammar for producing
 * self-similar fractal structures. It was originally designed to model plant
 * growth but works perfectly for crystal formations.
 *
 * An L-system has:
 *   Axiom:   the starting string (e.g. "F")
 *   Rules:   string rewriting rules (e.g. "F" → "F[+F]F[-F]F")
 *   Steps:   how many times to apply the rules (iterations = complexity)
 *
 * Here GrowthRule is the SEMANTIC interpretation of L-system characters.
 * Each character in the L-system string maps to a GrowthRule:
 *   'F' → Branch (grow forward)
 *   'X' → Terminate (stop branch)
 *   '[' → Split (fork into two directions)
 *   '+' or '-' → Turn (change direction)
 *
 * WHY SEALED INTERFACE?
 *   The set of crystal growth commands is CLOSED. There are exactly 4 types.
 *   By sealing the interface, the compiler can verify that CrystalGrower's
 *   switch statement handles every possible command. If we add a new variant
 *   later, every switch breaks — forcing us to handle it. This is the
 *   "make illegal states unrepresentable" principle in practice.
 */
public sealed interface GrowthRule
        permits GrowthRule.Branch, GrowthRule.Terminate, GrowthRule.Split, GrowthRule.Turn {

    /**
     * Grow a crystal segment in the given direction for the given length.
     * Each unit of length places one CrystalBlock.
     *
     * @param dir    which direction to grow
     * @param length how many blocks to place
     */
    record Branch(Direction dir, int length) implements GrowthRule {}

    /**
     * End this branch — stop growing, don't place any more blocks.
     * Corresponds to 'X' in many L-system grammars.
     */
    record Terminate() implements GrowthRule {}

    /**
     * Fork this branch into two sub-branches going in different directions.
     * Corresponds to '[' in bracket L-systems.
     *
     * @param left  direction of the first sub-branch
     * @param right direction of the second sub-branch
     */
    record Split(Direction left, Direction right) implements GrowthRule {}

    /**
     * Change the current growth direction.
     * Corresponds to '+' or '-' in L-system notation.
     *
     * @param newDir the new direction to grow toward
     */
    record Turn(Direction newDir) implements GrowthRule {}
}
