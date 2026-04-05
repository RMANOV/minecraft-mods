package com.erik.machinaarcana;

import com.erik.machinaarcana.registry.ModBlocks;
import com.erik.machinaarcana.registry.ModItems;
import com.erik.machinaarcana.registry.ModParticles;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ★ JAVA 21 FEATURE: ModInitializer Entry Point ★
 *
 * This class is the main entry point for the Machina Arcana mod.
 * Fabric calls {@link #onInitialize()} once during game startup.
 *
 * Design pattern: all registry calls delegated to static helpers —
 * single-responsibility principle.
 */
public class MachinaArcanaMod implements ModInitializer {

    /** Mod ID — must match the "id" field in fabric.mod.json exactly. */
    public static final String MOD_ID = "machinaarcana";

    /**
     * SLF4J logger bound to our mod ID.
     * Using a constant makes the logger reusable across the entire mod
     * without recreating it in every class.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("=== Machina Arcana loading! Magical machines awakening... ===");

        // Register order matters: blocks before items (items reference blocks).
        ModBlocks.register();
        ModItems.register();
        ModParticles.register();

        LOGGER.info("=== Machina Arcana ready! The arcane engines hum with power. ===");
    }
}
