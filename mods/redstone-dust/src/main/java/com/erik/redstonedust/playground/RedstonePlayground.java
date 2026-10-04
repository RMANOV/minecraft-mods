package com.erik.redstonedust.playground;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

/**
 * Builds a tiny, ready-made redstone "playground" so Eric can see a working
 * lever -> redstone dust -> redstone lamp circuit immediately, then take it
 * apart and rebuild it himself.
 *
 * <p>Layout (seen from above, building eastwards along +X from the origin):
 *
 * <pre>
 *   base (solid, placed first):   [S][S][S][S]
 *   components (placed on top):   [L][r][r][P]
 * </pre>
 *
 * where S = smooth stone support, L = lever (on the floor, starts OFF),
 * r = redstone dust, P = redstone lamp.
 *
 * <p>When Eric flips the lever, it powers the dust line, the signal travels
 * along the dust and lights the lamp. Flip it off and the lamp goes dark.
 */
public final class RedstonePlayground {

	private RedstonePlayground() {
	}

	/**
	 * Builds the playground starting one block above {@code clicked}.
	 * Must be called on the server side only.
	 *
	 * @param level   the world
	 * @param clicked the block the kit was used on (the platform sits on top of it)
	 */
	public static void build(Level level, BlockPos clicked) {
		// Origin = the air block directly above the clicked surface.
		BlockPos origin = clicked.above();

		// --- Pass 1: lay the full solid support platform FIRST. ---
		// Redstone dust needs a solid block beneath it the moment a neighbour
		// update fires, otherwise it pops off as an item. So all four support
		// blocks go down before any component on top.
		for (int i = 0; i < 4; i++) {
			BlockPos base = origin.relative(Direction.EAST, i);
			setBlock(level, base, Blocks.SMOOTH_STONE.defaultBlockState());
		}

		// --- Pass 2: place the components on top of the platform. ---
		BlockPos leverPos = origin.above();
		BlockPos dust1Pos = leverPos.relative(Direction.EAST, 1);
		BlockPos dust2Pos = leverPos.relative(Direction.EAST, 2);
		BlockPos lampPos = leverPos.relative(Direction.EAST, 3);

		// Lever on the floor, facing south, starting OFF so Eric gets the
		// satisfying "flip it on and the lamp lights up" payoff.
		BlockState lever = Blocks.LEVER.defaultBlockState()
				.setValue(LeverBlock.FACE, AttachFace.FLOOR)
				.setValue(LeverBlock.FACING, Direction.SOUTH)
				.setValue(LeverBlock.POWERED, false);
		setBlock(level, leverPos, lever);

		// Two redstone dust blocks form the wire. Default state + neighbour
		// updates let them auto-connect into a line.
		setBlock(level, dust1Pos, Blocks.REDSTONE_WIRE.defaultBlockState());
		setBlock(level, dust2Pos, Blocks.REDSTONE_WIRE.defaultBlockState());

		// The redstone lamp lights up when the adjacent dust is powered.
		setBlock(level, lampPos, Blocks.REDSTONE_LAMP.defaultBlockState());
	}

	/**
	 * Places a block with full neighbour updates (flag {@link Block#UPDATE_ALL}).
	 * Neighbour updates are what make the redstone dust connect into a line and
	 * the lamp re-evaluate its power — without them the demo silently breaks.
	 */
	private static void setBlock(Level level, BlockPos pos, BlockState state) {
		level.setBlock(pos, state, Block.UPDATE_ALL);
	}
}
