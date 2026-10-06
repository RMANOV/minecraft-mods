package com.erik.medievalconquest;

import com.erik.medievalconquest.registry.ModAnomalyBlocks;
import com.erik.medievalconquest.world.ModStructures;
import com.erik.medievalconquest.world.SnowCavernPiece;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/** Disposable, disjoint native chunks; the tracer delegates to the actual ServerLevel. */
public final class SnowResourceGameTest {
    private static BlockPos origin(int fixture) {
        return new BlockPos(-320000 + fixture * 4096, 16, -320000);
    }

    private static LevelChunk chunk(ServerLevel level, BlockPos origin, int dx, int dz, boolean snowy) {
        var chunk = level.getChunk((origin.getX() >> 4) + dx, (origin.getZ() >> 4) + dz);
        var biome = level.registryAccess().lookupOrThrow(Registries.BIOME)
                .getOrThrow(snowy ? Biomes.SNOWY_PLAINS : Biomes.PLAINS);
        chunk.fillBiomesFromNoise((x, y, z, sampler) -> biome,
                level.getChunkSource().randomState().sampler());
        return chunk;
    }

    private static void stone(ServerLevel level, BlockPos origin, int dx, int dz, boolean snowy) {
        chunk(level, origin, dx, dz, snowy);
        for (int x = dx * 16; x < dx * 16 + 16; x++)
            for (int z = dz * 16; z < dz * 16 + 16; z++)
                for (int y = 0; y < 24; y++)
                    level.setBlock(origin.offset(x, y, z), Blocks.STONE.defaultBlockState(), 2);
    }

    private static BoundingBox bounds(BlockPos origin, int dx, int dz) {
        return new BoundingBox(origin.getX() + dx * 16, 16, origin.getZ() + dz * 16,
                origin.getX() + dx * 16 + 15, 39, origin.getZ() + dz * 16 + 15);
    }

    private static final class Trace {
        final Set<BlockPos> writes = new HashSet<>();
        int attemptedWrites;
        int positionalAccesses;
        final WorldGenLevel level;

        Trace(GameTestHelper h, BoundingBox box, ChunkPos target) {
            var actual = h.getLevel();
            level = (WorldGenLevel) Proxy.newProxyInstance(WorldGenLevel.class.getClassLoader(),
                    new Class<?>[] {WorldGenLevel.class}, (proxy, method, args) -> {
                        String name = method.getName();
                        if (args != null && args.length > 0 && args[0] instanceof BlockPos pos) {
                            if (name.equals("setBlock") || name.equals("getBlockState")
                                    || name.equals("getFluidState") || name.equals("getBlockEntity")) {
                                h.assertTrue(box.isInside(pos) && pos.getY() >= actual.getMinY()
                                        && pos.getY() <= actual.getMaxY(), "application positional access clipped before delegation");
                                positionalAccesses++;
                            }
                            if (name.equals("setBlock")) {
                                attemptedWrites++;
                                h.assertTrue(attemptedWrites <= 6144, "all attempted writes bounded, including idempotent writes");
                                h.assertTrue(writes.add(pos.immutable()), "each native cell written at most once per piece");
                            }
                        }
                        if (name.equals("getChunk") && args != null && args.length >= 2
                                && args[0] instanceof Integer x && args[1] instanceof Integer z)
                            h.assertTrue(x == target.x && z == target.z, "only actual target chunk explicitly requested");
                        try { return method.invoke(actual, args); }
                        catch (InvocationTargetException error) { throw error.getCause(); }
                    });
        }
    }

    private static Trace generate(GameTestHelper h, BlockPos origin, int dx, int dz, SnowCavernPiece piece) {
        var target = new ChunkPos((origin.getX() >> 4) + dx, (origin.getZ() >> 4) + dz);
        var trace = new Trace(h, bounds(origin, dx, dz), target);
        piece.postProcess(trace.level, h.getLevel().structureManager(),
                h.getLevel().getChunkSource().getGenerator(), RandomSource.create(17),
                bounds(origin, dx, dz), target, origin);
        return trace;
    }

    private static List<BlockPos> icy(ServerLevel level, BoundingBox box) {
        var positions = new ArrayList<BlockPos>();
        for (int x = box.minX(); x <= box.maxX(); x++)
            for (int z = box.minZ(); z <= box.maxZ(); z++)
                for (int y = box.minY(); y <= box.maxY(); y++) {
                    var pos = new BlockPos(x, y, z);
                    if (level.getBlockState(pos).is(ModAnomalyBlocks.ICY_OBSIDIAN)) positions.add(pos);
                }
        return positions;
    }

