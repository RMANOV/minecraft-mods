package com.erik.medievalconquest.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** Newly registered compileable RED adapter; deliberately inert sniff before causal RED. */
public final class AlienFlowerItem extends Item {
    public AlienFlowerItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }
}
