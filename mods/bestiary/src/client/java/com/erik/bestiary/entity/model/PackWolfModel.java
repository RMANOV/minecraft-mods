package com.erik.bestiary.entity.model;

import com.erik.bestiary.BestiaryMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * PackWolfModel — a simple wolf-shaped model for PackWolfEntity.
 *
 * ★ V3 PATTERN: exact structure from OverworldDragonModel ★
 * - ModelLayerLocation registered in BestiaryClient
 * - createBodyLayer() returns a LayerDefinition
 * - setupAnim() applies animations based on LivingEntityRenderState
 *
 * Using LivingEntityRenderState (same as v3 dragon renderer) guarantees
 * compatibility and avoids the WolfModel/WolfRenderState complexity.
 *
 * Model layout (wolf-like proportions):
 * - body: main torso
 * - head: connected to front of body
 * - tail: connected to back of body
 * - 4 legs: front-left, front-right, back-left, back-right
 *
 * Texture: 64x32 (wolf standard UV)
 */
public class PackWolfModel extends EntityModel<LivingEntityRenderState> {

    /**
     * LAYER_LOCATION identifies this model to the EntityModelLayerRegistry.
     * Must be registered in BestiaryClient via EntityModelLayerRegistry.registerModelLayer().
     */
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(BestiaryMod.MOD_ID, "pack_wolf"),
            "main");

    // ─── Model Parts ──────────────────────────────────────────────────────────
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart tail;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftBackLeg;
    private final ModelPart rightBackLeg;

    public PackWolfModel(ModelPart root) {
        super(root);
        this.body         = root.getChild("body");
        this.head         = this.body.getChild("head");
        this.tail         = this.body.getChild("tail");
        this.leftFrontLeg = this.body.getChild("left_front_leg");
        this.rightFrontLeg = this.body.getChild("right_front_leg");
        this.leftBackLeg  = this.body.getChild("left_back_leg");
        this.rightBackLeg = this.body.getChild("right_back_leg");
    }

    /**
     * ★ V3 PATTERN: createBodyLayer() defines the 3D geometry ★
     *
     * Coordinates follow Minecraft's model convention:
     * - X: right (+) / left (-)
     * - Y: up (-) / down (+) [inverted from world space]
     * - Z: toward viewer (+) / away (-)
     *
     * addBox(x, y, z, width, height, depth):
     * - x, y, z: offset of the box corner FROM the part's origin
     * - width, height, depth: box size in pixels
     *
     * PartPose.offset(x, y, z): where the part ORIGIN is relative to parent
     *
     * Texture (64x32) UV layout:
     * - texOffs(u, v): top-left corner of this part's UV region on the texture
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDef = new MeshDefinition();
        PartDefinition root = meshDef.getRoot();

        // Body — main wolf torso (8x6x12 blocks)
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(18, 14).addBox(-4.0f, -3.0f, -6.0f, 8, 6, 12),
                PartPose.offset(0.0f, 14.0f, 0.0f));

        // Head — wolf's head (6x6x6, at front of body)
        body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0f, -3.0f, -4.0f, 6, 6, 6)
                        // Snout
                        .texOffs(16, 14).addBox(-1.5f, -1.0f, -6.0f, 3, 2, 2),
                PartPose.offset(0.0f, -1.0f, -6.0f));

        // Tail — angled upward, at back of body
        body.addOrReplaceChild("tail",
                CubeListBuilder.create()
                        .texOffs(9, 18).addBox(-1.0f, -6.0f, -1.0f, 2, 8, 2),
                PartPose.offsetAndRotation(0.0f, -1.0f, 6.0f, -0.3f, 0.0f, 0.0f));

        // Front legs (attached near front of body)
        body.addOrReplaceChild("left_front_leg",
                CubeListBuilder.create()
                        .texOffs(0, 18).addBox(-1.0f, 0.0f, -1.0f, 2, 6, 2),
                PartPose.offset(2.5f, 3.0f, -3.0f));

        body.addOrReplaceChild("right_front_leg",
                CubeListBuilder.create()
                        .texOffs(0, 18).mirror().addBox(-1.0f, 0.0f, -1.0f, 2, 6, 2),
                PartPose.offset(-2.5f, 3.0f, -3.0f));

        // Back legs (attached near back of body)
        body.addOrReplaceChild("left_back_leg",
                CubeListBuilder.create()
                        .texOffs(0, 26).addBox(-1.0f, 0.0f, -1.0f, 2, 6, 2),
                PartPose.offset(2.5f, 3.0f, 3.0f));

        body.addOrReplaceChild("right_back_leg",
                CubeListBuilder.create()
                        .texOffs(0, 26).mirror().addBox(-1.0f, 0.0f, -1.0f, 2, 6, 2),
                PartPose.offset(-2.5f, 3.0f, 3.0f));

        return LayerDefinition.create(meshDef, 64, 32);
    }

    /**
     * ★ V3 PATTERN: setupAnim() drives animations ★
     *
     * Called every render frame. We animate:
     * - Leg walking cycle using Mth.cos (same as vanilla quadruped)
     * - Head yaw/pitch tracking
     * - Tail wagging based on age
     *
     * state.ageInTicks: increases each game tick (20/sec)
     * Mth.cos(phase): oscillates between -1 and +1
     */
    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;

        // ─── Walking leg animation ─────────────────────────────────────────────
        // Each leg pair is 180° out of phase with the other pair
        float walkSpeed = 0.6662f;   // Standard Minecraft quadruped walk frequency
        float walkAmount = 1.4f;     // Swing amplitude

        this.leftFrontLeg.xRot  = Mth.cos(age * walkSpeed) * walkAmount;
        this.rightFrontLeg.xRot = Mth.cos(age * walkSpeed + (float)Math.PI) * walkAmount;
        this.leftBackLeg.xRot   = Mth.cos(age * walkSpeed + (float)Math.PI) * walkAmount;
        this.rightBackLeg.xRot  = Mth.cos(age * walkSpeed) * walkAmount;

        // ─── Head tracking ─────────────────────────────────────────────────────
        float headYaw = state.yRot * Mth.DEG_TO_RAD;
        float headPitch = state.xRot * Mth.DEG_TO_RAD;

        this.head.yRot  = headYaw * 0.5f;
        this.head.xRot  = headPitch * 0.5f;

        // ─── Tail wag ─────────────────────────────────────────────────────────
        this.tail.yRot = Mth.cos(age * 0.31831f) * 0.3f;
    }
}
