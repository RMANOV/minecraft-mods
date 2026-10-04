package com.erik.medievalconquest.mixin;

import com.erik.medievalconquest.event.EquipmentDropHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MobEquipmentDropMixin {
    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
    private void medievalconquest$equippedDrops(ServerLevel level, DamageSource source, CallbackInfo ci) {
        if ((Object) this instanceof Mob mob) EquipmentDropHandler.guaranteeEquippedDrops(mob);
    }
}
