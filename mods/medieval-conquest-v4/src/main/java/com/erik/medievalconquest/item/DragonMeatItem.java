package com.erik.medievalconquest.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Super food dropped by overworld dragons.
 */
public class DragonMeatItem extends Item {
	public DragonMeatItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
		ItemStack result = super.finishUsingItem(stack, level, livingEntity);

		if (!level.isClientSide()) {
			livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 20, 1));
			livingEntity.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 30 * 20, 0));
			livingEntity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 45 * 20, 0));
		}

		return result;
	}
}
