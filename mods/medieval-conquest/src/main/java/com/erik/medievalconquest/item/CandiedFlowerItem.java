package com.erik.medievalconquest.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A sweet made from the pink lilac flower.
 *
 * The effect is applied after the normal food consumption flow so the item
 * behaves like a regular edible item (including eating animation and sounds).
 */
public class CandiedFlowerItem extends Item {
	private static final int REGENERATION_DURATION_TICKS = 15 * 20;

	public CandiedFlowerItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
		ItemStack result = super.finishUsingItem(stack, level, livingEntity);

		if (!level.isClientSide()) {
			// Amplifier 1 is Regeneration II in Minecraft's zero-based effect levels.
			livingEntity.addEffect(new MobEffectInstance(
					MobEffects.REGENERATION,
					REGENERATION_DURATION_TICKS,
					1));
		}

		return result;
	}
}
