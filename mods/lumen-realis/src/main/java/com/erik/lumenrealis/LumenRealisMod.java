package com.erik.lumenrealis;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LumenRealisMod implements ModInitializer {

    public static final String MOD_ID = "lumenrealis";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("=== Lumen Realis loading! Preparing photorealistic rendering engine... ===");
        LOGGER.info("=== Lumen Realis ready! ===");
    }
}
