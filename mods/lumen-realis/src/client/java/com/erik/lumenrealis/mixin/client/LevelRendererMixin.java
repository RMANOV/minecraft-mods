package com.erik.lumenrealis.mixin.client;

import com.erik.lumenrealis.LumenRealisClient;
import com.erik.lumenrealis.lighting.LightTracker;
import com.erik.lumenrealis.lighting.LightUniformUploader;
import com.erik.lumenrealis.water.WaterRenderer;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks the head of {@code LevelRenderer#renderLevel} to update per-frame
 * lighting, uniform data, and water animation state before geometry is drawn.
 *
 * {@code require = 0} prevents a crash if Mojang renames or changes the
 * method signature in a patch release — the mod degrades gracefully.
 */
@Mixin(net.minecraft.client.renderer.LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Inject(method = "renderLevel", at = @At("HEAD"), require = 0)
    private void lumenrealis$updateSystems(CallbackInfo ci) {
        if (!LumenRealisClient.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.gameRenderer != null) {
            LightTracker.update(mc.level, mc.gameRenderer.getMainCamera());
            LightUniformUploader.prepare(LightTracker.getLights());
            WaterRenderer.tick();
        }
    }
}
