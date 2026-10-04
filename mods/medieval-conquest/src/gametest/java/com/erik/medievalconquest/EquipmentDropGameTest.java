package com.erik.medievalconquest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;

public final class EquipmentDropGameTest {
    @GameTest(maxTicks = 40)
    public void actualDeathDropsExactlyOneZeroChanceEquippedHelmet(GameTestHelper h) {
        var zombie = h.spawn(EntityType.ZOMBIE, 2, 1, 2);
        zombie.setNoAi(true);
        var helmet = new ItemStack(Items.DIAMOND_HELMET);
        helmet.setDamageValue(7);
        helmet.set(DataComponents.CUSTOM_NAME, Component.literal("fixture helmet"));
        var expectedHelmet = helmet.copy();
        zombie.setItemSlot(EquipmentSlot.HEAD, helmet);
        zombie.setDropChance(EquipmentSlot.HEAD, 0.0f);
        zombie.hurtServer(h.getLevel(), h.getLevel().damageSources().genericKill(), 1000.0f);
        h.runAtTickTime(2, () -> {
            h.assertItemEntityCountIs(Items.DIAMOND_HELMET,
                    new net.minecraft.core.BlockPos(2, 1, 2), 4.0, 1);
            var pos = h.absolutePos(new net.minecraft.core.BlockPos(2, 1, 2));
            var drops = h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(4.0),
                    item -> item.getItem().is(Items.DIAMOND_HELMET));
            h.assertTrue(drops.size() == 1, "exactly one native equipment entity");
            h.assertTrue(ItemStack.isSameItemSameComponents(drops.getFirst().getItem(), expectedHelmet),
                    "native equipment damage and components preserved");
            h.assertTrue(drops.getFirst().getItem().getCount() == 1, "native stack count preserved");
            h.succeed();
        });
    }
}
