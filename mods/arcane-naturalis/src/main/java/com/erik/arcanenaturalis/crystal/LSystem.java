package com.erik.arcanenaturalis.crystal;

import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ★ JAVA 21 FEATURE: Switch Expressions + Pattern Matching ★
 *
 * SWITCH EXPRESSIONS (finalized Java 14, standard in 21):
 *   The switch can now be used as an EXPRESSION that returns a value:
 *     GrowthRule rule = switch (ch) {
 *         case 'F' -> new GrowthRule.Branch(...);
 *         case 'X' -> new GrowthRule.Terminate();
 *         ...
 *     };
 *
 *   Key differences from old switch STATEMENT:
 *   - Arrow '->' syntax: no fall-through, no break needed
 *   - Returns a value (the whole switch is an expression)
 *   - Exhaustiveness: if all cases are covered, no default is needed
 *   - 'yield' keyword to return from multi-statement cases
 *
 * This class implements a string-rewriting L-System that generates
 * crystal growth instructions as GrowthRule sequences.
 *
 * HOW STRING REWRITING WORKS:
 *   Axiom: "F"
 *   Rule:  F → F[+F]X[-F]F    (replace each 'F' with this string)
 *   Step 0: "F"
 *   Step 1: "F[+F]X[-F]F"
 *   Step 2: "F[+F]X[-F]F[+F[+F]X[-F]F]X[-F[+F]X[-F]F]F[+F]X[-F]F"
 *   ...each iteration multiplies complexity → fractal-like branching
 */
public class LSystem {

    /** The axiom is the starting symbol string. */
    private static final String AXIOM = "F";

    /**
     * L-System rewriting rules.
     * Each character that appears as a key gets replaced by its value.
     * Characters with no rule stay as-is (identity rule).
     */
    private static final Map<Character, String> RULES = Map.of(
            'F', "FF+[+F-F-F]-[-F+F+F]",  // main branch forks with twists
            'X', "F[-X][+X]FX"             // terminator recurses
    );

    /**
     * Apply the L-System rules for the given number of iterations.
     * Each iteration expands the string by substituting each character
     * according to RULES.
     *
     * @param iterations how complex to grow (1=simple, 3=intricate)
     * @return the final symbol string ready for interpretation
     */
    public static String expand(int iterations) {
        String current = AXIOM;
        for (int i = 0; i < iterations; i++) {
            StringBuilder next = new StringBuilder(current.length() * 3);
            for (char c : current.toCharArray()) {
                // If a rule exists for this character, apply it; else keep as-is
                next.append(RULES.getOrDefault(c, String.valueOf(c)));
            }
            current = next.toString();
        }
        return current;
    }

    /**
     * ★ SWITCH EXPRESSION (returns GrowthRule) ★
     *
     * Interprets a single L-System character as a GrowthRule.
     * The switch expression returns a GrowthRule — no intermediate variable needed.
     *
     * Note the concise arrow syntax: each case is a single line.
     * For multi-statement cases, use '{ ... yield result; }' block form.
     *
     * @param ch         the L-System character to interpret
     * @param currentDir the current growth direction (context-dependent)
     * @param random     random source for direction variety
     * @return the corresponding GrowthRule, or null if ch is ignored
     */
    public static GrowthRule interpret(char ch, Direction currentDir, java.util.Random random) {
        return switch (ch) {
            // 'F' = grow forward (Branch) in current direction, 1 block
            case 'F' -> new GrowthRule.Branch(currentDir, 1);

            // 'X' = terminate this branch
            case 'X' -> new GrowthRule.Terminate();

            // '+' = turn right (rotate 90° clockwise around Y)
            case '+' -> new GrowthRule.Turn(rotateCW(currentDir));

            // '-' = turn left (rotate 90° counter-clockwise around Y)
            case '-' -> new GrowthRule.Turn(rotateCCW(currentDir));

            // '[' = fork into two sub-branches
            case '[' -> new GrowthRule.Split(
                    rotateCW(currentDir),
                    rotateCCW(currentDir)
            );

            // ']' = closing bracket — consumed by Split logic, no-op here
            case ']' -> null;

            // All other characters are ignored
            default -> null;
        };
    }

    /**
     * Converts the full expanded L-System string into a list of GrowthRules.
     * Null-rules from ignored characters are filtered out.
     *
     * ★ Java 21: method reference + stream pattern ★
     */
    public static List<GrowthRule> toRules(String lsystem, Direction startDir) {
        var random = new java.util.Random();
        Direction current = startDir;
        List<GrowthRule> rules = new ArrayList<>();

        for (char ch : lsystem.toCharArray()) {
            GrowthRule rule = interpret(ch, current, random);
            if (rule == null) continue;

            // Update current direction if the rule is a Turn
            if (rule instanceof GrowthRule.Turn turn) {
                current = turn.newDir();
            }
            rules.add(rule);
        }
        return rules;
    }

    // ── Direction rotation helpers ──────────────────────────────────────────

    /** Rotates a horizontal direction 90° clockwise (N→E→S→W→N). */
    private static Direction rotateCW(Direction dir) {
        return switch (dir) {
            case NORTH -> Direction.EAST;
            case EAST  -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST  -> Direction.NORTH;
            case UP    -> Direction.EAST;   // tilt forward
            case DOWN  -> Direction.WEST;
        };
    }

    /** Rotates a horizontal direction 90° counter-clockwise (N→W→S→E→N). */
    private static Direction rotateCCW(Direction dir) {
        return switch (dir) {
            case NORTH -> Direction.WEST;
            case WEST  -> Direction.SOUTH;
            case SOUTH -> Direction.EAST;
            case EAST  -> Direction.NORTH;
            case UP    -> Direction.WEST;   // tilt back
            case DOWN  -> Direction.EAST;
        };
    }
}
