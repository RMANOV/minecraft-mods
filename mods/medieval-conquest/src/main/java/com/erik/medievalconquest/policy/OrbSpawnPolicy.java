package com.erik.medievalconquest.policy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
/** Immutable spawn decisions; entity creation belongs to the engine adapter. */
public final class OrbSpawnPolicy {
    private OrbSpawnPolicy() {}
    public enum DifficultyKind { PEACEFUL, EASY, NORMAL, HARD }
    public static Map<String,Integer> plan(DifficultyKind difficulty) {
        if (difficulty == null) throw new IllegalArgumentException("difficulty is required");
        return switch (difficulty) {
            case PEACEFUL -> Map.of();
            case EASY -> Map.of("minecraft:zombie", 1);
            case NORMAL -> Map.of("minecraft:zombie", 8);
            case HARD -> Map.of("minecraft:skeleton", 2, "minecraft:enderman", 2);
        };
    }

    /** Spawn spots stay in square rings this far from the activator's block; never on top of them. */
    public static final int MIN_RING = 2, MAX_RING = 6;
    /** Spots may sit this many blocks above or below the activator's feet (hills, steps). */
    public static final int MAX_DY = 2;

    /** Offset from the activator's block position to a candidate mob feet position. */
    public record Offset(int dx, int dy, int dz) {
        public int ring() { return Math.max(Math.abs(dx), Math.abs(dz)); }
    }

    /**
     * Every candidate spot, nearest ring first and feet level before higher/lower steps. Order is
     * shuffled only inside one (ring, |dy|) band, so the result is near-first yet not a fixed pattern.
     */
    public static List<Offset> candidateOffsets(long seed) {
        Random random = new Random(seed);
        List<Offset> out = new ArrayList<>();
        for (int ring = MIN_RING; ring <= MAX_RING; ring++) {
            for (int ady = 0; ady <= MAX_DY; ady++) {
                List<Offset> band = new ArrayList<>();
                for (int dx = -ring; dx <= ring; dx++) for (int dz = -ring; dz <= ring; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) continue;
                    band.add(new Offset(dx, ady, dz));
                    if (ady != 0) band.add(new Offset(dx, -ady, dz));
                }
                Collections.shuffle(band, random);
                out.addAll(band);
            }
        }
        return List.copyOf(out);
    }
}
