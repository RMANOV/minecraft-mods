package com.erik.medievalconquest.item;

import com.erik.medievalconquest.policy.OrbSpawnPolicy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * R07 bad orb: right-click calls the hostile batch that {@link OrbSpawnPolicy} picks for the world
 * difficulty, all aimed at the user. The whole batch appears or nothing does: spots must be in live
 * chunks (the orb never loads or generates chunks), on solid ground, free of blocks and liquid. A failed
 * or Peaceful use starts no cooldown and the orb is never used up. Mobs get their normal vanilla spawn
 * setup (skeleton bows, zombie gear) but zombies always come as single grown-ups: no babies, no riders.
 */
public final class BadOrbItem extends Item {
    /** One second after a successful batch: a double click cannot call two batches at once. */
    public static final int COOLDOWN_TICKS = 20;
    static final String PEACEFUL_KEY = "item.medievalconquest.bad_orb.peaceful";
    static final String NO_ROOM_KEY = "item.medievalconquest.bad_orb.no_room";

    public BadOrbItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        Map<String, Integer> plan = OrbSpawnPolicy.plan(switch (level.getDifficulty()) {
            case PEACEFUL -> OrbSpawnPolicy.DifficultyKind.PEACEFUL;
            case EASY -> OrbSpawnPolicy.DifficultyKind.EASY;
            case NORMAL -> OrbSpawnPolicy.DifficultyKind.NORMAL;
            case HARD -> OrbSpawnPolicy.DifficultyKind.HARD;
        });
        if (plan.isEmpty()) {
            if (!level.isClientSide()) player.displayClientMessage(Component.translatable(PEACEFUL_KEY), true);
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        List<Mob> batch = prepare(server, player, plan);
        if (batch.isEmpty()) {
            player.displayClientMessage(Component.translatable(NO_ROOM_KEY), true);
            return InteractionResult.FAIL;
        }
        List<Mob> admitted = new ArrayList<>();
        for (Mob mob : batch) {
            mob.finalizeSpawn(server, server.getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.TRIGGERED,
                    mob instanceof Zombie ? new Zombie.ZombieGroupData(false, false) : null);
            mob.setTarget(player);
            if (mob instanceof NeutralMob angry) {
                angry.setPersistentAngerTarget(EntityReference.of(player));
                angry.startPersistentAngerTimer();
            }
            if (!server.addFreshEntity(mob)) {
                // Never leave half a batch: take back only the mobs this use added.
                for (Mob own : admitted) own.discard();
                player.displayClientMessage(Component.translatable(NO_ROOM_KEY), true);
                return InteractionResult.FAIL;
            }
            admitted.add(mob);
        }
        for (Mob mob : admitted) server.sendParticles(ParticleTypes.LARGE_SMOKE, mob.getX(), mob.getY() + 0.8,
                mob.getZ(), 12, 0.3, 0.6, 0.3, 0.01);
        server.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EVOKER_PREPARE_SUMMON,
                SoundSource.PLAYERS, 1.0f, 1.0f);
        player.getCooldowns().addCooldown(player.getItemInHand(hand), COOLDOWN_TICKS);
        return InteractionResult.SUCCESS;
    }

    /** Creates and places every planned mob without adding any; empty when even one has no safe spot. */
    private static List<Mob> prepare(ServerLevel level, Player player, Map<String, Integer> plan) {
        List<OrbSpawnPolicy.Offset> spots = OrbSpawnPolicy.candidateOffsets(level.getRandom().nextLong());
        BlockPos origin = player.blockPosition();
        Set<BlockPos> used = new HashSet<>();
        List<Mob> batch = new ArrayList<>();
        for (var entry : new TreeMap<>(plan).entrySet()) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.parse(entry.getKey())).orElse(null);
            for (int i = 0; i < entry.getValue(); i++) {
                if (type == null || !(type.create(level, EntitySpawnReason.TRIGGERED) instanceof Mob mob)) return List.of();
                BlockPos spot = place(level, mob, origin, spots, used);
                if (spot == null) return List.of();
                used.add(spot);
                batch.add(mob);
            }
        }
        return batch;
    }

    /** Moves {@code mob} to the first free safe spot and returns it, or null when none is left. */
    private static BlockPos place(ServerLevel level, Mob mob, BlockPos origin, List<OrbSpawnPolicy.Offset> spots,
                                  Set<BlockPos> used) {
        for (var offset : spots) {
            BlockPos pos = origin.offset(offset.dx(), offset.dy(), offset.dz());
            // Only live (entity-ticking) chunks: never loads chunks, never hides a frozen mob.
            if (used.contains(pos) || !level.isPositionEntityTicking(pos)) continue;
            BlockPos below = pos.below();
            BlockState ground = level.getBlockState(below);
            if (!ground.isFaceSturdy(level, below, Direction.UP) || mob.getType().isBlockDangerous(ground)
                    || mob.getType().isBlockDangerous(level.getBlockState(pos))) continue;
            mob.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.getRandom().nextFloat() * 360.0f, 0.0f);
            if (level.noCollision(mob) && !level.containsAnyLiquid(mob.getBoundingBox())) return pos;
        }
        return null;
    }
}
