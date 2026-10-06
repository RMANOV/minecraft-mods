package com.erik.medievalconquest;

import com.erik.medievalconquest.block.entity.TemporaryBarrierBlockEntity;
import com.erik.medievalconquest.registry.ModAnomalyBlocks;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;

/** Two separately admitted processes; the reader never constructs persisted input. */
public final class BarrierPersistenceGameTest {
    private static final BlockPos ACTIVE = new BlockPos(512, 80, 512);
    private static final BlockPos PERMANENT = new BlockPos(513, 80, 512);
    private static final long DURATION = 1_800_000L;
    private static final Block[] NIBBLE_WOOL = {
        Blocks.WHITE_WOOL, Blocks.ORANGE_WOOL, Blocks.MAGENTA_WOOL, Blocks.LIGHT_BLUE_WOOL,
        Blocks.YELLOW_WOOL, Blocks.LIME_WOOL, Blocks.PINK_WOOL, Blocks.GRAY_WOOL,
        Blocks.LIGHT_GRAY_WOOL, Blocks.CYAN_WOOL, Blocks.BLUE_WOOL, Blocks.PURPLE_WOOL,
        Blocks.BROWN_WOOL, Blocks.GREEN_WOOL, Blocks.RED_WOOL, Blocks.BLACK_WOOL
    };

