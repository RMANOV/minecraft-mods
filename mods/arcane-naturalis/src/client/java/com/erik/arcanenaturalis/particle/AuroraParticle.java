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
 * ★ JAVA 21 FEATURE: UnaryOperator Composition (continued) ★
 *
 * AuroraParticle demonstrates a different pipeline composition pattern:
 * rather than adding random drift, it creates a SINUSOIDAL WAVE motion
 * that makes the aurora shimmer side-to-side.
 *
 * The pipeline here uses:
 *   - A wave generator (sinusoidal displacement per tick)
 *   - Strong dampening (aurora particles float gently)
 *   - Slow rise (constant upward drift)
 *
 * COMPOSITION TYPES:
 *   f.andThen(g)  = g(f(x)) — f first, then g
 *   f.compose(g)  = f(g(x)) — g first, then f (reverse of andThen)
 *
 * AuroraParticle uses .compose() to show the other direction:
 *   riseFirst.compose(dampen) = dampen applied first, then rise added
 *
 * NOTE: andThen()/compose() return Function not UnaryOperator — we wrap
 * with explicit lambdas to produce proper UnaryOperator<Double> values.
 */
public class AuroraParticle extends SingleQuadParticle {

    // Strong dampening — aurora moves very slowly
    private static final UnaryOperator<Double> DAMPEN_STRONG =
            v -> v * 0.88;

    // Slow constant rise
    private static final UnaryOperator<Double> ADD_RISE =
            v -> v + 0.003;

    // Side-to-side wave: set once in constructor, captures 'age' via closure.
    // Each call to waveX.apply(v) reads this.age at invocation time.
    private UnaryOperator<Double> waveX;
    private UnaryOperator<Double> waveZ;

    // Composed pipeline for Y (rise then dampen) — wrapped in lambda to stay UnaryOperator
    private final UnaryOperator<Double> verticalPipeline =
            v -> ADD_RISE.andThen(DAMPEN_STRONG).apply(v);

    private AuroraParticle(ClientLevel level, double x, double y, double z,
                           TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite);

        // Green-blue aurora colors
        this.rCol = 0.1f;
        this.gCol = 0.8f + random.nextFloat() * 0.2f;
        this.bCol = 0.6f + random.nextFloat() * 0.4f;
        this.alpha = 0.6f;

        this.lifetime = 80 + random.nextInt(80);
        this.xd = (random.nextDouble() - 0.5) * 0.02;
        this.yd = 0.01 + random.nextDouble() * 0.02;
        this.zd = (random.nextDouble() - 0.5) * 0.02;
        this.quadSize = 0.18f;
        this.hasPhysics = false;

        // ★ Lambda capturing 'age' field — creates the sinusoidal wave ★
        // Lambdas capture 'this' (via 'age') and 'x','z' (starting position).
        // Wrapped in explicit lambda to produce UnaryOperator<Double> from compose().
        final double startX = x;
        final double startZ = z;
        this.waveX = v -> DAMPEN_STRONG.andThen(
                w -> w + Math.sin(age * 0.08 + startX * 0.3) * 0.002
        ).apply(v);
        this.waveZ = v -> DAMPEN_STRONG.andThen(
                w -> w + Math.cos(age * 0.08 + startZ * 0.3) * 0.002
        ).apply(v);
    }

    @Override
    public void tick() {
        super.tick();

        // ★ Use cached composed pipelines — no new objects created per tick ★
        // waveX is already (dampen → wave) composed at construction:
        //   waveX.apply(v) → waveX(DAMPEN_STRONG(v))
        this.xd = waveX.apply(this.xd);
        this.zd = waveZ.apply(this.zd);
        this.yd = verticalPipeline.apply(this.yd);

        // Pulse alpha — aurora shimmers
        float alphaPulse = (float) (Math.sin(age * 0.2) * 0.15);
        this.alpha = Math.max(0.0f, Math.min(0.9f, 0.6f + alphaPulse));

        // Fade out near end of life
        if (age > lifetime - 20) {
            this.alpha -= 0.03f;
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
            return new AuroraParticle(level, x, y, z, sprites.get(random));
        }
    }
}
