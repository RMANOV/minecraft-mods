package com.erik.lumenrealis.water;

/**
 * Enhanced water rendering placeholder.
 * Phase 7: wire waveTime into water surface vertex/fragment shaders
 * for animated normal maps and screen-space reflections.
 */
public class WaterRenderer {

    private static float waveTime = 0.0f;

    /**
     * Advances the wave animation timer. Call once per client tick.
     */
    public static void tick() {
        waveTime += 0.05f;
        // Wrap to avoid floating-point precision loss over long sessions
        if (waveTime > 1000.0f) waveTime -= 1000.0f;
    }

    /**
     * Returns the current wave animation time in seconds (wraps at 1000).
     * Upload this to a shader uniform (u_waveTime) on each frame.
     */
    public static float getWaveTime() {
        return waveTime;
    }
}
