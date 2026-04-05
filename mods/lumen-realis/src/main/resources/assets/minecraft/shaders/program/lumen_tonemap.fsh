#version 330

// ACES Filmic tone mapping + color grading
uniform sampler2D DiffuseSampler;
uniform float Exposure;
uniform float Gamma;
uniform float WhiteBalanceShift; // -1 cool, 0 neutral, +1 warm
uniform float VignetteIntensity;

in vec2 texCoord;
out vec4 fragColor;

// ACES filmic tone mapping curve (fitted by Krzysztof Narkowicz)
vec3 ACESFilm(vec3 x) {
    float a = 2.51;
    float b = 0.03;
    float c = 2.43;
    float d = 0.59;
    float e = 0.14;
    return clamp((x * (a * x + b)) / (x * (c * x + d) + e), 0.0, 1.0);
}

void main() {
    vec3 color = texture(DiffuseSampler, texCoord).rgb;

    // Exposure adjustment
    color *= Exposure;

    // White balance (simple temperature shift)
    if (WhiteBalanceShift > 0.0) {
        color.r *= 1.0 + WhiteBalanceShift * 0.1;
        color.b *= 1.0 - WhiteBalanceShift * 0.08;
    } else {
        color.r *= 1.0 + WhiteBalanceShift * 0.08;
        color.b *= 1.0 - WhiteBalanceShift * 0.1;
    }

    // ACES tone mapping (HDR -> LDR)
    color = ACESFilm(color);

    // Gamma correction
    color = pow(color, vec3(1.0 / Gamma));

    // Vignette (darken corners)
    vec2 center = texCoord - 0.5;
    float vignette = 1.0 - dot(center, center) * VignetteIntensity;
    color *= clamp(vignette, 0.0, 1.0);

    fragColor = vec4(color, 1.0);
}
