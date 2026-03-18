package com.erik.medievalconquest.world;

import com.erik.medievalconquest.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Procedural castle generator — builds a medieval castle from code.
 * Features: outer walls, 4 corner towers, battlements, gate, courtyard,
 * and a ClaimMarker in the center for ownership.
 */
public class CastleGenerator {

	// Castle dimensions
	private static final int HALF_SIZE = 12;   // 25x25 base
	private static final int WALL_HEIGHT = 6;
	private static final int TOWER_HEIGHT = 10;
	private static final int TOWER_RADIUS = 3;

	// Block palette
	private static final BlockState WALL = Blocks.STONE_BRICKS.defaultBlockState();
	private static final BlockState WALL_MOSSY = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
	private static final BlockState WALL_CRACKED = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
	private static final BlockState FLOOR = Blocks.STONE_BRICKS.defaultBlockState();
	private static final BlockState FLOOR_PATH = Blocks.GRAVEL.defaultBlockState();
	private static final BlockState TOWER_TOP = Blocks.STONE_BRICK_SLAB.defaultBlockState();
	private static final BlockState MERLON = Blocks.STONE_BRICK_WALL.defaultBlockState();
	private static final BlockState ROOF = Blocks.DARK_OAK_PLANKS.defaultBlockState();
	private static final BlockState TORCH = Blocks.TORCH.defaultBlockState();
	private static final BlockState WALL_TORCH = Blocks.WALL_TORCH.defaultBlockState();
	private static final BlockState BANNER = Blocks.RED_BANNER.defaultBlockState();
	private static final BlockState GATE = Blocks.DARK_OAK_FENCE_GATE.defaultBlockState();
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	private static final BlockState CARPET = Blocks.RED_CARPET.defaultBlockState();
	private static final BlockState GLASS = Blocks.GLASS_PANE.defaultBlockState();

	public static void generate(ServerLevel level, BlockPos origin) {
		// Clear the area first
		clearArea(level, origin);

		// Build from bottom up
		buildFloor(level, origin);
		buildWalls(level, origin);
		buildBattlements(level, origin);
		buildTowers(level, origin);
		buildGate(level, origin);
		buildCourtyard(level, origin);
		placeTorches(level, origin);
		placeClaimMarker(level, origin);
	}

	private static void clearArea(ServerLevel level, BlockPos origin) {
		for (int x = -HALF_SIZE - 2; x <= HALF_SIZE + 2; x++) {
			for (int z = -HALF_SIZE - 2; z <= HALF_SIZE + 2; z++) {
				for (int y = 1; y <= TOWER_HEIGHT + 3; y++) {
					level.setBlock(origin.offset(x, y, z), AIR, 2);
				}
			}
		}
	}

	private static void buildFloor(ServerLevel level, BlockPos origin) {
		for (int x = -HALF_SIZE; x <= HALF_SIZE; x++) {
			for (int z = -HALF_SIZE; z <= HALF_SIZE; z++) {
				// Foundation (2 blocks deep)
				level.setBlock(origin.offset(x, -1, z), Blocks.COBBLESTONE.defaultBlockState(), 2);
				level.setBlock(origin.offset(x, 0, z), FLOOR, 2);
			}
		}
	}

	private static void buildWalls(ServerLevel level, BlockPos origin) {
		for (int y = 1; y <= WALL_HEIGHT; y++) {
			for (int i = -HALF_SIZE; i <= HALF_SIZE; i++) {
				// Vary the wall blocks for a weathered look
				BlockState wallBlock = pickWallBlock(level, i, y);

				// North wall
				level.setBlock(origin.offset(i, y, -HALF_SIZE), wallBlock, 2);
				// South wall
				level.setBlock(origin.offset(i, y, HALF_SIZE), wallBlock, 2);
				// West wall
				level.setBlock(origin.offset(-HALF_SIZE, y, i), wallBlock, 2);
				// East wall
				level.setBlock(origin.offset(HALF_SIZE, y, i), wallBlock, 2);
			}
		}

		// Arrow slits (windows) in walls at height 4
		for (int i = -HALF_SIZE + 3; i <= HALF_SIZE - 3; i += 4) {
			level.setBlock(origin.offset(i, 4, -HALF_SIZE), GLASS, 2);
			level.setBlock(origin.offset(i, 4, HALF_SIZE), GLASS, 2);
			level.setBlock(origin.offset(-HALF_SIZE, 4, i), GLASS, 2);
			level.setBlock(origin.offset(HALF_SIZE, 4, i), GLASS, 2);
		}
	}

