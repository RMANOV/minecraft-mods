package com.erik.arcanenaturalis.world;

import com.erik.arcanenaturalis.ArcaneNaturalisMod;
import com.erik.arcanenaturalis.registry.ModEntities;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biomes;

/**
 * World generation registration for Arcane Naturalis.
 *
 * ★ JAVA 21 FEATURE: Method References (used with BiomeModifications) ★
 *
 * Butterflies spawn as AMBIENT mobs (like bats/fish) in flower-rich biomes.
 * BiomeModifications.addSpawn() registers spawn entries without data pack JSON.
 *
 * Butterfly spawn parameters:
 *   weight=12: moderately common (wolves=8, rabbits=10 for reference)
 *   minGroup=2, maxGroup=6: butterflies naturally come in small flocks!
 *   This works synergistically with FlockingGoal — the initial spawn group
 *   immediately starts flocking together.
 */
public class ModWorldGen {

    public static void register() {
        registerButterflySpawning();
        ArcaneNaturalisMod.LOGGER.info("World gen registered!");
    }

    /**
     * ★ Java 21 Stream + Method References: BiomeSelectors uses lambdas/refs ★
     *
     * Butterflies spawn in biomes with flowers: Flower Forest, Plains, Sunflower Plains,
     * Meadow, Cherry Grove, Forest, Birch Forest.
     */
    private static void registerButterflySpawning() {
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(
                        Biomes.FLOWER_FOREST,
                        Biomes.PLAINS,
                        Biomes.SUNFLOWER_PLAINS,
                        Biomes.MEADOW,
                        Biomes.CHERRY_GROVE,
                        Biomes.FOREST,
                        Biomes.BIRCH_FOREST
                ),
                MobCategory.AMBIENT,
                ModEntities.BUTTERFLY,
                12,  // weight
                2,   // min group size (they flock!)
                6    // max group size
        );
    }
}
