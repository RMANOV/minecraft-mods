package com.erik.medievalconquest.policy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.erik.medievalconquest.policy.FlightPenaltyPolicy.Outcome.*;
class FlightPenaltyPolicyTest {
    @Test void healthyStartHalfHeartEvenAfterDamage() { assertEquals(SET_HALF_HEART,FlightPenaltyPolicy.onEnd(20,20)); assertEquals(SET_HALF_HEART,FlightPenaltyPolicy.onEnd(20,1)); }
    @Test void lowStartDeathEvenAfterHealing() { assertEquals(DEATH,FlightPenaltyPolicy.onEnd(1,20)); assertEquals(DEATH,FlightPenaltyPolicy.onEnd(1,1)); assertEquals(DEATH,FlightPenaltyPolicy.onEnd(0,20)); }
    @Test void deadNeverRevived() { assertEquals(NONE,FlightPenaltyPolicy.onEnd(20,0)); }
    @Test void invalidHealthEachArgumentRejected() { for(float bad:new float[]{-1,Float.NaN,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY}) { assertThrows(IllegalArgumentException.class,()->FlightPenaltyPolicy.onEnd(bad,20)); assertThrows(IllegalArgumentException.class,()->FlightPenaltyPolicy.onEnd(20,bad)); } }
}
