package com.erik.bestiary.registry;

import com.erik.bestiary.BestiaryMod;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * ModParticles — custom particle type registration.
 *
 * Three visual feedback particles for the Bestiary mod:
 *
 * SIGNAL_WAVE   — blue expanding ring when a signal is broadcast
 * FEAR_SWEAT    — blue sweat drops when a wolf is panicking
 * COURAGE_FIRE  — orange sparks when a wolf is rallied/charging
 *
 * Particle types in Fabric 1.21.11:
 * 1. Create a SimpleParticleType (overrideLimit = false)
 * 2. Register via Registry.register + BuiltInRegistries.PARTICLE_TYPE
 * 3. Register the factory on the CLIENT (BestiaryClient)
 * 4. Provide the JSON particle definition in resources/assets/bestiary/particles/
 *
 * SimpleParticleType(boolean overrideLimit):
 * - overrideLimit=false: respects maxParticles setting (recommended)
 *
 * ★ PATTERN: matches arcane-naturalis ModParticles exactly ★
 */
public class ModParticles {

    /** Blue ring — visual signal for pack communication events */
    public static final SimpleParticleType SIGNAL_WAVE =
            FabricParticleTypes.simple();

    /** Blue sweat drops — visual indicator that a wolf is panicking */
    public static final SimpleParticleType FEAR_SWEAT =
            FabricParticleTypes.simple();

    /** Orange fire sparks — visual indicator that a wolf is rallied/charging */
    public static final SimpleParticleType COURAGE_FIRE =
            FabricParticleTypes.simple();

    /**
     * Register all particle types. Called from BestiaryMod.onInitialize().
     */
    public static void register() {
        Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(BestiaryMod.MOD_ID, "signal_wave"),
                SIGNAL_WAVE);

        Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(BestiaryMod.MOD_ID, "fear_sweat"),
                FEAR_SWEAT);

        Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(BestiaryMod.MOD_ID, "courage_fire"),
                COURAGE_FIRE);

        BestiaryMod.LOGGER.info("Particles registered!");
    }
}
