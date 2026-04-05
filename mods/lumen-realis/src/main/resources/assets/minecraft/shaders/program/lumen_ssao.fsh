#version 330

// Screen-Space Ambient Occlusion
// Hemisphere sampling with blue noise rotation for low-sample-count quality
uniform sampler2D DepthSampler;
uniform sampler2D NoiseSampler;
uniform vec2 TexelSize;
uniform float Radius;
uniform float Bias;
uniform int SampleCount;
uniform mat4 ProjectionMatrix;

in vec2 texCoord;
out vec4 fragColor;

// Reconstruct view-space position from depth
vec3 viewPosFromDepth(vec2 uv, float depth) {
    vec4 clipPos = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 viewPos = inverse(ProjectionMatrix) * clipPos;
    return viewPos.xyz / viewPos.w;
}

// 16-sample hemisphere kernel (pre-computed, cosine-weighted)
const vec3 kernel[16] = vec3[](
    vec3( 0.04, 0.04, 0.04), vec3(-0.09, 0.01, 0.06),
    vec3( 0.02,-0.06, 0.10), vec3( 0.13, 0.02, 0.01),
    vec3(-0.04, 0.12, 0.05), vec3( 0.07,-0.08, 0.12),
    vec3(-0.11, 0.05, 0.08), vec3( 0.01, 0.15, 0.03),
    vec3( 0.18,-0.02, 0.06), vec3(-0.06, 0.04, 0.20),
    vec3( 0.10, 0.10, 0.15), vec3(-0.15,-0.08, 0.10),
    vec3( 0.05, 0.22, 0.08), vec3(-0.20, 0.12, 0.05),
    vec3( 0.25, 0.06, 0.04), vec3(-0.08,-0.18, 0.15)
);

void main() {
    float depth = texture(DepthSampler, texCoord).r;
    if (depth >= 1.0) { fragColor = vec4(1.0); return; } // sky

    vec3 fragPos = viewPosFromDepth(texCoord, depth);

    // Random rotation from blue noise
    vec2 noiseScale = 1.0 / (TexelSize * 4.0);
    vec3 randomVec = texture(NoiseSampler, texCoord * noiseScale).rgb * 2.0 - 1.0;

    // Approximate normal from depth derivatives
    vec3 dFdxPos = viewPosFromDepth(texCoord + vec2(TexelSize.x, 0), texture(DepthSampler, texCoord + vec2(TexelSize.x, 0)).r) - fragPos;
    vec3 dFdyPos = viewPosFromDepth(texCoord + vec2(0, TexelSize.y), texture(DepthSampler, texCoord + vec2(0, TexelSize.y)).r) - fragPos;
    vec3 normal = normalize(cross(dFdxPos, dFdyPos));

    // Gramm-Schmidt to create TBN from random rotation
    vec3 tangent = normalize(randomVec - normal * dot(randomVec, normal));
    vec3 bitangent = cross(normal, tangent);
    mat3 TBN = mat3(tangent, bitangent, normal);

    float occlusion = 0.0;
    int samples = min(SampleCount, 16);
    for (int i = 0; i < samples; i++) {
        vec3 samplePos = fragPos + TBN * kernel[i] * Radius;
        // Project sample to screen
        vec4 offset = ProjectionMatrix * vec4(samplePos, 1.0);
        offset.xy = offset.xy / offset.w * 0.5 + 0.5;
        float sampleDepth = texture(DepthSampler, offset.xy).r;
        vec3 sampleViewPos = viewPosFromDepth(offset.xy, sampleDepth);
        // Range check + occlusion
        float rangeCheck = smoothstep(0.0, 1.0, Radius / abs(fragPos.z - sampleViewPos.z));
        occlusion += (sampleViewPos.z >= samplePos.z + Bias ? 1.0 : 0.0) * rangeCheck;
    }
    occlusion = 1.0 - (occlusion / float(samples));
    fragColor = vec4(vec3(occlusion), 1.0);
}
