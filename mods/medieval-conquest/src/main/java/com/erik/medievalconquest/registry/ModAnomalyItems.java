package com.erik.medievalconquest.registry;

import com.erik.medievalconquest.MedievalConquestMod;
import com.erik.medievalconquest.item.AlienFlowerItem;
import com.erik.medievalconquest.item.BadOrbItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

/** Independent anomaly items: physical-site provisioning is a later R05 integration seam. */
public final class ModAnomalyItems {
    private static final ResourceKey<Item> ORB_KEY=ResourceKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID,"bad_orb"));
    private static final ResourceKey<Item> FLOWER_KEY=ResourceKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath(MedievalConquestMod.MOD_ID,"alien_flower"));
    public static final Item BAD_ORB=new BadOrbItem(new Item.Properties().setId(ORB_KEY).stacksTo(1));
    /** Smell in the air, plant on the ground (places ModAnomalyBlocks.ALIEN_FLOWER). */
    public static final Item ALIEN_FLOWER=new AlienFlowerItem(ModAnomalyBlocks.ALIEN_FLOWER,
            new Item.Properties().setId(FLOWER_KEY).stacksTo(16));

    private ModAnomalyItems() {}

    public static void register() {
        Registry.register(BuiltInRegistries.ITEM,ORB_KEY,BAD_ORB);
        Registry.register(BuiltInRegistries.ITEM,FLOWER_KEY,ALIEN_FLOWER);
    }
}