    @GameTest(maxTicks = 100)
    public void writeLifetimeProbe(GameTestHelper h) throws IOException {
        requirePhase(h, "writer");
        requireSeparatedTemplate(h);
        String nonce = nonce(h);
        ServerLevel level = h.getLevel();
        h.assertTrue(level.getChunkSource().getChunk(32, 32, ChunkStatus.FULL, true) != null,
                "HARNESS: writer loads only the admitted private probe chunk");
        h.assertTrue(level.setBlock(ACTIVE, ModAnomalyBlocks.TEMPORARY_BARRIER.defaultBlockState(), 3),
                "writer places the actual registered temporary barrier");
        var entity = entity(h);
        long placed = System.currentTimeMillis();
        h.assertTrue(entity.initializeAt(placed), "writer initializes its real entity once");
        TemporaryBarrierBlockEntity.serverTickAt(level, ACTIVE, level.getBlockState(ACTIVE),
                entity, placed + DURATION - 1L);
        h.assertTrue(level.setBlock(PERMANENT,
                ModAnomalyBlocks.PERMANENT_BARRIER.defaultBlockState(), 3),
                "writer places the real permanent barrier");
        for (int i = 0; i < 32; i++) {
            h.assertTrue(level.setBlock(sentinelPos(i),
                    NIBBLE_WOOL[Character.digit(nonce.charAt(i), 16)].defaultBlockState(), 3),
                    "writer installs physical nonce block " + i);
        }
        requireSentinel(h, nonce);
        requireFields(h, entity, placed, placed + DURATION - 1L);
        h.assertTrue(level.getBlockEntity(PERMANENT) == null, "permanent has no expiry entity");
        level.save(null, true, false);
        Properties oracle = new Properties();
        oracle.setProperty("version", "1");
        oracle.setProperty("placed", Long.toString(placed));
        oracle.setProperty("deadline", Long.toString(placed + DURATION));
        oracle.setProperty("maxSeen", Long.toString(placed + DURATION - 1L));
        oracle.setProperty("nonce", nonce);
        try (Writer out = Files.newBufferedWriter(oraclePath(h), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            oracle.store(out, "scalar oracle only; no serialized block entity");
        }
        h.succeed();
    }

    @GameTest(maxTicks = 100)
    public void readLifetimeProbe(GameTestHelper h) throws IOException {
        requirePhase(h, "reader");
        requireSeparatedTemplate(h);
        Properties oracle = new Properties();
        try (Reader in = Files.newBufferedReader(oraclePath(h), StandardCharsets.UTF_8)) {
            oracle.load(in);
        }
        h.assertTrue(oracle.stringPropertyNames().equals(
                Set.of("version", "placed", "deadline", "maxSeen", "nonce")),
                "HARNESS: reader gets only the frozen scalar oracle");
        String nonce = nonce(h);
        long placed = Long.parseLong(oracle.getProperty("placed"));
        h.assertTrue("1".equals(oracle.getProperty("version"))
                && nonce.equals(oracle.getProperty("nonce"))
                && Long.parseLong(oracle.getProperty("deadline")) == placed + DURATION
                && Long.parseLong(oracle.getProperty("maxSeen")) == placed + DURATION - 1L,
                "HARNESS: literal lifetime and nonce oracle");
        h.assertTrue(System.currentTimeMillis() >= placed
                && System.currentTimeMillis() < placed + DURATION,
                "HARNESS: reader is inside the original real lifetime interval");
        ServerLevel level = h.getLevel();
        // Coordinator verified this chunk's allocated region header BEFORE launch.
        // FULL loads that existing storage; missing/reset data cannot pass nonce/fields.
        h.assertTrue(level.getChunkSource().getChunk(32, 32, ChunkStatus.FULL, true) != null,
                "HARNESS: reader loads the verified existing probe chunk");
        requireSentinel(h, nonce);
        h.assertTrue(level.getBlockState(PERMANENT).is(ModAnomalyBlocks.PERMANENT_BARRIER)
                && level.getBlockEntity(PERMANENT) == null,
                "actual permanent block survived the separate process");
        var loaded = entity(h);
        requireFields(h, loaded, placed, placed + DURATION - 1L);
        // All actual stored fields and nonce checked before the first initialize call.
        h.assertTrue(!loaded.initializeAt(System.currentTimeMillis()),
                "disk-loaded entity cannot restart its thirty-minute lifetime");
        requireFields(h, loaded, placed, placed + DURATION - 1L);
        TemporaryBarrierBlockEntity.serverTickAt(level, ACTIVE, level.getBlockState(ACTIVE),
                loaded, placed + 1L);
        h.assertTrue(level.getBlockState(ACTIVE).is(ModAnomalyBlocks.TEMPORARY_BARRIER),
                "lower clock neither expires nor renews the disk lifetime");
        requireFields(h, loaded, placed, placed + DURATION - 1L);
        TemporaryBarrierBlockEntity.serverTickAt(level, ACTIVE, level.getBlockState(ACTIVE),
                loaded, placed + DURATION - 1L);
        h.assertTrue(level.getBlockState(ACTIVE).is(ModAnomalyBlocks.TEMPORARY_BARRIER),
                "actual loaded barrier exists one millisecond before its disk deadline");
        TemporaryBarrierBlockEntity.serverTickAt(level, ACTIVE, level.getBlockState(ACTIVE),
                loaded, placed + DURATION);
        h.assertTrue(level.getBlockState(ACTIVE).isAir(),
                "actual disk-loaded barrier is removed at its unchanged deadline");
        requireSentinel(h, nonce);
        h.assertTrue(level.getBlockState(PERMANENT).is(ModAnomalyBlocks.PERMANENT_BARRIER)
                && level.getBlockEntity(PERMANENT) == null,
                "normal expiry did not remove its real permanent neighbor");
        h.succeed();
    }

    private static TemporaryBarrierBlockEntity entity(GameTestHelper h) {
        h.assertTrue(h.getLevel().getBlockState(ACTIVE).is(ModAnomalyBlocks.TEMPORARY_BARRIER)
                && h.getLevel().getBlockEntity(ACTIVE) instanceof TemporaryBarrierBlockEntity,
                "actual stored temporary block and registered entity are present");
        return (TemporaryBarrierBlockEntity) h.getLevel().getBlockEntity(ACTIVE);
    }

    private static void requireFields(GameTestHelper h, TemporaryBarrierBlockEntity entity,
            long placed, long maxSeen) {
        var fields = entity.saveCustomOnly(h.getLevel().registryAccess());
        h.assertTrue(fields.getInt("version").orElse(-1) == 1, "stored schema version");
        h.assertTrue(fields.getBoolean("initialized").orElse(false), "stored initialized flag");
        h.assertTrue(fields.getLong("placed").orElse(-1L) == placed, "stored original placement");
        h.assertTrue(fields.getLong("deadline").orElse(-1L) == placed + DURATION,
                "stored original exact thirty-minute deadline");
        h.assertTrue(fields.getLong("maxSeen").orElse(-1L) == maxSeen,
                "stored maximum observation survives and never regresses");
    }

    private static BlockPos sentinelPos(int index) {
        return new BlockPos(512 + index % 8, 80, 513 + index / 8);
    }

    private static void requireSentinel(GameTestHelper h, String nonce) {
        for (int i = 0; i < 32; i++) {
            h.assertTrue(h.getLevel().getBlockState(sentinelPos(i)).is(
                    NIBBLE_WOOL[Character.digit(nonce.charAt(i), 16)]),
                    "physical disk nonce block " + i + " is retained, not reconstructed");
        }
    }

    private static void requireSeparatedTemplate(GameTestHelper h) {
        AABB probeColumn = new AABB(512, -1_000_000, 512, 528, 1_000_000, 528);
        h.assertTrue(!h.getBounds().inflate(64.0).intersects(probeColumn),
                "HARNESS: actual template plus conservative clearance excludes whole probe chunk");
    }

    private static void requirePhase(GameTestHelper h, String phase) {
        h.assertTrue(phase.equals(System.getProperty("medieval.disk.phase")),
                "HARNESS: this fixture belongs only to its admitted process phase");
    }

    private static String nonce(GameTestHelper h) {
        String nonce = System.getProperty("medieval.disk.nonce", "");
        h.assertTrue(nonce.matches("[0-9a-f]{32}"), "HARNESS: exact 128-bit private nonce");
        return nonce;
    }

    private static Path oraclePath(GameTestHelper h) {
        Path path = Path.of(System.getProperty("medieval.disk.oracle", ""));
        h.assertTrue(path.isAbsolute() && !Files.isSymbolicLink(path),
                "HARNESS: admitted absolute regular oracle path");
        return path;
    }
}
