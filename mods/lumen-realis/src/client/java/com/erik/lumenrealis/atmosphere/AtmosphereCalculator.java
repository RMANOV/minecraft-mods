package com.erik.lumenrealis.atmosphere;

public final class AtmosphereCalculator {

    private AtmosphereCalculator() {}

    /**
     * Compute atmosphere state from game time and weather.
     * timeOfDay: 0.0-1.0 where 0.25 = noon, 0.0 = sunrise, 0.5 = sunset, 0.75 = midnight
     */
    public static AtmosphereState compute(float timeOfDay, boolean isRaining) {
        // Sun angle: 0 at sunrise, PI/2 at noon, PI at sunset
        float sunAngle = (float) ((timeOfDay - 0.0f) * Math.PI * 2.0);

        // Sun direction (rotates around X axis)
        float sunX = 0.0f;
        float sunY = (float) Math.sin(sunAngle);
        float sunZ = (float) -Math.cos(sunAngle);

        // Normalize
        float len = (float) Math.sqrt(sunX * sunX + sunY * sunY + sunZ * sunZ);
        if (len > 0.001f) { sunX /= len; sunY /= len; sunZ /= len; }

        // Sun color: warm at horizon, white at zenith
        float elevation = Math.max(0.0f, sunY); // 0 at horizon, 1 at zenith
        float horizonFactor = 1.0f - Math.min(1.0f, elevation * 3.0f);
        float sunR = 1.0f;
        float sunG = 0.7f + 0.3f * (1.0f - horizonFactor);
        float sunB = 0.4f + 0.6f * (1.0f - horizonFactor);
        float sunIntensity = Math.max(0.0f, elevation);
        sunR *= sunIntensity;
        sunG *= sunIntensity;
        sunB *= sunIntensity;

        // Ambient: sky blue during day, dark blue at night
        float dayFactor = Math.max(0.0f, Math.min(1.0f, sunY * 2.0f + 0.3f));
        float ambientR = 0.05f + 0.10f * dayFactor;
        float ambientG = 0.06f + 0.12f * dayFactor;
        float ambientB = 0.10f + 0.15f * dayFactor;

        // Fog: denser during rain
        float fogDensity = isRaining ? 0.04f : 0.01f;

        return new AtmosphereState(sunAngle, sunX, sunY, sunZ,
                sunR, sunG, sunB, ambientR, ambientG, ambientB,
                fogDensity, timeOfDay);
    }
}
