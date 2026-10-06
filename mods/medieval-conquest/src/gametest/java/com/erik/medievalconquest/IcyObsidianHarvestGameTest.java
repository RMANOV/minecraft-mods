package com.erik.medievalconquest;

import com.erik.medievalconquest.registry.ModAnomalyBlocks;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.phys.AABB;

/** R01 harvest rule on the real survival break path, plus the loaded world placement of the caverns. */
public final class IcyObsidianHarvestGameTest {
    /** Breaks one icy obsidian like a survival player holding {@code tool}; returns icy items dropped. */
    private static int mine(GameTestHelper h, int x, ItemStack tool) {
        h.setBlock(x, 0, 1, Blocks.STONE);
        h.setBlock(x, 1, 1, ModAnomalyBlocks.ICY_OBSIDIAN);
        BlockPos pos = h.absolutePos(new BlockPos(x, 1, 1));
        var player = h.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        player.setItemInHand(InteractionHand.MAIN_HAND, tool);
        player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 2.5);
        h.assertTrue(player.gameMode.destroyBlock(pos), "survival break of icy obsidian succeeds with " + tool);
        h.assertTrue(h.getLevel().getBlockState(pos).isAir(), "icy obsidian is removed by " + tool);
        return h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1.0),
                        entity -> entity.getItem().is(ModAnomalyBlocks.ICY_OBSIDIAN.asItem()))
                .stream().mapToInt(entity -> entity.getItem().getCount()).sum();
    }

    @GameTest
    public void diamondPickaxeHarvestsExactlyOneIcyObsidian(GameTestHelper h) {
        var state = ModAnomalyBlocks.ICY_OBSIDIAN.defaultBlockState();
        h.assertTrue(state.requiresCorrectToolForDrops(), "icy obsidian requires the correct tool for drops");
        h.assertTrue(new ItemStack(Items.DIAMOND_PICKAXE).isCorrectToolForDrops(state), "diamond pickaxe is correct");
        h.assertTrue(new ItemStack(Items.NETHERITE_PICKAXE).isCorrectToolForDrops(state), "netherite pickaxe is correct");
        int dropped = mine(h, 2, new ItemStack(Items.DIAMOND_PICKAXE));
        h.assertTrue(dropped == 1, "diamond pickaxe yields exactly one icy obsidian, got " + dropped);
        h.succeed();
    }

    @GameTest
    public void weakerToolsBreakIcyObsidianWithoutDrop(GameTestHelper h) {
        var state = ModAnomalyBlocks.ICY_OBSIDIAN.defaultBlockState();
        h.assertTrue(!new ItemStack(Items.IRON_PICKAXE).isCorrectToolForDrops(state), "iron pickaxe is too weak");
        int iron = mine(h, 1, new ItemStack(Items.IRON_PICKAXE));
        int hand = mine(h, 4, ItemStack.EMPTY);
        int shovel = mine(h, 6, new ItemStack(Items.DIAMOND_SHOVEL));
        h.assertTrue(iron == 0 && hand == 0 && shovel == 0,
                "no icy obsidian without a diamond-tier pickaxe: iron=" + iron + " hand=" + hand + " shovel=" + shovel);
        h.succeed();
    }

    @GameTest
    public void snowCavernStructureSetIsLoadedForWorldGeneration(GameTestHelper h) {
        var setKey = ResourceKey.create(Registries.STRUCTURE_SET,
                Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "snow_caverns"));
        var structureKey = ResourceKey.create(Registries.STRUCTURE,
                Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "snow_cavern"));
        var set = h.getLevel().registryAccess().lookupOrThrow(Registries.STRUCTURE_SET).getOrThrow(setKey).value();
        h.assertTrue(set.placement() instanceof RandomSpreadStructurePlacement spread
                        && spread.spacing() == 32 && spread.separation() == 8,
                "snow caverns use the approved random spread (spacing 32, separation 8)");
        h.assertTrue(set.structures().size() == 1 && set.structures().getFirst().structure().is(structureKey),
                "structure set places exactly the snow cavern structure");
        h.succeed();
    }
}
