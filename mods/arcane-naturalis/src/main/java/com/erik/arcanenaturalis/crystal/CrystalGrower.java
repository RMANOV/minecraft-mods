package com.erik.arcanenaturalis.crystal;

import com.erik.arcanenaturalis.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * ★ JAVA 21 FEATURE: Pattern Matching for Switch (applying GrowthRules) ★
 *
 * CrystalGrower interprets GrowthRule commands and places blocks in the world.
 * The switch over the sealed GrowthRule interface is EXHAUSTIVE — the compiler
 * verifies all 4 variants (Branch, Terminate, Split, Turn) are handled.
 *
 * PATTERN MATCHING FOR SWITCH:
 *   switch (rule) {
 *       case GrowthRule.Branch b  -> // b.dir(), b.length() are in scope
 *       case GrowthRule.Terminate -> // no data, just stop
 *       case GrowthRule.Split s   -> // s.left(), s.right() in scope
 *       case GrowthRule.Turn t    -> // t.newDir() in scope
 *   }
 *
 * Each 'case TypeName varName ->' binds the typed variable in scope.
 * This replaces:
 *   if (rule instanceof GrowthRule.Branch b) { ... b.dir() ... }
 *   else if (rule instanceof GrowthRule.Split s) { ... s.left() ... }
 *   ...
 */
public class CrystalGrower {

    /** Maximum blocks placed per growth event (prevents world destruction). */
    private static final int MAX_BLOCKS = 32;

    /**
     * Grows a crystal formation starting at 'origin' in 'startDir'.
     * Uses L-System with the given number of iterations.
     *
     * @param level      the game world
     * @param origin     where to start growing
     * @param startDir   initial growth direction (usually UP)
     * @param iterations L-System complexity: 1=simple spike, 2=branched, 3=complex
     */
    public static void grow(Level level, BlockPos origin, Direction startDir, int iterations) {
        if (level.isClientSide()) return;

        // Generate the L-System expansion and convert to rules
        String expanded = LSystem.expand(iterations);
        List<GrowthRule> rules = LSystem.toRules(expanded, startDir);

        // Use a stack (Deque) to handle branching positions
        // When we Split, we push the current position so we can return to it
        Deque<BlockPos> positionStack = new ArrayDeque<>();
        BlockPos current = origin;
        int blocksPlaced = 0;

        for (GrowthRule rule : rules) {
            if (blocksPlaced >= MAX_BLOCKS) break;  // safety limit

            // ★ Pattern Matching Switch — exhaustive over sealed interface ★
            // Each case binds the record components into scope.
            // No 'default' needed: sealed interface guarantees all 4 cases covered.
            switch (rule) {

                case GrowthRule.Branch b -> {
                    // Grow 'b.length()' blocks in direction 'b.dir()'
                    for (int i = 0; i < b.length() && blocksPlaced < MAX_BLOCKS; i++) {
                        current = current.relative(b.dir());
                        if (level.isEmptyBlock(current)) {
                            level.setBlock(current, ModBlocks.CRYSTAL_BLOCK.defaultBlockState(),
                                    /* flags= */ 3);
                            blocksPlaced++;
                        }
                    }
                }

                case GrowthRule.Terminate t -> {
                    // End this branch: pop back to last saved position
                    if (!positionStack.isEmpty()) {
                        current = positionStack.pop();
                    }
                }

                case GrowthRule.Split s -> {
                    // Save current position on the stack
                    // The 'right' branch will be processed after we return
                    positionStack.push(current);
                    // Immediately start the 'left' branch
                    // Right branch will continue from the stack when Terminate pops it
                    // (This is a simplified stack-based L-system interpreter)
                }

                case GrowthRule.Turn t -> {
                    // Change direction for future Branch commands
                    // The direction change is already handled in LSystem.toRules()
                    // so this is a no-op here — the direction is baked into Branch.
                }
            }
        }
    }

    /**
     * Places a single crystal block — used by CrystalSeedBlock for incremental growth.
     *
     * @param level  the world
     * @param pos    position to place the crystal
     * @param dir    direction to grow one block
     */
    public static void growOneBlock(Level level, BlockPos pos, Direction dir) {
        BlockPos target = pos.relative(dir);
        if (level.isEmptyBlock(target)) {
            level.setBlock(target, ModBlocks.CRYSTAL_BLOCK.defaultBlockState(), 3);
        }
    }
}
