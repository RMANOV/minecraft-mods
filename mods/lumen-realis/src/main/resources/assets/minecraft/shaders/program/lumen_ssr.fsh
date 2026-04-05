#version 330

// Screen-Space Reflections via ray marching
uniform sampler2D ColorSampler;
uniform sampler2D DepthSampler;
uniform vec2 TexelSize;
uniform int MaxSteps;
uniform float StepSize;
uniform mat4 ProjectionMatrix;
uniform mat4 ViewMatrix;

in vec2 texCoord;
out vec4 fragColor;

vec3 viewPosFromDepth(vec2 uv, float depth) {
    vec4 clipPos = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 viewPos = inverse(ProjectionMatrix) * clipPos;
    return viewPos.xyz / viewPos.w;
}

void main() {
    float depth = texture(DepthSampler, texCoord).r;
    vec3 color = texture(ColorSampler, texCoord).rgb;

    if (depth >= 1.0) { fragColor = vec4(color, 1.0); return; }

    vec3 fragPos = viewPosFromDepth(texCoord, depth);

    // Approximate normal from depth
    vec3 dFdxPos = viewPosFromDepth(texCoord + vec2(TexelSize.x, 0), texture(DepthSampler, texCoord + vec2(TexelSize.x, 0)).r) - fragPos;
    vec3 dFdyPos = viewPosFromDepth(texCoord + vec2(0, TexelSize.y), texture(DepthSampler, texCoord + vec2(0, TexelSize.y)).r) - fragPos;
    vec3 normal = normalize(cross(dFdxPos, dFdyPos));

    vec3 viewDir = normalize(fragPos);
    vec3 reflectDir = reflect(viewDir, normal);

    // Ray march in view space
    vec3 hitColor = vec3(0.0);
    float reflectivity = 0.0;

    vec3 rayPos = fragPos;
    for (int i = 0; i < MaxSteps; i++) {
        rayPos += reflectDir * StepSize * (1.0 + float(i) * 0.1);

        // Project to screen
        vec4 projected = ProjectionMatrix * vec4(rayPos, 1.0);
        vec2 screenUV = projected.xy / projected.w * 0.5 + 0.5;

        if (screenUV.x < 0.0 || screenUV.x > 1.0 || screenUV.y < 0.0 || screenUV.y > 1.0) break;

        float sampleDepth = texture(DepthSampler, screenUV).r;
        vec3 samplePos = viewPosFromDepth(screenUV, sampleDepth);

        if (rayPos.z < samplePos.z && rayPos.z > samplePos.z - StepSize * 2.0) {
            hitColor = texture(ColorSampler, screenUV).rgb;
            // Fade at screen edges
            float edgeFade = 1.0 - pow(max(abs(screenUV.x - 0.5), abs(screenUV.y - 0.5)) * 2.0, 3.0);
            reflectivity = clamp(edgeFade * 0.4, 0.0, 1.0);
            break;
        }
    }

    fragColor = vec4(mix(color, hitColor, reflectivity), 1.0);
}
