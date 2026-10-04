package com.erik.medievalconquest.policy;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.erik.medievalconquest.policy.OrbSpawnPolicy.DifficultyKind.*;
class OrbSpawnPolicyTest {
    @Test void peacefulSpawnsNothing() { assertEquals(Map.of(), OrbSpawnPolicy.plan(PEACEFUL)); }
    @Test void easySpawnsOneZombie() { assertEquals(Map.of("minecraft:zombie",1), OrbSpawnPolicy.plan(EASY)); }
    @Test void normalSpawnsEightZombies() { assertEquals(Map.of("minecraft:zombie",8), OrbSpawnPolicy.plan(NORMAL)); }
    @Test void hardSpawnsExactPairCounts() { assertEquals(Map.of("minecraft:skeleton",2,"minecraft:enderman",2), OrbSpawnPolicy.plan(HARD)); }
    @Test void rejectsNull() { assertThrows(IllegalArgumentException.class, () -> OrbSpawnPolicy.plan(null)); }
    @Test void immutablePlan() { assertThrows(UnsupportedOperationException.class, () -> OrbSpawnPolicy.plan(EASY).put("minecraft:zombie",99)); assertEquals(Map.of("minecraft:zombie",1), OrbSpawnPolicy.plan(EASY)); }
}
