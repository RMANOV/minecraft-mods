package com.erik.medievalconquest.policy;
import java.util.HashMap;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.erik.medievalconquest.policy.FlightPenaltyPolicy.Outcome.*;
class FlightTransitionTrackerTest {
    static final UUID PLAYER=new UUID(0,1);
    @Test void capturedHealthyFlightStopsOnce() {
        var t=new FlightTransitionTracker();
        assertEquals(NONE,t.observe(PLAYER,true,true,20));
        assertEquals(NONE,t.observe(PLAYER,true,true,1));
        assertEquals(SET_HALF_HEART,t.observe(PLAYER,false,true,1));
        assertEquals(NONE,t.observe(PLAYER,false,true,1)); assertTrue(t.snapshot().isEmpty());
    }
    @Test void healingKeepsLowCapturedHealth() {
        var t=new FlightTransitionTracker(); t.observe(PLAYER,true,true,1); t.observe(PLAYER,true,true,20);
        assertEquals(DEATH,t.observe(PLAYER,false,true,20));
    }
    @Test void ordinaryFlightNeverArmsMidflight() {
        var t=new FlightTransitionTracker();t.observe(PLAYER,true,false,20);t.observe(PLAYER,true,true,1);
        assertEquals(new FlightTransitionTracker.FlightState(false,20),t.snapshot().get(PLAYER));
        assertEquals(NONE,t.observe(PLAYER,false,true,1));
    }
    @Test void gearRemovalKeepsCapturedEligibility() {
        var t=new FlightTransitionTracker();t.observe(PLAYER,true,true,20);t.observe(PLAYER,true,false,1);
        assertEquals(SET_HALF_HEART,t.observe(PLAYER,false,false,1));assertEquals(NONE,t.observe(PLAYER,false,false,1));
    }
    @Test void deadStopConsumesRecord() {
        var t=new FlightTransitionTracker();t.observe(PLAYER,true,true,1);assertTrue(t.snapshot().containsKey(PLAYER));
        assertEquals(NONE,t.observe(PLAYER,false,true,0));assertFalse(t.snapshot().containsKey(PLAYER));
    }
    @Test void immutableSnapshotRoundTripRetainsCapturedAndOrdinaryState() {
        var source=new HashMap<UUID,FlightTransitionTracker.FlightState>();UUID ordinary=new UUID(0,2);
        source.put(PLAYER,new FlightTransitionTracker.FlightState(true,1));source.put(ordinary,new FlightTransitionTracker.FlightState(false,20));
        var t=FlightTransitionTracker.fromSnapshot(source);source.clear();var saved=t.snapshot();assertEquals(2,saved.size());
        assertThrows(UnsupportedOperationException.class,saved::clear);var restored=FlightTransitionTracker.fromSnapshot(saved);
        assertEquals(DEATH,restored.observe(PLAYER,false,false,20));assertEquals(NONE,restored.observe(PLAYER,false,true,20));
        assertEquals(NONE,restored.observe(ordinary,false,true,1));assertEquals(2,saved.size());
    }
    @Test void capacityRefusesWithoutEvictionAndExistingCanStop() {
        var t=new FlightTransitionTracker();for(int i=0;i<4096;i++)t.observe(new UUID(0,i),true,true,20);
        var before=t.snapshot();assertEquals(4096,before.size());
        assertThrows(IllegalStateException.class,()->t.observe(new UUID(1,0),true,true,20));assertEquals(before,t.snapshot());
        assertEquals(SET_HALF_HEART,t.observe(new UUID(0,0),false,false,20));assertEquals(4095,t.snapshot().size());
        t.observe(new UUID(1,0),true,true,20);assertEquals(4096,t.snapshot().size());
    }
    @Test void invalidObservationsAndDTOsNeverMutateState() {
        var t=new FlightTransitionTracker();t.observe(PLAYER,true,true,20);var before=t.snapshot();
        assertThrows(IllegalArgumentException.class,()->t.observe(null,false,false,20));
        for(float bad:new float[]{-1,Float.NaN,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class,()->t.observe(PLAYER,false,false,bad));
            assertThrows(IllegalArgumentException.class,()->new FlightTransitionTracker.FlightState(true,bad));
        }assertEquals(before,t.snapshot());
    }
    @Test void invalidSnapshotRefusesNullsAndOversize() {
        assertThrows(IllegalArgumentException.class,()->FlightTransitionTracker.fromSnapshot(null));
        var m=new HashMap<UUID,FlightTransitionTracker.FlightState>();m.put(null,new FlightTransitionTracker.FlightState(false,20));
        assertThrows(IllegalArgumentException.class,()->FlightTransitionTracker.fromSnapshot(m));m.clear();m.put(PLAYER,null);
        assertThrows(IllegalArgumentException.class,()->FlightTransitionTracker.fromSnapshot(m));m.clear();
        for(int i=0;i<4097;i++)m.put(new UUID(0,i),new FlightTransitionTracker.FlightState(false,20));
        assertThrows(IllegalArgumentException.class,()->FlightTransitionTracker.fromSnapshot(m));
    }
}
