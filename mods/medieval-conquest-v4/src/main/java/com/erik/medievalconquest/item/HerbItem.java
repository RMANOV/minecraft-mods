package com.erik.medievalconquest.item;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Herb Bundle — crafted from Hyacinth flowers.
 * Right-click to consume: applies a random potion effect (10-30 seconds).
 * Effects range from beneficial (Speed, Strength) to harmful (Poison, Weakness).
 */
public class HerbItem extends Item {

	private static final List<Holder<MobEffect>> POSSIBLE_EFFECTS = List.of(
			MobEffects.SPEED,
			MobEffects.STRENGTH,
			MobEffects.JUMP_BOOST,
			MobEffects.NIGHT_VISION,
			MobEffects.REGENERATION,
			MobEffects.POISON,
			MobEffects.SLOWNESS,
			MobEffects.WEAKNESS
	);

	public HerbItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (!level.isClientSide()) {
			RandomSource random = level.getRandom();

			// Pick a random effect from the pool
			Holder<MobEffect> effect = POSSIBLE_EFFECTS.get(random.nextInt(POSSIBLE_EFFECTS.size()));

			// Duration: 10-30 seconds (in ticks)
			int durationTicks = (10 + random.nextInt(21)) * 20;

			player.addEffect(new MobEffectInstance(effect, durationTicks, 0));

			// Consume one herb
			stack.shrink(1);

			// Play eating sound
			level.playSound(null, player.blockPosition(),
					SoundEvents.GENERIC_EAT.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
		}

		return InteractionResult.SUCCESS;
	}
}
