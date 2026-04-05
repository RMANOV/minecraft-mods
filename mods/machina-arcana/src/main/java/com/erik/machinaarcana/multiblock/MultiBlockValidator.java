package com.erik.machinaarcana.multiblock;

import com.erik.machinaarcana.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates that a 3×3×3 multi-block structure is correctly formed.
 *
 * Structure requirements:
 *   - One ASSEMBLER_CORE at the provided center position
 *   - ASSEMBLER_FRAME blocks filling all other positions in the 3×3×3 cube
 *   - Total: 1 core + 26 frame blocks = 27 blocks
 *
 * This class is used by {@link AssemblerBuilder} during the {@code .validate()} step.
 * It is intentionally stateless — all methods are pure functions of their inputs —
 * making it easy to test and reason about.
 *
 * ★ JAVA CONCEPT: Separation of Concerns ★
 * Validation logic is extracted from the Builder to keep each class focused.
 * MultiBlockValidator only answers "is this structure valid?" — it does nothing else.
 */
public class MultiBlockValidator {

    // ── Structure Constants ────────────────────────────────────────────────

    /** Half-size of the 3×3×3 structure (distance from center to edge). */
    public static final int RADIUS = 1;

    // ── Validation Entry Point ─────────────────────────────────────────────

    /**
     * Result of a validation attempt.
     * Using a record for compact immutable data transfer.
     *
     * ★ JAVA 16+: Record class ★
     * Records automatically generate: constructor, equals, hashCode, toString,
     * and accessor methods (valid(), failureReason()).
     *
     * @param valid         true if the structure is correctly formed
     * @param failureReason human-readable explanation if invalid, empty if valid
     */
    public record ValidationResult(boolean valid, String failureReason) {
        /** Convenience factory for success. */
        public static ValidationResult success() {
            return new ValidationResult(true, "");
        }

        /** Convenience factory for failure. */
        public static ValidationResult failure(String reason) {
            return new ValidationResult(false, reason);
        }
    }

    /**
     * Validates the 3×3×3 structure centered on {@code corePos}.
     *
     * @param level   the world to check blocks in
     * @param corePos the expected position of the ASSEMBLER_CORE
     * @return a {@link ValidationResult} describing success or the first failure found
     */
    public ValidationResult validate(Level level, BlockPos corePos) {
        // Step 1: Verify the core itself is in place
        Block atCore = level.getBlockState(corePos).getBlock();
        if (atCore != ModBlocks.ASSEMBLER_CORE) {
            return ValidationResult.failure(
                    "No Assembler Core at " + corePos + " (found: " + atCore + ")");
        }

        // Step 2: All other positions in the 3×3×3 must be ASSEMBLER_FRAME
        List<BlockPos> framePositions = getFramePositions(corePos);
        for (BlockPos framePos : framePositions) {
            Block atPos = level.getBlockState(framePos).getBlock();
            if (atPos != ModBlocks.ASSEMBLER_FRAME) {
                return ValidationResult.failure(
                        "Expected Assembler Frame at " + framePos + " (found: " + atPos + ")");
            }
        }

        return ValidationResult.success();
    }

    // ── Position Helpers ───────────────────────────────────────────────────

    /**
     * Returns all 26 frame positions surrounding the core.
     *
     * A 3×3×3 cube has 27 positions total.
     * The center is the core (1 block), so there are 26 frame positions.
     *
     * @param corePos the center position
     * @return list of all non-center positions in the 3×3×3 cube
     */
    public static List<BlockPos> getFramePositions(BlockPos corePos) {
        List<BlockPos> positions = new ArrayList<>(26);
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    // Skip the center (that's the core)
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    positions.add(corePos.offset(dx, dy, dz));
                }
            }
        }
        return positions;
    }

    /**
     * Returns ALL 27 positions in the 3×3×3 cube, including the center.
     *
     * @param corePos the center position
     * @return list of all 27 positions
     */
    public static List<BlockPos> getAllPositions(BlockPos corePos) {
        List<BlockPos> positions = new ArrayList<>(27);
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    positions.add(corePos.offset(dx, dy, dz));
                }
            }
        }
        return positions;
    }
}
