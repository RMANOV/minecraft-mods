package com.erik.bestiary;

import com.erik.bestiary.registry.ModEntities;
import com.erik.bestiary.registry.ModItems;
import com.erik.bestiary.registry.ModParticles;
import com.erik.bestiary.world.ModWorldGen;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BestiaryMod — main server-side initializer.
 *
 * ★ V3 PATTERN: exact structure from MedievalConquestMod ★
 *
 * ModInitializer.onInitialize() runs during mod loading:
 * 1. Register entities (must be first — items reference entity types)
 * 2. Register items (spawn egg references PACK_WOLF entity type)
 * 3. Register particles (client-side factories registered in BestiaryClient)
 * 4. Register world gen (biome modifications for spawning)
 *
 * Order matters: entities before items (spawn egg needs entity type).
 *
 * ─── Educational Features in This Mod ────────────────────────────────────────
 * Java 21 features demonstrated:
 *
 * 1. SEALED INTERFACES (PackRole, MoraleCause)
 *    → Closed set of types, compiler-verified exhaustive matching
 *
 * 2. RECORDS (MoraleState, Signal)
 *    → Immutable value objects with deconstruction pattern matching
 *
 * 3. PATTERN MATCHING with GUARDED PATTERNS (MoraleSystem)
 *    → case Type(var x, var y) when condition -> result
 *    → Two-level deconstruction: case Outer(_, _, Inner(var z))
 *
 * 4. GENERIC SEALED INTERFACES (BehaviorNode<T>)
 *    → Type-safe tree composition: Sequence<T>, Selector<T>, Action<T>, Condition<T>
 *
 * 5. OBSERVER PATTERN with FUNCTIONAL INTERFACES (SignalPropagator)
 *    → Consumer<Signal> listeners, broadcast with attenuation
 *    → Method references: wolf::onSignalReceived
 *
 * 6. SWITCH ON SEALED TYPES (registerGoals in PackWolfEntity)
 *    → Exhaustive switch without default case
 */
public class BestiaryMod implements ModInitializer {

    public static final String MOD_ID = "bestiary";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("=== Bestiary: Advanced Mob AI loading! ===");

        // Order: entities first (items/world gen reference them)
        ModEntities.register();
        ModItems.register();
        ModParticles.register();
        ModWorldGen.register();

        LOGGER.info("=== Bestiary ready! The pack hunts tonight! ===");
    }
}
