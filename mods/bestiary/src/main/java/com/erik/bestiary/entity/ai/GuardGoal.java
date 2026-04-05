package com.erik.bestiary.entity.ai;

import com.erik.bestiary.entity.PackWolfEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * GuardGoal — AI goal for GUARD wolves.
 *
 * Guards have a dual behavior:
 * 1. When the protected alpha is at full health → stay close, attack targets
 * 2. When the protected alpha is below 50% health → position between
 *    alpha and any attacker, intercepting damage
 *
 * The guard's activation depends on the ALPHA's health, not the guard's own.
 * This creates emergent pack behavior: the alpha being hurt mobilizes its guards.
 *
 * ─── Pattern Matching ────────────────────────────────────────────────────────
 * Guard role stores a reference to the protectedAlpha:
 *   PackRole.Guard(var alpha) → we have the alpha entity directly
 *
 * No need for a separate "alpha reference" field — the data lives in the role.
 */
public class GuardGoal extends Goal {

    private static final double GUARD_RADIUS = 3.0;
    private static final double INTERCEPT_RADIUS = 2.0;
    private static final float CRITICAL_HP_THRESHOLD = 0.5f;

    private final PackWolfEntity guard;
    private PackWolfEntity protectedAlpha;
    private LivingEntity alphaAttacker;

    public GuardGoal(PackWolfEntity guard) {
        this.guard = guard;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    /**
     * Uses instanceof pattern matching to extract protectedAlpha from Guard role.
     */
    @Override
    public boolean canUse() {
        if (!(guard.getPackRole() instanceof PackRole.Guard(var alpha))) {
            return false; // Not a guard role
        }

        if (!alpha.isAlive()) return false;
        if (guard.getMoraleState().isPanicking()) return false;

        protectedAlpha = alpha;
        // Guard is always active when assigned to a living alpha
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return protectedAlpha != null
                && protectedAlpha.isAlive()
                && !guard.getMoraleState().isPanicking();
    }

    @Override
    public void tick() {
        if (protectedAlpha == null) return;

        float alphaHpRatio = protectedAlpha.getHealth() / protectedAlpha.getMaxHealth();
        alphaAttacker = protectedAlpha.getTarget();

        if (alphaHpRatio < CRITICAL_HP_THRESHOLD && alphaAttacker != null) {
            // CRITICAL MODE: intercept between alpha and its attacker
            interceptAttacker();
        } else {
            // GUARD MODE: stay near the alpha, attack anything targeting the alpha
            guardAlpha();
        }
    }

    @Override
    public void stop() {
        protectedAlpha = null;
        alphaAttacker = null;
        guard.getNavigation().stop();
    }

    /**
     * Stay close to alpha and attack anything threatening the alpha.
     */
    private void guardAlpha() {
        double distToAlphaSq = guard.distanceToSqr(protectedAlpha);

        if (distToAlphaSq > GUARD_RADIUS * GUARD_RADIUS) {
            // Move toward alpha to maintain guard formation
            guard.getNavigation().moveTo(protectedAlpha, 1.2);
        } else if (alphaAttacker != null && alphaAttacker.isAlive()) {
            // In guard radius AND alpha has a target — attack it!
            guard.setTarget(alphaAttacker);
            guard.getLookControl().setLookAt(alphaAttacker);
            if (guard.distanceToSqr(alphaAttacker) < 4.0) {
                float damage = (float) guard.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                alphaAttacker.hurt(guard.damageSources().mobAttack(guard), damage);
            }
        }
    }

    /**
     * Intercept mode: position BETWEEN the alpha and its attacker.
     *
     * Calculates the midpoint between alpha and attacker, then moves there.
     * This places the guard in the line of fire, absorbing attacks.
     */
    private void interceptAttacker() {
        if (alphaAttacker == null) return;

        guard.getLookControl().setLookAt(alphaAttacker);
        guard.setTarget(alphaAttacker);

        // Calculate interception position: 40% from alpha toward attacker
        // (closer to alpha so we actually block the attacker's path)
        Vec3 alphaPos = protectedAlpha.position();
        Vec3 attackerPos = alphaAttacker.position();

        double interceptX = alphaPos.x + (attackerPos.x - alphaPos.x) * 0.4;
        double interceptY = alphaPos.y;
        double interceptZ = alphaPos.z + (attackerPos.z - alphaPos.z) * 0.4;

        double distToInterceptSq = guard.distanceToSqr(interceptX, interceptY, interceptZ);

        if (distToInterceptSq > INTERCEPT_RADIUS * INTERCEPT_RADIUS) {
            // Sprint to intercept position
            guard.getNavigation().moveTo(interceptX, interceptY, interceptZ, 1.5);
        } else {
            // At intercept position — attack the threat
            if (guard.distanceToSqr(alphaAttacker) < 4.0) {
                float damage = (float) guard.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                alphaAttacker.hurt(guard.damageSources().mobAttack(guard), damage);
            }
        }
    }
}
