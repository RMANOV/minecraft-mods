package com.erik.machinaarcana.registry;

import com.erik.machinaarcana.MachinaArcanaMod;
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
 * ★ JAVA CONCEPT: Item Registration with Creative Tab integration ★
 *
 * All items — standalone items AND block items (the "pick up a block" items)
 * are registered here. Following the v3 pattern:
 *
 *   1. Create ResourceKey for each item
 *   2. Instantiate items with Properties (setId mandatory)
 *   3. Register in register()
 *   4. Add to creative tabs via ItemGroupEvents.modifyEntriesEvent()
 *
 * ★ BlockItem ★
 * When a block is placed, Minecraft uses a BlockItem as its item form.
 * BlockItem wraps a Block and delegates placement logic to it.
 * The Properties use {@code useBlockDescriptionPrefix()} to auto-use the
 * block's translation key as the item's name.
 *
 * ★ Creative Tab ★
 * We use ItemGroupEvents.modifyEntriesEvent() to add items into existing
 * vanilla creative tabs — no custom tab needed.
 */
public class ModItems {

    // ── Item Resource Keys ─────────────────────────────────────────────────

    private static final ResourceKey<Item> MANA_CRYSTAL_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(MachinaArcanaMod.MOD_ID, "mana_crystal"));

    private static final ResourceKey<Item> ARCANE_INGOT_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(MachinaArcanaMod.MOD_ID, "arcane_ingot"));

    private static final ResourceKey<Item> ASSEMBLER_FRAME_ITEM_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(MachinaArcanaMod.MOD_ID, "assembler_frame"));

    private static final ResourceKey<Item> ASSEMBLER_CORE_ITEM_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(MachinaArcanaMod.MOD_ID, "assembler_core"));

    private static final ResourceKey<Item> MANA_CONDUIT_ITEM_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(MachinaArcanaMod.MOD_ID, "mana_conduit"));

    // ── Item Instances ─────────────────────────────────────────────────────

    /**
     * Mana Crystal — primary mana storage item.
     * When held, the HUD overlay shows the mana bar.
     */
    public static final Item MANA_CRYSTAL = new Item(
            new Item.Properties().setId(MANA_CRYSTAL_KEY)
    );

    /**
     * Arcane Ingot — crafting material for assembler components.
     */
    public static final Item ARCANE_INGOT = new Item(
            new Item.Properties().setId(ARCANE_INGOT_KEY)
    );

    /**
     * Block items — the inventory form of each block.
     * {@code useBlockDescriptionPrefix()} makes the item name read from the block's
     * translation key ({@code block.machinaarcana.assembler_frame}).
     */
    public static final Item ASSEMBLER_FRAME_ITEM = new BlockItem(
            ModBlocks.ASSEMBLER_FRAME,
            new Item.Properties().setId(ASSEMBLER_FRAME_ITEM_KEY).useBlockDescriptionPrefix()
    );

    public static final Item ASSEMBLER_CORE_ITEM = new BlockItem(
            ModBlocks.ASSEMBLER_CORE,
            new Item.Properties().setId(ASSEMBLER_CORE_ITEM_KEY).useBlockDescriptionPrefix()
    );

    public static final Item MANA_CONDUIT_ITEM = new BlockItem(
            ModBlocks.MANA_CONDUIT,
            new Item.Properties().setId(MANA_CONDUIT_ITEM_KEY).useBlockDescriptionPrefix()
    );

    // ── Registration ───────────────────────────────────────────────────────

    /**
     * Registers all items and adds them to creative tabs.
     * Must be called after {@link ModBlocks#register()}.
     */
    public static void register() {
        // Register standalone items
        Registry.register(BuiltInRegistries.ITEM, MANA_CRYSTAL_KEY, MANA_CRYSTAL);
        Registry.register(BuiltInRegistries.ITEM, ARCANE_INGOT_KEY, ARCANE_INGOT);

        // Register block items
        Registry.register(BuiltInRegistries.ITEM, ASSEMBLER_FRAME_ITEM_KEY, ASSEMBLER_FRAME_ITEM);
        Registry.register(BuiltInRegistries.ITEM, ASSEMBLER_CORE_ITEM_KEY, ASSEMBLER_CORE_ITEM);
        Registry.register(BuiltInRegistries.ITEM, MANA_CONDUIT_ITEM_KEY, MANA_CONDUIT_ITEM);

        // Add to creative tabs using ItemGroupEvents (v3 pattern — no custom tab needed)
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
            entries.accept(MANA_CRYSTAL);
            entries.accept(ARCANE_INGOT);
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(entries -> {
            entries.accept(ASSEMBLER_FRAME_ITEM);
            entries.accept(ASSEMBLER_CORE_ITEM);
            entries.accept(MANA_CONDUIT_ITEM);
        });

        MachinaArcanaMod.LOGGER.info("ModItems registered!");
    }
}
