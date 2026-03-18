package com.erik.medievalconquest.entity.model;

import com.erik.medievalconquest.MedievalConquestMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Simple dragon model — blocky, medieval style.
 * Body + head + 4 legs + tail + 2 wings.
 * Textures map to a 128x64 texture sheet.
 */
public class OverworldDragonModel extends EntityModel<LivingEntityRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "overworld_dragon"),
			"main");

	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart tail;
	private final ModelPart leftWing;
	private final ModelPart rightWing;
	private final ModelPart leftFrontLeg;
	private final ModelPart rightFrontLeg;
	private final ModelPart leftBackLeg;
	private final ModelPart rightBackLeg;

	public OverworldDragonModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.head = root.getChild("head");
		this.tail = root.getChild("tail");
		this.leftWing = root.getChild("left_wing");
		this.rightWing = root.getChild("right_wing");
		this.leftFrontLeg = root.getChild("left_front_leg");
		this.rightFrontLeg = root.getChild("right_front_leg");
		this.leftBackLeg = root.getChild("left_back_leg");
		this.rightBackLeg = root.getChild("right_back_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDef = new MeshDefinition();
		PartDefinition root = meshDef.getRoot();

		// Body — large center mass
		root.addOrReplaceChild("body", CubeListBuilder.create()
						.texOffs(0, 0).addBox(-8.0f, -8.0f, -12.0f, 16, 12, 24),
				PartPose.offset(0.0f, 12.0f, 0.0f));

		// Head — smaller box in front
		root.addOrReplaceChild("head", CubeListBuilder.create()
						.texOffs(0, 36).addBox(-4.0f, -4.0f, -8.0f, 8, 8, 8),
				PartPose.offset(0.0f, 8.0f, -14.0f));

		// Tail — long thin extension
		root.addOrReplaceChild("tail", CubeListBuilder.create()
						.texOffs(56, 0).addBox(-2.0f, -2.0f, 0.0f, 4, 4, 16),
				PartPose.offset(0.0f, 12.0f, 12.0f));

		// Wings — flat panels
		root.addOrReplaceChild("left_wing", CubeListBuilder.create()
						.texOffs(32, 36).addBox(0.0f, -1.0f, -6.0f, 16, 2, 12),
				PartPose.offset(8.0f, 6.0f, 0.0f));

		root.addOrReplaceChild("right_wing", CubeListBuilder.create()
						.texOffs(32, 36).mirror().addBox(-16.0f, -1.0f, -6.0f, 16, 2, 12),
				PartPose.offset(-8.0f, 6.0f, 0.0f));

		// Front legs
		root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
						.texOffs(88, 36).addBox(-2.0f, 0.0f, -2.0f, 4, 8, 4),
				PartPose.offset(6.0f, 16.0f, -8.0f));

		root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
						.texOffs(88, 36).mirror().addBox(-2.0f, 0.0f, -2.0f, 4, 8, 4),
				PartPose.offset(-6.0f, 16.0f, -8.0f));

		// Back legs
		root.addOrReplaceChild("left_back_leg", CubeListBuilder.create()
						.texOffs(104, 36).addBox(-2.0f, 0.0f, -2.0f, 4, 8, 4),
				PartPose.offset(6.0f, 16.0f, 8.0f));

		root.addOrReplaceChild("right_back_leg", CubeListBuilder.create()
						.texOffs(104, 36).mirror().addBox(-2.0f, 0.0f, -2.0f, 4, 8, 4),
				PartPose.offset(-6.0f, 16.0f, 8.0f));

		return LayerDefinition.create(meshDef, 128, 64);
	}

	@Override
	public void setupAnim(LivingEntityRenderState state) {
		super.setupAnim(state);

		// Head follows look direction
		this.head.yRot = state.yRot * ((float) Math.PI / 180f);
		this.head.xRot = state.xRot * ((float) Math.PI / 180f);

		// Leg animation (walking)
		float limbSwing = state.walkAnimationPos;
		float limbSwingAmount = state.walkAnimationSpeed;
		float legSwing = Mth.cos(limbSwing * 0.6662f) * 1.4f * limbSwingAmount;
		this.leftFrontLeg.xRot = legSwing;
		this.rightFrontLeg.xRot = -legSwing;
		this.leftBackLeg.xRot = -legSwing;
		this.rightBackLeg.xRot = legSwing;

		// Tail sway
		this.tail.yRot = Mth.cos(state.ageInTicks * 0.1f) * 0.2f;

		// Wing flap (idle)
		float wingFlap = Mth.cos(state.ageInTicks * 0.15f) * 0.3f;
		this.leftWing.zRot = -0.3f + wingFlap;
		this.rightWing.zRot = 0.3f - wingFlap;
	}
}
