package com.erik.medievalconquest.policy;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
/** Bounded first-observed flight state; not a persistence adapter. */
public final class FlightTransitionTracker {
    public static final int MAX_PLAYERS = 4096;
    private final Map<UUID,FlightState> flights = new HashMap<>();
    public record FlightState(boolean customAtStart, float capturedStartHealth) {
        public FlightState { FlightPenaltyPolicy.validateHealth(capturedStartHealth); }
    }
    public FlightPenaltyPolicy.Outcome observe(UUID player, boolean flying, boolean custom, float hp) {
        if (player == null) throw new IllegalArgumentException("player is required");
        FlightPenaltyPolicy.validateHealth(hp);
        if (flying) {
            if (!flights.containsKey(player)) {
                if (flights.size() >= MAX_PLAYERS) throw new IllegalStateException("flight capacity exhausted");
                flights.put(player, new FlightState(custom, hp));
            }
            return FlightPenaltyPolicy.Outcome.NONE;
        }
        FlightState previous = flights.remove(player);
        if (previous == null || !previous.customAtStart()) return FlightPenaltyPolicy.Outcome.NONE;
        return FlightPenaltyPolicy.onEnd(previous.capturedStartHealth(), hp);
    }
    public Map<UUID,FlightState> snapshot() { return Map.copyOf(flights); }
    public static FlightTransitionTracker fromSnapshot(Map<UUID,FlightState> saved) {
        if (saved == null || saved.size() > MAX_PLAYERS)
            throw new IllegalArgumentException("invalid flight snapshot");
        FlightTransitionTracker tracker = new FlightTransitionTracker();
        for (var entry : saved.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null)
                throw new IllegalArgumentException("null flight record");
            FlightState state = entry.getValue();
            FlightPenaltyPolicy.validateHealth(state.capturedStartHealth());
            tracker.flights.put(entry.getKey(), state);
        }
        return tracker;
    }
}