    @GameTest
    public void loadedSnowStructureRegistryAndBiomeTag(GameTestHelper h) {
        var key = ResourceKey.create(Registries.STRUCTURE,
                Identifier.fromNamespaceAndPath("medievalconquest", "snow_cavern"));
        var structure = h.getLevel().registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(key).value();
        h.assertTrue(structure.type() == ModStructures.SNOW_CAVERN_TYPE, "actual registered snow structure type");
        var biomeRegistry = h.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        for (var biome : List.of(Biomes.SNOWY_PLAINS, Biomes.ICE_SPIKES, Biomes.SNOWY_TAIGA,
                Biomes.GROVE, Biomes.SNOWY_SLOPES, Biomes.FROZEN_PEAKS, Biomes.JAGGED_PEAKS))
            h.assertTrue(structure.biomes().contains(biomeRegistry.getOrThrow(biome)), "loaded snowy whitelist member");
        h.assertTrue(!structure.biomes().contains(biomeRegistry.getOrThrow(Biomes.PLAINS)), "warm start biome excluded");
        h.succeed();
    }

    @GameTest(maxTicks = 200)
    public void nativePieceWritesStayInCurrentChunk(GameTestHelper h) {
        var o = origin(1);
        stone(h.getLevel(), o, 1, 1, true);
        var outside = o.offset(15, 12, 24);
        h.getLevel().setBlock(outside, Blocks.GOLD_BLOCK.defaultBlockState(), 2);
        var before = h.getLevel().getBlockState(outside);
        var trace = generate(h, o, 1, 1, new SnowCavernPiece(o.getX(), o.getY(), o.getZ(), 1, 1));
        h.assertTrue(h.getLevel().getBlockState(outside).equals(before), "outside-face sentinel preserved at negative origin");
        h.assertTrue(trace.attemptedWrites > 0 && trace.positionalAccesses > 0,
                "registered snow piece must actually generate through bounded native access");
        h.assertTrue(!icy(h.getLevel(), bounds(o, 1, 1)).isEmpty(), "mineable icy resource generated in snowy native stone");
        h.succeed();
    }

    @GameTest(maxTicks = 400)
    public void completeNinePiecesShareOneFormation(GameTestHelper h) {
        var a = origin(2);
        var b = origin(3);
        var context = StructurePieceSerializationContext.fromLevel(h.getLevel());
        int[][] order = {{2,2},{0,0},{1,2},{2,0},{0,1},{1,1},{2,1},{0,2},{1,0}};
        for (int[] cell : order) {
            stone(h.getLevel(), a, cell[0], cell[1], true);
            stone(h.getLevel(), b, cell[0], cell[1], true);
        }
        for (int[] cell : order) {
            var piece = new SnowCavernPiece(a.getX(), a.getY(), a.getZ(), cell[0], cell[1]);
            var saved = piece.createTag(context);
            var loaded = new SnowCavernPiece(context, saved);
            h.assertTrue(loaded.getBoundingBox().equals(piece.getBoundingBox()), "piece NBT preserves exact origin/bounds");
            generate(h, a, cell[0], cell[1], loaded);
        }
        for (int i = order.length - 1; i >= 0; i--) {
            int[] cell = order[i];
            generate(h, b, cell[0], cell[1], new SnowCavernPiece(b.getX(), b.getY(), b.getZ(), cell[0], cell[1]));
        }
        for (int x = 0; x < 48; x++) for (int z = 0; z < 48; z++) for (int y = 0; y < 24; y++)
            h.assertTrue(h.getLevel().getBlockState(a.offset(x,y,z)).equals(h.getLevel().getBlockState(b.offset(x,y,z))),
                    "nine-piece output independent of processing order and NBT reload");
        for (int x : List.of(15,16,31,32)) h.assertTrue(h.getLevel().getBlockState(a.offset(x,12,24)).isAir(),
                "shared cavity crosses both literal inter-piece seams");
        h.assertTrue(!icy(h.getLevel(), new BoundingBox(a.getX(),16,a.getZ(),a.getX()+47,39,a.getZ()+47)).isEmpty(),
                "complete shared formation contains icy shell faces");
        h.succeed();
    }

