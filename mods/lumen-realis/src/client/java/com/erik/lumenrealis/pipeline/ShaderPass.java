package com.erik.lumenrealis.pipeline;

/**
 * Sealed interface representing individual post-processing stages.
 * Each permit record carries its own typed configuration parameters.
 */
public sealed interface ShaderPass
        permits ShaderPass.BloomExtract, ShaderPass.BloomBlur, ShaderPass.BloomComposite,
                ShaderPass.SSAO, ShaderPass.SSR, ShaderPass.Volumetric, ShaderPass.Tonemap {

    String name();

    record BloomExtract(float threshold, float softKnee) implements ShaderPass {
        @Override
        public String name() { return "bloom_extract"; }
    }

    record BloomBlur(int iterations, float radius) implements ShaderPass {
        @Override
        public String name() { return "bloom_blur"; }
    }

    record BloomComposite(float intensity) implements ShaderPass {
        @Override
        public String name() { return "bloom_composite"; }
    }

    record SSAO(int sampleCount, float radius, float bias) implements ShaderPass {
        @Override
        public String name() { return "ssao"; }
    }

    record SSR(int maxSteps, float stepSize) implements ShaderPass {
        @Override
        public String name() { return "ssr"; }
    }

    record Volumetric(int sampleCount, float density) implements ShaderPass {
        @Override
        public String name() { return "volumetric"; }
    }

    record Tonemap(float exposure, float gamma) implements ShaderPass {
        @Override
        public String name() { return "tonemap"; }
    }
}
