package com.erik.medievalconquest.registry;

import com.erik.medievalconquest.MedievalConquestMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/** One creative tab that lists every item this mod registers, in registration order. */
public final class ModCreativeTab {
    public static final ResourceKey<CreativeModeTab> KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID, "medieval_conquest"));

    public static final CreativeModeTab TAB = FabricItemGroup.builder()
            .title(Component.translatable("itemGroup." + MedievalConquestMod.MOD_ID))
            .icon(() -> new ItemStack(ModAnomalyBlocks.ICY_OBSIDIAN))
            // Read the registry when the tab is built, so items added by any module show up.
            .displayItems((parameters, output) -> BuiltInRegistries.ITEM.listElements()
                    .filter(holder -> holder.key().identifier().getNamespace().equals(MedievalConquestMod.MOD_ID))
                    .forEach(holder -> output.accept(holder.value())))
            .build();

    private ModCreativeTab() { }

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, KEY, TAB);
    }
}
