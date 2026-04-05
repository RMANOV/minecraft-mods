package com.erik.bestiary.registry;

import com.erik.bestiary.BestiaryMod;
import com.erik.bestiary.entity.PackWolfEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * ModEntities — entity type registration.
 *
 * ★ V3 PATTERN: exact copy of ModEntities structure from medieval-conquest-v3 ★
 *
 * Key API notes for 1.21.11:
 * - ResourceKey created with Registries.ENTITY_TYPE
 * - Identifier.fromNamespaceAndPath() — NOT new ResourceLocation()
 * - EntityType.Builder.of(...).sized(...).build(key) — KEY IN build(), not elsewhere
 * - FabricDefaultAttributeRegistry.register() for attribute sets
 */
public class ModEntities {

    /**
     * ResourceKey links the entity type to its registry path.
     * This is required by build() in 1.21.11.
     */
    private static final ResourceKey<EntityType<?>> PACK_WOLF_KEY = ResourceKey.create(
            Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(BestiaryMod.MOD_ID, "pack_wolf"));

    /**
     * PACK_WOLF entity type definition.
     *
     * MobCategory.MONSTER means:
     * - Spawns only in darkness (light level 0)
     * - Despawns when player is far away
     * - Counts toward hostile mob cap
     *
     * sized(0.6, 0.85): wolf dimensions (width, height)
     * clientTrackingRange(8): 8-chunk tracking range (standard for hostile mobs)
     */
    public static final EntityType<PackWolfEntity> PACK_WOLF =
            EntityType.Builder.of(PackWolfEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 0.85f)
                    .clientTrackingRange(8)
                    .build(PACK_WOLF_KEY);

    /**
     * Register all entities. Called from BestiaryMod.onInitialize().
     *
     * ★ V3 PATTERN: Registry.register() + FabricDefaultAttributeRegistry.register() ★
     */
    public static void register() {
        Registry.register(BuiltInRegistries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(BestiaryMod.MOD_ID, "pack_wolf"),
                PACK_WOLF);

        FabricDefaultAttributeRegistry.register(PACK_WOLF,
                PackWolfEntity.createAttributes());

        BestiaryMod.LOGGER.info("Entities registered!");
    }
}
