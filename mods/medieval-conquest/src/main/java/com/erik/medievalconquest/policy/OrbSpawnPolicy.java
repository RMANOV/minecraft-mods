package com.erik.medievalconquest.policy;
import java.util.Map;
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
}
