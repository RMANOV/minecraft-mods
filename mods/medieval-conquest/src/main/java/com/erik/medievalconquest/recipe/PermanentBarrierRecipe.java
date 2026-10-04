package com.erik.medievalconquest.recipe;

import com.erik.medievalconquest.registry.ModAnomalyRecipes;
import com.erik.medievalconquest.registry.ModAnomalyBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/** One icy ingredient and a genuine Infinity book, without mutating the input. */
public final class PermanentBarrierRecipe extends CustomRecipe {
    public PermanentBarrierRecipe(CraftingBookCategory category) { super(category); }
    @Override public boolean matches(CraftingInput input, Level level) {
        return validInput(input, level.registryAccess());
    }
    @Override public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return validInput(input, registries)
                ? new ItemStack(ModAnomalyBlocks.PERMANENT_BARRIER) : ItemStack.EMPTY;
    }
    private boolean validInput(CraftingInput input, HolderLookup.Provider registries) {
        boolean icy = false;
        boolean book = false;
        var infinity = registries.lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.INFINITY);
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(ModAnomalyBlocks.ICY_OBSIDIAN.asItem()) && !icy) {
                icy = true;
            } else if (stack.is(Items.ENCHANTED_BOOK) && !book
                    && stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS,
                            ItemEnchantments.EMPTY).getLevel(infinity) > 0) {
                book = true;
            } else {
                return false;
            }
        }
        return icy && book;
    }
    @Override public RecipeSerializer<PermanentBarrierRecipe> getSerializer() {
        return ModAnomalyRecipes.PERMANENT_BARRIER;
    }
}
