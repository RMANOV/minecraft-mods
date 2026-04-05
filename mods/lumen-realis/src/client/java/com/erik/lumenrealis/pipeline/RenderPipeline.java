package com.erik.lumenrealis.pipeline;

import com.erik.lumenrealis.config.QualityPreset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton that owns the ordered list of active post-processing passes and
 * tracks per-frame timing for adaptive quality decisions.
 */
public class RenderPipeline {

    private static final RenderPipeline INSTANCE = new RenderPipeline();

    private List<ShaderPass> activePasses = new ArrayList<>();
    private QualityPreset currentPreset = QualityPreset.HIGH;

    // Rolling 60-frame timing buffer (nanoseconds)
    private final long[] frameTimesNs = new long[60];
    private int frameIndex = 0;
    private long lastFrameNs = System.nanoTime();

    private RenderPipeline() {}

    public static RenderPipeline getInstance() {
        return INSTANCE;
    }

    /**
     * Rebuilds the active pass list to match the given preset.
     * Call this when the user changes quality settings.
     */
    public void recompose(QualityPreset preset) {
        currentPreset = preset;
        activePasses = new ArrayList<>();

        if (preset.bloomEnabled()) {
            activePasses.add(new ShaderPass.BloomExtract(1.0f, 0.5f));
            activePasses.add(new ShaderPass.BloomBlur(preset.bloomIterations(), 1.0f));
            activePasses.add(new ShaderPass.BloomComposite(0.5f));
        }
        if (preset.ssaoEnabled()) {
            activePasses.add(new ShaderPass.SSAO(16, 0.5f, 0.025f));
        }
        if (preset.ssrEnabled()) {
            activePasses.add(new ShaderPass.SSR(64, 0.1f));
        }
        if (preset.volumetricEnabled()) {
            activePasses.add(new ShaderPass.Volumetric(32, 0.02f));
        }
        // Tonemap is always last
        activePasses.add(new ShaderPass.Tonemap(1.0f, 2.2f));
    }

    /**
     * Called once per rendered frame. Phase 4+ will iterate activePasses and
     * apply FBO operations; for now records frame timing for adaptive quality.
     */
    public void execute() {
        long now = System.nanoTime();
        frameTimesNs[frameIndex % frameTimesNs.length] = now - lastFrameNs;
        frameIndex++;
        lastFrameNs = now;
        // Phase 4: iterate activePasses and dispatch FBO draw calls here
    }

    /**
     * Returns the rolling average FPS over the last 60 frames.
     */
    public float getAverageFps() {
        int count = Math.min(frameIndex, frameTimesNs.length);
        if (count == 0) return 60.0f;
        long total = 0;
        for (int i = 0; i < count; i++) total += frameTimesNs[i];
        return 1_000_000_000.0f / (total / (float) count);
    }

    public List<ShaderPass> getActivePasses() {
        return Collections.unmodifiableList(activePasses);
    }

    public QualityPreset getCurrentPreset() {
        return currentPreset;
    }
}
