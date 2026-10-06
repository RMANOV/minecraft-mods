package com.erik.medievalconquest.world;

import com.erik.medievalconquest.registry.ModAnomalyBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/** One clipped, disjoint chunk of a deterministic shared snowy cavern and icy shell. */
public final class SnowCavernPiece extends StructurePiece {
    private final int originX;
    private final int originY;
    private final int originZ;
    private final int pieceX;
    private final int pieceZ;

    public SnowCavernPiece(int x, int y, int z, int dx, int dz) {
        super(ModStructures.SNOW_CAVERN_PIECE, 0, box(x,y,z,dx,dz));
        originX=x; originY=y; originZ=z; pieceX=dx; pieceZ=dz;
    }

    public SnowCavernPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.SNOW_CAVERN_PIECE, tag);
        if (tag.getIntOr("SnowVersion", -1) != 1) throw new IllegalArgumentException("unsupported snow piece version");
        originX=required(tag,"OriginX"); originY=required(tag,"OriginY"); originZ=required(tag,"OriginZ");
        pieceX=required(tag,"PieceX"); pieceZ=required(tag,"PieceZ");
        if (!getBoundingBox().equals(box(originX,originY,originZ,pieceX,pieceZ)))
            throw new IllegalArgumentException("snow piece bounds disagree with persisted origin");
    }

    private static int required(CompoundTag tag, String name) {
        return tag.getInt(name).orElseThrow(() -> new IllegalArgumentException("missing snow piece field " + name));
    }

    private static BoundingBox box(int x, int y, int z, int dx, int dz) {
        if ((x & 15) != 0 || (z & 15) != 0 || dx < 0 || dx > 2 || dz < 0 || dz > 2)
            throw new IllegalArgumentException("unaligned snow origin or invalid piece index");
        return new BoundingBox(Math.addExact(x,dx*16),y,Math.addExact(z,dz*16),
                Math.addExact(x,dx*16+15),Math.addExact(y,23),Math.addExact(z,dz*16+15));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("SnowVersion",1);
        tag.putInt("OriginX",originX); tag.putInt("OriginY",originY); tag.putInt("OriginZ",originZ);
        tag.putInt("PieceX",pieceX); tag.putInt("PieceZ",pieceZ);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager manager, ChunkGenerator generator,
            RandomSource random, BoundingBox chunkBounds, ChunkPos chunkPos, BlockPos pivot) {
        BoundingBox pieceBounds = getBoundingBox();
        if (chunkPos.x != (pieceBounds.minX() >> 4)
                || chunkPos.z != (pieceBounds.minZ() >> 4)) return;
        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();
        int minX = Math.max(pieceBounds.minX(), Math.max(chunkBounds.minX(), chunkMinX));
        int maxX = Math.min(pieceBounds.maxX(), Math.min(chunkBounds.maxX(), chunkMinX + 15));
        int minY = Math.max(pieceBounds.minY(), Math.max(chunkBounds.minY(), level.getMinY()));
        int maxY = Math.min(pieceBounds.maxY(), Math.min(chunkBounds.maxY(), level.getMaxY()));
        int minZ = Math.max(pieceBounds.minZ(), Math.max(chunkBounds.minZ(), chunkMinZ));
        int maxZ = Math.min(pieceBounds.maxZ(), Math.min(chunkBounds.maxZ(), chunkMinZ + 15));
        if (minX > maxX || minY > maxY || minZ > maxZ) return;

        // Clip before any positional access; only this exact target chunk is requested.
        var chunk = level.getChunk(chunkPos.x, chunkPos.z);
        // Local offsets avoid endpoint overflow and visit at most 16 * 16 * 24 cells.
        for (int dx = 0; dx <= maxX - minX; dx++)
            for (int dz = 0; dz <= maxZ - minZ; dz++)
                for (int dy = 0; dy <= maxY - minY; dy++) {
                    int x = minX + dx, y = minY + dy, z = minZ + dz;
                    double nx = (x - (double) originX - 23.5) / 22.0;
                    double ny = (y - (double) originY - 11.5) / 10.0;
                    double nz = (z - (double) originZ - 23.5) / 22.0;
                    double q = nx * nx + ny * ny + nz * nz;
                    if (q > 1.0) continue;

                    var biome = chunk.getNoiseBiome(x >> 2, y >> 2, z >> 2);
                    if (!(biome.is(Biomes.SNOWY_PLAINS) || biome.is(Biomes.ICE_SPIKES)
                            || biome.is(Biomes.SNOWY_TAIGA) || biome.is(Biomes.GROVE)
                            || biome.is(Biomes.SNOWY_SLOPES) || biome.is(Biomes.FROZEN_PEAKS)
                            || biome.is(Biomes.JAGGED_PEAKS))) continue;
                    var pos = new BlockPos(x, y, z);
                    var state = level.getBlockState(pos);
                    if (!state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(Blocks.BEDROCK)
                            || !state.getFluidState().isEmpty() || state.hasBlockEntity()
                            || level.getBlockEntity(pos) != null) continue;
                    level.setBlock(pos, q <= 0.72 ? Blocks.AIR.defaultBlockState()
                            : ModAnomalyBlocks.ICY_OBSIDIAN.defaultBlockState(), 2);
                }
    }
}
