#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:chunksection.glsl>
#moj_import <minecraft:lumen_common.glsl>
#moj_import <minecraft:lumen_pbr.glsl>

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec3 fragWorldPos;
in vec3 fragNormal;
in vec3 fragViewPos;
in vec2 fragLightmapUV;

out vec4 fragColor;

// ── RGSS Antialiasing (from Medieval Conquest v4) ──────────────────────

vec4 sampleNearest(sampler2D sampler, vec2 uv, vec2 pixelSize, vec2 du, vec2 dv, vec2 texelScreenSize) {
    vec2 uvTexelCoords = uv / pixelSize;
    vec2 texelCenter = round(uvTexelCoords) - 0.5f;
    vec2 texelOffset = uvTexelCoords - texelCenter;
    texelOffset = (texelOffset - 0.5f) * pixelSize / texelScreenSize + 0.5f;
    texelOffset = clamp(texelOffset, 0.0f, 1.0f);
    uv = (texelCenter + texelOffset) * pixelSize;
    return textureGrad(sampler, uv, du, dv);
}

vec4 sampleNearest(sampler2D source, vec2 uv, vec2 pixelSize) {
    vec2 du = dFdx(uv);
    vec2 dv = dFdy(uv);
    vec2 texelScreenSize = sqrt(du * du + dv * dv);
    return sampleNearest(source, uv, pixelSize, du, dv, texelScreenSize);
}

vec4 sampleRGSS(sampler2D source, vec2 uv, vec2 pixelSize) {
    vec2 du = dFdx(uv);
    vec2 dv = dFdy(uv);
    vec2 texelScreenSize = sqrt(du * du + dv * dv);
    float maxTexelSize = max(texelScreenSize.x, texelScreenSize.y);
    float minPixelSize = min(pixelSize.x, pixelSize.y);

    float transitionStart = minPixelSize * 1.0;
    float transitionEnd = minPixelSize * 2.0;
    float blendFactor = smoothstep(transitionStart, transitionEnd, maxTexelSize);

    float duLength = length(du);
    float dvLength = length(dv);
    float effectiveDerivative = sqrt(min(duLength, dvLength) * max(duLength, dvLength));
    float mipLevelExact = max(0.0, log2(effectiveDerivative / minPixelSize));
    float mipLevelLow = floor(mipLevelExact);
    float mipLevelHigh = mipLevelLow + 1.0;
    float mipBlend = fract(mipLevelExact);

    const vec2 offsets[4] = vec2[](
        vec2(0.125, 0.375), vec2(-0.125, -0.375),
        vec2(0.375, -0.125), vec2(-0.375, 0.125)
    );

    vec4 rgssColorLow = vec4(0.0);
    vec4 rgssColorHigh = vec4(0.0);
    for (int i = 0; i < 4; ++i) {
        vec2 sampleUV = uv + offsets[i] * pixelSize;
        rgssColorLow += textureLod(source, sampleUV, mipLevelLow);
        rgssColorHigh += textureLod(source, sampleUV, mipLevelHigh);
    }
    rgssColorLow *= 0.25;
    rgssColorHigh *= 0.25;

    vec4 rgssColor = mix(rgssColorLow, rgssColorHigh, mipBlend);
    vec4 nearestColor = sampleNearest(source, uv, pixelSize, du, dv, texelScreenSize);
    return mix(nearestColor, rgssColor, blendFactor);
}

// ── PBR Lighting ───────────────────────────────────────────────────────

void main() {
    // Sample albedo with RGSS antialiasing
    vec4 texColor = (UseRgss == 1
        ? sampleRGSS(Sampler0, texCoord0, 1.0f / TextureSize)
        : sampleNearest(Sampler0, texCoord0, 1.0f / TextureSize));

    // === PBR PATH ===
    // Convert albedo to linear space for physically correct lighting
    vec3 albedoLinear = lumen_srgbToLinear(texColor.rgb);

    // Default material properties (Phase 1 — no PBR textures yet)
    float roughness = 0.7;
    float metallic = 0.0;

    // Surface normal (flat shading from vertex normal)
    vec3 N = normalize(fragNormal);

    // View direction (from fragment to camera)
    vec3 V = normalize(-fragViewPos);

    // Hardcoded sun direction and color (Phase 1)
    // Will be replaced with uniforms from AtmosphereCalculator in Phase 3
    vec3 sunDir = normalize(vec3(0.4, 0.75, 0.3));
    vec3 sunColor = vec3(1.0, 0.95, 0.88);
    float sunIntensity = 2.5;

    // Ambient light from vanilla lightmap
    // fragLightmapUV.x = block light (torches), fragLightmapUV.y = sky light
    float skyLight = fragLightmapUV.y;
    float blockLight = fragLightmapUV.x;
    vec3 ambient = vec3(0.08, 0.09, 0.12) + vec3(0.12, 0.14, 0.18) * skyLight
                 + vec3(0.9, 0.6, 0.3) * blockLight * 0.3;

    // Directional sun light (PBR)
    vec3 directLight = lumen_evaluatePBR(albedoLinear, metallic, roughness,
                                          N, V, sunDir, sunColor, sunIntensity);

    // Attenuate sun by sky light factor (underground = no sun)
    directLight *= smoothstep(0.0, 0.5, skyLight);

    // Combine: ambient + direct + vanilla vertex color contribution
    vec3 litColor = ambient * albedoLinear + directLight;

    // Blend with vanilla vertex color for biome tinting
    litColor *= vertexColor.rgb;

    // Convert back to sRGB for display
    vec3 finalColor = lumen_linearToSrgb(litColor);

    vec4 color = vec4(finalColor, texColor.a * vertexColor.a);

    // Fog and visibility
    color = mix(FogColor * vec4(1, 1, 1, color.a), color, ChunkVisibility);

#ifdef ALPHA_CUTOUT
    if (color.a < ALPHA_CUTOUT) {
        discard;
    }
#endif

    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance,
                           FogEnvironmentalStart, FogEnvironmentalEnd,
                           FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
