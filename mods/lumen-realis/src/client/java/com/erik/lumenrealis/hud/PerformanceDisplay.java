package com.erik.lumenrealis.hud;

import com.erik.lumenrealis.config.QualityPreset;

public record PerformanceDisplay(
        int fps, float frameTimeMs, long memoryUsedMb,
        QualityPreset activePreset, boolean enabled
) {
    public String fpsText() { return fps + " FPS"; }
    public String frameTimeText() { return String.format("%.1fms", frameTimeMs); }
    public String presetText() { return activePreset.name(); }
    public String statusText() { return enabled ? "ON" : "OFF"; }

    public int fpsColor() {
        if (fps >= 60) return 0x44FF44;
        if (fps >= 30) return 0xFFFF44;
        return 0xFF4444;
    }
}
