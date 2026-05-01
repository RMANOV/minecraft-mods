package com.erik.medievalconquest.registry;

import com.erik.medievalconquest.MedievalConquestMod;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;

public class ModBrewingRecipes {
	public static void register() {
		FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
			FabricBrewingRecipeRegistryBuilder fabricBuilder = (FabricBrewingRecipeRegistryBuilder) builder;
			Ingredient herb = Ingredient.of(ModItems.HERB_BUNDLE);

			fabricBuilder.registerPotionRecipe(Potions.AWKWARD, herb, Potions.REGENERATION);
			fabricBuilder.registerPotionRecipe(Potions.REGENERATION, herb, Potions.STRONG_REGENERATION);
		});

		MedievalConquestMod.LOGGER.info("Brewing recipes registered!");
	}
}
