package com.erik.arcanenaturalis.registry;

import com.erik.arcanenaturalis.ArcaneNaturalisMod;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * ★ JAVA 21 FEATURE: Method References (used in client registration) ★
 *
 * This registry defines three SimpleParticleType entries:
 *   FIREFLY      — warm yellow glow, spawns in forests and plains at night
 *   AURORA       — green/blue shimmer, spawns in snow and ice biomes
 *   MAGICAL_DUST — purple sparkle, spawns near crystal blocks
 *
 * FabricParticleTypes.simple() creates a SimpleParticleType via the Fabric API
 * (the vanilla constructor is protected in 1.21.x).
 */
public class ModParticles {

    public static final SimpleParticleType FIREFLY =
            FabricParticleTypes.simple();

    public static final SimpleParticleType AURORA =
            FabricParticleTypes.simple();

    public static final SimpleParticleType MAGICAL_DUST =
            FabricParticleTypes.simple();

    public static void register() {
        Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(ArcaneNaturalisMod.MOD_ID, "firefly"),
                FIREFLY);

        Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(ArcaneNaturalisMod.MOD_ID, "aurora"),
                AURORA);

        Registry.register(BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(ArcaneNaturalisMod.MOD_ID, "magical_dust"),
                MAGICAL_DUST);

        ArcaneNaturalisMod.LOGGER.info("Particles registered!");
    }
}
