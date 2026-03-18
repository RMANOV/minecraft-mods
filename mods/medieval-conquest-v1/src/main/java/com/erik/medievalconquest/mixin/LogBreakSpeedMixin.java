package com.erik.medievalconquest.mixin;

import com.erik.medievalconquest.item.BrazierItem;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes logs impossible to mine by hand.
 * The mining progress bar won't even appear unless holding a Brazier.
 * (Even with a Brazier, logs are harvested via right-click, not mining.)
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class LogBreakSpeedMixin {

	@Inject(method = "getDestroyProgress", at = @At("HEAD"), cancellable = true)
	private void medievalconquest$preventLogMining(Player player, BlockGetter level, BlockPos pos,
			CallbackInfoReturnable<Float> cir) {
		// Cast 'this' to get the BlockState
		BlockState state = (BlockState) (Object) this;
		if (state.is(BlockTags.LOGS) && !player.isCreative()) {
			cir.setReturnValue(0.0f); // Cannot mine — zero progress
		}
	}
}