	private static void buildBattlements(ServerLevel level, BlockPos origin) {
		int y = WALL_HEIGHT + 1;
		for (int i = -HALF_SIZE; i <= HALF_SIZE; i++) {
			// Crenellations — alternating merlons
			if (i % 2 == 0) {
				level.setBlock(origin.offset(i, y, -HALF_SIZE), MERLON, 2);
				level.setBlock(origin.offset(i, y, HALF_SIZE), MERLON, 2);
				level.setBlock(origin.offset(-HALF_SIZE, y, i), MERLON, 2);
				level.setBlock(origin.offset(HALF_SIZE, y, i), MERLON, 2);
			}
		}

		// Walkway on top of walls
		for (int i = -HALF_SIZE + 1; i <= HALF_SIZE - 1; i++) {
			level.setBlock(origin.offset(i, WALL_HEIGHT, -HALF_SIZE + 1), TOWER_TOP, 2);
			level.setBlock(origin.offset(i, WALL_HEIGHT, HALF_SIZE - 1), TOWER_TOP, 2);
			level.setBlock(origin.offset(-HALF_SIZE + 1, WALL_HEIGHT, i), TOWER_TOP, 2);
			level.setBlock(origin.offset(HALF_SIZE - 1, WALL_HEIGHT, i), TOWER_TOP, 2);
		}
	}

	private static void buildTowers(ServerLevel level, BlockPos origin) {
		// 4 corner towers
		int[][] corners = {
				{-HALF_SIZE, -HALF_SIZE},  // NW
				{HALF_SIZE, -HALF_SIZE},   // NE
				{-HALF_SIZE, HALF_SIZE},   // SW
				{HALF_SIZE, HALF_SIZE}     // SE
		};

		for (int[] corner : corners) {
			int cx = corner[0];
			int cz = corner[1];

			for (int y = 1; y <= TOWER_HEIGHT; y++) {
				for (int dx = -TOWER_RADIUS; dx <= TOWER_RADIUS; dx++) {
					for (int dz = -TOWER_RADIUS; dz <= TOWER_RADIUS; dz++) {
						// Circular-ish tower (manhattan distance check)
						if (Math.abs(dx) + Math.abs(dz) <= TOWER_RADIUS + 1) {
							boolean isEdge = Math.abs(dx) == TOWER_RADIUS
									|| Math.abs(dz) == TOWER_RADIUS
									|| Math.abs(dx) + Math.abs(dz) == TOWER_RADIUS + 1;
							if (isEdge) {
								level.setBlock(origin.offset(cx + dx, y, cz + dz),
										pickWallBlock(level, dx + y, y), 2);
							} else if (y == TOWER_HEIGHT) {
								// Tower floor at top
								level.setBlock(origin.offset(cx + dx, y, cz + dz), FLOOR, 2);
							}
						}
					}
				}
			}

			// Tower top — battlements
			for (int dx = -TOWER_RADIUS; dx <= TOWER_RADIUS; dx++) {
				for (int dz = -TOWER_RADIUS; dz <= TOWER_RADIUS; dz++) {
					if (Math.abs(dx) + Math.abs(dz) <= TOWER_RADIUS + 1) {
						boolean isEdge = Math.abs(dx) == TOWER_RADIUS
								|| Math.abs(dz) == TOWER_RADIUS
								|| Math.abs(dx) + Math.abs(dz) == TOWER_RADIUS + 1;
						if (isEdge && (dx + dz) % 2 == 0) {
							level.setBlock(origin.offset(cx + dx, TOWER_HEIGHT + 1, cz + dz),
									MERLON, 2);
						}
					}
				}
			}

			// Banner on top of each tower
			level.setBlock(origin.offset(cx, TOWER_HEIGHT + 1, cz), BANNER, 2);

			// Torch in each tower
			level.setBlock(origin.offset(cx, TOWER_HEIGHT + 1, cz + 1), TORCH, 2);
		}
	}

