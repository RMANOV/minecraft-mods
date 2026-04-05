package com.erik.arcanenaturalis;

import com.erik.arcanenaturalis.entity.model.ButterflyModel;
import com.erik.arcanenaturalis.entity.renderer.ButterflyRenderer;
import com.erik.arcanenaturalis.particle.AuroraParticle;
import com.erik.arcanenaturalis.particle.FireflyParticle;
import com.erik.arcanenaturalis.particle.MagicalDustParticle;
import com.erik.arcanenaturalis.registry.ModEntities;
import com.erik.arcanenaturalis.registry.ModParticles;
import com.erik.arcanenaturalis.season.SeasonColorHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * Client-side mod initializer.
 * Registers all client-only systems:
 *   - Entity renderers (visual representation of entities)
 *   - Entity model layers (geometry definitions)
 *   - Particle factories (how each particle type is created/updated)
 *   - Color providers (seasonal foliage colors)
 *
 * Client code only runs on the player's machine (not the server).
 * This separation is critical: servers don't render anything.
 */
public class ArcaneNaturalisClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ArcaneNaturalisMod.LOGGER.info("Arcane Naturalis client initializing...");

        // ── Entity rendering ───────────────────────────────────────────────
        // Register the butterfly renderer (links entity type → renderer class)
        EntityRendererRegistry.register(ModEntities.BUTTERFLY,
                ButterflyRenderer::new);

        // Register the butterfly model layer (geometry for the renderer)
        EntityModelLayerRegistry.registerModelLayer(
                ButterflyModel.LAYER_LOCATION,
                ButterflyModel::createBodyLayer);

        // ── Particles ──────────────────────────────────────────────────────
        // ★ Java 21: Method references as ParticleProvider factories ★
        // Each registration maps a particle type → a factory that creates
        // the particle object when spawned. The factory receives the
        // particle's sprite (texture) and returns a functional interface.
        ParticleFactoryRegistry.getInstance().register(
                ModParticles.FIREFLY,
                FireflyParticle.Provider::new);

        ParticleFactoryRegistry.getInstance().register(
                ModParticles.AURORA,
                AuroraParticle.Provider::new);

        ParticleFactoryRegistry.getInstance().register(
                ModParticles.MAGICAL_DUST,
                MagicalDustParticle.Provider::new);

        // ── Seasonal colors ────────────────────────────────────────────────
        SeasonColorHandler.register();

        ArcaneNaturalisMod.LOGGER.info("Arcane Naturalis client ready!");
    }
}
