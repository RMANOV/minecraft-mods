package com.erik.machinaarcana.registry;

import com.erik.machinaarcana.MachinaArcanaMod;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * ★ JAVA CONCEPT: Particle Type Registration ★
 *
 * Particles in Minecraft need two registrations:
 *   1. Server-side: {@link ParticleType} in PARTICLE_TYPE registry (done here)
 *   2. Client-side: a {@link net.minecraft.client.particle.ParticleProvider} registered
 *      in {@link com.erik.machinaarcana.MachinaArcanaClient}
 *
 * {@link SimpleParticleType} is the base for particles that have no custom data —
 * they are just "spawn X particles at Y position with Z velocity".
 * If you need data (e.g., color per-particle), you'd extend ParticleOptions instead.
 *
 * The boolean in {@code new SimpleParticleType(false)} controls whether the particle
 * is allowed when particles are set to "Minimal" — false means it only shows on
 * "All" particle setting.
 */
public class ModParticles {

    /**
     * Mana Stream particle — visible purple/blue sparks flowing between conduits.
     * SimpleParticleType: no per-particle data, just position + velocity.
     */
    public static final SimpleParticleType MANA_STREAM =
            FabricParticleTypes.simple();

    /**
     * Registers particle types with the server registry.
     * Must be called from {@link MachinaArcanaMod#onInitialize()}.
     */
    public static void register() {
        Registry.register(
                BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(MachinaArcanaMod.MOD_ID, "mana_stream"),
                MANA_STREAM
        );

        MachinaArcanaMod.LOGGER.info("ModParticles registered!");
    }
}
