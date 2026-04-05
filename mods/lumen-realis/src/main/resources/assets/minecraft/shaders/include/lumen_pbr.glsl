// lumen_pbr.glsl — Cook-Torrance BRDF for physically-based rendering

#define LUMEN_PI 3.14159265359

// GGX/Trowbridge-Reitz normal distribution function
// Describes the statistical distribution of microfacet normals.
// Higher roughness = wider distribution = more diffuse highlights.
float lumen_distributionGGX(vec3 N, vec3 H, float roughness) {
    float a = roughness * roughness;
    float a2 = a * a;
    float NdotH = max(dot(N, H), 0.0);
    float NdotH2 = NdotH * NdotH;
    float denom = NdotH2 * (a2 - 1.0) + 1.0;
    return a2 / (LUMEN_PI * denom * denom + 0.0001);
}

// Schlick-GGX geometry function (single direction)
// Models self-shadowing of microfacets.
float lumen_geometrySchlickGGX(float NdotV, float roughness) {
    float r = roughness + 1.0;
    float k = (r * r) / 8.0; // remapping for direct lighting
    return NdotV / (NdotV * (1.0 - k) + k + 0.0001);
}

// Smith's method: combine geometry obstruction for both view and light directions
float lumen_geometrySmith(vec3 N, vec3 V, vec3 L, float roughness) {
    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    return lumen_geometrySchlickGGX(NdotV, roughness)
         * lumen_geometrySchlickGGX(NdotL, roughness);
}

// Fresnel-Schlick approximation
// At grazing angles, all surfaces become mirrors (Fresnel effect).
// F0 = base reflectivity: 0.04 for dielectrics, albedo for metals.
vec3 lumen_fresnelSchlick(float cosTheta, vec3 F0) {
    return F0 + (1.0 - F0) * pow(clamp(1.0 - cosTheta, 0.0, 1.0), 5.0);
}

// Full PBR evaluation for a single light source.
// Returns the combined diffuse + specular contribution.
vec3 lumen_evaluatePBR(vec3 albedo, float metallic, float roughness,
                       vec3 N, vec3 V, vec3 L, vec3 lightColor, float lightIntensity) {
    vec3 H = normalize(V + L);

    // F0: base reflectivity — blend between dielectric (0.04) and metal (albedo)
    vec3 F0 = mix(vec3(0.04), albedo, metallic);

    // Cook-Torrance specular BRDF
    float D = lumen_distributionGGX(N, H, roughness);
    float G = lumen_geometrySmith(N, V, L, roughness);
    vec3  F = lumen_fresnelSchlick(max(dot(H, V), 0.0), F0);

    vec3 numerator = D * G * F;
    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    float denominator = 4.0 * NdotV * NdotL + 0.0001;
    vec3 specular = numerator / denominator;

    // Energy conservation: diffuse gets what specular doesn't reflect
    vec3 kD = (vec3(1.0) - F) * (1.0 - metallic);

    // Lambertian diffuse
    vec3 diffuse = kD * albedo / LUMEN_PI;

    return (diffuse + specular) * lightColor * lightIntensity * NdotL;
}
