package com.erik.machinaarcana.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

/**
 * ★ JAVA CONCEPT: Client-side Particle Rendering (MC 1.21.11 API) ★
 *
 * Particles in Minecraft are split across server and client:
 *   - Server: registers the ParticleType (done in ModParticles)
 *   - Client: provides a Factory that creates Particle instances for rendering
 *
 * This class contains both:
 *   1. {@link ManaStreamParticle} itself — the visual particle
 *   2. {@link Factory} — creates new instances when the server requests a particle
 *
 * ★ SingleQuadParticle (MC 1.21.11) ★
 * TextureSheetParticle was replaced by SingleQuadParticle in MC 1.21.11.
 * The constructor now takes a TextureAtlasSprite directly (obtained from SpriteSet).
 *
 * ★ getLayer() (MC 1.21.11) ★
 * getRenderType() was replaced by getLayer() returning SingleQuadParticle.Layer.
 * Use Layer.TRANSLUCENT for alpha-blended particles.
 *
 * ★ ParticleProvider.createParticle (MC 1.21.11) ★
 * The signature now includes RandomSource as the final parameter.
 *
 * ★ Static inner class (Factory) ★
 * The Factory is a static nested class — it doesn't need a reference to the outer
 * instance. Standard Minecraft pattern registered in MachinaArcanaClient.
 */
public class ManaStreamParticle extends SingleQuadParticle {

    // ── Particle Properties ────────────────────────────────────────────────

    /** How fast the particle fades (subtracted from alpha per tick). */
    private static final float FADE_RATE = 0.04f;

    // ── Constructor ────────────────────────────────────────────────────────

    /**
     * Constructs a new mana stream particle.
     *
     * @param level  the client world
     * @param x      spawn X
     * @param y      spawn Y
     * @param z      spawn Z
     * @param xd     X velocity (delta)
     * @param yd     Y velocity (delta)
     * @param zd     Z velocity (delta)
     * @param sprite the specific sprite to use for this particle (from SpriteSet)
     */
    protected ManaStreamParticle(ClientLevel level,
                                  double x, double y, double z,
                                  double xd, double yd, double zd,
                                  TextureAtlasSprite sprite) {
        // SingleQuadParticle(level, x, y, z, xd, yd, zd, TextureAtlasSprite)
        super(level, x, y, z, xd, yd, zd, sprite);

        // Visual appearance — rCol/gCol/bCol/alpha are fields on SingleQuadParticle
        this.rCol = 0.5f; // red channel
        this.gCol = 0.3f; // green channel
        this.bCol = 0.9f; // blue channel → purple-blue color
        this.alpha = 0.8f;
        this.quadSize = 0.4f; // small particle

        // Lifetime in ticks (1 second = 20 ticks)
        this.lifetime = 20 + this.random.nextInt(10);

        // Velocity: slight upward drift + dampened provided velocity
        this.xd = xd * 0.1;
        this.yd = yd * 0.1 + 0.02; // gentle upward float
        this.zd = zd * 0.1;

        // No gravity — mana floats
        this.gravity = 0.0f;
        this.hasPhysics = false;
    }

    // ── Particle Behavior ──────────────────────────────────────────────────

    /**
     * Called every tick to update particle state.
     * Fades out gradually over its lifetime.
     */
    @Override
    public void tick() {
        super.tick();
        // Fade out as the particle ages
        this.alpha = Math.max(0.0f, this.alpha - FADE_RATE);
        // Remove when fully transparent
        if (this.alpha <= 0.0f) {
            this.remove();
        }
    }

    /**
     * Specifies which rendering layer this particle uses.
     * Layer.TRANSLUCENT enables alpha blending (needed for glow effect).
     *
     * In MC 1.21.11, getRenderType() was replaced by getLayer() returning
     * SingleQuadParticle.Layer.
     *
     * @return the render layer
     */
    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    // ── Factory (Static Nested Class) ─────────────────────────────────────

    /**
     * ★ JAVA CONCEPT: Static Nested Class as Factory ★
     *
     * Factory is a static inner class implementing {@link ParticleProvider}.
     * It is static because it needs no access to ManaStreamParticle's instance
     * fields — it only creates NEW instances of the particle.
     *
     * The {@code SpriteSet} is injected at factory construction time by Fabric's
     * {@link net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry},
     * which maps the particle type's JSON (mana_stream.json) to sprites.
     */
    public static class Factory implements ParticleProvider<SimpleParticleType> {

        /** The sprite set for this particle type, loaded from the texture atlas. */
        private final SpriteSet sprites;

        /**
         * Constructor called by Fabric's particle factory registry.
         * Receives the sprite set for our particle's texture.
         *
         * @param sprites the sprite set mapped from particles/mana_stream.json
         */
        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        /**
         * Creates a new mana stream particle when requested by the server (or
         * local commands). Returns null to suppress the particle (e.g., off-screen).
         *
         * In MC 1.21.11, createParticle takes a RandomSource as the final parameter.
         * We use it to pick a random sprite from the sprite set.
         *
         * @param type       the particle type (ignored — we know it's MANA_STREAM)
         * @param level      the client world
         * @param x, y, z    spawn position
         * @param xd, yd, zd initial velocity
         * @param random     the random source for this spawn
         * @return a new ManaStreamParticle, or null to not spawn
         */
        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xd, double yd, double zd,
                                       RandomSource random) {
            return new ManaStreamParticle(level, x, y, z, xd, yd, zd, sprites.get(random));
        }
    }
}
