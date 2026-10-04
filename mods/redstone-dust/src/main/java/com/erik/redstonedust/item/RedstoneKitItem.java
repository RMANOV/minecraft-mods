package com.erik.redstonedust.item;

import java.util.function.Consumer;

import com.erik.redstonedust.playground.RedstonePlayground;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * The Redstone Playground Kit.
 *
 * <p>Right-click it on the ground and it instantly builds a tiny working
 * circuit: lever -> redstone dust -> redstone lamp. Eric flips the lever and
 * the lamp lights up. Then he can break it and rebuild it himself, following
 * the Redstone Tutorial Book.
 */
public class RedstoneKitItem extends Item {

	public RedstoneKitItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos clicked = context.getClickedPos();

		if (!level.isClientSide()) {
			RedstonePlayground.build(level, clicked);
			level.playSound(null, clicked, SoundEvents.NOTE_BLOCK_PLING.value(),
					SoundSource.BLOCKS, 1.0f, 1.2f);
			if (context.getPlayer() != null) {
				context.getPlayer().displayClientMessage(
						Component.translatable("message.redstonedust.playground_built")
								.withStyle(ChatFormatting.RED),
						true);
				// Use up one kit (in survival).
				context.getItemInHand().consume(1, context.getPlayer());
			}
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(net.minecraft.world.item.ItemStack stack, TooltipContext tooltipContext,
			TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
		consumer.accept(Component.translatable("tooltip.redstonedust.redstone_kit.line1")
				.withStyle(ChatFormatting.GRAY));
		consumer.accept(Component.translatable("tooltip.redstonedust.redstone_kit.line2")
				.withStyle(ChatFormatting.DARK_GRAY));
	}
}
