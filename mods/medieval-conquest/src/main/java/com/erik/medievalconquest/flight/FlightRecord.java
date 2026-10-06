package com.erik.medievalconquest.flight;

/** Immutable API DTO only; strict persistence and outcome behavior are not implemented here. */
public record FlightRecord(Phase phase, boolean custom, float hp) {
    public enum Phase { ACTIVE, PENDING, LOCKED }

    public FlightRecord {
        if (phase == null || !Float.isFinite(hp) || hp < 0f) {
            throw new IllegalArgumentException("invalid flight record");
        }
    }

    public static FlightRecord locked() {
        return new FlightRecord(Phase.LOCKED, false, 0f);
    }

    public FlightRecord pending() {
        return phase == Phase.ACTIVE ? new FlightRecord(Phase.PENDING, custom, hp) : this;
    }
}
