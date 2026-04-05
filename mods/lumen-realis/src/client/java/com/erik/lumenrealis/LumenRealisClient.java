package com.erik.lumenrealis;

import com.erik.lumenrealis.config.ConfigManager;
import com.erik.lumenrealis.config.LumenConfig;
import com.erik.lumenrealis.hud.PerformanceHudOverlay;
import com.erik.lumenrealis.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class LumenRealisClient implements ClientModInitializer {

    public static final Identifier PERF_HUD_LAYER =
            Identifier.fromNamespaceAndPath(LumenRealisMod.MOD_ID, "performance_hud");

    private static final KeyMapping TOGGLE_KEY = new KeyMapping(
            "key.lumenrealis.toggle",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_F7,
            KeyMapping.Category.MISC
    );

    @Override
    public void onInitializeClient() {
        LumenRealisMod.LOGGER.info("Lumen Realis client initializing...");

        ConfigManager.load();

        KeyBindingHelper.registerKeyBinding(TOGGLE_KEY);

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.HOTBAR,
                PERF_HUD_LAYER,
                PerformanceHudOverlay::render
        );

        // Initialize render pipeline with current preset
        RenderPipeline.getInstance().recompose(ConfigManager.get().preset());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE_KEY.consumeClick()) {
                LumenConfig cfg = ConfigManager.get();
                var window = Minecraft.getInstance().getWindow();
                boolean shift = InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT);
                boolean ctrl = InputConstants.isKeyDown(window, InputConstants.KEY_LCONTROL);

                if (shift) {
                    LumenConfig updated = cfg.withPreset(cfg.preset().next());
                    ConfigManager.set(updated);
                    RenderPipeline.getInstance().recompose(updated.preset());
                    LumenRealisMod.LOGGER.info("Preset: {}", updated.preset());
                } else if (ctrl) {
                    ConfigManager.set(cfg.withShowPerformanceHud(!cfg.showPerformanceHud()));
                } else {
                    ConfigManager.set(cfg.withEnabled(!cfg.enabled()));
                    LumenRealisMod.LOGGER.info("Lumen Realis: {}",
                            ConfigManager.get().enabled() ? "ON" : "OFF");
                }
            }

            // Adaptive quality check every 60 ticks (~3 seconds)
            if (ConfigManager.get().adaptiveQualityEnabled()
                    && client.level != null
                    && client.player != null) {
                checkAdaptiveQuality();
            }
        });

        LumenRealisMod.LOGGER.info("Lumen Realis client ready! Shaders loaded.");
    }

    public static boolean isEnabled() {
        return ConfigManager.get().enabled();
    }

    public static LumenConfig getConfig() {
        return ConfigManager.get();
    }

    private static int adaptiveTick = 0;
    private static int stableFrames = 0;

    private static void checkAdaptiveQuality() {
        adaptiveTick++;
        if (adaptiveTick % 60 != 0) return;

        LumenConfig cfg = ConfigManager.get();
        float fps = RenderPipeline.getInstance().getAverageFps();
        float target = cfg.targetFps();

        if (fps < target * 0.85f && cfg.preset().ordinal() > 0) {
            // Step down
            var values = com.erik.lumenrealis.config.QualityPreset.values();
            var newPreset = values[cfg.preset().ordinal() - 1];
            ConfigManager.set(cfg.withPreset(newPreset));
            RenderPipeline.getInstance().recompose(newPreset);
            stableFrames = 0;
            LumenRealisMod.LOGGER.info("Adaptive quality: {} -> {} (FPS={:.0f})", cfg.preset(), newPreset, fps);
        } else if (fps > target * 1.15f) {
            stableFrames++;
            if (stableFrames > 5 && cfg.preset().ordinal() < com.erik.lumenrealis.config.QualityPreset.values().length - 1) {
                var values = com.erik.lumenrealis.config.QualityPreset.values();
                var newPreset = values[cfg.preset().ordinal() + 1];
                ConfigManager.set(cfg.withPreset(newPreset));
                RenderPipeline.getInstance().recompose(newPreset);
                stableFrames = 0;
                LumenRealisMod.LOGGER.info("Adaptive quality: {} -> {} (FPS={:.0f})", cfg.preset(), newPreset, fps);
            }
        } else {
            stableFrames = 0;
        }
    }
}
