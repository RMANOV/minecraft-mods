package com.erik.bestiary.world;

import com.erik.bestiary.BestiaryMod;
import com.erik.bestiary.registry.ModEntities;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biomes;

/**
 * ModWorldGen — world generation spawning configuration.
 *
 * ★ V3 PATTERN: BiomeModifications.addSpawn for entity spawning ★
 *
 * Pack wolves spawn naturally in FOREST biomes — forested areas suit their
 * pack hunting behavior (terrain creates natural ambush corridors).
 *
 * Spawn parameters:
 * - weight: relative probability vs other mobs in the same category
 *   (wolf packs should be RARE — higher than 1 but less than zombies at ~95)
 * - minGroupSize: minimum per spawn attempt (1 = alpha only, pack spawns via finalizeSpawn)
 * - maxGroupSize: maximum per spawn attempt (1 because the alpha spawns the rest)
 *
 * Note: minGroupSize/maxGroupSize is the NATURAL SPAWN group size.
 * The actual pack formation (2-4 wolves) is handled by PackWolfEntity.finalizeSpawn().
 * Setting max to 1 means one Alpha spawns, then it creates 2-4 packmates itself.
 */
public class ModWorldGen {

    /**
     * Register world gen entries. Called from BestiaryMod.onInitialize().
     */
    public static void register() {
        registerPackWolfSpawning();
        BestiaryMod.LOGGER.info("World gen registered!");
    }

    /**
     * Pack wolves spawn in FOREST biomes — their natural hunting ground.
     *
     * Biome selection:
     * - FOREST: standard forest (primary habitat)
     * - DARK_FOREST: dark woodland (favors pack ambush tactics)
     * - BIRCH_FOREST: birch woodland (secondary range)
     * - TAIGA: pine forests (northern range)
     * - OLD_GROWTH_BIRCH_FOREST: old growth variant
     * - OLD_GROWTH_PINE_TAIGA: pine taiga variant
     *
     * Weight 4 = somewhat rare (zombies are ~95, skeletons ~100, but we want
     * wolves to be uncommon threats — finding a pack should feel special).
     *
     * We spawn only 1 per spawn event because each alpha spawns 2-4 packmates
     * automatically in finalizeSpawn(), giving us packs of 3-5 total wolves.
     */
    private static void registerPackWolfSpawning() {
        BiomeModifications.addSpawn(
                BiomeSelectors.includeByKey(
                        Biomes.FOREST,
                        Biomes.DARK_FOREST,
                        Biomes.BIRCH_FOREST,
                        Biomes.TAIGA,
                        Biomes.OLD_GROWTH_BIRCH_FOREST,
                        Biomes.OLD_GROWTH_PINE_TAIGA
                ),
                MobCategory.MONSTER,
                ModEntities.PACK_WOLF,
                4,    // weight: rare but not impossible
                1,    // minGroupSize: 1 alpha (creates pack in finalizeSpawn)
                1     // maxGroupSize: 1 alpha per spawn event
        );
    }
}
