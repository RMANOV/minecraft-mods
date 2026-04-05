package com.erik.arcanenaturalis.registry;

import com.erik.arcanenaturalis.ArcaneNaturalisMod;
import com.erik.arcanenaturalis.entity.ButterflyEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Entity registry following the exact 1.21.11 pattern:
 *   - EntityType.Builder.of(constructor, category).sized(w, h).build(key)
 *   - FabricDefaultAttributeRegistry.register to attach attribute values
 *
 * ButterflyEntity is AMBIENT (passive, non-hostile) like bats and fish.
 */
public class ModEntities {

    private static final ResourceKey<EntityType<?>> BUTTERFLY_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(ArcaneNaturalisMod.MOD_ID, "butterfly"));

    public static final EntityType<ButterflyEntity> BUTTERFLY =
            EntityType.Builder.of(ButterflyEntity::new, MobCategory.AMBIENT)
                    .sized(0.4f, 0.3f)
                    .clientTrackingRange(8)
                    .build(BUTTERFLY_KEY);

    public static void register() {
        Registry.register(BuiltInRegistries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(ArcaneNaturalisMod.MOD_ID, "butterfly"),
                BUTTERFLY);

        FabricDefaultAttributeRegistry.register(BUTTERFLY,
                ButterflyEntity.createAttributes());

        ArcaneNaturalisMod.LOGGER.info("Entities registered!");
    }
}
