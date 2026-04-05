package com.erik.medievalconquest.world;

import com.erik.medievalconquest.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Structure piece that procedurally builds a medieval castle.
 * Adapts CastleGenerator logic to work with the structure generation system,
 * using placeBlock() for chunk-boundary-safe block placement.
 */
public class CastlePiece extends StructurePiece {

	// Castle dimensions (same as CastleGenerator)
	private static final int HALF_SIZE = 12;
	private static final int WALL_HEIGHT = 6;
	private static final int TOWER_HEIGHT = 10;
	private static final int TOWER_RADIUS = 3;

	// Bounding box padding around the castle
	private static final int PADDING = 2;
	private static final int FULL_HALF = HALF_SIZE + PADDING; // 14

	// Block palette
	private static final BlockState WALL = Blocks.STONE_BRICKS.defaultBlockState();
	private static final BlockState WALL_MOSSY = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
	private static final BlockState WALL_CRACKED = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
	private static final BlockState FLOOR = Blocks.STONE_BRICKS.defaultBlockState();
	private static final BlockState FLOOR_PATH = Blocks.GRAVEL.defaultBlockState();
	private static final BlockState TOWER_TOP = Blocks.STONE_BRICK_SLAB.defaultBlockState();
	private static final BlockState MERLON = Blocks.STONE_BRICK_WALL.defaultBlockState();
	private static final BlockState TORCH = Blocks.TORCH.defaultBlockState();
	private static final BlockState BANNER = Blocks.RED_BANNER.defaultBlockState();
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	private static final BlockState CARPET = Blocks.RED_CARPET.defaultBlockState();
	private static final BlockState GLASS = Blocks.GLASS_PANE.defaultBlockState();

	/**
	 * Construction-time constructor — creates the piece with a bounding box.
	 */
	public CastlePiece(int centerX, int groundY, int centerZ) {
		super(ModStructures.CASTLE_PIECE, 0, makeBoundingBox(centerX, groundY, centerZ));
	}

