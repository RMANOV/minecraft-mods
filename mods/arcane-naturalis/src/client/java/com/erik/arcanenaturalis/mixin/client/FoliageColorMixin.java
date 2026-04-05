package com.erik.arcanenaturalis.mixin.client;

import com.erik.arcanenaturalis.season.Season;
import com.erik.arcanenaturalis.season.SeasonManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.FoliageColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * ★ Client Mixin: Override foliage color with the current season ★
 *
 * Placed in src/client so it can safely access Minecraft.getInstance()
 * (a client-only class). Registered in arcanenaturalis.client.mixins.json.
 *
 * @Inject(at = @At("HEAD"), cancellable = true):
 *   - "HEAD" means we inject at the very start of the method
 *   - cancellable = true means our code can call cir.setReturnValue()
 *     to skip the original method entirely and return our custom color
 *
 * This mixin hooks FoliageColor.get(double temperature, double humidity)
 * which Minecraft calls for every visible leaf block each frame.
 * We return the season's color instead of the biome-based color.
 */
@Mixin(FoliageColor.class)
public class FoliageColorMixin {

    /**
     * Injects at the start of FoliageColor.get(double, double) to return
     * the current season's foliage color instead of the biome-derived color.
     *
     * The season is determined from the Minecraft client's current world time.
     * If no world is loaded, we fall back to original behavior.
     */
    @Inject(method = "get(DD)I", at = @At("HEAD"), cancellable = true)
    private static void getSeasonFoliageColor(
            double temperature, double humidity,
            CallbackInfoReturnable<Integer> cir) {

        // Access the Minecraft client singleton (safe here — client-only source set)
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        long gameTime = mc.level.getGameTime();
        Season season = SeasonManager.getCurrentSeason(gameTime);

        // ★ Pattern matching: extract color from any Season variant ★
        int color = switch (season) {
            case Season.Spring s -> s.foliageColor();
            case Season.Summer s -> s.foliageColor();
            case Season.Autumn s -> s.foliageColor();
            case Season.Winter s -> s.foliageColor();
        };

        cir.setReturnValue(color);
    }
}
