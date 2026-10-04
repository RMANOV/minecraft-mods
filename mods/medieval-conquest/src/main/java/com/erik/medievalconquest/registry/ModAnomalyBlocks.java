package com.erik.medievalconquest.registry;

import com.erik.medievalconquest.block.TemporaryBarrierBlock;
import com.erik.medievalconquest.block.entity.TemporaryBarrierBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

public final class ModAnomalyBlocks {
    private ModAnomalyBlocks() { }
    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath("medievalconquest", name);
    }
    private static BlockBehaviour.Properties properties(String name) {
        return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id(name)))
                .strength(5.0f, 1200.0f).pushReaction(PushReaction.BLOCK);
    }
    public static final Block ICY_OBSIDIAN = new Block(properties("icy_obsidian"));
    public static final Block TEMPORARY_BARRIER = new TemporaryBarrierBlock(
            properties("temporary_barrier").noLootTable());
    public static final Block PERMANENT_BARRIER = new Block(properties("permanent_barrier"));
    public static final BlockEntityType<TemporaryBarrierBlockEntity> TEMPORARY_BARRIER_ENTITY =
            FabricBlockEntityTypeBuilder.create(TemporaryBarrierBlockEntity::new, TEMPORARY_BARRIER).build();
    public static void register() {
        register("icy_obsidian", ICY_OBSIDIAN);
        register("temporary_barrier", TEMPORARY_BARRIER);
        register("permanent_barrier", PERMANENT_BARRIER);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("temporary_barrier"),
                TEMPORARY_BARRIER_ENTITY);
    }
    private static void register(String name, Block block) {
        Registry.register(BuiltInRegistries.BLOCK, id(name), block);
        Registry.register(BuiltInRegistries.ITEM, id(name), new BlockItem(block,
                new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id(name)))));
    }
}
