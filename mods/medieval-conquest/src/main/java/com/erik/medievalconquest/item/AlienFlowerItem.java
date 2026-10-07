package com.erik.medievalconquest.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

/**
 * R08 alien flower. Right-click into the air smells it: hunger and saturation fill to the maximum and
 * the flower stays in the hand. Right-click on the ground plants it; the planted flower smells the same.
 * The candied lilac flower keeps its own separate food effect.
 */
public final class AlienFlowerItem extends BlockItem {
    /** About thirty seconds between smells, shared by held and planted flowers. */
    public static final int COOLDOWN_TICKS = 30 * 20;

    public AlienFlowerItem(Block block, Properties properties) { super(block, properties); }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return smell(level, player, this, player.getEyePosition()) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    /**
     * Smells an alien flower at {@code source}. False (nothing happens) while the {@code flower} item's
     * cooldown runs or when hunger and saturation are already full; only the server changes food,
     * cooldown and effects. Held and planted flowers pass the same item, so they share one cooldown.
     */
    public static boolean smell(Level level, Player player, Item flower, Vec3 source) {
        ItemStack cooldownKey = new ItemStack(flower);
        FoodData food = player.getFoodData();
        if (player.getCooldowns().isOnCooldown(cooldownKey) || (food.getFoodLevel() >= FoodConstants.MAX_FOOD
                && food.getSaturationLevel() >= FoodConstants.MAX_SATURATION)) {
            return false;
        }
        if (level instanceof ServerLevel server) {
            food.setFoodLevel(FoodConstants.MAX_FOOD);
            food.setSaturation(FoodConstants.MAX_SATURATION);
            player.getCooldowns().addCooldown(cooldownKey, COOLDOWN_TICKS);
            server.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNIFFER_SNIFFING,
                    SoundSource.PLAYERS, 1.0f, 1.4f);
            server.sendParticles(ParticleTypes.HAPPY_VILLAGER, source.x, source.y, source.z, 8, 0.3, 0.3, 0.3, 0.0);
        }
        return true;
    }
}
