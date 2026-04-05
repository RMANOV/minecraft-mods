package com.erik.lumenrealis.pbr;

/**
 * PBR surface parameters for common Minecraft material categories.
 * roughness: 0 = mirror-smooth, 1 = fully diffuse.
 * metallic: 0 = dielectric, 1 = fully metallic.
 * emissive: 0 = no self-emission, 1 = full emissive intensity.
 */
public record MaterialProperties(float roughness, float metallic, float emissive) {

    public static final MaterialProperties STONE    = new MaterialProperties(0.85f, 0.0f, 0.0f);
    public static final MaterialProperties WOOD     = new MaterialProperties(0.70f, 0.0f, 0.0f);
    public static final MaterialProperties METAL    = new MaterialProperties(0.20f, 0.9f, 0.0f);
    public static final MaterialProperties EARTH    = new MaterialProperties(0.95f, 0.0f, 0.0f);
    public static final MaterialProperties GLASS    = new MaterialProperties(0.05f, 0.0f, 0.0f);
    public static final MaterialProperties EMISSIVE = new MaterialProperties(0.60f, 0.0f, 0.85f);
    public static final MaterialProperties DEFAULT  = new MaterialProperties(0.70f, 0.0f, 0.0f);
}
