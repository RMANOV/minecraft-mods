package com.erik.medievalconquest.registry;

import com.erik.medievalconquest.MedievalConquestMod;
import com.erik.medievalconquest.item.BrazierItem;
import com.erik.medievalconquest.item.CandiedFlowerItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.food.FoodProperties;

public class ModItems {

	private static final ResourceKey<Item> BRAZIER_KEY = ResourceKey.create(
			Registries.ITEM,
			Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "brazier"));

	private static final ResourceKey<Item> CLAIM_MARKER_KEY = ResourceKey.create(
			Registries.ITEM,
			Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "claim_marker"));

	private static final ResourceKey<Item> LILAC_LOG_KEY = ResourceKey.create(
			Registries.ITEM,
			Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "lilac_log"));

	private static final ResourceKey<Item> LILAC_LEAVES_KEY = ResourceKey.create(
			Registries.ITEM,
			Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "lilac_leaves"));

	private static final ResourceKey<Item> LILAC_FLOWERING_LEAVES_KEY = ResourceKey.create(
			Registries.ITEM,
			Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "lilac_flowering_leaves"));

	private static final ResourceKey<Item> PINK_FLOWER_KEY = ResourceKey.create(
			Registries.ITEM,
			Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "pink_flower"));

	private static final ResourceKey<Item> CANDIED_FLOWER_KEY = ResourceKey.create(
			Registries.ITEM,
			Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "candied_flower"));

	private static final ResourceKey<Item> OVERWORLD_DRAGON_SPAWN_EGG_KEY = ResourceKey.create(
			Registries.ITEM,
			Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "overworld_dragon_spawn_egg"));

	public static final Item BRAZIER = new BrazierItem(
			new Item.Properties().setId(BRAZIER_KEY).durability(128)
	);

	public static final Item CLAIM_MARKER = new BlockItem(
			ModBlocks.CLAIM_MARKER,
			new Item.Properties().setId(CLAIM_MARKER_KEY).useBlockDescriptionPrefix()
	);

	public static final Item LILAC_LOG = new BlockItem(
			ModBlocks.LILAC_LOG,
			new Item.Properties().setId(LILAC_LOG_KEY).useBlockDescriptionPrefix()
	);

	public static final Item LILAC_LEAVES = new BlockItem(
			ModBlocks.LILAC_LEAVES,
			new Item.Properties().setId(LILAC_LEAVES_KEY).useBlockDescriptionPrefix()
	);

	public static final Item LILAC_FLOWERING_LEAVES = new BlockItem(
			ModBlocks.LILAC_FLOWERING_LEAVES,
			new Item.Properties().setId(LILAC_FLOWERING_LEAVES_KEY).useBlockDescriptionPrefix()
	);

	public static final Item PINK_FLOWER = new Item(
			new Item.Properties().setId(PINK_FLOWER_KEY)
	);

	public static final Item CANDIED_FLOWER = new CandiedFlowerItem(
			new Item.Properties()
					.setId(CANDIED_FLOWER_KEY)
					.stacksTo(16)
					.food(new FoodProperties.Builder()
							.nutrition(4)
							.saturationModifier(0.6f)
							.build(), Consumables.DEFAULT_FOOD)
	);

	public static final Item OVERWORLD_DRAGON_SPAWN_EGG = new SpawnEggItem(
			new Item.Properties()
					.setId(OVERWORLD_DRAGON_SPAWN_EGG_KEY)
					.spawnEgg(ModEntities.OVERWORLD_DRAGON)
	);

	public static void register() {
		Registry.register(BuiltInRegistries.ITEM, BRAZIER_KEY, BRAZIER);
		Registry.register(BuiltInRegistries.ITEM, CLAIM_MARKER_KEY, CLAIM_MARKER);
		Registry.register(BuiltInRegistries.ITEM, LILAC_LOG_KEY, LILAC_LOG);
		Registry.register(BuiltInRegistries.ITEM, LILAC_LEAVES_KEY, LILAC_LEAVES);
		Registry.register(BuiltInRegistries.ITEM, LILAC_FLOWERING_LEAVES_KEY, LILAC_FLOWERING_LEAVES);
		Registry.register(BuiltInRegistries.ITEM, PINK_FLOWER_KEY, PINK_FLOWER);
		Registry.register(BuiltInRegistries.ITEM, CANDIED_FLOWER_KEY, CANDIED_FLOWER);
		Registry.register(BuiltInRegistries.ITEM, OVERWORLD_DRAGON_SPAWN_EGG_KEY, OVERWORLD_DRAGON_SPAWN_EGG);

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
			entries.accept(BRAZIER);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
			entries.accept(CLAIM_MARKER);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries -> {
			entries.accept(LILAC_LOG);
			entries.accept(LILAC_LEAVES);
			entries.accept(LILAC_FLOWERING_LEAVES);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
			entries.accept(PINK_FLOWER);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(entries -> {
			entries.accept(CANDIED_FLOWER);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> {
			entries.accept(OVERWORLD_DRAGON_SPAWN_EGG);
		});

		MedievalConquestMod.LOGGER.info("Items registered!");
	}
}
