#version 330

// Volumetric light rays (god rays) via ray marching
uniform sampler2D ColorSampler;
uniform sampler2D DepthSampler;
uniform vec2 SunScreenPos;
uniform float Density;
uniform float Weight;
uniform float Decay;
uniform float Exposure;
uniform int SampleCount;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec3 sceneColor = texture(ColorSampler, texCoord).rgb;

    vec2 deltaUV = (texCoord - SunScreenPos) / float(SampleCount);
    vec2 uv = texCoord;
    float illumination = 1.0;
    vec3 godRays = vec3(0.0);

    for (int i = 0; i < SampleCount; i++) {
        uv -= deltaUV;
        if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) break;

        float depth = texture(DepthSampler, uv).r;
        // Sky pixels (depth=1.0) contribute light; solid blocks occlude
        float isSky = step(0.999, depth);
        vec3 sampleColor = texture(ColorSampler, uv).rgb * isSky;

        sampleColor *= illumination * Weight;
        godRays += sampleColor;
        illumination *= Decay;
    }

    godRays *= Exposure * Density;
    fragColor = vec4(sceneColor + godRays, 1.0);
}
