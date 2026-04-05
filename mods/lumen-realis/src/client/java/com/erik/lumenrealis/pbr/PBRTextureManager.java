package com.erik.lumenrealis.pbr;

import com.erik.lumenrealis.LumenRealisMod;

/**
 * Manages PBR normal (_n.png) and specular (_s.png) texture atlases.
 * Phase 2: load from lumenrealis assets and build parallel atlases matching
 * the vanilla block atlas UV layout.
 */
public class PBRTextureManager {

    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        // Phase 2: scan assets/lumenrealis/textures/block/ for _n.png / _s.png
        // and stitch them into parallel GPU texture atlases whose UV coordinates
        // mirror the vanilla block texture atlas. The stitched atlases can then
        // be bound to shader sampler2D uniforms (normalAtlas, specularAtlas).
        LumenRealisMod.LOGGER.info("PBR texture manager initialized");
        initialized = true;
    }

    public static void reload() {
        initialized = false;
        init();
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
