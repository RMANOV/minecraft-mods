package com.erik.medievalconquest.registry;

import com.erik.medievalconquest.block.AlienFlowerBlock;
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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
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
    /** Natural snow-cavern resource: only a diamond-tier pickaxe harvests it (tags in data/minecraft). */
    public static final Block ICY_OBSIDIAN = new Block(properties("icy_obsidian").requiresCorrectToolForDrops());
    public static final Block TEMPORARY_BARRIER = new TemporaryBarrierBlock(
            properties("temporary_barrier").noLootTable());
    public static final Block PERMANENT_BARRIER = new Block(properties("permanent_barrier"));
    public static final BlockEntityType<TemporaryBarrierBlockEntity> TEMPORARY_BARRIER_ENTITY =
            FabricBlockEntityTypeBuilder.create(TemporaryBarrierBlockEntity::new, TEMPORARY_BARRIER).build();
    /** Planted R08 flower with a faint glow; its item is ModAnomalyItems.ALIEN_FLOWER (no extra BlockItem). */
    public static final Block ALIEN_FLOWER = new AlienFlowerBlock(BlockBehaviour.Properties.of()
            .setId(ResourceKey.create(Registries.BLOCK, id("alien_flower"))).mapColor(MapColor.COLOR_CYAN)
            .noCollision().instabreak().sound(SoundType.GRASS).lightLevel(state -> 5)
            .pushReaction(PushReaction.DESTROY));
    public static void register() {
        register("icy_obsidian", ICY_OBSIDIAN);
        register("temporary_barrier", TEMPORARY_BARRIER);
        register("permanent_barrier", PERMANENT_BARRIER);
        Registry.register(BuiltInRegistries.BLOCK, id("alien_flower"), ALIEN_FLOWER);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("temporary_barrier"),
                TEMPORARY_BARRIER_ENTITY);
    }
    private static void register(String name, Block block) {
        Registry.register(BuiltInRegistries.BLOCK, id(name), block);
        Registry.register(BuiltInRegistries.ITEM, id(name), new BlockItem(block,
                new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id(name)))
                        .useBlockDescriptionPrefix()));
    }
}
