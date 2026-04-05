package com.erik.lumenrealis.config;

public enum QualityPreset {
    LOW(false, false, false, false, 0, 0),
    MEDIUM(true, false, false, false, 5, 8),
    HIGH(true, true, false, false, 7, 24),
    ULTRA(true, true, true, true, 7, 32);

    private final boolean bloomEnabled;
    private final boolean ssaoEnabled;
    private final boolean ssrEnabled;
    private final boolean volumetricEnabled;
    private final int bloomIterations;
    private final int maxLights;

    QualityPreset(boolean bloom, boolean ssao, boolean ssr, boolean volumetric,
                  int bloomIterations, int maxLights) {
        this.bloomEnabled = bloom;
        this.ssaoEnabled = ssao;
        this.ssrEnabled = ssr;
        this.volumetricEnabled = volumetric;
        this.bloomIterations = bloomIterations;
        this.maxLights = maxLights;
    }

    public boolean bloomEnabled() { return bloomEnabled; }
    public boolean ssaoEnabled() { return ssaoEnabled; }
    public boolean ssrEnabled() { return ssrEnabled; }
    public boolean volumetricEnabled() { return volumetricEnabled; }
    public int bloomIterations() { return bloomIterations; }
    public int maxLights() { return maxLights; }

    public QualityPreset next() {
        QualityPreset[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
