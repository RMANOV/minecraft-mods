package com.erik.medievalconquest.registry;

import com.erik.medievalconquest.MedievalConquestMod;
import com.erik.medievalconquest.item.BrazierItem;
import com.erik.medievalconquest.item.DragonMeatItem;
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

	private static final ResourceKey<Item> DRAGON_MEAT_KEY = ResourceKey.create(
			Registries.ITEM,
			Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "dragon_meat"));

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

	public static final Item DRAGON_MEAT = new DragonMeatItem(
			new Item.Properties()
					.setId(DRAGON_MEAT_KEY)
					.stacksTo(16)
					.food(new FoodProperties.Builder()
							.nutrition(12)
							.saturationModifier(1.2f)
							.alwaysEdible()
							.build(), Consumables.DEFAULT_FOOD)
	);

	public static final Item OVERWORLD_DRAGON_SPAWN_EGG = new SpawnEggItem(
			new Item.Properties().setId(OVERWORLD_DRAGON_SPAWN_EGG_KEY).spawnEgg(ModEntities.OVERWORLD_DRAGON)
	);

	public static void register() {
		Registry.register(BuiltInRegistries.ITEM, BRAZIER_KEY, BRAZIER);
		Registry.register(BuiltInRegistries.ITEM, CLAIM_MARKER_KEY, CLAIM_MARKER);
		Registry.register(BuiltInRegistries.ITEM, LILAC_LOG_KEY, LILAC_LOG);
		Registry.register(BuiltInRegistries.ITEM, LILAC_LEAVES_KEY, LILAC_LEAVES);
		Registry.register(BuiltInRegistries.ITEM, HYACINTH_KEY, HYACINTH);
		Registry.register(BuiltInRegistries.ITEM, HERB_BUNDLE_KEY, HERB_BUNDLE);
		Registry.register(BuiltInRegistries.ITEM, DRAGON_MEAT_KEY, DRAGON_MEAT);
		Registry.register(BuiltInRegistries.ITEM, OVERWORLD_DRAGON_SPAWN_EGG_KEY, OVERWORLD_DRAGON_SPAWN_EGG);

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
			entries.accept(BRAZIER);
			entries.accept(HERB_BUNDLE);
			entries.accept(DRAGON_MEAT);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> {
			entries.accept(OVERWORLD_DRAGON_SPAWN_EGG);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
			entries.accept(CLAIM_MARKER);
		});

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries -> {
			entries.accept(HYACINTH);
			entries.accept(LILAC_LOG);
			entries.accept(LILAC_LEAVES);
		});

		MedievalConquestMod.LOGGER.info("Items registered!");
	}
}
