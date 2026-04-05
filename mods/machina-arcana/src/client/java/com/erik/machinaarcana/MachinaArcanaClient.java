package com.erik.machinaarcana;

import com.erik.machinaarcana.hud.ManaHudOverlay;
import com.erik.machinaarcana.particle.ManaStreamParticle;
import com.erik.machinaarcana.registry.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

/**
 * ★ JAVA CONCEPT: Client-only Initialization ★
 *
 * {@link ClientModInitializer} is called ONLY on the client side (the player's game).
 * Server-side code runs in {@link MachinaArcanaMod}.
 *
 * Why separate?
 *   - Servers don't have rendering code (OpenGL, etc.) — crashes if loaded there
 *   - The {@code splitEnvironmentSourceSets()} in build.gradle enforces this split:
 *     client/ sources are compiled separately and only included in client builds
 *   - fabric.mod.json lists both "main" and "client" entrypoints
 *
 * What goes here:
 *   - Particle factory registration (rendering particles)
 *   - HUD overlay registration
 *   - Block entity renderers
 *   - Entity model/renderer registration
 *
 * ★ HudElementRegistry API (Fabric 0.139.4+) ★
 * The new API uses {@link HudElementRegistry#attachElementAfter} with a
 * {@link VanillaHudElements} anchor and a {@link HudElement} functional interface.
 * This replaces the old HudRenderCallback approach.
 */
public class MachinaArcanaClient implements ClientModInitializer {

    /** Identifier for our HUD layer — used to order layers relative to each other. */
    public static final Identifier MANA_HUD_LAYER =
            Identifier.fromNamespaceAndPath(MachinaArcanaMod.MOD_ID, "mana_hud");

    @Override
    public void onInitializeClient() {
        MachinaArcanaMod.LOGGER.info("Machina Arcana client initializing...");

        // Register the particle factory — links server-side particle type to renderer
        // ★ ParticleFactoryRegistry: client-only, hence in ClientModInitializer ★
        ParticleFactoryRegistry.getInstance().register(
                ModParticles.MANA_STREAM,
                ManaStreamParticle.Factory::new
        );

        // Register the mana HUD overlay after the hotbar layer
        // ★ HudElementRegistry: new Fabric API for ordered HUD layers ★
        // HudElement is a @FunctionalInterface: render(GuiGraphics, DeltaTracker)
        // We pass ManaHudOverlay::render as a method reference — it matches the signature
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.HOTBAR,
                MANA_HUD_LAYER,
                ManaHudOverlay::render
        );

        MachinaArcanaMod.LOGGER.info("Machina Arcana client ready!");
    }
}
