package com.erik.medievalconquest.registry;

import com.erik.medievalconquest.MedievalConquestMod;
import com.erik.medievalconquest.entity.OverworldDragonEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

	private static final ResourceKey<EntityType<?>> DRAGON_KEY = ResourceKey.create(
			Registries.ENTITY_TYPE,
			Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "overworld_dragon"));

	public static final EntityType<OverworldDragonEntity> OVERWORLD_DRAGON =
			EntityType.Builder.of(OverworldDragonEntity::new, MobCategory.MONSTER)
					.sized(3.0f, 2.5f)
					.clientTrackingRange(10)
					.build(DRAGON_KEY);

	public static void register() {
		Registry.register(BuiltInRegistries.ENTITY_TYPE,
				Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "overworld_dragon"),
				OVERWORLD_DRAGON);

		FabricDefaultAttributeRegistry.register(OVERWORLD_DRAGON,
				OverworldDragonEntity.createAttributes());

		MedievalConquestMod.LOGGER.info("Entities registered!");
	}
}
