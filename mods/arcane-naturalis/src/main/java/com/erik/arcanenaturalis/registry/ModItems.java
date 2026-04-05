package com.erik.arcanenaturalis.registry;

import com.erik.arcanenaturalis.ArcaneNaturalisMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

/**
 * Item registry following the exact 1.21.11 pattern.
 * BlockItem wraps a Block and adds it to the inventory system.
 * useBlockDescriptionPrefix() tells the item to use the block's translation key.
 */
public class ModItems {

    // ── Crystal Seed item (BlockItem wrapping the CrystalSeedBlock) ──────────
    private static final ResourceKey<Item> CRYSTAL_SEED_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(ArcaneNaturalisMod.MOD_ID, "crystal_seed"));

    public static final Item CRYSTAL_SEED = new BlockItem(
            ModBlocks.CRYSTAL_SEED,
            new Item.Properties()
                    .setId(CRYSTAL_SEED_KEY)
                    .useBlockDescriptionPrefix()
    );

    // ── Crystal Block item ────────────────────────────────────────────────────
    private static final ResourceKey<Item> CRYSTAL_BLOCK_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(ArcaneNaturalisMod.MOD_ID, "crystal_block"));

    public static final Item CRYSTAL_BLOCK = new BlockItem(
            ModBlocks.CRYSTAL_BLOCK,
            new Item.Properties()
                    .setId(CRYSTAL_BLOCK_KEY)
                    .useBlockDescriptionPrefix()
    );

    // ── Registration ─────────────────────────────────────────────────────────
    public static void register() {
        Registry.register(BuiltInRegistries.ITEM, CRYSTAL_SEED_KEY, CRYSTAL_SEED);
        Registry.register(BuiltInRegistries.ITEM, CRYSTAL_BLOCK_KEY, CRYSTAL_BLOCK);

        // Add to the Nature tab in Creative inventory
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries -> {
            entries.accept(CRYSTAL_SEED);
            entries.accept(CRYSTAL_BLOCK);
        });

        ArcaneNaturalisMod.LOGGER.info("Items registered!");
    }
}
