#version 330

// Dual Kawase blur — more efficient than Gaussian for large radii
uniform sampler2D DiffuseSampler;
uniform vec2 TexelSize;
uniform float Radius;
uniform int Direction; // 0 = downsample, 1 = upsample

in vec2 texCoord;
out vec4 fragColor;

vec4 downsample(vec2 uv, vec2 halfpixel) {
    vec4 sum = texture(DiffuseSampler, uv) * 4.0;
    sum += texture(DiffuseSampler, uv - halfpixel);
    sum += texture(DiffuseSampler, uv + halfpixel);
    sum += texture(DiffuseSampler, uv + vec2(halfpixel.x, -halfpixel.y));
    sum += texture(DiffuseSampler, uv - vec2(halfpixel.x, -halfpixel.y));
    return sum / 8.0;
}

vec4 upsample(vec2 uv, vec2 halfpixel) {
    vec4 sum = texture(DiffuseSampler, uv + vec2(-halfpixel.x * 2.0, 0.0));
    sum += texture(DiffuseSampler, uv + vec2(-halfpixel.x, halfpixel.y)) * 2.0;
    sum += texture(DiffuseSampler, uv + vec2(0.0, halfpixel.y * 2.0));
    sum += texture(DiffuseSampler, uv + vec2(halfpixel.x, halfpixel.y)) * 2.0;
    sum += texture(DiffuseSampler, uv + vec2(halfpixel.x * 2.0, 0.0));
    sum += texture(DiffuseSampler, uv + vec2(halfpixel.x, -halfpixel.y)) * 2.0;
    sum += texture(DiffuseSampler, uv + vec2(0.0, -halfpixel.y * 2.0));
    sum += texture(DiffuseSampler, uv + vec2(-halfpixel.x, -halfpixel.y)) * 2.0;
    return sum / 12.0;
}

void main() {
    vec2 halfpixel = TexelSize * Radius * 0.5;
    if (Direction == 0) {
        fragColor = downsample(texCoord, halfpixel);
    } else {
        fragColor = upsample(texCoord, halfpixel);
    }
}
