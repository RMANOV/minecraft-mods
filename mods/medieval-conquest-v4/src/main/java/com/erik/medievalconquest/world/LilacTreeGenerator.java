package com.erik.medievalconquest.world;

import com.erik.medievalconquest.MedievalConquestMod;
import com.erik.medievalconquest.registry.ModBlocks;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

public class LilacTreeGenerator {
	private static final int CHUNK_CHANCE = 5;

	public static void register() {
		ServerChunkEvents.CHUNK_GENERATE.register(LilacTreeGenerator::onChunkGenerate);
		MedievalConquestMod.LOGGER.info("Lilac tree generator registered!");
	}

	private static void onChunkGenerate(ServerLevel level, LevelChunk chunk) {
		if (!level.dimension().equals(Level.OVERWORLD)) {
			return;
		}

		ChunkPos chunkPos = chunk.getPos();
		long seed = level.getSeed()
				^ ((long) chunkPos.x * 341873128712L)
				^ ((long) chunkPos.z * 132897987541L);
		RandomSource random = RandomSource.create(seed);

		if (random.nextInt(CHUNK_CHANCE) != 0) {
			return;
		}

		for (int attempt = 0; attempt < 2; attempt++) {
			int x = chunkPos.getBlockX(random.nextInt(16));
			int z = chunkPos.getBlockZ(random.nextInt(16));
			int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
			BlockPos base = new BlockPos(x, y, z);

			if (level.getBiome(base).is(BiomeTags.IS_TAIGA) && canPlaceAt(level, base)) {
				placeLilacTree(level, base, random);
				return;
			}
		}
	}

	private static boolean canPlaceAt(ServerLevel level, BlockPos base) {
		BlockState ground = level.getBlockState(base.below());
		if (!ground.is(BlockTags.DIRT) && !ground.is(Blocks.SNOW) && !ground.is(Blocks.SNOW_BLOCK)) {
			return false;
		}

		for (int y = 0; y <= 7; y++) {
			BlockState state = level.getBlockState(base.above(y));
			if (!canReplace(state)) {
				return false;
			}
		}

		return true;
	}

	private static void placeLilacTree(ServerLevel level, BlockPos base, RandomSource random) {
		int trunkHeight = 4 + random.nextInt(3);
		BlockState log = ModBlocks.LILAC_LOG.defaultBlockState();
		BlockState leaves = ModBlocks.LILAC_LEAVES.defaultBlockState()
				.setValue(LeavesBlock.PERSISTENT, true);

		for (int y = 0; y < trunkHeight; y++) {
			level.setBlock(base.above(y), log, Block.UPDATE_ALL);
		}

		int crownBase = trunkHeight - 2;
		for (int y = crownBase; y <= trunkHeight + 2; y++) {
			int layer = y - crownBase;
			int radius = layer == 0 || layer == 4 ? 1 : 2;

			for (int dx = -radius; dx <= radius; dx++) {
				for (int dz = -radius; dz <= radius; dz++) {
					if (Math.abs(dx) + Math.abs(dz) > radius + 1) {
						continue;
					}

					BlockPos leafPos = base.offset(dx, y, dz);
					if (!leafPos.equals(base.above(y)) && canReplace(level.getBlockState(leafPos))) {
						level.setBlock(leafPos, leaves, Block.UPDATE_ALL);
					}
				}
			}
		}
	}

	private static boolean canReplace(BlockState state) {
		return state.isAir() || state.canBeReplaced() || state.is(BlockTags.LEAVES);
	}
}
