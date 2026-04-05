package com.erik.arcanenaturalis.entity.ai;

/**
 * ★ JAVA 21 FEATURE: Records ★
 *
 * A RECORD is a special class form introduced in Java 16 and finalized in Java 21.
 * It is:
 *   - IMMUTABLE: all fields are final, no setters
 *   - CONCISE: one line declares the whole class + auto-generates:
 *       constructor, getters (x(), y(), z()), equals(), hashCode(), toString()
 *   - DATA-ORIENTED: expresses "this is just data" semantically
 *
 * BoidForce represents the composite steering force in the Boids algorithm.
 * In the classic Craig Reynolds boids (1987):
 *   Separation  — steer away from nearby neighbors (avoid crowding)
 *   Alignment   — steer toward average heading of neighbors
 *   Cohesion    — steer toward center of mass of neighbors
 *
 * The final force = separation*w1 + alignment*w2 + cohesion*w3
 *
 * Why a record here?
 *   Forces are naturally immutable mathematical objects. Each boid tick
 *   produces a NEW force rather than mutating an existing one. Records
 *   enforce this intent and eliminate an entire class of mutation bugs.
 */
public record BoidForce(double x, double y, double z) {

    /** The zero force — no steering input. */
    public static final BoidForce ZERO = new BoidForce(0.0, 0.0, 0.0);

    /**
     * Vector addition. Returns a new BoidForce = this + other.
     * Immutability: both original forces are unchanged.
     */
    public BoidForce add(BoidForce other) {
        return new BoidForce(
                this.x + other.x,
                this.y + other.y,
                this.z + other.z
        );
    }

    /**
     * Scalar multiplication. Returns a new BoidForce = this * factor.
     * Used to apply weights: separation * 1.5, alignment * 1.0, cohesion * 0.8
     */
    public BoidForce scale(double factor) {
        return new BoidForce(x * factor, y * factor, z * factor);
    }

    /**
     * Returns the Euclidean magnitude (length) of this force vector.
     * Used to check if a force is meaningful before applying it.
     */
    public double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Returns a unit vector (magnitude=1) pointing in this direction.
     * If magnitude ≈ 0 returns ZERO to avoid division-by-zero.
     */
    public BoidForce normalize() {
        double mag = magnitude();
        if (mag < 0.0001) return ZERO;
        return new BoidForce(x / mag, y / mag, z / mag);
    }

    /**
     * Clamps the magnitude to maxMag without changing direction.
     * Prevents butterflies from accelerating to unrealistic speeds.
     *
     * Pattern: normalize() → scale(min(magnitude, maxMag))
     */
    public BoidForce clampMagnitude(double maxMag) {
        double mag = magnitude();
        if (mag <= maxMag) return this;
        return normalize().scale(maxMag);
    }

    /**
     * Creates a BoidForce from separation between two positions.
     * The force points from 'other' toward 'self', weighted by inverse distance
     * (closer neighbor = stronger push away).
     */
    public static BoidForce separation(
            double selfX, double selfY, double selfZ,
            double otherX, double otherY, double otherZ) {
        double dx = selfX - otherX;
        double dy = selfY - otherY;
        double dz = selfZ - otherZ;
        double distSq = dx * dx + dy * dy + dz * dz;
        if (distSq < 0.0001) return ZERO;
        double invDist = 1.0 / Math.sqrt(distSq);
        // Weight by inverse distance — closer = stronger push
        return new BoidForce(dx * invDist * invDist, dy * invDist * invDist, dz * invDist * invDist);
    }
}
