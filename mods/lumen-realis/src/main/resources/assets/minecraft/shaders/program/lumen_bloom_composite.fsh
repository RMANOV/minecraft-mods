#version 330

uniform sampler2D DiffuseSampler;  // Original scene
uniform sampler2D BloomSampler;     // Blurred bloom
uniform float Intensity;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec3 scene = texture(DiffuseSampler, texCoord).rgb;
    vec3 bloom = texture(BloomSampler, texCoord).rgb;
    fragColor = vec4(scene + bloom * Intensity, 1.0);
}
