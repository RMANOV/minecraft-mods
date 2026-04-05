package com.erik.lumenrealis.mixin.client;

import com.erik.lumenrealis.LumenRealisClient;
import com.erik.lumenrealis.pipeline.RenderPipeline;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks the end of {@code GameRenderer#render} to run the post-processing pipeline.
 */
@Mixin(net.minecraft.client.renderer.GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void lumenrealis$applyPostProcessing(DeltaTracker deltaTracker,
                                                  boolean renderLevel,
                                                  CallbackInfo ci) {
        if (!LumenRealisClient.isEnabled()) return;
        RenderPipeline.getInstance().execute();
    }
}
