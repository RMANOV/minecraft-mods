package com.erik.bestiary.entity.ai;

import com.erik.bestiary.entity.PackWolfEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * PackCoordinatorGoal — AI goal for ALPHA wolves only.
 *
 * The Alpha surveys the area and issues commands to packmates.
 * This goal:
 * 1. Scans for a target (player or hostile mob) nearby
 * 2. Sets the target on the Alpha itself
 * 3. Broadcasts a DANGER signal through the SignalPropagator
 * 4. Commands Flanker wolves to spread around the target
 *
 * ─── Minecraft Goal System ───────────────────────────────────────────────────
 * Goals are polled every tick. canUse() determines if it should start.
 * canContinueToUse() determines if it should keep running.
 * tick() does the work each game tick while the goal is active.
 *
 * FLAG.TARGET means this goal controls the mob's attack target.
 * Other goals that also want FLAG.TARGET will be blocked while this runs.
 */
public class PackCoordinatorGoal extends Goal {

    private final PackWolfEntity alpha;
    private LivingEntity target;
    private int commandCooldown = 0;

    /**
     * @param alpha the Alpha wolf that owns this goal
     */
    public PackCoordinatorGoal(PackWolfEntity alpha) {
        this.alpha = alpha;
        // FLAG.TARGET: this goal sets the attack target
        // FLAG.LOOK: this goal controls where the mob looks
        this.setFlags(EnumSet.of(Flag.TARGET, Flag.LOOK));
    }

    /**
     * This goal can start if the Alpha has packmates AND there's a nearby player.
     */
    @Override
    public boolean canUse() {
        // Don't coordinate if alpha is fleeing (morale check)
        if (alpha.getMoraleState().isPanicking()) return false;

        // Only useful if we have packmates to coordinate
        if (!(alpha.getPackRole() instanceof PackRole.Alpha(var members))) return false;
        if (members.isEmpty()) return false;

        // Look for a nearby player to target
        Player nearestPlayer = alpha.level().getNearestPlayer(alpha, 20.0);
        if (nearestPlayer != null && !nearestPlayer.isCreative()) {
            target = nearestPlayer;
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null
                && target.isAlive()
                && alpha.distanceTo(target) < 32.0
                && !alpha.getMoraleState().isPanicking();
    }

    @Override
    public void start() {
        alpha.setTarget(target);
        commandCooldown = 0;
        broadcastDanger();
    }

    /**
     * Every 20 ticks (1 second), re-issue commands to packmates.
     * This keeps flankers on the correct positions as target moves.
     */
    @Override
    public void tick() {
        if (target == null || !target.isAlive()) return;

        alpha.getLookControl().setLookAt(target);
        commandCooldown++;

        if (commandCooldown >= 20) {
            commandCooldown = 0;
            issuePackCommands();
        }
    }

    @Override
    public void stop() {
        target = null;
        commandCooldown = 0;
    }

    /**
     * Broadcast a DANGER signal through the alpha's SignalPropagator.
     * All registered packmates will receive the signal via their Consumer<Signal> callback.
     */
    private void broadcastDanger() {
        if (target == null) return;
        Signal dangerSignal = Signal.create(
                SignalType.DANGER,
                target.position(),
                0.9f
        );
        alpha.getSignalPropagator().broadcast(dangerSignal);
    }

    /**
     * Issue coordinated commands:
     * 1. Set the same target on all packmates
     * 2. Command Flankers to their assigned angles
     */
    private void issuePackCommands() {
        if (!(alpha.getPackRole() instanceof PackRole.Alpha(var members))) return;

        for (PackWolfEntity packmate : members) {
            if (!packmate.isAlive()) continue;

            // Set the same target
            packmate.setTarget(target);

            // Update packmate morale with pack presence
            int aliveCount = (int) members.stream().filter(LivingEntity::isAlive).count();
            MoraleCause rallyCause = new MoraleCause.PackPresence(aliveCount);
            MoraleState newMorale = packmate.getMoraleState()
                    .withCourage(0.6f + aliveCount * 0.05f)
                    .withCause(rallyCause);
            packmate.setMoraleState(newMorale);
        }

        // Broadcast RALLY signal
        Signal rallySignal = Signal.create(
                SignalType.RALLY,
                alpha.position(),
                0.8f
        );
        alpha.getSignalPropagator().broadcast(rallySignal);
    }
}
