package com.erik.medievalconquest.event;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EquipmentSlot;

/** Configure guaranteed chances; vanilla remains the only death-loot producer. */
public final class EquipmentDropHandler {
    private EquipmentDropHandler() { }
    public static void guaranteeEquippedDrops(Mob mob) {
        if (mob.level().isClientSide()) return;
        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD,
                EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET,
                EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND }) {
            if (!mob.getItemBySlot(slot).isEmpty()) mob.setGuaranteedDrop(slot);
        }
    }
}
