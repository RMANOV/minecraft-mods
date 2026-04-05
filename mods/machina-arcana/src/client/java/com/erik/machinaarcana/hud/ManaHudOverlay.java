package com.erik.machinaarcana.hud;

import com.erik.machinaarcana.block.entity.ManaConduitBlockEntity;
import com.erik.machinaarcana.mana.ManaType;
import com.erik.machinaarcana.registry.ModItems;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * ★ JAVA CONCEPT: HudElement as a Functional Interface ★
 *
 * ManaHudOverlay renders a mana bar on the player's HUD.
 * It activates when the player is holding a Mana Crystal OR looking at a conduit.
 *
 * The Fabric API's {@link net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement}
 * is a {@code @FunctionalInterface} with one method:
 *   {@code void render(GuiGraphics context, DeltaTracker tickCounter)}
 *
 * Because it's functional, we can register it as a method reference:
 *   {@code ManaHudOverlay::render}
 * This avoids creating an anonymous class or lambda wrapper.
 *
 * Design:
 *   - This class is stateless — all methods are static
 *   - No instantiation needed: the render method is passed as a method reference
 *     to HudElementRegistry in MachinaArcanaClient
 *   - Uses {@link ManaDisplay} record as a pure data transfer object
 *
 * ★ Record usage ★
 * We create a {@link ManaDisplay} record snapshot from live game data,
 * then pass it to the drawing methods. The record's computed properties
 * ({@code percentage()}, {@code color()}, {@code displayText()}) keep the
 * drawing code clean and free of raw number arithmetic.
 *
 * HUD coordinates: (0,0) is top-left of the screen.
 * We draw the bar near the bottom-center, above the hotbar.
 */
public class ManaHudOverlay {

    // ── Layout Constants ───────────────────────────────────────────────────

    private static final int BAR_WIDTH  = 100;
    private static final int BAR_HEIGHT = 8;
    private static final int BAR_MARGIN_BOTTOM = 36; // pixels above hotbar

    // ── Entry Point (matches HudElement functional interface) ──────────────

    /**
     * Called every frame to render the HUD layer.
     *
     * This method signature matches {@code HudElement.render(GuiGraphics, DeltaTracker)},
     * so it can be passed as a method reference: {@code ManaHudOverlay::render}
     *
     * @param guiGraphics the graphics context for this frame
     * @param deltaTracker provides partial tick for smooth animations
     */
    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Player player = mc.player;

        // Determine if we should show the HUD
        ManaDisplay display = buildDisplay(player);
        if (display == null) return;

        // Draw the bar
        int screenWidth  = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int barX = (screenWidth  - BAR_WIDTH) / 2;
        int barY = screenHeight - BAR_MARGIN_BOTTOM - BAR_HEIGHT;

        drawManaBar(guiGraphics, barX, barY, display);
    }

    // ── Display Data Building ──────────────────────────────────────────────

    /**
     * Builds a ManaDisplay record from current game state, or returns null
     * if the HUD should not be shown.
     *
     * ★ Record construction: {@code new ManaDisplay(current, max, type)} ★
     * Records are constructed like normal objects — the constructor auto-assigns
     * all fields declared in the header.
     *
     * @param player the local player
     * @return a ManaDisplay snapshot, or null to suppress the overlay
     */
    private static ManaDisplay buildDisplay(Player player) {
        // Condition 1: player holds a mana crystal
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand  = player.getOffhandItem();

        boolean holdingCrystal = mainHand.is(ModItems.MANA_CRYSTAL)
                || offHand.is(ModItems.MANA_CRYSTAL);

        if (holdingCrystal) {
            // Show a placeholder mana bar (in a real mod, the crystal would
            // store its own mana and we'd read it here)
            return new ManaDisplay(250, 1000, ManaType.ARCANE);
        }

        // Condition 2: player is looking at a mana conduit
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.hitResult instanceof BlockHitResult blockHit
                && blockHit.getType() == HitResult.Type.BLOCK) {

            BlockEntity be = mc.level.getBlockEntity(blockHit.getBlockPos());
            if (be instanceof ManaConduitBlockEntity conduit) {
                int stored = conduit.getStoredMana(ManaType.ARCANE);
                // 500 is the max buffer from ManaConduitBlockEntity constants
                return new ManaDisplay(stored, 500, ManaType.ARCANE);
            }
        }

        return null; // Don't show
    }

    // ── Drawing ────────────────────────────────────────────────────────────

    /**
     * Draws the mana bar using the data from a {@link ManaDisplay} record.
     *
     * ★ Using record methods ★
     * Instead of raw arithmetic, we call {@code display.percentage()} and
     * {@code display.color()} — clean, readable, and testable in isolation.
     *
     * @param g       the graphics context
     * @param x       bar left edge
     * @param y       bar top edge
     * @param display the mana data record
     */
    private static void drawManaBar(GuiGraphics g, int x, int y, ManaDisplay display) {
        // Background (dark gray)
        g.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xFF222222);

        // Filled portion
        int fillWidth = (int) (BAR_WIDTH * display.percentage());
        int barColor  = 0xFF000000 | display.color(); // add full alpha

        // Flash red if critically low
        if (display.isCriticallyLow() && (System.currentTimeMillis() / 500) % 2 == 0) {
            barColor = 0xFFFF4444;
        }

        if (fillWidth > 0) {
            g.fill(x, y, x + fillWidth, y + BAR_HEIGHT, barColor);
        }

        // Border outline (slightly lighter gray)
        g.fill(x,               y,               x + BAR_WIDTH, y + 1,           0xFF666666);
        g.fill(x,               y + BAR_HEIGHT-1, x + BAR_WIDTH, y + BAR_HEIGHT,  0xFF666666);
        g.fill(x,               y,               x + 1,          y + BAR_HEIGHT, 0xFF666666);
        g.fill(x + BAR_WIDTH-1, y,               x + BAR_WIDTH,  y + BAR_HEIGHT, 0xFF666666);

        // Text label
        Minecraft mc = Minecraft.getInstance();
        String text = display.displayText();
        int textColor = display.isFull() ? 0xFFFFD700 : 0xFFCCCCCC; // gold if full, gray otherwise
        g.drawString(mc.font, text,
                x + BAR_WIDTH / 2 - mc.font.width(text) / 2,
                y + BAR_HEIGHT + 2,
                textColor, false);
    }
}
