package com.erik.bestiary;

import com.erik.bestiary.entity.model.PackWolfModel;
import com.erik.bestiary.entity.renderer.PackWolfRenderer;
import com.erik.bestiary.registry.ModEntities;
import com.erik.bestiary.registry.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.HeartParticle;
import net.minecraft.client.particle.SuspendedParticle;

/**
 * BestiaryClient — client-side initializer.
 *
 * ★ V3 PATTERN: exact structure from MedievalConquestClient ★
 *
 * Client init registers:
 * 1. EntityModelLayerRegistry: links LAYER_LOCATION → createBodyLayer()
 * 2. EntityRendererRegistry: links PACK_WOLF entity → PackWolfRenderer
 * 3. ParticleFactoryRegistry: links custom particle types → vanilla factories
 *
 * Order matters:
 * - Model layer must be registered before the renderer tries to bake it
 * - EntityModelLayerRegistry processes before EntityRendererRegistry
 */
public class BestiaryClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BestiaryMod.LOGGER.info("Bestiary client initializing...");

        // ─── Register model layer (must be before renderer) ───────────────────
        // Links PackWolfModel.LAYER_LOCATION → PackWolfModel.createBodyLayer()
        // ★ V3 PATTERN: EntityModelLayerRegistry.registerModelLayer ★
        EntityModelLayerRegistry.registerModelLayer(
                PackWolfModel.LAYER_LOCATION,
                PackWolfModel::createBodyLayer);

        // ─── Register entity renderer ─────────────────────────────────────────
        // PackWolfEntity → PackWolfRenderer (uses the baked layer above)
        // ★ V3 PATTERN: EntityRendererRegistry.register ★
        EntityRendererRegistry.register(ModEntities.PACK_WOLF, PackWolfRenderer::new);

        // ─── Register particle factories ──────────────────────────────────────
        // Each custom particle type needs a factory to create particle instances.
        // We reuse vanilla factories (they know how to render the particle texture).
        //
        // SIGNAL_WAVE: SuspendedParticle.UnderwaterProvider — gentle floating particles
        //   → Appropriate for a signal "wave" spreading through air
        ParticleFactoryRegistry.getInstance().register(
                ModParticles.SIGNAL_WAVE,
                SuspendedParticle.UnderwaterProvider::new
        );

        // FEAR_SWEAT: HeartParticle.Provider — small poof particles
        //   → Repurposed for sweat drops (same visual weight, different texture)
        ParticleFactoryRegistry.getInstance().register(
                ModParticles.FEAR_SWEAT,
                HeartParticle.Provider::new
        );

        // COURAGE_FIRE: FlameParticle.Provider — actual fire particles
        //   → Perfect for "rallied" wolves with courage fire
        ParticleFactoryRegistry.getInstance().register(
                ModParticles.COURAGE_FIRE,
                FlameParticle.Provider::new
        );

        BestiaryMod.LOGGER.info("Bestiary client ready!");
    }
}
