package com.erik.arcanenaturalis;

import com.erik.arcanenaturalis.registry.ModBlocks;
import com.erik.arcanenaturalis.registry.ModEntities;
import com.erik.arcanenaturalis.registry.ModItems;
import com.erik.arcanenaturalis.registry.ModParticles;
import com.erik.arcanenaturalis.world.ModWorldGen;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod initializer for Arcane Naturalis (Living World).
 *
 * This mod is an educational Java 21 showcase — each feature demonstrates
 * specific modern Java language features through visually impressive gameplay:
 *
 *   Feature 1 — Flocking Butterflies: Records, Sealed Interfaces, Pattern Matching + when guards
 *   Feature 2 — Crystal Growth (L-System): Sealed Interfaces, Switch Expressions
 *   Feature 3 — Magical Particles: Streams, Lambdas, UnaryOperator composition
 *   Feature 4 — Seasonal Foliage: Sealed Interface State Machine, Switch Expressions, Records
 */
public class ArcaneNaturalisMod implements ModInitializer {

    public static final String MOD_ID = "arcanenaturalis";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("=== Arcane Naturalis — Living World loading! ===");

        ModBlocks.register();
        ModItems.register();
        ModEntities.register();
        ModParticles.register();
        ModWorldGen.register();

        LOGGER.info("=== Arcane Naturalis ready! The world lives! ===");
    }
}
