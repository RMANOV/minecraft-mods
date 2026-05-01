package com.erik.medievalconquest.registry;

import com.erik.medievalconquest.MedievalConquestMod;
import com.erik.medievalconquest.block.ClaimMarkerBlock;
import com.erik.medievalconquest.block.LightSourceBlock;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.FlowerBlock;
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
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;

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

	// ── Hyacinth flower (лулък) ──
	public static final ResourceKey<Block> HYACINTH_KEY = ResourceKey.create(
			Registries.BLOCK,
			Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "hyacinth"));

	public static final Block HYACINTH = new FlowerBlock(MobEffects.LUCK, 5.0f,
			BlockBehaviour.Properties.of()
					.setId(HYACINTH_KEY)
					.noCollision()
					.instabreak()
					.sound(SoundType.GRASS)
					.offsetType(BlockBehaviour.OffsetType.XZ)
					.pushReaction(PushReaction.DESTROY)
	);

	// ── Lilac tree blocks for spruce/taiga biomes ──
	public static final ResourceKey<Block> LILAC_LOG_KEY = ResourceKey.create(
			Registries.BLOCK,
			Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "lilac_log"));

	public static final Block LILAC_LOG = new RotatedPillarBlock(
			BlockBehaviour.Properties.of()
					.setId(LILAC_LOG_KEY)
					.strength(2.0f)
					.sound(SoundType.WOOD)
					.ignitedByLava()
	);

	public static final ResourceKey<Block> LILAC_LEAVES_KEY = ResourceKey.create(
			Registries.BLOCK,
			Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "lilac_leaves"));

	public static final Block LILAC_LEAVES = new UntintedParticleLeavesBlock(0.025f, ParticleTypes.CHERRY_LEAVES,
			BlockBehaviour.Properties.of()
					.setId(LILAC_LEAVES_KEY)
					.strength(0.2f)
					.randomTicks()
					.sound(SoundType.GRASS)
					.noOcclusion()
					.ignitedByLava()
					.pushReaction(PushReaction.DESTROY)
	);

	// ── Invisible dynamic light source ──
	public static final ResourceKey<Block> LIGHT_SOURCE_KEY = ResourceKey.create(
			Registries.BLOCK,
			Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "light_source"));

	public static final Block LIGHT_SOURCE = new LightSourceBlock(
			BlockBehaviour.Properties.of()
					.setId(LIGHT_SOURCE_KEY)
					.replaceable()
					.noCollision()
					.noLootTable()
					.noOcclusion()
					.lightLevel(state -> 14)
					.air()
					.pushReaction(PushReaction.DESTROY)
	);

	public static void register() {
		Registry.register(BuiltInRegistries.BLOCK,
				CLAIM_MARKER_KEY,
				CLAIM_MARKER);

		Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
				Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "claim_marker"),
				CLAIM_MARKER_ENTITY);

		Registry.register(BuiltInRegistries.BLOCK, HYACINTH_KEY, HYACINTH);
		Registry.register(BuiltInRegistries.BLOCK, LILAC_LOG_KEY, LILAC_LOG);
		Registry.register(BuiltInRegistries.BLOCK, LILAC_LEAVES_KEY, LILAC_LEAVES);
		Registry.register(BuiltInRegistries.BLOCK, LIGHT_SOURCE_KEY, LIGHT_SOURCE);

		MedievalConquestMod.LOGGER.info("Blocks registered!");
	}
}
