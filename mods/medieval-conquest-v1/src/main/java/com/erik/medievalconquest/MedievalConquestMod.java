package com.erik.medievalconquest;

import com.erik.medievalconquest.command.ModCommands;
import com.erik.medievalconquest.event.TreeMechanicsHandler;
import com.erik.medievalconquest.registry.ModBlocks;
import com.erik.medievalconquest.registry.ModEntities;
import com.erik.medievalconquest.registry.ModItems;
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
		ModEntities.register();
		TreeMechanicsHandler.register();
		ModWorldGen.register();
		ModCommands.register();

		LOGGER.info("=== Medieval Conquest ready! Time to conquer! ===");
	}
}