	/**
	 * Deserialization constructor — loads from NBT (for saved worlds).
	 */
	public CastlePiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
		super(ModStructures.CASTLE_PIECE, tag);
	}

	private static BoundingBox makeBoundingBox(int cx, int gy, int cz) {
		return new BoundingBox(
				cx - FULL_HALF, gy - 1, cz - FULL_HALF,
				cx + FULL_HALF, gy + TOWER_HEIGHT + 3, cz + FULL_HALF);
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
		// No additional data — castle is fully procedural from bounding box position
	}

	@Override
	public void postProcess(WorldGenLevel level, StructureManager manager,
							ChunkGenerator generator, RandomSource random,
							BoundingBox chunkBounds, ChunkPos chunkPos, BlockPos pivot) {
		clearArea(level, chunkBounds);
		buildFloor(level, chunkBounds);
		buildWalls(level, chunkBounds);
		buildBattlements(level, chunkBounds);
		buildTowers(level, chunkBounds);
		buildGate(level, chunkBounds);
		buildCourtyard(level, chunkBounds);
		placeTorches(level, chunkBounds);
		placeClaimMarker(level, chunkBounds);
	}

	// Maps castle-relative coordinates to local bounding-box coordinates
	private void castle(WorldGenLevel level, BlockState state, int x, int y, int z, BoundingBox bounds) {
		this.placeBlock(level, state, x + FULL_HALF, y + 1, z + FULL_HALF, bounds);
	}

	private void clearArea(WorldGenLevel level, BoundingBox bounds) {
		for (int x = -HALF_SIZE - 2; x <= HALF_SIZE + 2; x++) {
			for (int z = -HALF_SIZE - 2; z <= HALF_SIZE + 2; z++) {
				for (int y = 1; y <= TOWER_HEIGHT + 3; y++) {
					castle(level, AIR, x, y, z, bounds);
				}
			}
		}
	}

	private void buildFloor(WorldGenLevel level, BoundingBox bounds) {
		for (int x = -HALF_SIZE; x <= HALF_SIZE; x++) {
			for (int z = -HALF_SIZE; z <= HALF_SIZE; z++) {
				castle(level, Blocks.COBBLESTONE.defaultBlockState(), x, -1, z, bounds);
				castle(level, FLOOR, x, 0, z, bounds);
			}
		}
	}

	private void buildWalls(WorldGenLevel level, BoundingBox bounds) {
		for (int y = 1; y <= WALL_HEIGHT; y++) {
			for (int i = -HALF_SIZE; i <= HALF_SIZE; i++) {
				BlockState wallBlock = pickWallBlock(i, y);
				castle(level, wallBlock, i, y, -HALF_SIZE, bounds);
				castle(level, wallBlock, i, y, HALF_SIZE, bounds);
				castle(level, wallBlock, -HALF_SIZE, y, i, bounds);
				castle(level, wallBlock, HALF_SIZE, y, i, bounds);
			}
		}

		// Arrow slits
		for (int i = -HALF_SIZE + 3; i <= HALF_SIZE - 3; i += 4) {
			castle(level, GLASS, i, 4, -HALF_SIZE, bounds);
			castle(level, GLASS, i, 4, HALF_SIZE, bounds);
			castle(level, GLASS, -HALF_SIZE, 4, i, bounds);
			castle(level, GLASS, HALF_SIZE, 4, i, bounds);
		}
	}

	private void buildBattlements(WorldGenLevel level, BoundingBox bounds) {
		int y = WALL_HEIGHT + 1;
		for (int i = -HALF_SIZE; i <= HALF_SIZE; i++) {
			if (i % 2 == 0) {
				castle(level, MERLON, i, y, -HALF_SIZE, bounds);
				castle(level, MERLON, i, y, HALF_SIZE, bounds);
				castle(level, MERLON, -HALF_SIZE, y, i, bounds);
				castle(level, MERLON, HALF_SIZE, y, i, bounds);
			}
		}

		// Walkway
		for (int i = -HALF_SIZE + 1; i <= HALF_SIZE - 1; i++) {
			castle(level, TOWER_TOP, i, WALL_HEIGHT, -HALF_SIZE + 1, bounds);
			castle(level, TOWER_TOP, i, WALL_HEIGHT, HALF_SIZE - 1, bounds);
			castle(level, TOWER_TOP, -HALF_SIZE + 1, WALL_HEIGHT, i, bounds);
			castle(level, TOWER_TOP, HALF_SIZE - 1, WALL_HEIGHT, i, bounds);
		}
	}

	private void buildTowers(WorldGenLevel level, BoundingBox bounds) {
		int[][] corners = {
				{-HALF_SIZE, -HALF_SIZE},
				{HALF_SIZE, -HALF_SIZE},
				{-HALF_SIZE, HALF_SIZE},
				{HALF_SIZE, HALF_SIZE}
		};

		for (int[] corner : corners) {
			int cx = corner[0];
			int cz = corner[1];

			for (int y = 1; y <= TOWER_HEIGHT; y++) {
				for (int dx = -TOWER_RADIUS; dx <= TOWER_RADIUS; dx++) {
					for (int dz = -TOWER_RADIUS; dz <= TOWER_RADIUS; dz++) {
						if (Math.abs(dx) + Math.abs(dz) <= TOWER_RADIUS + 1) {
							boolean isEdge = Math.abs(dx) == TOWER_RADIUS
									|| Math.abs(dz) == TOWER_RADIUS
									|| Math.abs(dx) + Math.abs(dz) == TOWER_RADIUS + 1;
							if (isEdge) {
								castle(level, pickWallBlock(dx + y, y), cx + dx, y, cz + dz, bounds);
							} else if (y == TOWER_HEIGHT) {
								castle(level, FLOOR, cx + dx, y, cz + dz, bounds);
							}
						}
					}
				}
			}

			// Tower battlements
			for (int dx = -TOWER_RADIUS; dx <= TOWER_RADIUS; dx++) {
				for (int dz = -TOWER_RADIUS; dz <= TOWER_RADIUS; dz++) {
					if (Math.abs(dx) + Math.abs(dz) <= TOWER_RADIUS + 1) {
						boolean isEdge = Math.abs(dx) == TOWER_RADIUS
								|| Math.abs(dz) == TOWER_RADIUS
								|| Math.abs(dx) + Math.abs(dz) == TOWER_RADIUS + 1;
						if (isEdge && (dx + dz) % 2 == 0) {
							castle(level, MERLON, cx + dx, TOWER_HEIGHT + 1, cz + dz, bounds);
						}
					}
				}
			}

			// Banner and torch on each tower
			castle(level, BANNER, cx, TOWER_HEIGHT + 1, cz, bounds);
			castle(level, TORCH, cx, TOWER_HEIGHT + 1, cz + 1, bounds);
		}
	}

	private void buildGate(WorldGenLevel level, BoundingBox bounds) {
		int gateZ = HALF_SIZE;
		for (int y = 1; y <= 4; y++) {
			for (int dx = -1; dx <= 1; dx++) {
				castle(level, AIR, dx, y, gateZ, bounds);
			}
		}

		// Gate arch
		castle(level, Blocks.STONE_BRICK_STAIRS.defaultBlockState(), -2, 4, gateZ, bounds);
		castle(level, WALL, 0, 5, gateZ, bounds);
		castle(level, Blocks.STONE_BRICK_STAIRS.defaultBlockState(), 2, 4, gateZ, bounds);

		// Path from gate to center
		for (int z = 0; z <= HALF_SIZE; z++) {
			castle(level, FLOOR_PATH, 0, 0, z, bounds);
			castle(level, FLOOR_PATH, -1, 0, z, bounds);
			castle(level, FLOOR_PATH, 1, 0, z, bounds);
		}
	}

	private void buildCourtyard(WorldGenLevel level, BoundingBox bounds) {
		// Central platform with carpet
		for (int x = -2; x <= 2; x++) {
			for (int z = -2; z <= 2; z++) {
				castle(level, Blocks.POLISHED_ANDESITE.defaultBlockState(), x, 0, z, bounds);
				castle(level, CARPET, x, 1, z, bounds);
			}
		}

		// Well
		castle(level, Blocks.COBBLESTONE.defaultBlockState(), 0, 0, -5, bounds);
		castle(level, Blocks.CAULDRON.defaultBlockState(), 0, 1, -5, bounds);

		// Crafting stations
		castle(level, Blocks.CRAFTING_TABLE.defaultBlockState(), 5, 1, -5, bounds);
		castle(level, Blocks.FURNACE.defaultBlockState(), 6, 1, -5, bounds);
		castle(level, Blocks.CHEST.defaultBlockState(), 7, 1, -5, bounds);

		// Stables
		castle(level, Blocks.HAY_BLOCK.defaultBlockState(), -5, 1, 5, bounds);
		castle(level, Blocks.HAY_BLOCK.defaultBlockState(), -6, 1, 5, bounds);
		castle(level, Blocks.HAY_BLOCK.defaultBlockState(), -5, 2, 5, bounds);
	}

	private void placeTorches(WorldGenLevel level, BoundingBox bounds) {
		for (int i = -HALF_SIZE + 4; i <= HALF_SIZE - 4; i += 5) {
			castle(level, TORCH, i, 3, -HALF_SIZE + 1, bounds);
			castle(level, TORCH, i, 3, HALF_SIZE - 1, bounds);
			castle(level, TORCH, -HALF_SIZE + 1, 3, i, bounds);
			castle(level, TORCH, HALF_SIZE - 1, 3, i, bounds);
		}

		// Courtyard torches
		castle(level, TORCH, 3, 1, 3, bounds);
		castle(level, TORCH, -3, 1, 3, bounds);
		castle(level, TORCH, 3, 1, -3, bounds);
		castle(level, TORCH, -3, 1, -3, bounds);
	}

	private void placeClaimMarker(WorldGenLevel level, BoundingBox bounds) {
		castle(level, AIR, 0, 1, 0, bounds);
		castle(level, ModBlocks.CLAIM_MARKER.defaultBlockState(), 0, 1, 0, bounds);
	}

	private static BlockState pickWallBlock(int x, int y) {
		int hash = (x * 31 + y * 17) & 0xFF;
		if (hash < 30) return WALL_MOSSY;
		if (hash < 50) return WALL_CRACKED;
		return WALL;
	}
}
