package com.erik.arcanenaturalis.entity.model;

import com.erik.arcanenaturalis.ArcaneNaturalisMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * ButterflyModel — a small body + two animated wing planes.
 *
 * Texture: 32x32 (matches sized(0.4f, 0.3f) entity — small!)
 *
 * Hierarchy:
 *   root
 *   └── body (tiny elongated cube)
 *       ├── left_wing  (flat plane, pivots from body center)
 *       └── right_wing (mirrored)
 *
 * Wing animation: cosine wave on Z rotation (flap up and down).
 * Body bob: gentle sine wave on Y — makes it look like it's hovering.
 *
 * The model follows the exact same pattern as OverworldDragonModel:
 *   - createBodyLayer() defines geometry statically
 *   - setupAnim() runs each frame to animate
 *   - Constructor extracts named ModelParts from the root
 */
public class ButterflyModel extends EntityModel<LivingEntityRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(ArcaneNaturalisMod.MOD_ID, "butterfly"),
            "main");

    private final ModelPart body;
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public ButterflyModel(ModelPart root) {
        super(root);
        this.body      = root.getChild("body");
        this.leftWing  = this.body.getChild("left_wing");
        this.rightWing = this.body.getChild("right_wing");
    }

    /**
     * Defines the model's geometry. Called once, cached as a baked layer.
     * Returns a LayerDefinition with the mesh and texture dimensions.
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDef = new MeshDefinition();
        PartDefinition root    = meshDef.getRoot();

        // Body: 4W × 3H × 6D — tiny elongated pill shape
        // Offset on texture: top-left corner (0, 0)
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.0f, -1.5f, -3.0f, 4, 3, 6),
                PartPose.offset(0.0f, 23.5f, 0.0f));

        // Left wing: a thin flat plane (1 block thick)
        // Pivots from the body's left side (x=+2 from center)
        // zRot of -0.5 = default slight upward angle
        body.addOrReplaceChild("left_wing",
                CubeListBuilder.create()
                        .texOffs(0, 9)
                        .addBox(0.0f, -1.0f, -5.0f, 10, 1, 10),
                PartPose.offsetAndRotation(2.0f, 0.0f, 0.0f,
                        0.0f, 0.0f, -0.5f));  // slight upward tilt at rest

        // Right wing: mirrored on X axis
        body.addOrReplaceChild("right_wing",
                CubeListBuilder.create()
                        .texOffs(0, 9).mirror()
                        .addBox(-10.0f, -1.0f, -5.0f, 10, 1, 10),
                PartPose.offsetAndRotation(-2.0f, 0.0f, 0.0f,
                        0.0f, 0.0f, 0.5f));   // mirrored upward tilt

        // 32×32 texture sheet
        return LayerDefinition.create(meshDef, 32, 32);
    }

    /**
     * Called every frame to update part rotations based on the entity state.
     *
     * Wing flap: cosine wave on Z-axis rotation.
     *   - Frequency 0.3f → about 1 full flap per second (20 ticks/s ÷ 0.3)
     *   - Amplitude 0.8f → ~46° flap range
     *   - The body bobs up/down in sync (Y translation via yRot trick isn't used here)
     */
    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float age = state.ageInTicks;

        // Wing flap: symmetrical — left goes up when right goes down
        float flap = Mth.cos(age * 0.3f) * 0.8f;
        this.leftWing.zRot  = -0.5f - flap;   // negative = up for left wing
        this.rightWing.zRot = 0.5f + flap;    // positive = up for right wing (mirrored)

        // Gentle hover bob on the body
        this.body.y = 23.5f + Mth.sin(age * 0.15f) * 0.3f;
    }
}
