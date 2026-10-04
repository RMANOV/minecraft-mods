package com.erik.medievalconquest.policy;
/** Pure end-of-flight decision; never revives an already dead player. */
public final class FlightPenaltyPolicy {
    private FlightPenaltyPolicy() {}
    public enum Outcome { NONE, SET_HALF_HEART, DEATH }
    public static Outcome onEnd(float start, float current) {
        validateHealth(start);
        validateHealth(current);
        if (current == 0) return Outcome.NONE;
        return start <= 1 ? Outcome.DEATH : Outcome.SET_HALF_HEART;
    }
    static void validateHealth(float health) {
        if (!Float.isFinite(health) || health < 0)
            throw new IllegalArgumentException("health must be finite and nonnegative");
    }
}
