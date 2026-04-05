package com.erik.machinaarcana.registry;

import com.erik.machinaarcana.MachinaArcanaMod;
import com.erik.machinaarcana.block.AssemblerCoreBlock;
import com.erik.machinaarcana.block.AssemblerFrameBlock;
import com.erik.machinaarcana.block.ManaConduitBlock;
import com.erik.machinaarcana.block.entity.ArcaneAssemblerBlockEntity;
import com.erik.machinaarcana.block.entity.ManaConduitBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * ★ JAVA CONCEPT: Static Registry Class ★
 *
 * All blocks for Machina Arcana are registered here, following the exact
 * same pattern as medieval-conquest-v3's ModBlocks:
 *
 *   1. Create a {@link ResourceKey} — the unique registry identifier
 *   2. Instantiate the block with Properties that reference the key ({@code setId})
 *   3. Register via {@link Registry#register} in the {@link #register()} method
 *
 * ★ setId() is MANDATORY in 1.21+ ★
 * Every BlockBehaviour.Properties must call {@code .setId(key)} so Minecraft
 * can resolve the block's registry name. Forgetting this causes NPEs.
 *
 * ★ FabricBlockEntityTypeBuilder ★
 * BlockEntityTypes are also registered here, linking each block entity class
 * to the block(s) it belongs to. The builder pattern ensures type safety.
 */
public class ModBlocks {

    // ── Resource Keys ──────────────────────────────────────────────────────

    public static final ResourceKey<Block> ASSEMBLER_FRAME_KEY = ResourceKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(MachinaArcanaMod.MOD_ID, "assembler_frame"));

    public static final ResourceKey<Block> ASSEMBLER_CORE_KEY = ResourceKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(MachinaArcanaMod.MOD_ID, "assembler_core"));

    public static final ResourceKey<Block> MANA_CONDUIT_KEY = ResourceKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(MachinaArcanaMod.MOD_ID, "mana_conduit"));

    // ── Block Instances ────────────────────────────────────────────────────

    /**
     * Assembler Frame — structural block for the 3×3×3 multi-block.
     * Hard as obsidian (can't be destroyed by normal tools easily),
     * sounds like stone.
     */
    public static final Block ASSEMBLER_FRAME = new AssemblerFrameBlock(
            BlockBehaviour.Properties.of()
                    .setId(ASSEMBLER_FRAME_KEY)
                    .strength(5.0f, 1200.0f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.DEEPSLATE)
    );

    /**
     * Assembler Core — the central control block of the multi-block.
     * Glows (light level 12) when the structure is formed and active.
     */
    public static final Block ASSEMBLER_CORE = new AssemblerCoreBlock(
            BlockBehaviour.Properties.of()
                    .setId(ASSEMBLER_CORE_KEY)
                    .strength(5.0f, 1200.0f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 12)
                    .sound(SoundType.AMETHYST)
    );

    /**
     * Mana Conduit — connects mana devices in a network.
     * Emits a soft glow (level 7) indicating active mana flow.
     */
    public static final Block MANA_CONDUIT = new ManaConduitBlock(
            BlockBehaviour.Properties.of()
                    .setId(MANA_CONDUIT_KEY)
                    .strength(2.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 7)
                    .sound(SoundType.GLASS)
    );

    // ── Block Entity Types ─────────────────────────────────────────────────

    /**
     * Block entity type for the Assembler Core.
     * Links {@link ArcaneAssemblerBlockEntity} to the {@link #ASSEMBLER_CORE} block.
     *
     * ★ FabricBlockEntityTypeBuilder ★
     * Fabric's builder wraps the Vanilla builder and handles the final .build() call.
     * The constructor reference {@code ArcaneAssemblerBlockEntity::new} is the factory
     * Minecraft uses when it needs to create a new block entity instance.
     */
    public static final BlockEntityType<ArcaneAssemblerBlockEntity> ASSEMBLER_CORE_ENTITY =
            FabricBlockEntityTypeBuilder
                    .create(ArcaneAssemblerBlockEntity::new, ASSEMBLER_CORE)
                    .build();

    /**
     * Block entity type for the Mana Conduit.
     * Links {@link ManaConduitBlockEntity} to the {@link #MANA_CONDUIT} block.
     */
    public static final BlockEntityType<ManaConduitBlockEntity> MANA_CONDUIT_ENTITY =
            FabricBlockEntityTypeBuilder
                    .create(ManaConduitBlockEntity::new, MANA_CONDUIT)
                    .build();

    // ── Registration ───────────────────────────────────────────────────────

    /**
     * Registers all blocks and block entity types with Minecraft's registries.
     * Must be called from {@link MachinaArcanaMod#onInitialize()}.
     *
     * Order: blocks first, then block entity types (entity types reference blocks).
     */
    public static void register() {
        // Register blocks
        Registry.register(BuiltInRegistries.BLOCK, ASSEMBLER_FRAME_KEY, ASSEMBLER_FRAME);
        Registry.register(BuiltInRegistries.BLOCK, ASSEMBLER_CORE_KEY, ASSEMBLER_CORE);
        Registry.register(BuiltInRegistries.BLOCK, MANA_CONDUIT_KEY, MANA_CONDUIT);

        // Register block entity types
        Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(MachinaArcanaMod.MOD_ID, "assembler_core"),
                ASSEMBLER_CORE_ENTITY);

        Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(MachinaArcanaMod.MOD_ID, "mana_conduit"),
                MANA_CONDUIT_ENTITY);

        MachinaArcanaMod.LOGGER.info("ModBlocks registered!");
    }
}