    @GameTest(maxTicks = 200)
    public void warmOrProtectedCellsRemainUntouched(GameTestHelper h) {
        var o = origin(4);
        stone(h.getLevel(), o, 1, 1, false);
        generate(h, o, 1, 1, new SnowCavernPiece(o.getX(),16,o.getZ(),1,1));
        for (int x = 16; x < 32; x++) for (int z = 16; z < 32; z++) for (int y = 0; y < 24; y++)
            h.assertTrue(h.getLevel().getBlockState(o.offset(x,y,z)).is(Blocks.STONE), "warm raw-quart biome remains stone");
        stone(h.getLevel(), o, 1, 1, true);
        var protectedPositions = List.of(o.offset(20,12,23),o.offset(21,12,23),o.offset(22,12,23),o.offset(23,12,23));
        var states = List.of(Blocks.BEDROCK.defaultBlockState(),Blocks.WATER.defaultBlockState(),
                Blocks.CHEST.defaultBlockState(),Blocks.STONE_BRICKS.defaultBlockState());
        for (int i = 0; i < states.size(); i++) h.getLevel().setBlock(protectedPositions.get(i),states.get(i),2);
        h.assertTrue(h.getLevel().getBlockEntity(protectedPositions.get(2)) != null, "real chest block entity control");
        var target = chunk(h.getLevel(), o, 1, 1, true);
        h.assertTrue(target.getNoiseBiome((o.getX()+24)>>2,28>>2,(o.getZ()+24)>>2).is(Biomes.SNOWY_PLAINS),
                "actual target raw quart snowy biome setup");
        generate(h, o, 1, 1, new SnowCavernPiece(o.getX(),16,o.getZ(),1,1));
        for (int i = 0; i < states.size(); i++) h.assertTrue(h.getLevel().getBlockState(protectedPositions.get(i)).equals(states.get(i)),
                "bedrock/liquid/block-entity/non-natural sentinel unchanged");
        h.assertTrue(!icy(h.getLevel(), bounds(o,1,1)).isEmpty(), "snowy natural control still generates around protected cells");
        h.succeed();
    }

