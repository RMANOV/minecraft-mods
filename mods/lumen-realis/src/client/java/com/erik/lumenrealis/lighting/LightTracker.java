package com.erik.lumenrealis.lighting;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LightTracker {

    private static final int MAX_LIGHTS = 32;
    private static final int SCAN_RADIUS = 24;

    private static final List<LightSource> activeLights = new ArrayList<>();

    // Throttle: only scan every 4 ticks
    private static int tickCounter = 0;

    public static List<LightSource> getLights() {
        return activeLights;
    }

    public static void update(ClientLevel level, Camera camera) {
        tickCounter++;
        if (tickCounter % 4 != 0) return;

        activeLights.clear();

        Vec3 camPos = camera.position();
        BlockPos center = BlockPos.containing(camPos);
        double cx = camPos.x;
        double cy = camPos.y;
        double cz = camPos.z;

        List<LightSource> candidates = new ArrayList<>();

        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dy = -SCAN_RADIUS; dy <= SCAN_RADIUS; dy++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                    // Sphere culling: skip corners of the cube
                    if (dx * dx + dy * dy + dz * dz > SCAN_RADIUS * SCAN_RADIUS) continue;

                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);

                    int emission = state.getLightEmission();
                    if (emission <= 0) continue;

                    float[] color = getBlockColor(state);
                    float intensity = emission / 15.0f;
                    float radius = emission * 1.5f;

                    double bx = pos.getX() + 0.5;
                    double by = pos.getY() + 0.5;
                    double bz = pos.getZ() + 0.5;

                    candidates.add(new LightSource(bx, by, bz,
                            color[0], color[1], color[2],
                            intensity, radius));
                }
            }
        }

        // Sort by distance to camera, keep closest MAX_LIGHTS
        candidates.sort(Comparator.comparingDouble(l -> {
            double ddx = l.x() - cx;
            double ddy = l.y() - cy;
            double ddz = l.z() - cz;
            return ddx * ddx + ddy * ddy + ddz * ddz;
        }));

        int limit = Math.min(candidates.size(), MAX_LIGHTS);
        for (int i = 0; i < limit; i++) {
            activeLights.add(candidates.get(i));
        }
    }

    private static float[] getBlockColor(BlockState state) {
        // Warm orange: torches and lanterns
        if (state.is(Blocks.TORCH) || state.is(Blocks.WALL_TORCH)
                || state.is(Blocks.LANTERN)) {
            return new float[]{1.0f, 0.7f, 0.4f};
        }
        // Cold cyan: soul torches and soul lanterns
        if (state.is(Blocks.SOUL_TORCH) || state.is(Blocks.SOUL_WALL_TORCH)
                || state.is(Blocks.SOUL_LANTERN)) {
            return new float[]{0.3f, 0.8f, 0.9f};
        }
        // Warm white: glowstone
        if (state.is(Blocks.GLOWSTONE)) {
            return new float[]{1.0f, 0.9f, 0.7f};
        }
        // Cool white: sea lantern
        if (state.is(Blocks.SEA_LANTERN)) {
            return new float[]{0.7f, 0.9f, 1.0f};
        }
        // Hot orange: lava
        if (state.is(Blocks.LAVA)) {
            return new float[]{1.0f, 0.4f, 0.1f};
        }
        // Warm amber: shroomlight
        if (state.is(Blocks.SHROOMLIGHT)) {
            return new float[]{0.95f, 0.65f, 0.3f};
        }
        // Warm red-orange: powered redstone lamp
        if (state.is(Blocks.REDSTONE_LAMP)) {
            return new float[]{0.9f, 0.5f, 0.3f};
        }
        // Default: neutral warm white for any other emitter
        return new float[]{1.0f, 0.85f, 0.7f};
    }
}
