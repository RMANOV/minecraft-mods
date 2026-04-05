package com.erik.arcanenaturalis.entity.ai;

import com.erik.arcanenaturalis.entity.ButterflyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * ★ JAVA 21 FEATURE: Pattern Matching for Switch + 'when' Guards ★
 *
 * PATTERN MATCHING FOR SWITCH (JEP 441, finalized Java 21):
 *   switch (obj) {
 *       case SomeType t when t.field() > 0 -> ...
 *       case OtherType o -> ...
 *   }
 *
 * This combines TYPE TESTING (is it a SomeType?) with CONDITION GUARDS
 * ('when' clause) in a single expression. Before Java 21 this required:
 *   if (obj instanceof SomeType t) { if (t.field() > 0) { ... } }
 *
 * Because FlockState is a SEALED INTERFACE, the switch is EXHAUSTIVE —
 * the compiler verifies all cases are covered. No default needed.
 *
 * ─── BOIDS ALGORITHM (Craig Reynolds, 1987) ──────────────────────────────
 * Three simple rules create emergent flocking behavior:
 *
 *   1. SEPARATION:  Steer away from neighbors that are too close
 *                   Prevents crowding — personal space!
 *
 *   2. ALIGNMENT:   Steer toward the average heading of nearby neighbors
 *                   Makes the flock move in the same direction
 *
 *   3. COHESION:    Steer toward the center of mass of nearby neighbors
 *                   Keeps the flock together as a group
 *
 * Emergence: No single butterfly "knows" about the flock. The global
 * flocking behavior emerges purely from local neighbor interactions.
 * This is a key example of complex systems / emergence in nature.
 */
public class FlockingGoal extends Goal {

    // ── Tuning constants ──────────────────────────────────────────────────
    private static final double SEPARATION_RADIUS = 3.0;   // blocks
    private static final double ALIGNMENT_RADIUS  = 8.0;   // blocks
    private static final double COHESION_RADIUS   = 8.0;   // blocks
    private static final double FLEE_RADIUS       = 6.0;   // player scare distance
    private static final double FLOWER_SEARCH_RADIUS = 12.0;

    private static final double SEPARATION_WEIGHT = 1.5;
    private static final double ALIGNMENT_WEIGHT  = 1.0;
    private static final double COHESION_WEIGHT   = 0.8;

    private static final double MAX_FORCE = 0.6;
    private static final int    IDLE_TICKS = 40;

    private final ButterflyEntity butterfly;

    // ★ The current behavioral state — a sealed interface instance ★
    private FlockState state = new FlockState.Idle(IDLE_TICKS);

    public FlockingGoal(ButterflyEntity butterfly) {
        this.butterfly = butterfly;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return true;   // Always active — butterfly is always doing something
    }

    @Override
    public boolean canContinueToUse() {
        return true;
    }

