package com.erik.lumenrealis.hud;

import com.erik.lumenrealis.config.ConfigManager;
import com.erik.lumenrealis.config.LumenConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class PerformanceHudOverlay {

    private static final int MARGIN = 4;
    private static final int LINE_HEIGHT = 10;
    private static final int BG_PADDING = 3;

    public static void render(GuiGraphics g, DeltaTracker deltaTracker) {
        LumenConfig config = ConfigManager.get();
        if (!config.showPerformanceHud()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        PerformanceDisplay display = buildDisplay(mc, config);
        drawOverlay(g, mc, display);
    }

    private static PerformanceDisplay buildDisplay(Minecraft mc, LumenConfig config) {
        int fps = mc.getFps();
        float frameTime = fps > 0 ? 1000.0f / fps : 0;
        long memMb = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
        return new PerformanceDisplay(fps, frameTime, memMb, config.preset(), config.enabled());
    }

    private static void drawOverlay(GuiGraphics g, Minecraft mc, PerformanceDisplay d) {
        int x = MARGIN;
        int y = MARGIN;

        String line1 = "Lumen Realis [" + d.statusText() + "]";
        String line2 = d.fpsText() + " | " + d.frameTimeText();
        String line3 = "Preset: " + d.presetText();
        String line4 = d.memoryUsedMb() + " MB";

        int maxWidth = Math.max(
                Math.max(mc.font.width(line1), mc.font.width(line2)),
                Math.max(mc.font.width(line3), mc.font.width(line4))
        );

        // Background
        g.fill(x - BG_PADDING, y - BG_PADDING,
                x + maxWidth + BG_PADDING, y + LINE_HEIGHT * 4 + BG_PADDING,
                0x88000000);

        // Text lines
        g.drawString(mc.font, line1, x, y, d.enabled() ? 0xFF66CCFF : 0xFF888888, false);
        g.drawString(mc.font, line2, x, y + LINE_HEIGHT, 0xFF000000 | d.fpsColor(), false);
        g.drawString(mc.font, line3, x, y + LINE_HEIGHT * 2, 0xFFCCCCCC, false);
        g.drawString(mc.font, line4, x, y + LINE_HEIGHT * 3, 0xFF999999, false);
    }
}
