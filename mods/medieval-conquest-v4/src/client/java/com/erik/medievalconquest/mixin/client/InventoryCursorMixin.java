package com.erik.medievalconquest.mixin.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the mouse cursor with a carrot sprite when any inventory screen is open.
 * Erik wanted this — "мишката да ми е морков" (the mouse should be a carrot).
 */
@Mixin(AbstractContainerScreen.class)
public abstract class InventoryCursorMixin {

	private static final Identifier CARROT_CURSOR = Identifier.fromNamespaceAndPath(
			"medievalconquest", "textures/gui/carrot_cursor.png");

	@Inject(method = "render", at = @At("TAIL"))
	private void medievalconquest$renderCarrotCursor(GuiGraphics guiGraphics, int mouseX,
			int mouseY, float partialTick, CallbackInfo ci) {
		// Draw carrot texture at mouse position, offset so the tip aligns with cursor point
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CARROT_CURSOR,
				mouseX - 1, mouseY - 1,
				0, 0,
				16, 16,
				16, 16);
	}
}
