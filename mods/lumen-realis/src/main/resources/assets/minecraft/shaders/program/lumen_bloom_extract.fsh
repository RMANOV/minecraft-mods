#version 330

uniform sampler2D DiffuseSampler;
uniform float Threshold;
uniform float SoftKnee;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec3 color = texture(DiffuseSampler, texCoord).rgb;
    float brightness = dot(color, vec3(0.2126, 0.7152, 0.0722));
    float knee = Threshold * SoftKnee;
    float soft = brightness - Threshold + knee;
    soft = clamp(soft, 0.0, 2.0 * knee);
    soft = soft * soft / (4.0 * knee + 0.00001);
    float contribution = max(soft, brightness - Threshold) / max(brightness, 0.00001);
    fragColor = vec4(color * contribution, 1.0);
}
