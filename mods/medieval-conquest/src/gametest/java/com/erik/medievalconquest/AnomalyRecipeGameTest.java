package com.erik.medievalconquest;

import com.erik.medievalconquest.recipe.PermanentBarrierRecipe;
import com.erik.medievalconquest.registry.ModAnomalyBlocks;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

public final class AnomalyRecipeGameTest {
    @GameTest
    public void loadedIcyRecipeUsesLiteralThreeFourTwoGrid(GameTestHelper h) {
        var input = CraftingInput.of(3, 3, List.of(
                new ItemStack(Items.OBSIDIAN), new ItemStack(Items.DIAMOND), new ItemStack(Items.ICE),
                new ItemStack(Items.DIAMOND), new ItemStack(Items.OBSIDIAN), new ItemStack(Items.DIAMOND),
                new ItemStack(Items.ICE), new ItemStack(Items.DIAMOND), new ItemStack(Items.OBSIDIAN)));
        var recipe = h.getLevel().getServer().getRecipeManager()
                .byKey(ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath("medievalconquest", "icy_obsidian")));
        h.assertTrue(recipe.isPresent() && recipe.orElseThrow().value() instanceof CraftingRecipe,
                "loaded icy recipe must be a real crafting recipe");
        var crafting = (CraftingRecipe) recipe.orElseThrow().value();
        h.assertTrue(crafting.matches(input, h.getLevel()),
                "real loaded icy recipe must match literal ODI/DOD/IDO");
        var result = crafting.assemble(input, h.getLevel().registryAccess());
        h.assertTrue(result.is(ModAnomalyBlocks.ICY_OBSIDIAN.asItem()) && result.getCount() == 1,
                "one real icy obsidian result");
        h.succeed();
    }

    @GameTest
    public void loadedTemporaryRecipeConsumesOneIcyGridSlot(GameTestHelper h) {
        var input = CraftingInput.of(1, 1, List.of(new ItemStack(ModAnomalyBlocks.ICY_OBSIDIAN)));
        var recipe = h.getLevel().getServer().getRecipeManager()
                .byKey(ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath("medievalconquest", "temporary_barrier")));
        h.assertTrue(recipe.isPresent() && recipe.orElseThrow().value() instanceof CraftingRecipe,
                "loaded temporary recipe must be a real crafting recipe");
        var crafting = (CraftingRecipe) recipe.orElseThrow().value();
        h.assertTrue(crafting.matches(input, h.getLevel()), "one icy recipe loaded");
        var result = crafting.assemble(input, h.getLevel().registryAccess());
        h.assertTrue(result.is(ModAnomalyBlocks.TEMPORARY_BARRIER.asItem()) && result.getCount() == 1,
                "exactly one temporary barrier");
        h.succeed();
    }
    private ItemStack infinityBook(GameTestHelper h) {
        var book = new ItemStack(Items.ENCHANTED_BOOK);
        var enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        enchantments.upgrade(h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.INFINITY), 1);
        book.set(DataComponents.STORED_ENCHANTMENTS, enchantments.toImmutable());
        return book;
    }

    @GameTest
    public void genuineInfinityAndOneIcyAssembleOnePermanentWithoutMutation(GameTestHelper h) {
        var icy = new ItemStack(ModAnomalyBlocks.ICY_OBSIDIAN, 3);
        var book = infinityBook(h);
        var input = CraftingInput.of(2, 1, List.of(book, icy));
        var recipe = new PermanentBarrierRecipe(CraftingBookCategory.BUILDING);
        h.assertTrue(recipe.matches(input, h.getLevel()), "one icy plus genuine Infinity must match");
        var result = recipe.assemble(input, h.getLevel().registryAccess());
        h.assertTrue(result.is(ModAnomalyBlocks.PERMANENT_BARRIER.asItem()) && result.getCount() == 1,
                "exactly one permanent barrier result");
        h.assertTrue(icy.getCount() == 3 && book.getCount() == 1,
                "match and assemble must not consume input outside the native menu");
        h.succeed();
    }

    @GameTest
    public void extraOccupiedSlotRejectsInfinityRecipe(GameTestHelper h) {
        var input = CraftingInput.of(3, 1, List.of(new ItemStack(ModAnomalyBlocks.ICY_OBSIDIAN),
                infinityBook(h), new ItemStack(Items.STONE)));
        h.assertTrue(!new PermanentBarrierRecipe(CraftingBookCategory.BUILDING)
                .matches(input, h.getLevel()), "extra occupied slot must reject");
        h.succeed();
    }
    @GameTest
    public void ordinaryBookCannotGrantPermanentBarrier(GameTestHelper h) {
        var input = CraftingInput.of(2, 1, List.of(
                new ItemStack(ModAnomalyBlocks.ICY_OBSIDIAN), new ItemStack(Items.BOOK)));
        h.assertTrue(!new PermanentBarrierRecipe(CraftingBookCategory.BUILDING)
                .matches(input, h.getLevel()), "ordinary book is not Infinity");
        h.succeed();
    }

    @GameTest
    public void missingBookCannotGrantPermanentBarrier(GameTestHelper h) {
        var input = CraftingInput.of(1, 1, List.of(new ItemStack(ModAnomalyBlocks.ICY_OBSIDIAN)));
        h.assertTrue(!new PermanentBarrierRecipe(CraftingBookCategory.BUILDING)
                .matches(input, h.getLevel()), "one icy alone is not permanent");
        h.succeed();
    }
}
