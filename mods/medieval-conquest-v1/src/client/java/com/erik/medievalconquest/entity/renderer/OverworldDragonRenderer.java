package com.erik.medievalconquest.entity.renderer;

import com.erik.medievalconquest.MedievalConquestMod;
import com.erik.medievalconquest.entity.OverworldDragonEntity;
import com.erik.medievalconquest.entity.model.OverworldDragonModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class OverworldDragonRenderer extends MobRenderer<OverworldDragonEntity, LivingEntityRenderState, OverworldDragonModel> {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
			MedievalConquestMod.MOD_ID, "textures/entity/overworld_dragon.png");

	public OverworldDragonRenderer(EntityRendererProvider.Context context) {
		super(context,
				new OverworldDragonModel(context.bakeLayer(OverworldDragonModel.LAYER_LOCATION)),
				1.5f);
	}

	@Override
	public Identifier getTextureLocation(LivingEntityRenderState state) {
		return TEXTURE;
	}

	@Override
	public LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}
}
