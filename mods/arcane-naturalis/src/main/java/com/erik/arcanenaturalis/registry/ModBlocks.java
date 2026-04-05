package com.erik.arcanenaturalis.registry;

import com.erik.arcanenaturalis.ArcaneNaturalisMod;
import com.erik.arcanenaturalis.block.CrystalBlock;
import com.erik.arcanenaturalis.block.CrystalSeedBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Block registry following the exact 1.21.11 pattern:
 *   1. Create a ResourceKey with the block's namespaced id
 *   2. Instantiate the Block passing Properties.of().setId(key)
 *   3. Call Registry.register(BuiltInRegistries.BLOCK, key, block) in register()
 *
 * The setId() call is MANDATORY in 1.21.11 — it ties the Properties to the
 * registry key so the game can look up the block's name/loot table correctly.
 */
public class ModBlocks {

    // ── Crystal Seed ────────────────────────────────────────────────────────
    public static final ResourceKey<Block> CRYSTAL_SEED_KEY = ResourceKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(ArcaneNaturalisMod.MOD_ID, "crystal_seed"));

    public static final Block CRYSTAL_SEED = new CrystalSeedBlock(
            BlockBehaviour.Properties.of()
                    .setId(CRYSTAL_SEED_KEY)
                    .noCollision()
                    .randomTicks()
                    .instabreak()
                    .noOcclusion()
    );

    // ── Crystal Block ────────────────────────────────────────────────────────
    public static final ResourceKey<Block> CRYSTAL_BLOCK_KEY = ResourceKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(ArcaneNaturalisMod.MOD_ID, "crystal_block"));

    public static final Block CRYSTAL_BLOCK = new CrystalBlock(
            BlockBehaviour.Properties.of()
                    .setId(CRYSTAL_BLOCK_KEY)
                    .strength(1.5f, 6.0f)
                    .lightLevel(state -> 7)
                    .noOcclusion()
    );

    // ── Registration ─────────────────────────────────────────────────────────
    public static void register() {
        Registry.register(BuiltInRegistries.BLOCK, CRYSTAL_SEED_KEY, CRYSTAL_SEED);
        Registry.register(BuiltInRegistries.BLOCK, CRYSTAL_BLOCK_KEY, CRYSTAL_BLOCK);

        ArcaneNaturalisMod.LOGGER.info("Blocks registered!");
    }
}
