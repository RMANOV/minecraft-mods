package com.erik.medievalconquest.event;

import com.erik.medievalconquest.registry.ModBlocks;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Dynamic lighting — places invisible light blocks at the player's position
 * when holding a torch or lantern. Removes them when the item is unequipped.
 *
 * Erik asked for this "for the third time" — finally delivered!
 */
public class DynamicLightingHandler {

	private static final Map<UUID, BlockPos> activeLights = new HashMap<>();

	private static final Set<Item> LIGHT_ITEMS = Set.of(
			Items.TORCH, Items.SOUL_TORCH, Items.LANTERN, Items.SOUL_LANTERN
	);

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(DynamicLightingHandler::onServerTick);

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ServerPlayer player = handler.getPlayer();
			cleanup(player.level(), player.getUUID());
		});
	}

	private static void onServerTick(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			ItemStack mainHand = player.getMainHandItem();
			ItemStack offHand = player.getOffhandItem();

			boolean holdingLight = LIGHT_ITEMS.contains(mainHand.getItem())
					|| LIGHT_ITEMS.contains(offHand.getItem());

			BlockPos playerPos = player.blockPosition();
			UUID uuid = player.getUUID();
			ServerLevel level = player.level();

			if (holdingLight) {
				BlockPos prev = activeLights.get(uuid);

				// Only update if player moved to a new block
				if (prev == null || !prev.equals(playerPos)) {
					// Remove old light
					if (prev != null) {
						removeLight(level, prev);
					}
					// Place new light
					placeLight(level, playerPos);
					activeLights.put(uuid, playerPos);
				}
			} else {
				// Not holding light — cleanup
				cleanup(level, uuid);
			}
		}
	}

	private static void placeLight(ServerLevel level, BlockPos pos) {
		BlockState current = level.getBlockState(pos);
		// Only place in air or existing light source — never overwrite real blocks
		if (current.isAir() || current.is(ModBlocks.LIGHT_SOURCE)) {
			level.setBlock(pos, ModBlocks.LIGHT_SOURCE.defaultBlockState(),
					Block.UPDATE_ALL_IMMEDIATE);
		}
	}

	private static void removeLight(ServerLevel level, BlockPos pos) {
		if (level.getBlockState(pos).is(ModBlocks.LIGHT_SOURCE)) {
			level.setBlock(pos, Blocks.AIR.defaultBlockState(),
					Block.UPDATE_ALL_IMMEDIATE);
		}
	}

	private static void cleanup(ServerLevel level, UUID uuid) {
		BlockPos prev = activeLights.remove(uuid);
		if (prev != null) {
			removeLight(level, prev);
		}
	}
}
