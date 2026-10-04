package com.erik.medievalconquest.registry;

import com.erik.medievalconquest.recipe.PermanentBarrierRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class ModAnomalyRecipes {
    private ModAnomalyRecipes() { }
    public static final RecipeSerializer<PermanentBarrierRecipe> PERMANENT_BARRIER =
            new CustomRecipe.Serializer<>(PermanentBarrierRecipe::new);
    public static void register() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath("medievalconquest", "permanent_barrier"), PERMANENT_BARRIER);
    }
}
