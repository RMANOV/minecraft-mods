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
 * Detailed dragon model — long neck, segmented tail, large articulated wings.
 * 256x128 texture sheet for high detail.
 *
 * Hierarchy: body → neck1 → neck2 → head → jaw
 *                 → tail1 → tail2 → tail3
 *                 → left/right wing inner → outer
 *                 → 4 legs
 */
public class OverworldDragonModel extends EntityModel<LivingEntityRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "overworld_dragon"),
			"main");

	private final ModelPart body;
	private final ModelPart neck1;
	private final ModelPart neck2;
	private final ModelPart head;
	private final ModelPart jaw;
	private final ModelPart tail1;
	private final ModelPart tail2;
	private final ModelPart tail3;
	private final ModelPart leftWingInner;
	private final ModelPart leftWingOuter;
	private final ModelPart rightWingInner;
	private final ModelPart rightWingOuter;
	private final ModelPart leftFrontLeg;
	private final ModelPart rightFrontLeg;
	private final ModelPart leftBackLeg;
	private final ModelPart rightBackLeg;

	public OverworldDragonModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.neck1 = this.body.getChild("neck1");
		this.neck2 = this.neck1.getChild("neck2");
		this.head = this.neck2.getChild("head");
		this.jaw = this.head.getChild("jaw");
		this.tail1 = this.body.getChild("tail1");
		this.tail2 = this.tail1.getChild("tail2");
		this.tail3 = this.tail2.getChild("tail3");
		this.leftWingInner = this.body.getChild("left_wing_inner");
		this.leftWingOuter = this.leftWingInner.getChild("left_wing_outer");
		this.rightWingInner = this.body.getChild("right_wing_inner");
		this.rightWingOuter = this.rightWingInner.getChild("right_wing_outer");
		this.leftFrontLeg = this.body.getChild("left_front_leg");
		this.rightFrontLeg = this.body.getChild("right_front_leg");
		this.leftBackLeg = this.body.getChild("left_back_leg");
		this.rightBackLeg = this.body.getChild("right_back_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDef = new MeshDefinition();
		PartDefinition root = meshDef.getRoot();

		// Body — elongated torso
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
						.texOffs(0, 0).addBox(-7.0f, -5.0f, -10.0f, 14, 10, 20),
				PartPose.offset(0.0f, 14.0f, 0.0f));

		// Neck segment 1
		PartDefinition neck1 = body.addOrReplaceChild("neck1", CubeListBuilder.create()
						.texOffs(0, 30).addBox(-3.0f, -3.0f, -7.0f, 6, 6, 7),
				PartPose.offset(0.0f, -2.0f, -10.0f));

		// Neck segment 2
		PartDefinition neck2 = neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
						.texOffs(26, 30).addBox(-2.5f, -2.5f, -6.0f, 5, 5, 6),
				PartPose.offset(0.0f, 0.0f, -7.0f));

		// Head
		PartDefinition head = neck2.addOrReplaceChild("head", CubeListBuilder.create()
						.texOffs(0, 43).addBox(-4.0f, -3.0f, -8.0f, 8, 6, 8),
				PartPose.offset(0.0f, 0.0f, -6.0f));

		// Lower jaw
		head.addOrReplaceChild("jaw", CubeListBuilder.create()
						.texOffs(32, 43).addBox(-3.0f, 0.0f, -7.0f, 6, 2, 7),
				PartPose.offset(0.0f, 3.0f, 0.0f));

		// Tail segment 1
		PartDefinition tail1 = body.addOrReplaceChild("tail1", CubeListBuilder.create()
						.texOffs(68, 0).addBox(-3.0f, -3.0f, 0.0f, 6, 6, 10),
				PartPose.offset(0.0f, -1.0f, 10.0f));

		// Tail segment 2
		PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
						.texOffs(100, 0).addBox(-2.0f, -2.0f, 0.0f, 4, 4, 10),
				PartPose.offset(0.0f, 0.0f, 10.0f));

		// Tail tip
		tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
						.texOffs(128, 0).addBox(-1.5f, -1.5f, 0.0f, 3, 3, 12),
				PartPose.offset(0.0f, 0.0f, 10.0f));

		// Left wing — inner segment
		PartDefinition leftWingInner = body.addOrReplaceChild("left_wing_inner", CubeListBuilder.create()
						.texOffs(0, 57).addBox(0.0f, -1.0f, -5.0f, 16, 2, 10),
				PartPose.offset(7.0f, -5.0f, 0.0f));

		// Left wing — outer segment
		leftWingInner.addOrReplaceChild("left_wing_outer", CubeListBuilder.create()
						.texOffs(0, 69).addBox(0.0f, 0.0f, -4.0f, 20, 1, 8),
				PartPose.offset(16.0f, 0.0f, 0.0f));

		// Right wing — inner segment (mirrored)
		PartDefinition rightWingInner = body.addOrReplaceChild("right_wing_inner", CubeListBuilder.create()
						.texOffs(0, 57).mirror().addBox(-16.0f, -1.0f, -5.0f, 16, 2, 10),
				PartPose.offset(-7.0f, -5.0f, 0.0f));

		// Right wing — outer segment (mirrored)
		rightWingInner.addOrReplaceChild("right_wing_outer", CubeListBuilder.create()
						.texOffs(0, 69).mirror().addBox(-20.0f, 0.0f, -4.0f, 20, 1, 8),
				PartPose.offset(-16.0f, 0.0f, 0.0f));

		// Front legs
		body.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
						.texOffs(48, 0).addBox(-2.0f, 0.0f, -2.0f, 4, 10, 4),
				PartPose.offset(5.0f, 5.0f, -6.0f));

		body.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
						.texOffs(48, 0).mirror().addBox(-2.0f, 0.0f, -2.0f, 4, 10, 4),
				PartPose.offset(-5.0f, 5.0f, -6.0f));

		// Back legs (slightly larger)
		body.addOrReplaceChild("left_back_leg", CubeListBuilder.create()
						.texOffs(48, 14).addBox(-2.5f, 0.0f, -2.5f, 5, 10, 5),
				PartPose.offset(5.0f, 5.0f, 6.0f));

		body.addOrReplaceChild("right_back_leg", CubeListBuilder.create()
						.texOffs(48, 14).mirror().addBox(-2.5f, 0.0f, -2.5f, 5, 10, 5),
				PartPose.offset(-5.0f, 5.0f, 6.0f));

		return LayerDefinition.create(meshDef, 256, 128);
	}

	@Override
	public void setupAnim(LivingEntityRenderState state) {
		super.setupAnim(state);
		float age = state.ageInTicks;

		// ─── Wing flap (primary flight animation) ─────────────
		float wingFlap = Mth.cos(age * 0.2f) * 0.6f;
		this.leftWingInner.zRot = -0.5f + wingFlap;
		this.rightWingInner.zRot = 0.5f - wingFlap;

		// Outer wings flap with more amplitude and slight phase delay
		float outerFlap = Mth.cos(age * 0.2f - 0.5f) * 0.4f;
		this.leftWingOuter.zRot = -0.3f + outerFlap;
		this.rightWingOuter.zRot = 0.3f - outerFlap;

		// ─── Head tracking (distributed across neck chain) ────
		float headYaw = state.yRot * Mth.DEG_TO_RAD;
		float headPitch = state.xRot * Mth.DEG_TO_RAD;

		this.neck1.yRot = headYaw * 0.2f + Mth.cos(age * 0.08f) * 0.05f;
		this.neck1.xRot = headPitch * 0.2f + Mth.cos(age * 0.1f) * 0.08f;
		this.neck2.yRot = headYaw * 0.3f;
		this.neck2.xRot = headPitch * 0.3f + Mth.cos(age * 0.1f + 1.0f) * 0.08f;
		this.head.yRot = headYaw * 0.3f;
		this.head.xRot = headPitch * 0.3f;

		// ─── Jaw — slightly open, breathes ────────────────────
		this.jaw.xRot = 0.1f + Mth.cos(age * 0.15f) * 0.05f;

		// ─── Tail sway — increasing amplitude per segment ─────
		this.tail1.yRot = Mth.cos(age * 0.08f) * 0.15f;
		this.tail1.xRot = Mth.cos(age * 0.06f) * 0.05f;
		this.tail2.yRot = Mth.cos(age * 0.08f + 0.5f) * 0.2f;
		this.tail3.yRot = Mth.cos(age * 0.08f + 1.0f) * 0.3f;

		// ─── Legs — dangling while flying ─────────────────────
		float legDangle = Mth.cos(age * 0.1f) * 0.1f;
		this.leftFrontLeg.xRot = 0.3f + legDangle;
		this.rightFrontLeg.xRot = 0.3f - legDangle;
		this.leftBackLeg.xRot = 0.2f + legDangle * 0.5f;
		this.rightBackLeg.xRot = 0.2f - legDangle * 0.5f;
	}
}
