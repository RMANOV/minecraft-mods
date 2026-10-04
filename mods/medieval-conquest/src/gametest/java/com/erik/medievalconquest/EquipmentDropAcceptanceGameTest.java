package com.erik.medievalconquest;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;

public final class EquipmentDropAcceptanceGameTest {
    private static final BlockPos ORIGIN = new BlockPos(2, 1, 2);

    private static Map<EquipmentSlot, ItemStack> sixStacks(String marker) {
        var stacks = new EnumMap<EquipmentSlot, ItemStack>(EquipmentSlot.class);
        stacks.put(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
        stacks.put(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        stacks.put(EquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
        stacks.put(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS));
        stacks.put(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        stacks.put(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        stacks.get(EquipmentSlot.HEAD).setDamageValue(7);
        stacks.get(EquipmentSlot.CHEST).setDamageValue(11);
        stacks.get(EquipmentSlot.LEGS).setDamageValue(3);
        stacks.get(EquipmentSlot.FEET).setDamageValue(5);
        stacks.get(EquipmentSlot.MAINHAND).setDamageValue(9);
        stacks.get(EquipmentSlot.OFFHAND).setDamageValue(13);
        stacks.forEach((slot, stack) -> stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("fixture-" + marker + "-" + slot.name())));
        return stacks;
    }

    private static Mob victim(GameTestHelper h, EntityType<? extends Mob> type) {
        var mob = h.spawn(type, ORIGIN.getX(), ORIGIN.getY(), ORIGIN.getZ());
        mob.setNoAi(true);
        return mob;
    }

    private static Map<EquipmentSlot, ItemStack> equip(Mob mob, String test) {
        var expected = sixStacks(test + "-" + mob.getUUID());
        expected.forEach((slot, stack) -> {
            mob.setItemSlot(slot, stack.copy());
            mob.setDropChance(slot, 0.0f);
        });
        return expected;
    }

    private static void kill(GameTestHelper h, Mob mob) {
        h.assertTrue(mob.hurtServer(h.getLevel(), h.getLevel().damageSources().genericKill(), 1000.0f),
                "real native death must be accepted");
        h.assertTrue(mob.isDeadOrDying(), "victim must actually die");
    }

    private static List<ItemEntity> drops(GameTestHelper h, Map<EquipmentSlot, ItemStack> expected) {
        return h.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(h.absolutePos(ORIGIN)).inflate(4.0), item -> expected.values().stream()
                        .anyMatch(stack -> item.getItem().is(stack.getItem())
                                && java.util.Objects.equals(stack.get(DataComponents.CUSTOM_NAME),
                                        item.getItem().get(DataComponents.CUSTOM_NAME))));
    }

    private static void assertExact(GameTestHelper h, Map<EquipmentSlot, ItemStack> expected) {
        var actual = drops(h, expected);
        h.assertTrue(actual.size() == 6, "exactly six fixture-unique equipment entities");
        h.assertTrue(actual.stream().mapToInt(item -> item.getItem().getCount()).sum() == 6,
                "exactly six total equipment items, not duplicated stacks");
        for (var stack : expected.values()) {
            var same = actual.stream().filter(item -> item.getItem().is(stack.getItem())).toList();
            h.assertTrue(same.size() == 1 && same.getFirst().getItem().getCount() == 1,
                    "one copy of each equipped slot");
            h.assertTrue(ItemStack.isSameItemSameComponents(same.getFirst().getItem(), stack),
                    "independent expected damage/name/components retained");
        }
    }

    @GameTest(maxTicks = 40)
    public void zombieDropsAllSixZeroChanceSlotsOnce(GameTestHelper h) {
        var mob = victim(h, EntityType.ZOMBIE);
        var expected = equip(mob, "zombie");
        kill(h, mob);
        h.runAtTickTime(2, () -> { assertExact(h, expected); h.succeed(); });
    }

    @GameTest(maxTicks = 40)
    public void skeletonDropsAllSixZeroChanceSlotsOnce(GameTestHelper h) {
        var mob = victim(h, EntityType.SKELETON);
        var expected = equip(mob, "skeleton");
        kill(h, mob);
        h.runAtTickTime(2, () -> { assertExact(h, expected); h.succeed(); });
    }

    @GameTest(maxTicks = 40)
    public void emptySlotsDoNotFabricateEquipment(GameTestHelper h) {
        var mob = victim(h, EntityType.ZOMBIE);
        var identities = sixStacks("empty-" + mob.getUUID());
        kill(h, mob);
        h.runAtTickTime(2, () -> {
            var actual = h.getLevel().getEntitiesOfClass(ItemEntity.class,
                    new AABB(h.absolutePos(ORIGIN)).inflate(4.0), item -> identities.values().stream()
                            .anyMatch(stack -> item.getItem().is(stack.getItem())));
            h.assertTrue(actual.isEmpty(), "empty slots cannot fabricate any of six equipment identities");
            h.succeed();
        });
    }

    @GameTest(maxTicks = 40)
    public void mobDropsFalseSuppressesEquipment(GameTestHelper h) {
        var mob = victim(h, EntityType.ZOMBIE);
        var expected = equip(mob, "no-drops");
        var rules = h.getLevel().getGameRules();
        boolean original = rules.get(GameRules.MOB_DROPS);
        try {
            rules.set(GameRules.MOB_DROPS, false, h.getLevel().getServer());
            kill(h, mob);
        } finally {
            rules.set(GameRules.MOB_DROPS, original, h.getLevel().getServer());
        }
        h.assertTrue(rules.get(GameRules.MOB_DROPS) == original, "global rule restored synchronously");
        h.runAtTickTime(2, () -> {
            h.assertTrue(drops(h, expected).isEmpty(), "disabled native mob loot must suppress fixture gear");
            h.succeed();
        });
    }

    @GameTest(maxTicks = 40)
    public void repeatedDeathDoesNotDuplicateEquipment(GameTestHelper h) {
        var mob = victim(h, EntityType.ZOMBIE);
        var expected = equip(mob, "repeated");
        kill(h, mob);
        h.runAtTickTime(2, () -> {
            assertExact(h, expected);
            mob.hurtServer(h.getLevel(), h.getLevel().damageSources().genericKill(), 1000.0f);
            h.runAtTickTime(4, () -> { assertExact(h, expected); h.succeed(); });
        });
    }
}
