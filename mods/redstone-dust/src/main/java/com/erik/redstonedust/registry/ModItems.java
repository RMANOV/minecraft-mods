package com.erik.redstonedust.registry;

import com.erik.redstonedust.RedstoneDustMod;
import com.erik.redstonedust.item.RedstoneKitItem;
import com.erik.redstonedust.item.TutorialBookItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class ModItems {

	private static final ResourceKey<Item> REDSTONE_KIT_KEY = ResourceKey.create(
			Registries.ITEM,
			Identifier.fromNamespaceAndPath(RedstoneDustMod.MOD_ID, "redstone_kit"));

	private static final ResourceKey<Item> TUTORIAL_BOOK_KEY = ResourceKey.create(
			Registries.ITEM,
			Identifier.fromNamespaceAndPath(RedstoneDustMod.MOD_ID, "tutorial_book"));

	public static final Item REDSTONE_KIT = new RedstoneKitItem(
			new Item.Properties().setId(REDSTONE_KIT_KEY).stacksTo(16)
	);

	public static final Item TUTORIAL_BOOK = new TutorialBookItem(
			new Item.Properties().setId(TUTORIAL_BOOK_KEY).stacksTo(1)
	);

	public static void register() {
		Registry.register(BuiltInRegistries.ITEM, REDSTONE_KIT_KEY, REDSTONE_KIT);
		Registry.register(BuiltInRegistries.ITEM, TUTORIAL_BOOK_KEY, TUTORIAL_BOOK);

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(entries -> {
			entries.accept(REDSTONE_KIT);
			entries.accept(TUTORIAL_BOOK);
		});

		RedstoneDustMod.LOGGER.info("Items registered!");
	}
}
