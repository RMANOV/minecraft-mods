package com.erik.lumenrealis.atmosphere;

public record AtmosphereState(
        float sunAngle,
        float sunX, float sunY, float sunZ,
        float sunR, float sunG, float sunB,
        float ambientR, float ambientG, float ambientB,
        float fogDensity,
        float timeOfDay
) {}
