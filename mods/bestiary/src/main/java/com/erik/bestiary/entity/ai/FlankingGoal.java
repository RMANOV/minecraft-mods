package com.erik.bestiary.entity.ai;

import com.erik.bestiary.entity.PackWolfEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * FlankingGoal — AI goal for FLANKER wolves.
 *
 * Instead of charging straight at the target, Flankers move to a position
 * at a specific ANGLE offset from the direct attack vector.
 *
 * Two flankers at 90° and 270° completely surround the target:
 *   [Target]
 *   Alpha → direct attack from front
 *   Flanker1 at +90° → attacks from left side
 *   Flanker2 at -90° → attacks from right side
 *
 * The flankAngle is stored in the Flanker record (accessible via pattern matching).
 *
 * ─── Geometry ─────────────────────────────────────────────────────────────────
 * Given: target position T, flanker starting at position F
 * Alpha attack vector: normalize(T - Alpha.position)
 * Flank position: rotate the attack vector by flankAngle around Y axis
 *                 scale by FLANK_RADIUS, offset from T
 *
 * We compute this using 2D rotation:
 *   dx' =  dx * cos(angle) - dz * sin(angle)
 *   dz' =  dx * sin(angle) + dz * cos(angle)
 */
public class FlankingGoal extends Goal {

    private static final double FLANK_RADIUS = 5.0; // blocks from target
    private static final double ATTACK_RANGE_SQ = 4.0; // 2 blocks squared

    private final PackWolfEntity flanker;
    private LivingEntity target;
    private Vec3 flankPosition;

    public FlankingGoal(PackWolfEntity flanker) {
        this.flanker = flanker;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    /**
     * Uses instanceof pattern matching to extract flankAngle from Flanker role.
     */
    @Override
    public boolean canUse() {
        if (!(flanker.getPackRole() instanceof PackRole.Flanker(var flankAngle))) {
            return false; // Not a flanker
        }

        LivingEntity currentTarget = flanker.getTarget();
        if (currentTarget == null || !currentTarget.isAlive()) return false;
        if (flanker.getMoraleState().isPanicking()) return false;

        target = currentTarget;
        flankPosition = calculateFlankPosition(target, flankAngle);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null
                && target.isAlive()
                && !flanker.getMoraleState().isPanicking();
    }

    @Override
    public void start() {
        if (target != null && flanker.getPackRole() instanceof PackRole.Flanker(var angle)) {
            flankPosition = calculateFlankPosition(target, angle);
        }
    }

    @Override
    public void tick() {
        if (target == null || !target.isAlive()) return;

        // Update flank position as target moves
        if (flanker.getPackRole() instanceof PackRole.Flanker(var angle)) {
            flankPosition = calculateFlankPosition(target, angle);
        }

        if (flankPosition != null) {
            flanker.getLookControl().setLookAt(target);

            // If not at flank position yet, move there
            double distToFlankSq = flanker.distanceToSqr(flankPosition.x, flankPosition.y, flankPosition.z);
            if (distToFlankSq > 2.0) {
                flanker.getNavigation().moveTo(flankPosition.x, flankPosition.y, flankPosition.z, 1.3);
            } else {
                // At flank position — attack!
                if (flanker.distanceToSqr(target) < ATTACK_RANGE_SQ) {
                    float damage = (float) flanker.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    target.hurt(flanker.damageSources().mobAttack(flanker), damage);
                }
            }
        }
    }

    @Override
    public void stop() {
        target = null;
        flankPosition = null;
        flanker.getNavigation().stop();
    }

    /**
     * Calculate the flank position: FLANK_RADIUS blocks from target at flankAngle offset.
     *
     * Math:
     * 1. Vector from target to flanker (current approach direction)
     * 2. Normalize it
     * 3. Rotate by flankAngle around Y-axis
     * 4. Scale by FLANK_RADIUS
     * 5. Add to target position
     *
     * Using 2D rotation in XZ plane (Y-axis rotation in Minecraft).
     */
    private Vec3 calculateFlankPosition(LivingEntity target, double flankAngle) {
        // Direction vector from target to this wolf (approach direction)
        double dx = flanker.getX() - target.getX();
        double dz = flanker.getZ() - target.getZ();
        double length = Math.sqrt(dx * dx + dz * dz);

        // Normalize (avoid division by zero with small epsilon)
        if (length < 0.1) {
            dx = 1.0;
            dz = 0.0;
        } else {
            dx /= length;
            dz /= length;
        }

        // Rotate by flankAngle (2D rotation matrix around Y-axis)
        double cosA = Math.cos(flankAngle);
        double sinA = Math.sin(flankAngle);
        double rotX = dx * cosA - dz * sinA;
        double rotZ = dx * sinA + dz * cosA;

        // Position is FLANK_RADIUS blocks from target in the rotated direction
        return new Vec3(
                target.getX() + rotX * FLANK_RADIUS,
                target.getY(),
                target.getZ() + rotZ * FLANK_RADIUS
        );
    }
}
