// lumen_common.glsl — Lumen Realis shared utilities

float lumen_hash(vec2 p) {
    p = fract(p * vec2(443.8975, 397.2973));
    p += dot(p, p + 19.19);
    return fract(p.x * p.y);
}

float lumen_noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    float a = lumen_hash(i);
    float b = lumen_hash(i + vec2(1.0, 0.0));
    float c = lumen_hash(i + vec2(0.0, 1.0));
    float d = lumen_hash(i + vec2(1.0, 1.0));
    vec2 u = f * f * f * (f * (f * 6.0 - 15.0) + 10.0);
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

vec3 lumen_decodeNormal(vec2 encodedNormal) {
    vec2 xy = encodedNormal * 2.0 - 1.0;
    float z = sqrt(max(0.0, 1.0 - dot(xy, xy)));
    return normalize(vec3(xy, z));
}

float lumen_linearizeDepth(float depth, float near, float far) {
    float ndc = depth * 2.0 - 1.0;
    return (2.0 * near * far) / (far + near - ndc * (far - near));
}

vec3 lumen_srgbToLinear(vec3 srgb) {
    return pow(max(srgb, vec3(0.0)), vec3(2.2));
}

vec3 lumen_linearToSrgb(vec3 lin) {
    return pow(max(lin, vec3(0.0)), vec3(1.0 / 2.2));
}
