package com.erik.medievalconquest.registry;

import com.erik.medievalconquest.MedievalConquestMod;
import com.erik.medievalconquest.block.ClaimMarkerBlock;
import com.erik.medievalconquest.block.entity.ClaimMarkerBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {

	public static final ResourceKey<Block> CLAIM_MARKER_KEY = ResourceKey.create(
			Registries.BLOCK,
			Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "claim_marker"));

	public static final Block CLAIM_MARKER = new ClaimMarkerBlock(
			BlockBehaviour.Properties.of()
					.setId(CLAIM_MARKER_KEY)
					.strength(5.0f, 1200.0f)
					.requiresCorrectToolForDrops()
					.lightLevel(state -> 7)
	);

	public static final BlockEntityType<ClaimMarkerBlockEntity> CLAIM_MARKER_ENTITY =
			FabricBlockEntityTypeBuilder.create(ClaimMarkerBlockEntity::new, CLAIM_MARKER).build();

	public static void register() {
		Registry.register(BuiltInRegistries.BLOCK,
				CLAIM_MARKER_KEY,
				CLAIM_MARKER);

		Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
				Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "claim_marker"),
				CLAIM_MARKER_ENTITY);

		MedievalConquestMod.LOGGER.info("Blocks registered!");
	}
}
