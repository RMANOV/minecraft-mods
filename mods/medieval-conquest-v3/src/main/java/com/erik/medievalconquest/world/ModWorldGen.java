package com.erik.medievalconquest.world;

import com.erik.medievalconquest.MedievalConquestMod;
import com.erik.medievalconquest.registry.ModEntities;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * World generation: dragon spawning + castle structures.
 *
 * Dragons spawn naturally in Plains and Forest biomes (rare).
 * Castles use data-driven Jigsaw structures — add .nbt files to enable.
 */
public class ModWorldGen {

	public static void register() {
		ModStructures.register();
		registerDragonSpawning();
		MedievalConquestMod.LOGGER.info("World gen registered!");
	}

	private static void registerDragonSpawning() {
		// Dragons spawn in Plains and Forest — rare (weight 3, max group 1)
		BiomeModifications.addSpawn(
				BiomeSelectors.includeByKey(Biomes.PLAINS, Biomes.FOREST,
						Biomes.DARK_FOREST, Biomes.BIRCH_FOREST,
						Biomes.TAIGA, Biomes.SNOWY_PLAINS),
				MobCategory.MONSTER,
				ModEntities.OVERWORLD_DRAGON,
				6,   // weight (was 3 — Erik wants more dragons!)
				1,   // min group size
				1    // max group size
		);

		// Register spawn restrictions: surface, dark enough
		// SpawnRestriction.register is data-driven in newer versions
		// The mob will use Monster default spawn rules (darkness check)
	}
}
