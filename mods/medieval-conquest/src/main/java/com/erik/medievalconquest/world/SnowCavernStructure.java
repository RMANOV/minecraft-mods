package com.erik.medievalconquest.world;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/** New-chunk admission for nine disjoint pieces of one shared snowy cavern. */
public final class SnowCavernStructure extends Structure {
    public static final MapCodec<SnowCavernStructure> CODEC = simpleCodec(SnowCavernStructure::new);

    public SnowCavernStructure(StructureSettings settings) { super(settings); }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        return onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG, builder -> {
            int x = context.chunkPos().getMinBlockX() - 16;
            int z = context.chunkPos().getMinBlockZ() - 16;
            int surface = context.chunkGenerator().getFirstOccupiedHeight(
                    context.chunkPos().getMiddleBlockX(), context.chunkPos().getMiddleBlockZ(),
                    Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            int y = surface - 24;
            if (y < context.heightAccessor().getMinY() || y + 23 > context.heightAccessor().getMaxY()) return;
            for (int dx = 0; dx < 3; dx++) for (int dz = 0; dz < 3; dz++)
                builder.addPiece(new SnowCavernPiece(x, y, z, dx, dz));
        });
    }

    @Override
    public StructureType<?> type() { return ModStructures.SNOW_CAVERN_TYPE; }
}
