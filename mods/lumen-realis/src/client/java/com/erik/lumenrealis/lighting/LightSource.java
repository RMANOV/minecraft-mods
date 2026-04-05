package com.erik.lumenrealis.lighting;

public record LightSource(
        double x, double y, double z,
        float r, float g, float b,
        float intensity, float radius
) {}
