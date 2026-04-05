// lumen_atmosphere.glsl — Rayleigh/Mie atmospheric scattering

float lumen_rayleighPhase(float cosAngle) {
    return 0.75 * (1.0 + cosAngle * cosAngle);
}

float lumen_miePhase(float cosAngle, float g) {
    float g2 = g * g;
    float num = 3.0 * (1.0 - g2) * (1.0 + cosAngle * cosAngle);
    float denom = 2.0 * (2.0 + g2) * pow(1.0 + g2 - 2.0 * g * cosAngle, 1.5);
    return num / (denom + 0.0001);
}

vec3 lumen_atmosphericScattering(vec3 viewDir, vec3 sunDir, float sunIntensity) {
    float cosAngle = dot(viewDir, sunDir);

    // Rayleigh: short wavelengths scatter more (blue sky)
    vec3 rayleighCoeff = vec3(5.5e-6, 13.0e-6, 22.4e-6);
    float rayleigh = lumen_rayleighPhase(cosAngle);

    // Mie: forward scattering (sun glow), g=0.76
    float mie = lumen_miePhase(cosAngle, 0.76);
    vec3 mieCoeff = vec3(21e-6);

    // Optical depth (simplified — constant atmosphere)
    float zenith = max(viewDir.y, 0.01);
    float opticalDepth = 1.0 / zenith;

    vec3 extinction = exp(-(rayleighCoeff + mieCoeff) * opticalDepth * 8500.0);

    vec3 inScatter = (rayleighCoeff * rayleigh + mieCoeff * mie) * sunIntensity;
    inScatter *= (1.0 - extinction) / (rayleighCoeff + mieCoeff + 0.0001);

    return inScatter;
}
