package com.erik.arcanenaturalis.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

import java.util.function.UnaryOperator;

/**
 * ★ JAVA 21 FEATURE: UnaryOperator Composition + Method References ★
 *
 * FUNCTIONAL INTERFACES (java.util.function):
 *   UnaryOperator<T> represents a function: T → T
 *   It has a compose() method that creates a new operator that applies
 *   two operators in sequence:
 *     f.compose(g) = f(g(x)) = "apply g first, then f"
 *     f.andThen(g) = g(f(x)) = "apply f first, then g"
 *
 * PIPELINE PATTERN:
 *   We build a movement pipeline by composing multiple small operators:
 *     1. addDrift      — adds a tiny random drift component
 *     2. dampVelocity  — multiplies by 0.95 (air resistance)
 *     3. addBob        — adds a gentle sine wave component
 *
 *   Combined: v → addBob(dampVelocity(addDrift(v)))
 *
 *   This is the functional/declarative approach vs. imperative:
 *     // Imperative:
 *     vx *= 0.95; vx += random * 0.01; vx += sin * 0.005;
 *     // Functional (composable, testable, named):
 *     double vx = pipeline.apply(xd);
 *
 * NOTE: andThen() returns Function<T,T> not UnaryOperator<T> — we wrap with
 * an explicit lambda 'v -> ...' to create a proper UnaryOperator<Double>.
 */
public class FireflyParticle extends SingleQuadParticle {

    // ── Movement pipeline components ──────────────────────────────────────

    /**
     * Slight dampening (95% velocity retained per tick = air resistance).
     * UnaryOperator<Double>: takes a velocity component, returns dampened version.
     */
    private static final UnaryOperator<Double> DAMPEN =
            v -> v * 0.95;

    /** Add a very small random drift (±0.01). Simulates air turbulence. */
    private final UnaryOperator<Double> addDrift =
            v -> v + (random.nextDouble() * 0.02 - 0.01);

    /** Gentle sine-wave bob on Y — fireflies hover and drift up/down. */
    private final UnaryOperator<Double> bobY =
            v -> v + Math.sin(age * 0.15) * 0.003;

    /**
     * The composed movement pipeline for horizontal components (X/Z):
     *   addDrift → then dampen
     * Execution order: addDrift is applied first, then DAMPEN.
     *
     * ★ UnaryOperator.andThen returns Function — wrap in explicit lambda ★
     */
    private final UnaryOperator<Double> horizontalPipeline =
            v -> addDrift.andThen(DAMPEN).apply(v);

    /**
     * Y-axis pipeline: bob up/down, then dampen.
     */
    private final UnaryOperator<Double> verticalPipeline =
            v -> bobY.andThen(DAMPEN).apply(v);

    // ── Particle state ─────────────────────────────────────────────────────

    private FireflyParticle(ClientLevel level, double x, double y, double z,
                            TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite);

        // Warm yellow color for firefly glow
        this.rCol = 1.0f;       // red  = full
        this.gCol = 0.9f;       // green = mostly (→ warm yellow)
        this.bCol = 0.3f;       // blue  = low (→ yellow, not white)
        this.alpha = 0.8f;

        // Lifetime: 3-8 seconds (60-160 ticks at 20 tps)
        this.lifetime = 60 + random.nextInt(100);

        // Start with a tiny random velocity
        this.xd = (random.nextDouble() - 0.5) * 0.04;
        this.yd = (random.nextDouble() - 0.5) * 0.02;
        this.zd = (random.nextDouble() - 0.5) * 0.04;

        // Large, glowy sprite
        this.quadSize = 0.12f;
        this.hasPhysics = false;  // floats freely, no gravity
    }

    @Override
    public void tick() {
        super.tick();

        // ★ Apply the composed movement pipelines ★
        // Each call passes the current velocity component through all pipeline stages
        this.xd = horizontalPipeline.apply(this.xd);
        this.zd = horizontalPipeline.apply(this.zd);
        this.yd = verticalPipeline.apply(this.yd);

        // Fade out in the last 20 ticks
        if (this.age > this.lifetime - 20) {
            this.alpha = Math.max(0.0f, this.alpha - 0.05f);
        }
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    // ── Provider (factory) ─────────────────────────────────────────────────

    /**
     * Provider is the factory interface that Minecraft calls to create particles.
     * Registered in ArcaneNaturalisClient via ParticleFactoryRegistry.
     *
     * ★ Method reference usage:
     *   ParticleFactoryRegistry.register(FIREFLY, FireflyParticle.Provider::new)
     *   → calls new Provider(sprites) for each sprite set
     */
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed,
                                       RandomSource random) {
            return new FireflyParticle(level, x, y, z, sprites.get(random));
        }
    }
}
