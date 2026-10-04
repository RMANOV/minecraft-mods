package com.erik.redstonedust;

import com.erik.redstonedust.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedstoneDustMod implements ModInitializer {
	public static final String MOD_ID = "redstonedust";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("=== Eric's Redstone Dust Playground loading! ===");

		ModItems.register();

		LOGGER.info("=== Redstone Dust Playground ready! Let's learn redstone! ===");
	}
}
