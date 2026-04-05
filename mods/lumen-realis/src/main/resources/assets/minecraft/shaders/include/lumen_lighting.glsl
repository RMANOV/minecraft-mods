// lumen_lighting.glsl — Dynamic point light calculations

vec3 lumen_pointLight(vec3 fragPos, vec3 N, vec3 V, vec3 albedo,
                      float metallic, float roughness,
                      vec3 lightPos, vec3 lightColor, float lightRadius) {
    vec3 toLight = lightPos - fragPos;
    float dist = length(toLight);

    if (dist > lightRadius) return vec3(0.0);

    vec3 L = toLight / dist;

    // Quadratic attenuation with smooth falloff at radius edge
    float attenuation = 1.0 / (1.0 + 0.09 * dist + 0.032 * dist * dist);
    float edgeFade = 1.0 - smoothstep(lightRadius * 0.75, lightRadius, dist);
    attenuation *= edgeFade;

    return lumen_evaluatePBR(albedo, metallic, roughness, N, V, L, lightColor, attenuation);
}
