package com.erik.medievalconquest;

import com.erik.medievalconquest.entity.renderer.OverworldDragonRenderer;
import com.erik.medievalconquest.entity.model.OverworldDragonModel;
import com.erik.medievalconquest.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class MedievalConquestClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Register dragon renderer
		EntityRendererRegistry.register(ModEntities.OVERWORLD_DRAGON,
				OverworldDragonRenderer::new);

		// Register dragon model layer
		EntityModelLayerRegistry.registerModelLayer(
				OverworldDragonModel.LAYER_LOCATION,
				OverworldDragonModel::createBodyLayer);

		MedievalConquestMod.LOGGER.info("Client initialized!");
	}
}
