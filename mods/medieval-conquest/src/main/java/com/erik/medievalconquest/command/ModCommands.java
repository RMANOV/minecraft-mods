package com.erik.medievalconquest.command;

import com.erik.medievalconquest.world.CastleGenerator;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Chat commands for the mod:
 *   /castle — generates a castle at the player's position
 */
public class ModCommands {

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("castle")
					.executes(context -> {
						ServerPlayer player = context.getSource().getPlayerOrException();
						ServerLevel level = context.getSource().getLevel();
						BlockPos pos = player.blockPosition();

						// Build the castle 5 blocks ahead of the player
						player.displayClientMessage(
								Component.literal("Building your castle..."), false);

						CastleGenerator.generate(level, pos.above(1));

						player.displayClientMessage(
								Component.literal("Castle built! Right-click the golden marker to claim it!"),
								false);
						return 1;
					})
			);
		});
	}
}
