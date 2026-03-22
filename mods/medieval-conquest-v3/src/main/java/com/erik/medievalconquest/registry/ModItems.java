package com.erik.medievalconquest.registry;

import com.erik.medievalconquest.MedievalConquestMod;
import com.erik.medievalconquest.item.BrazierItem;
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

	public static void register() {
		Registry.register(BuiltInRegistries.ITEM, BRAZIER_KEY, BRAZIER);
		Registry.register(BuiltInRegistries.ITEM, CLAIM_MARKER_KEY, CLAIM_MARKER);

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
			entries.accept(BRAZIER);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
			entries.accept(CLAIM_MARKER);
		});

		MedievalConquestMod.LOGGER.info("Items registered!");
	}
}