	private static void buildGate(ServerLevel level, BlockPos origin) {
		// Gate on the south wall — 3 blocks wide, 4 blocks tall
		int gateZ = HALF_SIZE;
		for (int y = 1; y <= 4; y++) {
			for (int dx = -1; dx <= 1; dx++) {
				level.setBlock(origin.offset(dx, y, gateZ), AIR, 2);
			}
		}

		// Gate arch
		level.setBlock(origin.offset(-2, 4, gateZ), Blocks.STONE_BRICK_STAIRS.defaultBlockState(), 2);
		level.setBlock(origin.offset(0, 5, gateZ), WALL, 2);
		level.setBlock(origin.offset(2, 4, gateZ), Blocks.STONE_BRICK_STAIRS.defaultBlockState(), 2);

		// Path from gate to center
		for (int z = 0; z <= HALF_SIZE; z++) {
			level.setBlock(origin.offset(0, 0, z), FLOOR_PATH, 2);
			level.setBlock(origin.offset(-1, 0, z), FLOOR_PATH, 2);
			level.setBlock(origin.offset(1, 0, z), FLOOR_PATH, 2);
		}
	}

	private static void buildCourtyard(ServerLevel level, BlockPos origin) {
		// Central platform with carpet
		for (int x = -2; x <= 2; x++) {
			for (int z = -2; z <= 2; z++) {
				level.setBlock(origin.offset(x, 0, z), Blocks.POLISHED_ANDESITE.defaultBlockState(), 2);
				level.setBlock(origin.offset(x, 1, z), CARPET, 2);
			}
		}

		// Small fountain/well
		level.setBlock(origin.offset(0, 1, -5), Blocks.CAULDRON.defaultBlockState(), 2);
		level.setBlock(origin.offset(0, 0, -5), Blocks.COBBLESTONE.defaultBlockState(), 2);

		// Crafting stations
		level.setBlock(origin.offset(5, 1, -5), Blocks.CRAFTING_TABLE.defaultBlockState(), 2);
		level.setBlock(origin.offset(6, 1, -5), Blocks.FURNACE.defaultBlockState(), 2);
		level.setBlock(origin.offset(7, 1, -5), Blocks.CHEST.defaultBlockState(), 2);

		// Hay bales (stables area)
		level.setBlock(origin.offset(-5, 1, 5), Blocks.HAY_BLOCK.defaultBlockState(), 2);
		level.setBlock(origin.offset(-6, 1, 5), Blocks.HAY_BLOCK.defaultBlockState(), 2);
		level.setBlock(origin.offset(-5, 2, 5), Blocks.HAY_BLOCK.defaultBlockState(), 2);
	}

	private static void placeTorches(ServerLevel level, BlockPos origin) {
		// Torches along walls (inside)
		for (int i = -HALF_SIZE + 4; i <= HALF_SIZE - 4; i += 5) {
			level.setBlock(origin.offset(i, 3, -HALF_SIZE + 1), TORCH, 2);
			level.setBlock(origin.offset(i, 3, HALF_SIZE - 1), TORCH, 2);
			level.setBlock(origin.offset(-HALF_SIZE + 1, 3, i), TORCH, 2);
			level.setBlock(origin.offset(HALF_SIZE - 1, 3, i), TORCH, 2);
		}

		// Courtyard torches
		level.setBlock(origin.offset(3, 1, 3), TORCH, 2);
		level.setBlock(origin.offset(-3, 1, 3), TORCH, 2);
		level.setBlock(origin.offset(3, 1, -3), TORCH, 2);
		level.setBlock(origin.offset(-3, 1, -3), TORCH, 2);
	}

	private static void placeClaimMarker(ServerLevel level, BlockPos origin) {
		// Remove carpet at center, place the claim marker
		level.setBlock(origin.offset(0, 1, 0), AIR, 2);
		level.setBlock(origin.offset(0, 1, 0), ModBlocks.CLAIM_MARKER.defaultBlockState(), 3);
	}

	private static BlockState pickWallBlock(ServerLevel level, int x, int y) {
		// Pseudo-random weathering based on position
		int hash = (x * 31 + y * 17) & 0xFF;
		if (hash < 30) return WALL_MOSSY;
		if (hash < 50) return WALL_CRACKED;
		return WALL;
	}
}
