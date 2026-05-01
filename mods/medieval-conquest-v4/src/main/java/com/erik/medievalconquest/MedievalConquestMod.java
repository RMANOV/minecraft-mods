package com.erik.medievalconquest;

import com.erik.medievalconquest.event.DynamicLightingHandler;
import com.erik.medievalconquest.event.TreeMechanicsHandler;
import com.erik.medievalconquest.registry.ModBlocks;
import com.erik.medievalconquest.registry.ModBrewingRecipes;
import com.erik.medievalconquest.registry.ModEntities;
import com.erik.medievalconquest.registry.ModItems;
import com.erik.medievalconquest.world.LilacTreeGenerator;
import com.erik.medievalconquest.world.ModWorldGen;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MedievalConquestMod implements ModInitializer {
	public static final String MOD_ID = "medievalconquest";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("=== Erik's Medieval Conquest loading! ===");

		ModBlocks.register();
		ModItems.register();
		ModBrewingRecipes.register();
		ModEntities.register();
		TreeMechanicsHandler.register();
		ModWorldGen.register();
		LilacTreeGenerator.register();
		DynamicLightingHandler.register();

		LOGGER.info("=== Medieval Conquest v4 ready! Time to conquer! ===");
	}
}
