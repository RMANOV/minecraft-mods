package com.erik.lumenrealis.config;

public record LumenConfig(
        QualityPreset preset,
        boolean enabled,
        boolean pbrEnabled,
        boolean dynamicLightsEnabled,
        boolean enhancedSkyEnabled,
        boolean enhancedWaterEnabled,
        float bloomIntensity,
        float ssaoRadius,
        float exposure,
        boolean showPerformanceHud,
        boolean adaptiveQualityEnabled,
        int targetFps
) {
    public LumenConfig {
        bloomIntensity = Math.max(0.0f, Math.min(2.0f, bloomIntensity));
        ssaoRadius = Math.max(0.1f, Math.min(2.0f, ssaoRadius));
        exposure = Math.max(0.1f, Math.min(5.0f, exposure));
        targetFps = Math.max(30, Math.min(240, targetFps));
    }

    public static LumenConfig defaults() {
        return new LumenConfig(
                QualityPreset.HIGH, true, true, true, true, true,
                0.5f, 0.5f, 1.0f, false, true, 60
        );
    }

    public LumenConfig withEnabled(boolean enabled) {
        return new LumenConfig(preset, enabled, pbrEnabled, dynamicLightsEnabled,
                enhancedSkyEnabled, enhancedWaterEnabled, bloomIntensity, ssaoRadius,
                exposure, showPerformanceHud, adaptiveQualityEnabled, targetFps);
    }

    public LumenConfig withPreset(QualityPreset preset) {
        return new LumenConfig(preset, enabled, pbrEnabled, dynamicLightsEnabled,
                enhancedSkyEnabled, enhancedWaterEnabled, bloomIntensity, ssaoRadius,
                exposure, showPerformanceHud, adaptiveQualityEnabled, targetFps);
    }

    public LumenConfig withShowPerformanceHud(boolean show) {
        return new LumenConfig(preset, enabled, pbrEnabled, dynamicLightsEnabled,
                enhancedSkyEnabled, enhancedWaterEnabled, bloomIntensity, ssaoRadius,
                exposure, show, adaptiveQualityEnabled, targetFps);
    }
}
