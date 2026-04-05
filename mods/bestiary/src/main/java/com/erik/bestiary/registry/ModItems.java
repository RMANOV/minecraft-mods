package com.erik.bestiary.registry;

import com.erik.bestiary.BestiaryMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

/**
 * ModItems — item registration.
 *
 * ★ V3 PATTERN: ResourceKey + Registry.register + ItemGroupEvents ★
 *
 * We register:
 * - PACK_WOLF_SPAWN_EGG: lets players spawn pack wolves manually
 *   Added to SPAWN_EGGS creative tab (standard location for spawn eggs)
 *
 * SpawnEggItem(entityType, backgroundColor, overlayColor, properties):
 * - backgroundColor: main egg color (gray wolf body)
 * - overlayColor: spots color (dark for wolf markings)
 */
public class ModItems {

    private static final ResourceKey<Item> PACK_WOLF_SPAWN_EGG_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(BestiaryMod.MOD_ID, "pack_wolf_spawn_egg"));

    /**
     * Spawn egg for PackWolfEntity.
     * Colors: 0x8a8a8a (gray body) and 0x2c2c2c (dark spots).
     *
     * SpawnEggItem requires the entity type at registration time.
     * It uses the entity's registered EntityType to spawn it.
     */
    public static final Item PACK_WOLF_SPAWN_EGG = new SpawnEggItem(
            new Item.Properties().setId(PACK_WOLF_SPAWN_EGG_KEY).spawnEgg(ModEntities.PACK_WOLF)
    );

    /**
     * Register all items. Called from BestiaryMod.onInitialize().
     *
     * ItemGroupEvents.modifyEntriesEvent: adds our item to an existing creative tab.
     * We use SPAWN_EGGS tab so it appears with all other spawn eggs.
     */
    public static void register() {
        Registry.register(BuiltInRegistries.ITEM, PACK_WOLF_SPAWN_EGG_KEY, PACK_WOLF_SPAWN_EGG);

        // Add to spawn eggs creative tab
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> {
            entries.accept(PACK_WOLF_SPAWN_EGG);
        });

        BestiaryMod.LOGGER.info("Items registered!");
    }
}
