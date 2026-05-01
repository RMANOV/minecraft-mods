package com.erik.medievalconquest.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Lilac Herb — dropped from lilac leaves.
 * Right-click to consume: applies regeneration.
 */
public class HerbItem extends Item {

	public HerbItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (!level.isClientSide()) {
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 15 * 20, 0));

			// Consume one herb
			stack.shrink(1);

			// Play eating sound
			level.playSound(null, player.blockPosition(),
					SoundEvents.GENERIC_EAT.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
		}

		return InteractionResult.SUCCESS;
	}
}
