package com.erik.medievalconquest.block;

import com.erik.medievalconquest.block.entity.ClaimMarkerBlockEntity;
import com.erik.medievalconquest.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Castle claim marker — place this in a castle to claim ownership.
 * Right-click to claim. Once claimed, the castle is yours.
 */
public class ClaimMarkerBlock extends BaseEntityBlock {
	public static final MapCodec<ClaimMarkerBlock> CODEC = simpleCodec(ClaimMarkerBlock::new);

	public ClaimMarkerBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ClaimMarkerBlockEntity(pos, state);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
			Player player, BlockHitResult hitResult) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof ClaimMarkerBlockEntity marker) {
			if (!marker.hasOwner()) {
				// Claim the castle!
				marker.setOwner(player.getUUID(), player.getName().getString());
				player.displayClientMessage(
						Component.literal("Castle claimed by " + player.getName().getString() + "!"),
						true);
				level.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
						SoundSource.BLOCKS, 1.0f, 1.0f);
				// Spawn particles
				for (int i = 0; i < 20; i++) {
					level.addParticle(ParticleTypes.TOTEM_OF_UNDYING,
							pos.getX() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 2,
							pos.getY() + 1.0 + level.getRandom().nextDouble(),
							pos.getZ() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 2,
							0, 0.1, 0);
				}
			} else if (marker.isOwner(player)) {
				// Show info
				player.displayClientMessage(
						Component.literal("Your castle! Level: " + marker.getUpgradeLevel()),
						true);
			} else {
				player.displayClientMessage(
						Component.literal("This castle belongs to " + marker.getOwnerName() + "!"),
						true);
			}
		}

		return InteractionResult.SUCCESS;
	}
}
