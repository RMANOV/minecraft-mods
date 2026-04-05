package com.erik.lumenrealis.lighting;

import java.util.List;

/**
 * Prepares light data into flat float arrays ready for GPU uniform upload.
 * Actual shader uniform binding is wired in Phase 3 Mixin integration.
 */
public class LightUniformUploader {

    private static final int MAX_LIGHTS = 32;

    private static final float[] lightPositions = new float[MAX_LIGHTS * 3];
    private static final float[] lightColors    = new float[MAX_LIGHTS * 3];
    private static int lightCount = 0;

    public static void prepare(List<LightSource> lights) {
        lightCount = Math.min(lights.size(), MAX_LIGHTS);
        for (int i = 0; i < lightCount; i++) {
            LightSource l = lights.get(i);
            lightPositions[i * 3]     = (float) l.x();
            lightPositions[i * 3 + 1] = (float) l.y();
            lightPositions[i * 3 + 2] = (float) l.z();

            lightColors[i * 3]     = l.r() * l.intensity();
            lightColors[i * 3 + 1] = l.g() * l.intensity();
            lightColors[i * 3 + 2] = l.b() * l.intensity();
        }
    }

    public static int getLightCount() {
        return lightCount;
    }

    public static float[] getLightPositions() {
        return lightPositions;
    }

    public static float[] getLightColors() {
        return lightColors;
    }
}
