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
 * ★ JAVA 21 FEATURE: Method References + Functional Composition ★
 *
 * MagicalDustParticle demonstrates combining NAMED METHOD REFERENCES with
 * UnaryOperator to create a reusable, composable particle physics system.
 *
 * METHOD REFERENCES (Java 8, matured with Java 21 sealed types):
 *   Instead of: v -> MagicalDustParticle.spiral(v, age)
 *   We write:   this::spiralX   (bound method reference)
 *
 *   Types of method references:
 *     ClassName::staticMethod    — static, no instance
 *     instance::instanceMethod   — bound to specific instance
 *     ClassName::instanceMethod  — unbound, takes instance as first arg
 *     ClassName::new             — constructor reference
 *
 * The dust particles near crystals spiral slowly around them —
 * simulating magical energy being drawn to the crystal.
 *
 * The spiral effect uses:
 *   xd += cos(angle) * 0.005  → circular X force
 *   zd += sin(angle) * 0.005  → circular Z force
 *   yd += rise                 → gentle upward drift
 *
 * NOTE: andThen() returns Function not UnaryOperator — method references
 * satisfy UnaryOperator<Double> directly since the signatures match.
 * However, andThen-chains must be wrapped in explicit lambdas.
 */
public class MagicalDustParticle extends SingleQuadParticle {

    // ── Named movement methods (referenced as method references) ─────────

    /** Dampens velocity — shared across X, Y, Z. */
    private static double dampen(double v) {
        return v * 0.92;
    }

    /** Spirals X component — bound method reference captures 'age'. */
    private double spiralX(double v) {
        double angle = age * 0.1;
        return v + Math.cos(angle) * 0.003;
    }

    /** Spirals Z component. */
    private double spiralZ(double v) {
        double angle = age * 0.1;
        return v + Math.sin(angle) * 0.003;
    }

    /** Gentle rise for Y — dust floats up toward crystal tip. */
    private static double rise(double v) {
        return v + 0.004;
    }

    // ── Composed pipelines using method references ─────────────────────
    //
    // Pipelines are built ONCE at construction and cached.
    // ★ Bound method reference: this::spiralX ★
    //   - 'this' is bound at construction time
    //   - spiralX reads this.age — the method is called each tick at invocation
    //   - UnaryOperator<Double> is satisfied by any Double→Double method
    //
    // The pipelines themselves are immutable after construction:
    //   xPipeline.apply(v) → calls this.spiralX(v) which reads current this.age
    //   This means: cache the pipeline, but age is read at call-time. Correct!
    //
    // ★ Static method reference: MagicalDustParticle::dampen ★
    //   No instance needed — dampen is a static method
    //
    // andThen() returns Function — wrap in explicit lambda for UnaryOperator<Double>

    /** X: spiral then dampen. Uses bound instance method ref + static method ref. */
    private final UnaryOperator<Double> xPipeline =
            v -> ((UnaryOperator<Double>) this::spiralX).andThen(MagicalDustParticle::dampen).apply(v);

    /** Z: spiral then dampen (different phase from X via spiralZ). */
    private final UnaryOperator<Double> zPipeline =
            v -> ((UnaryOperator<Double>) this::spiralZ).andThen(MagicalDustParticle::dampen).apply(v);

    /** Y: rise then dampen. Uses static method refs only. */
    private final UnaryOperator<Double> yPipeline =
            v -> ((UnaryOperator<Double>) MagicalDustParticle::rise).andThen(MagicalDustParticle::dampen).apply(v);

    // ── Particle ────────────────────────────────────────────────────────

    private MagicalDustParticle(ClientLevel level, double x, double y, double z,
                                TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite);

        // Purple/violet magic dust color
        this.rCol = 0.7f + random.nextFloat() * 0.3f;
        this.gCol = 0.1f + random.nextFloat() * 0.2f;
        this.bCol = 0.9f + random.nextFloat() * 0.1f;
        this.alpha = 0.7f;

        this.lifetime = 40 + random.nextInt(40);
        this.xd = (random.nextDouble() - 0.5) * 0.05;
        this.yd = random.nextDouble() * 0.03;
        this.zd = (random.nextDouble() - 0.5) * 0.05;
        this.quadSize = 0.08f;
        this.hasPhysics = false;
    }

    @Override
    public void tick() {
        super.tick();

        // ★ Use cached pipelines — age is read at invocation time via this::spiralX ★
        this.xd = xPipeline.apply(this.xd);
        this.zd = zPipeline.apply(this.zd);
        this.yd = yPipeline.apply(this.yd);

        // Sparkle: random alpha flicker
        if (random.nextFloat() < 0.15f) {
            this.alpha = 0.4f + random.nextFloat() * 0.6f;
        }

        if (age > lifetime - 15) {
            this.alpha -= 0.05f;
        }
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

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
            return new MagicalDustParticle(level, x, y, z, sprites.get(random));
        }
    }
}
