package com.erik.arcanenaturalis.entity;

import com.erik.arcanenaturalis.entity.ai.FlockingGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * ButterflyEntity — a small ambient flying mob that demonstrates:
 *
 * ★ JAVA 21 FEATURE: (via FlockingGoal)
 *   - Sealed Interface as algebraic state type (FlockState)
 *   - Records as immutable value objects (BoidForce, FlockState variants)
 *   - Pattern Matching Switch with 'when' guards for state transitions
 *
 * Technical notes for 1.21.11:
 *   - Extends PathfinderMob (can use Goals system)
 *   - Uses FlyingMoveControl — smooth aerial movement with bank angles
 *   - Uses FlyingPathNavigation — finds paths through 3D airspace
 *   - setNoGravity(true) — entity is not pulled down each tick
 *   - travel() uses travelFlying() — momentum-based aerial physics
 */
public class ButterflyEntity extends PathfinderMob {

    public ButterflyEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        // FlyingMoveControl(entity, maxTurnDegrees, hoversInPlace)
        // maxTurnDegrees=30 → butterfly banks naturally, not sharp turns
        this.moveControl = new FlyingMoveControl(this, 30, true);
        this.setNoGravity(true);
    }

    /** Replaces the default ground-based navigation with a 3D flying navigator. */
    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        // Priority 1: float if somehow in water (safety)
        this.goalSelector.addGoal(1, new FloatGoal(this));
        // Priority 2: flocking behavior (the main showcase)
        this.goalSelector.addGoal(2, new FlockingGoal(this));
        // Priority 3: occasional look-around when near idle
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    /**
     * Custom movement physics for a flying entity.
     * travelFlying() applies drag (0.91f friction) and momentum.
     * Without this override, PathfinderMob would use ground-walking physics.
     */
    @Override
    public void travel(Vec3 travelVector) {
        if (this.isInWater()) {
            // Basic swimming — don't fly through water
            this.moveRelative(0.02f, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8));
        } else {
            // Standard flying physics
            this.travelFlying(travelVector, 0.91f);
        }
        this.calculateEntityAnimation(false);
    }

    /** Butterflies don't climb vines/ladders. */
    @Override
    public boolean onClimbable() {
        return false;
    }

    /**
     * Defines the mob's base attribute values.
     * Called once during registration via FabricDefaultAttributeRegistry.
     *
     * Health 4.0 = 2 hearts (fragile — like a real butterfly!)
     * Speed 0.3  = walk speed (used as base, flight uses FLYING_SPEED)
     * Flying 0.5 = fast flier, agile in air
     */
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.5)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }
}