    @GameTest(maxTicks = 200)
    public void realPlayerMinesGeneratedIcyForOneItem(GameTestHelper h) {
        var o = origin(5);
        stone(h.getLevel(), o, 1, 1, true);
        generate(h, o,1,1,new SnowCavernPiece(o.getX(),16,o.getZ(),1,1));
        var generated = icy(h.getLevel(),bounds(o,1,1));
        h.assertTrue(!generated.isEmpty(), "registered snow generation supplies the icy block to mine");
        var pos = generated.getFirst();
        var player = h.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));
        // Position movement is connection-independent; ticking is proved separately below.
        player.setPos(pos.getX() + 0.5, pos.getY() + 2.0, pos.getZ() + 0.5);
        var level = h.getLevel();
        var target = new ChunkPos(pos);
        long targetKey = target.toLong();
        boolean preexistingForce = level.getForceLoadedChunks().contains(targetKey);
        boolean[] ownedForce = {false};
        boolean[] done = {false};
        long[] minedTick = {-1};
        long deadline = Math.min(h.getTick() + 100, 180);
        java.util.function.Supplier<List<net.minecraft.world.entity.item.ItemEntity>> matchingDrops =
                () -> level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                        new net.minecraft.world.phys.AABB(pos).inflate(2),
                        entity -> entity.getItem().is(ModAnomalyBlocks.ICY_OBSIDIAN.asItem()));
        java.util.function.Function<String, String> diagnostic = observation ->
                "R01_MINING " + observation + " target=" + pos + " chunk=" + target
                        + " player=" + player.position() + " mode=" + player.gameMode.getGameModeForPlayer()
                        + " instabuild=" + player.getAbilities().instabuild + " tool=" + player.getMainHandItem()
                        + " tick=" + h.getTick() + " minedTick=" + minedTick[0]
                        + " entityTicking=" + level.isPositionEntityTicking(pos)
                        + " entitiesLoadedTicking=" + level.areEntitiesActuallyLoadedAndTicking(target)
                        + " preexistingForce=" + preexistingForce + " ownedForce=" + ownedForce[0]
                        + " forcedNow=" + level.getForceLoadedChunks().contains(targetKey);
        Runnable release = () -> {
            if (ownedForce[0]) {
                boolean removed = level.setChunkForced(target.x, target.z, false);
                ownedForce[0] = false;
                h.assertTrue(removed, diagnostic.apply("force release transition failed sum=NOT_QUERIED"));
            }
            h.assertTrue(level.getForceLoadedChunks().contains(targetKey) == preexistingForce,
                    diagnostic.apply("original force membership not restored sum=NOT_QUERIED"));
            MedievalConquestMod.LOGGER.info(diagnostic.apply("FORCE_RESTORED sum=NOT_QUERIED"));
        };
        try {
            if (!preexistingForce) {
                boolean acquired;
                try {
                    acquired = level.setChunkForced(target.x, target.z, true);
                } catch (RuntimeException | Error failure) {
                    done[0] = true;
                    // setChunkForced can force successfully then throw while loading the chunk.
                    try { ownedForce[0] |= level.getForceLoadedChunks().contains(targetKey); }
                    catch (RuntimeException | Error ownershipFailure) { failure.addSuppressed(ownershipFailure); }
                    throw failure;
                }
                ownedForce[0] = acquired;
                if (!acquired || !level.getForceLoadedChunks().contains(targetKey)) {
                    done[0] = true;
                    h.assertTrue(false, diagnostic.apply("false-to-true force acquisition inconsistent sum=NOT_QUERIED"));
                }
            }
            h.onEachTick(() -> {
                if (done[0]) return;
                boolean success = false;
                Throwable primary = null;
                try {
                    long now = h.getTick();
                    if (now >= deadline) {
                        done[0] = true;
                        h.assertTrue(false, diagnostic.apply("bounded readiness/observation timeout sum=NOT_QUERIED"));
                    }
                    boolean ready = level.isPositionEntityTicking(pos)
                            && level.areEntitiesActuallyLoadedAndTicking(target);
                    if (minedTick[0] < 0) {
                        if (!ready) return;
                        var before = matchingDrops.get();
                        int beforeSum = before.stream().mapToInt(entity -> entity.getItem().getCount()).sum();
                        MedievalConquestMod.LOGGER.info(diagnostic.apply("PRE_MINE sum=" + beforeSum
                                + " drops=" + before.stream().limit(8).map(entity -> entity.getUUID()
                                        + ":" + entity.getItem().getCount() + "@" + entity.position()).toList()));
                        if (beforeSum != 0) {
                            done[0] = true;
                            h.assertTrue(false, diagnostic.apply("pre-mine icy sum must be zero actualSum=" + beforeSum));
                        }
                        if (!player.gameMode.destroyBlock(pos)) {
                            done[0] = true;
                            h.assertTrue(false, diagnostic.apply("actual survival native block break succeeds sum=NOT_QUERIED"));
                        }
                        if (!level.getBlockState(pos).isAir()) {
                            done[0] = true;
                            h.assertTrue(false, diagnostic.apply("generated icy block removed by actual mining sum=NOT_QUERIED"));
                        }
                        minedTick[0] = now;
                        return;
                    }
                    if (now < minedTick[0] + 2) return;
                    done[0] = true;
                    h.assertTrue(now == minedTick[0] + 2,
                            diagnostic.apply("missed exact two-tick observation sum=NOT_QUERIED"));
                    h.assertTrue(ready, diagnostic.apply("entity readiness lost before exact tick2 query sum=NOT_QUERIED"));
                    var drops = matchingDrops.get();
                    int sum = drops.stream().mapToInt(entity -> entity.getItem().getCount()).sum();
                    MedievalConquestMod.LOGGER.info(diagnostic.apply("POST_MINE actualSum=" + sum
                            + " drops=" + drops.stream().limit(8).map(entity -> entity.getUUID()
                                    + ":" + entity.getItem().getCount() + "@" + entity.position()).toList()));
                    h.assertTrue(sum == 1, diagnostic.apply(
                            "native loot yields exactly one obtainable icy item, no fabricated duplicate grant actualSum=" + sum));
                    success = true;
                } catch (RuntimeException | Error failure) {
                    done[0] = true;
                    primary = failure;
                    throw failure;
                } finally {
                    if (done[0]) {
                        try { release.run(); }
                        catch (RuntimeException | Error cleanupFailure) {
                            if (primary != null) primary.addSuppressed(cleanupFailure);
                            else throw cleanupFailure;
                        }
                    }
                }
                if (success) h.succeed();
            });
        } catch (RuntimeException | Error failure) {
            done[0] = true;
            try { release.run(); }
            catch (RuntimeException | Error cleanupFailure) { failure.addSuppressed(cleanupFailure); }
            throw failure;
        }
    }
}
