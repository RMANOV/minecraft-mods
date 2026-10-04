package com.erik.medievalconquest;

import com.erik.medievalconquest.block.entity.TemporaryBarrierBlockEntity;
import com.erik.medievalconquest.registry.ModAnomalyBlocks;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class BarrierGameTest {
    @GameTest
    public void realPlayerPlacementStartsTemporaryLifetime(GameTestHelper h) {
        h.setBlock(1, 0, 1, Blocks.STONE);
        h.setBlock(1, 1, 1, Blocks.AIR);
        var target = h.absolutePos(new BlockPos(1, 1, 1));
        var support = target.below();
        var player = h.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.setPos(target.getX() + 2.5, target.getY(), target.getZ() + 2.5);
        var stack = new ItemStack(ModAnomalyBlocks.TEMPORARY_BARRIER, 1);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var hit = new BlockHitResult(new Vec3(support.getX() + 0.5,
                support.getY() + 1.0, support.getZ() + 0.5), Direction.UP, support, false);
        long beforePlacement = System.currentTimeMillis();
        var result = ((BlockItem) stack.getItem()).place(new BlockPlaceContext(
                player, InteractionHand.MAIN_HAND, stack, hit));
        long afterPlacement = System.currentTimeMillis();
        h.assertTrue(result.consumesAction(), "vanilla BlockItem placement must succeed");
        h.assertBlockPresent(ModAnomalyBlocks.TEMPORARY_BARRIER, 1, 1, 1);
        h.assertTrue(stack.isEmpty(), "survival placement consumes the one barrier item");
        h.assertTrue(h.getLevel().getBlockEntity(target) instanceof TemporaryBarrierBlockEntity,
                "actual placement creates the registered temporary entity");
        var entity = (TemporaryBarrierBlockEntity) h.getLevel().getBlockEntity(target);
        var saved = entity.saveCustomOnly(h.getLevel().registryAccess());
        h.assertTrue(saved.getBoolean("initialized").orElse(false),
                "placement did not initialize the temporary lifetime");
        long placed = saved.getLong("placed").orElse(-1L);
        h.assertTrue(placed >= beforePlacement && placed <= afterPlacement,
                "placement timestamp belongs to the actual placement interval");
        h.assertTrue(saved.getLong("deadline").orElse(-1L) == placed + 1800000L,
                "actual placement grants exactly thirty minutes");
        h.succeed();
    }

    private TemporaryBarrierBlockEntity place(GameTestHelper h) {
        h.setBlock(1, 1, 1, ModAnomalyBlocks.TEMPORARY_BARRIER);
        return (TemporaryBarrierBlockEntity) h.getLevel().getBlockEntity(
                h.absolutePos(new BlockPos(1, 1, 1)));
    }

    @GameTest
    public void temporaryExpiresAtExactDeadline(GameTestHelper h) {
        var entity = place(h);
        h.assertTrue(entity.initializeAt(1000L), "first initialization must succeed");
        var pos = h.absolutePos(new BlockPos(1, 1, 1));
        TemporaryBarrierBlockEntity.serverTickAt(h.getLevel(), pos,
                h.getLevel().getBlockState(pos), entity, 1800999L);
        h.assertBlockPresent(ModAnomalyBlocks.TEMPORARY_BARRIER, 1, 1, 1);
        TemporaryBarrierBlockEntity.serverTickAt(h.getLevel(), pos,
                h.getLevel().getBlockState(pos), entity, 1801000L);
        h.assertBlockPresent(Blocks.AIR, 1, 1, 1);
        h.succeed();
    }

    @GameTest
    public void secondInitializationCannotRenewLifetime(GameTestHelper h) {
        var entity = place(h);
        h.assertTrue(entity.initializeAt(1000L), "first initialization");
        h.assertTrue(!entity.initializeAt(2000L), "second initialization must refuse reset");
        h.succeed();
    }

    @GameTest
    public void uninitializedTemporaryFailsClosed(GameTestHelper h) {
        var entity = place(h);
        var pos = h.absolutePos(new BlockPos(1, 1, 1));
        TemporaryBarrierBlockEntity.serverTickAt(h.getLevel(), pos,
                h.getLevel().getBlockState(pos), entity, 1000L);
        h.assertBlockPresent(Blocks.AIR, 1, 1, 1);
        h.succeed();
    }

    @GameTest
    public void staleEntityCannotRemoveReplacementStone(GameTestHelper h) {
        var entity = place(h);
        entity.initializeAt(1000L);
        var pos = h.absolutePos(new BlockPos(1, 1, 1));
        h.setBlock(1, 1, 1, Blocks.STONE);
        TemporaryBarrierBlockEntity.serverTickAt(h.getLevel(), pos,
                ModAnomalyBlocks.TEMPORARY_BARRIER.defaultBlockState(), entity, 1801000L);
        h.assertBlockPresent(Blocks.STONE, 1, 1, 1);
        h.succeed();
    }

    @GameTest
    public void staleEntityCannotRemoveNewTemporaryEntity(GameTestHelper h) {
        var old = place(h);
        old.initializeAt(1000L);
        h.setBlock(1, 1, 1, Blocks.AIR);
        var replacement = place(h);
        replacement.initializeAt(1801000L);
        var pos = h.absolutePos(new BlockPos(1, 1, 1));
        h.assertTrue(old != replacement, "replacement must be a fresh real entity");
        TemporaryBarrierBlockEntity.serverTickAt(h.getLevel(), pos,
                h.getLevel().getBlockState(pos), old, 1801000L);
        h.assertBlockPresent(ModAnomalyBlocks.TEMPORARY_BARRIER, 1, 1, 1);
        h.succeed();
    }

    @GameTest
    public void staleEntityCannotRemovePermanentBlock(GameTestHelper h) {
        var entity = place(h);
        entity.initializeAt(1000L);
        var pos = h.absolutePos(new BlockPos(1, 1, 1));
        h.setBlock(1, 1, 1, ModAnomalyBlocks.PERMANENT_BARRIER);
        h.assertTrue(h.getLevel().getBlockEntity(pos) == null, "permanent has no expiry entity");
        TemporaryBarrierBlockEntity.serverTickAt(h.getLevel(), pos,
                ModAnomalyBlocks.TEMPORARY_BARRIER.defaultBlockState(), entity, 1801000L);
        h.assertBlockPresent(ModAnomalyBlocks.PERMANENT_BARRIER, 1, 1, 1);
        h.succeed();
    }
}
