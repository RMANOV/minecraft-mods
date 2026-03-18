package com.erik.medievalconquest.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * Custom structure that spawns castles in the world like villages.
 * Uses data-driven placement (see worldgen/structure/castle.json and structure_set/castles.json).
 */
public class CastleStructure extends Structure {

	public static final MapCodec<CastleStructure> CODEC = simpleCodec(CastleStructure::new);

	public CastleStructure(StructureSettings settings) {
		super(settings);
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		return onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG, builder -> {
			int cx = context.chunkPos().getMiddleBlockX();
			int cz = context.chunkPos().getMiddleBlockZ();

			// Get surface height at chunk center
			int y = context.chunkGenerator().getFirstOccupiedHeight(
					cx, cz, Heightmap.Types.WORLD_SURFACE_WG,
					context.heightAccessor(), context.randomState());

			builder.addPiece(new CastlePiece(cx, y, cz));
		});
	}

	@Override
	public StructureType<?> type() {
		return ModStructures.CASTLE_TYPE;
	}
}
