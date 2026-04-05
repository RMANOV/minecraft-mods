package com.erik.bestiary.entity.ai;

import com.erik.bestiary.entity.PackWolfEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * ScoutGoal — AI goal for SCOUT wolves.
 *
 * Scout wolves have a wider detection range than normal wolves.
 * When they detect prey:
 * 1. They DO NOT attack immediately (scouts avoid direct combat)
 * 2. They broadcast a FOOD/DANGER signal to the pack via SignalPropagator
 * 3. They retreat to a "reporting position" near the alpha
 * 4. The alpha then takes over via PackCoordinatorGoal
 *
 * ─── Pattern Matching Usage ──────────────────────────────────────────────────
 * The detectionRange comes from the Scout record component:
 *   if (wolf.getPackRole() instanceof PackRole.Scout(var range))
 * We use instanceof pattern matching to extract the range.
 *
 * This is cleaner than: if (role instanceof PackRole.Scout) { double range = ((PackRole.Scout) role).detectionRange(); }
 */
public class ScoutGoal extends Goal {

    private final PackWolfEntity scout;
    private LivingEntity detectedTarget;
    private int reportCooldown = 0;

    public ScoutGoal(PackWolfEntity scout) {
        this.scout = scout;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    /**
     * Scout checks with its WIDER detection range.
     * Uses instanceof pattern matching to extract detectionRange from Scout role.
     */
    @Override
    public boolean canUse() {
        if (!(scout.getPackRole() instanceof PackRole.Scout(var detectionRange))) {
            return false; // Not a scout
        }

        // Don't scout if panicking
        if (scout.getMoraleState().isPanicking()) return false;

        // Scan with the scout's wider detection range
        Player nearestPlayer = scout.level().getNearestPlayer(scout, detectionRange);
        if (nearestPlayer != null && !nearestPlayer.isCreative()) {
            detectedTarget = nearestPlayer;
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return detectedTarget != null && detectedTarget.isAlive() && reportCooldown < 40;
    }

    @Override
    public void start() {
        reportCooldown = 0;
        // Broadcast detection immediately
        broadcastDetection();
    }

    /**
     * Scout looks toward target but doesn't chase (not FLAG.MOVE).
     * Re-broadcasts every 20 ticks in case the alpha missed the signal.
     */
    @Override
    public void tick() {
        if (detectedTarget == null) return;

        scout.getLookControl().setLookAt(detectedTarget);
        reportCooldown++;

        // Re-broadcast every 20 ticks
        if (reportCooldown % 20 == 0) {
            broadcastDetection();
        }
    }

    @Override
    public void stop() {
        detectedTarget = null;
        reportCooldown = 0;
    }

    /**
     * Broadcast a FOOD or DANGER signal depending on target type.
     * Player → DANGER (threat detected)
     * Non-hostile mob → FOOD (prey detected)
     */
    private void broadcastDetection() {
        if (detectedTarget == null) return;

        SignalType signalType = (detectedTarget instanceof Player) ? SignalType.DANGER : SignalType.FOOD;
        float urgency = (detectedTarget instanceof Player) ? 0.85f : 0.6f;

        Signal signal = Signal.create(signalType, detectedTarget.position(), urgency);
        scout.getSignalPropagator().broadcast(signal);
    }
}
