package com.erik.medievalconquest.registry;

import com.erik.medievalconquest.MedievalConquestMod;
import com.erik.medievalconquest.item.BrazierItem;
import com.erik.medievalconquest.item.HerbItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class ModItems {

	private static final ResourceKey<Item> BRAZIER_KEY = ResourceKey.create(
			Registries.ITEM,
			Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "brazier"));

	private static final ResourceKey<Item> CLAIM_MARKER_KEY = ResourceKey.create(
			Registries.ITEM,
			Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "claim_marker"));

	public static final Item BRAZIER = new BrazierItem(
			new Item.Properties().setId(BRAZIER_KEY).durability(128)
	);

	public static final Item CLAIM_MARKER = new BlockItem(
			ModBlocks.CLAIM_MARKER,
			new Item.Properties().setId(CLAIM_MARKER_KEY).useBlockDescriptionPrefix()
	);

	// ── Hyacinth flower item ──
	private static final ResourceKey<Item> HYACINTH_KEY = ResourceKey.create(
			Registries.ITEM,
			Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "hyacinth"));

	public static final Item HYACINTH = new BlockItem(
			ModBlocks.HYACINTH,
			new Item.Properties().setId(HYACINTH_KEY).useBlockDescriptionPrefix()
	);

	// ── Herb Bundle ──
	private static final ResourceKey<Item> HERB_BUNDLE_KEY = ResourceKey.create(
			Registries.ITEM,
			Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "herb_bundle"));

	public static final Item HERB_BUNDLE = new HerbItem(
			new Item.Properties().setId(HERB_BUNDLE_KEY).stacksTo(16)
	);

	public static void register() {
		Registry.register(BuiltInRegistries.ITEM, BRAZIER_KEY, BRAZIER);
		Registry.register(BuiltInRegistries.ITEM, CLAIM_MARKER_KEY, CLAIM_MARKER);
		Registry.register(BuiltInRegistries.ITEM, HYACINTH_KEY, HYACINTH);
		Registry.register(BuiltInRegistries.ITEM, HERB_BUNDLE_KEY, HERB_BUNDLE);

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
			entries.accept(BRAZIER);
			entries.accept(HERB_BUNDLE);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
			entries.accept(CLAIM_MARKER);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries -> {
			entries.accept(HYACINTH);
		});

		MedievalConquestMod.LOGGER.info("Items registered!");
	}
}
