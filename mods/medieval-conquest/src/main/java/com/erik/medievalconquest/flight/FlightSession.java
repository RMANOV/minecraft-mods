package com.erik.medievalconquest.flight;

import java.util.UUID;

/**
 * INERT controller API for compile-complete behavioral-RED source preparation.
 * Only direct immutable DTO load/get is available; no transition, outcome, or hook is implemented.
 * No caller is registered, no player owns this scaffold, and runtime admission remains separate.
 */
public final class FlightSession {
    private FlightRecord record;

    public FlightRecord record() { return record; }

    public void load(FlightRecord record) {
        this.record = record; // Bootstrap direct pending-debt tests; not lifecycle implementation.
    }

    public void copyFrom(FlightSession old, boolean alive) {}
    public void prepareStart(boolean alreadyFlying, boolean eligibleCustom, boolean living, float hp) {}
    public void finishStart(boolean actuallyFlying) {}
    public boolean blocksCustomStart(boolean currentlyFlying) { return false; }
    public void ended(boolean wasFlying, boolean nowFlying) {}
    public void leave() {}
    public void resume() {}
    public void genuineDeath() {}
    public void tick(boolean nowFlying) {}
    public void resolve(OutcomePort port) {}
    public void blockOutcomeFailure(FlightRecord priorDebt) {}
    public boolean claimOutcomeFailureWarning() { return false; }

    public interface OutcomePort {
        UUID id();
        float hp();
        boolean dead();
        boolean removed();
        void setHp(float hp);
        void dieOnce();
        void incompatibility(String constantReason);
    }
}