    @Override
    public void tick() {
        // ★ PATTERN MATCHING SWITCH with 'when' guards ★
        //
        // Each 'case' binds the variable AND checks a condition.
        // The 'when' guard is evaluated AFTER the type check succeeds.
        // This is equivalent to: instanceof check + if-condition, but:
        //   - More readable (all in one expression)
        //   - Exhaustive (compiler checks all FlockState variants are covered)
        //   - Works as both a statement and expression
        //
        // Note: sealed interface → no 'default' needed → compile-time safety!
        state = switch (state) {

            // ── Idle: count down, then transition ─────────────────────────
            case FlockState.Idle idle when idle.isDone() ->
                    // Idle period expired → choose next behavior
                    chooseNextState();

            case FlockState.Idle idle ->
                    // Still idle, just decrement the counter
                    idle.tick();

            // ── Flocking: apply boid forces, check for interrupts ──────────
            case FlockState.Flocking flocking when isPlayerNearby() ->
                    // Player is close! Interrupt flocking → flee immediately
                    new FlockState.Fleeing(nearestPlayerPos(), 0.9f);

            case FlockState.Flocking flocking when hasNearbyFlower() ->
                    // A flower is visible! Switch to feeding
                    new FlockState.Feeding(nearestFlowerPos());

            case FlockState.Flocking flocking -> {
                    // Normal flocking — compute boid forces and move
                    BoidForce force = computeBoidForce();
                    applyForce(force);
                    yield new FlockState.Flocking(force);
            }

            // ── Feeding: fly to flower, feed for a bit ────────────────────
            case FlockState.Feeding feeding when isFlowerGone(feeding.flowerPos()) ->
                    // Flower was broken or we can't see it anymore
                    new FlockState.Idle(IDLE_TICKS);

            case FlockState.Feeding feeding when isAtFlower(feeding.flowerPos()) ->
                    // Arrived at flower — rest here for a while
                    new FlockState.Idle(IDLE_TICKS * 3);

            case FlockState.Feeding feeding -> {
                    // Still flying toward the flower
                    flyToward(feeding.flowerPos().getCenter(), 1.0);
                    yield feeding;  // stay in Feeding state
            }

            // ── Fleeing: run from danger ───────────────────────────────────
            case FlockState.Fleeing fleeing when !isPlayerNearby() ->
                    // Danger has passed — transition back to flocking
                    new FlockState.Flocking(BoidForce.ZERO);

            case FlockState.Fleeing fleeing -> {
                    // Flee away from danger source
                    fleeFrom(fleeing.dangerSource(), fleeing.urgency());
                    yield fleeing;
            }
        };
    }

    // ── State transition logic ─────────────────────────────────────────────

    /**
     * Choose what the butterfly does after idling.
     * ★ Java 21: switch expression (returns a value) ★
     */
    private FlockState chooseNextState() {
        if (isPlayerNearby()) {
            return new FlockState.Fleeing(nearestPlayerPos(), 0.7f);
        }
        if (hasNearbyFlower()) {
            return new FlockState.Feeding(nearestFlowerPos());
        }
        // Default: join or form a flock
        return new FlockState.Flocking(BoidForce.ZERO);
    }

    // ── Boids force computation ────────────────────────────────────────────

    /**
     * Computes the composite boid steering force from all three rules.
     * Each nearby butterfly contributes to separation/alignment/cohesion.
     */
    private BoidForce computeBoidForce() {
        List<ButterflyEntity> neighbors = butterfly.level()
                .getEntitiesOfClass(
                        ButterflyEntity.class,
                        new AABB(butterfly.getX() - COHESION_RADIUS,
                                butterfly.getY() - COHESION_RADIUS,
                                butterfly.getZ() - COHESION_RADIUS,
                                butterfly.getX() + COHESION_RADIUS,
                                butterfly.getY() + COHESION_RADIUS,
                                butterfly.getZ() + COHESION_RADIUS),
                        e -> e != butterfly
                );

        if (neighbors.isEmpty()) {
            // No flock — wander randomly
            return randomWanderForce();
        }

        BoidForce separation = BoidForce.ZERO;
        BoidForce alignment  = BoidForce.ZERO;
        double    cohesionX  = 0, cohesionY = 0, cohesionZ = 0;
        int       cohesionCount = 0;

        for (ButterflyEntity neighbor : neighbors) {
            double dist = butterfly.distanceTo(neighbor);

            // ── Rule 1: Separation ──────────────────────────────────────
            if (dist < SEPARATION_RADIUS) {
                separation = separation.add(BoidForce.separation(
                        butterfly.getX(), butterfly.getY(), butterfly.getZ(),
                        neighbor.getX(), neighbor.getY(), neighbor.getZ()
                ));
            }

            // ── Rule 2: Alignment ───────────────────────────────────────
            if (dist < ALIGNMENT_RADIUS) {
                Vec3 neighborVel = neighbor.getDeltaMovement();
                alignment = alignment.add(new BoidForce(
                        neighborVel.x, neighborVel.y, neighborVel.z));
            }

            // ── Rule 3: Cohesion (accumulate center of mass) ───────────
            if (dist < COHESION_RADIUS) {
                cohesionX += neighbor.getX();
                cohesionY += neighbor.getY();
                cohesionZ += neighbor.getZ();
                cohesionCount++;
            }
        }

        // Cohesion: steer toward center of mass
        BoidForce cohesion = BoidForce.ZERO;
        if (cohesionCount > 0) {
            double cx = cohesionX / cohesionCount - butterfly.getX();
            double cy = cohesionY / cohesionCount - butterfly.getY();
            double cz = cohesionZ / cohesionCount - butterfly.getZ();
            cohesion = new BoidForce(cx, cy, cz).normalize();
        }

        // Combine the three forces with weights
        return separation.scale(SEPARATION_WEIGHT)
                .add(alignment.normalize().scale(ALIGNMENT_WEIGHT))
                .add(cohesion.scale(COHESION_WEIGHT))
                .clampMagnitude(MAX_FORCE);
    }

