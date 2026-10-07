package com.erik.medievalconquest.policy;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

    @Test void candidatesCoverEveryRingSpotExactlyOnceAndNeverTheActivatorColumn() {
        var spots = OrbSpawnPolicy.candidateOffsets(42L);
        Set<OrbSpawnPolicy.Offset> unique = new HashSet<>(spots);
        assertEquals(spots.size(), unique.size(), "no duplicate spot");
        // Rings 2..6 hold 13*13 - 3*3 = 160 columns; each has 5 heights (-2..+2).
        assertEquals(160 * 5, spots.size());
        for (var spot : spots) {
            assertTrue(spot.ring() >= 2 && spot.ring() <= 6, "ring bound " + spot);
            assertTrue(Math.abs(spot.dy()) <= 2, "height bound " + spot);
        }
    }

    @Test void candidatesAreNearestRingFirstThenFeetLevelFirst() {
        var spots = OrbSpawnPolicy.candidateOffsets(7L);
        for (int i = 1; i < spots.size(); i++) {
            var a = spots.get(i - 1);
            var b = spots.get(i);
            assertTrue(a.ring() < b.ring() || (a.ring() == b.ring() && Math.abs(a.dy()) <= Math.abs(b.dy())),
                    "order breaks between " + a + " and " + b);
        }
        assertEquals(0, spots.getFirst().dy());
        assertEquals(2, spots.getFirst().ring());
    }

    @Test void candidateOrderIsSeededAndImmutable() {
        assertEquals(OrbSpawnPolicy.candidateOffsets(5L), OrbSpawnPolicy.candidateOffsets(5L));
        assertNotEquals(OrbSpawnPolicy.candidateOffsets(5L), OrbSpawnPolicy.candidateOffsets(6L));
        assertThrows(UnsupportedOperationException.class,
                () -> OrbSpawnPolicy.candidateOffsets(5L).add(new OrbSpawnPolicy.Offset(0, 0, 0)));
    }
}
