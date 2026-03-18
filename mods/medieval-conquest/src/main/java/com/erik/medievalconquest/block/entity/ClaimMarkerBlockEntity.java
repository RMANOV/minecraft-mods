package com.erik.medievalconquest.block.entity;

import com.erik.medievalconquest.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

/**
 * Stores castle ownership and upgrade level.
 */
public class ClaimMarkerBlockEntity extends BlockEntity {
	private UUID ownerUuid = null;
	private String ownerName = "";
	private int upgradeLevel = 0;

	public ClaimMarkerBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlocks.CLAIM_MARKER_ENTITY, pos, state);
	}

	public boolean hasOwner() {
		return ownerUuid != null;
	}

	public boolean isOwner(Player player) {
		return ownerUuid != null && ownerUuid.equals(player.getUUID());
	}

	public void setOwner(UUID uuid, String name) {
		this.ownerUuid = uuid;
		this.ownerName = name;
		setChanged();
	}

	public String getOwnerName() {
		return ownerName;
	}

	public int getUpgradeLevel() {
		return upgradeLevel;
	}

	public void upgrade() {
		this.upgradeLevel++;
		setChanged();
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (ownerUuid != null) {
			output.putString("Owner", ownerUuid.toString());
			output.putString("OwnerName", ownerName);
		}
		output.putInt("UpgradeLevel", upgradeLevel);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		String ownerStr = input.getStringOr("Owner", "");
		if (!ownerStr.isEmpty()) {
			ownerUuid = UUID.fromString(ownerStr);
			ownerName = input.getStringOr("OwnerName", "");
		}
		upgradeLevel = input.getIntOr("UpgradeLevel", 0);
	}
}
