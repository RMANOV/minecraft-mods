package com.erik.medievalconquest.world;

import com.erik.medievalconquest.MedievalConquestMod;
import com.erik.medievalconquest.registry.ModBlocks;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Places small lilac trees sparsely while forest chunks are generated.
 *
 * This is intentionally a light-weight scattered generator: it does not
 * replace the vanilla forest decorator and derives its random seed from the
 * world seed and chunk position, so a chunk gets the same result on reload.
 */
public final class LilacTreeGenerator {
	private static final int CHUNK_CHANCE = 8;
	private static final int MAX_ATTEMPTS_PER_CHUNK = 2;

	private LilacTreeGenerator() {
	}

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

		for (int attempt = 0; attempt < MAX_ATTEMPTS_PER_CHUNK; attempt++) {
			// Keep the complete radius-2 crown inside this event's chunk.  A
			// CHUNK_GENERATE callback must never synchronously load a neighbour.
			int localX = 3 + random.nextInt(10);
			int localZ = 3 + random.nextInt(10);
			int x = chunkPos.getBlockX(localX);
			int z = chunkPos.getBlockZ(localZ);
			int y = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, localX, localZ);
			BlockPos base = new BlockPos(x, y, z);

			if (chunk.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z))
					.is(BiomeTags.IS_FOREST)
					&& canPlaceAt(chunk, base)) {
				placeLilacTree(chunk, base, random);
				return;
			}
		}
	}

	private static boolean canPlaceAt(ChunkAccess chunk, BlockPos base) {
		BlockState ground = chunk.getBlockState(base.below());
		if (!ground.is(BlockTags.DIRT)) {
			return false;
		}

		for (int y = 0; y <= 8; y++) {
			if (!canReplace(chunk.getBlockState(base.above(y)))) {
				return false;
			}
		}

		return true;
	}

	private static void placeLilacTree(ChunkAccess chunk, BlockPos base, RandomSource random) {
		int trunkHeight = 4 + random.nextInt(3);
		BlockState log = ModBlocks.LILAC_LOG.defaultBlockState();
		BlockState leaves = ModBlocks.LILAC_LEAVES.defaultBlockState()
				.setValue(LeavesBlock.PERSISTENT, true);
		BlockState floweringLeaves = ModBlocks.LILAC_FLOWERING_LEAVES.defaultBlockState()
				.setValue(LeavesBlock.PERSISTENT, true);
		boolean floweringLeafPlaced = false;

		for (int y = 0; y < trunkHeight; y++) {
			chunk.setBlockState(base.above(y), log);
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
					if (leafPos.equals(base.above(y)) || !canReplace(chunk.getBlockState(leafPos))) {
						continue;
					}

					boolean flowering = random.nextFloat() < 0.30f;
					chunk.setBlockState(leafPos, flowering ? floweringLeaves : leaves);
					floweringLeafPlaced |= flowering;
				}
			}
		}

		// Keep the requested pink flowering feature visible even on a rare roll
		// that otherwise produced no flowering leaves.
		if (!floweringLeafPlaced) {
			BlockPos fallback = base.above(trunkHeight + 1);
			if (canReplace(chunk.getBlockState(fallback))) {
				chunk.setBlockState(fallback, floweringLeaves);
			}
		}
	}

	private static boolean canReplace(BlockState state) {
		return state.isAir()
				|| state.canBeReplaced()
				|| state.is(BlockTags.LEAVES)
				|| state.is(ModBlocks.LILAC_LEAVES)
				|| state.is(ModBlocks.LILAC_FLOWERING_LEAVES);
	}
}
