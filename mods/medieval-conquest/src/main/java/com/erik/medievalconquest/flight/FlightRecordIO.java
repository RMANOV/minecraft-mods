package com.erik.medievalconquest.flight;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * INERT behavioral-RED scaffold. No strict decoding or present-record persistence exists yet.
 * This API is not registered or connected to player storage.
 */
public final class FlightRecordIO {
    public static final String FIELD = "medievalconquest_flight";

    private FlightRecordIO() {}

    public static FlightRecord decodeRaw(Object raw) {
        return null; // Deliberately fails present/malformed behavioral assertions, without an NPE.
    }

    public static FlightRecord read(ValueInput in) {
        return null; // Actual read bridge is deferred until an admitted RED run.
    }

    public static void write(ValueOutput out, FlightRecord record) {
        out.discard(FIELD); // Deliberately no present-record implementation before actual RED.
    }
}
