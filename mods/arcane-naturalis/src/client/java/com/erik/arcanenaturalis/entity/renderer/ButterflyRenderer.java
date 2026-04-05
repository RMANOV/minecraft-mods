package com.erik.arcanenaturalis.entity.renderer;

import com.erik.arcanenaturalis.ArcaneNaturalisMod;
import com.erik.arcanenaturalis.entity.ButterflyEntity;
import com.erik.arcanenaturalis.entity.model.ButterflyModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/**
 * ButterflyRenderer — the rendering bridge between ButterflyEntity and ButterflyModel.
 *
 * MobRenderer<EntityType, RenderState, ModelType> provides the full rendering pipeline:
 *   1. Entity → createRenderState() extracts data into a RenderState snapshot
 *   2. RenderState → model.setupAnim() animates model parts
 *   3. Model → renderToBuffer() sends geometry + texture to GPU
 *
 * The separation of Entity (server logic) from RenderState (client snapshot)
 * is a key 1.21.x architectural change — the renderer never touches entity fields
 * directly, only the snapshot. This enables render-thread safety.
 *
 * Shadow radius 0.2f — appropriate for a tiny butterfly (0.4×0.3 size).
 * Larger mobs use larger shadows (dragon=2.0, player=0.5).
 */
public class ButterflyRenderer extends MobRenderer<ButterflyEntity, LivingEntityRenderState, ButterflyModel> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            ArcaneNaturalisMod.MOD_ID, "textures/entity/butterfly.png");

    public ButterflyRenderer(EntityRendererProvider.Context context) {
        super(context,
                new ButterflyModel(context.bakeLayer(ButterflyModel.LAYER_LOCATION)),
                0.2f);  // shadow radius (small — it's a tiny butterfly!)
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