    private BoidForce randomWanderForce() {
        double rx = (butterfly.getRandom().nextDouble() * 2 - 1) * 0.3;
        double ry = (butterfly.getRandom().nextDouble() * 2 - 1) * 0.1;
        double rz = (butterfly.getRandom().nextDouble() * 2 - 1) * 0.3;
        return new BoidForce(rx, ry, rz);
    }

    // ── Movement helpers ────────────────────────────────────────────────────

    private void applyForce(BoidForce force) {
        if (force.magnitude() < 0.001) return;
        flyToward(new Vec3(
                butterfly.getX() + force.x(),
                butterfly.getY() + force.y() + 1.5,
                butterfly.getZ() + force.z()
        ), 0.5 + force.magnitude());
    }

    private void flyToward(Vec3 target, double speed) {
        butterfly.getMoveControl().setWantedPosition(
                target.x, target.y, target.z, speed);
    }

    private void fleeFrom(Vec3 danger, float urgency) {
        double dx = butterfly.getX() - danger.x;
        double dz = butterfly.getZ() - danger.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 0.01) return;
        double nx = dx / dist;
        double nz = dz / dist;
        double fleeSpeed = 0.5 + urgency;
        butterfly.getMoveControl().setWantedPosition(
                butterfly.getX() + nx * 8,
                butterfly.getY() + 3,
                butterfly.getZ() + nz * 8,
                fleeSpeed
        );
    }

    // ── Sensor helpers ──────────────────────────────────────────────────────

    private boolean isPlayerNearby() {
        return butterfly.level()
                .getNearestPlayer(butterfly, FLEE_RADIUS) != null;
    }

    private Vec3 nearestPlayerPos() {
        var player = butterfly.level().getNearestPlayer(butterfly, FLEE_RADIUS * 2);
        return player != null ? player.position() : butterfly.position();
    }

    private boolean hasNearbyFlower() {
        return !findFlowers().isEmpty();
    }

    private boolean isFlowerGone(BlockPos pos) {
        return !butterfly.level().getBlockState(pos).is(BlockTags.FLOWERS);
    }

    private boolean isAtFlower(BlockPos pos) {
        return butterfly.distanceToSqr(
                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5) < 4.0;
    }

    private BlockPos nearestFlowerPos() {
        List<BlockPos> flowers = findFlowers();
        if (flowers.isEmpty()) return butterfly.blockPosition();
        return flowers.getFirst();
    }

    /**
     * ★ Java 21: Stream + method reference to collect nearby flower positions ★
     */
    private List<BlockPos> findFlowers() {
        BlockPos center = butterfly.blockPosition();
        int r = (int) FLOWER_SEARCH_RADIUS;
        return BlockPos.betweenClosedStream(
                center.offset(-r, -3, -r),
                center.offset(r, 3, r)
        )
        .filter(pos -> butterfly.level().getBlockState(pos).is(BlockTags.FLOWERS))
        .map(BlockPos::immutable)
        .limit(5)
        .toList();
    }
}
