package com.erik.medievalconquest;

import com.erik.medievalconquest.registry.ModAnomalyItems;
import com.erik.medievalconquest.registry.ModItems;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/** Native use is synchronous while difficulty changes; no delayed shared-state assertions. */
public final class AnomalyItemGameTest {
    private static final class Fixture implements AutoCloseable {
        final GameTestHelper h;
        final ServerPlayer player;
        final Difficulty oldDifficulty;
        final boolean oldLock;
        final Difficulty requested;
        final AABB region;
        final Set<UUID> before;

        Fixture(GameTestHelper h, int index, Difficulty difficulty, boolean blocked) {
            this.h=h;
            var server=h.getLevel().getServer();
            oldDifficulty=server.getWorldData().getDifficulty();
            oldLock=server.getWorldData().isDifficultyLocked();
            requested=difficulty;
            var center=new BlockPos(-200000+index*4096,64,-200000);
            // Test setup may load its own disposable chunks, unlike the item adapter.
            for (int x=-6;x<=6;x++) for(int z=-6;z<=6;z++) {
                h.getLevel().setBlock(center.offset(x,0,z),Blocks.STONE.defaultBlockState(),2);
                for(int y=1;y<=4;y++) h.getLevel().setBlock(center.offset(x,y,z),
                        blocked ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(),2);
            }
            player=h.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.SURVIVAL);
            player.getInventory().clearContent();
            player.setPos(center.getX()+0.5,65,center.getZ()+0.5);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModAnomalyItems.BAD_ORB));
            region=new AABB(center).inflate(6);
            before=new HashSet<>();
            for(var entity:entities()) before.add(entity.getUUID());
            // Global difficulty changes only after entering try-with-resources, never in construction.
        }

        void activateDifficulty() { h.getLevel().getServer().setDifficulty(requested,true); }

        List<Entity> entities() {
            return h.getLevel().getEntitiesOfClass(Entity.class,region,entity->true);
        }

        List<Entity> created() {
            return entities().stream().filter(entity->!before.contains(entity.getUUID())).toList();
        }

        void use() {
            h.assertTrue(h.getLevel().getDifficulty()==requested,
                    "actual native difficulty matches the requested fixture difficulty");
            var stack=player.getItemInHand(InteractionHand.MAIN_HAND);
            var expected=stack.copy();
            player.gameMode.useItem(player,h.getLevel(),stack,InteractionHand.MAIN_HAND);
            h.assertTrue(ItemStack.isSameItemSameComponents(player.getItemInHand(InteractionHand.MAIN_HAND),expected)
                    && player.getItemInHand(InteractionHand.MAIN_HAND).getCount()==expected.getCount(),
                    "orb use preserves actual held stack/components");
        }

        @Override
        public void close() {
            try { for(var mob:created()) mob.discard(); }
            finally {
                var server=h.getLevel().getServer();
                server.setDifficulty(oldDifficulty,true);
                server.getWorldData().setDifficultyLocked(oldLock);
                player.discard();
            }
        }
    }

    private static void targets(GameTestHelper h, Fixture fixture) {
        for(var entity:fixture.created()) h.assertTrue(entity instanceof Mob mob && mob.getTarget()==fixture.player,
                "each newly admitted native mob targets the actual activator");
    }

    @GameTest
    public void easyUseSpawnsOneActualZombie(GameTestHelper h) {
        try(var f=new Fixture(h,1,Difficulty.EASY,false)) {
            f.activateDifficulty();
            h.assertTrue(h.getLevel().getDifficulty()==Difficulty.EASY,"Easy native setup");
            f.use();
            h.assertTrue(f.created().size()==1 && f.created().getFirst().getType()==EntityType.ZOMBIE,
                    "Easy native activation creates exactly one actual zombie");
            targets(h,f);
            h.assertTrue(f.player.getCooldowns().isOnCooldown(f.player.getItemInHand(InteractionHand.MAIN_HAND)),
                    "successful native use enters item cooldown");
        }
        h.succeed();
    }

    @GameTest
    public void normalUseSpawnsEightActualZombies(GameTestHelper h) {
        try(var f=new Fixture(h,2,Difficulty.NORMAL,false)) {
            f.activateDifficulty();
            h.assertTrue(h.getLevel().getDifficulty()==Difficulty.NORMAL,"Normal native setup");
            f.use();
            h.assertTrue(f.created().size()==8 && f.created().stream().allMatch(mob->mob.getType()==EntityType.ZOMBIE),
                    "Normal native activation creates exactly eight actual zombies");
            targets(h,f);
        }
        h.succeed();
    }

    @GameTest
    public void hardUseSpawnsTwoSkeletonsAndTwoAngryEndermen(GameTestHelper h) {
        try(var f=new Fixture(h,3,Difficulty.HARD,false)) {
            f.activateDifficulty();
            h.assertTrue(h.getLevel().getDifficulty()==Difficulty.HARD,"Hard native setup");
            f.use();
            var mobs=f.created();
            h.assertTrue(mobs.size()==4 && mobs.stream().filter(mob->mob.getType()==EntityType.SKELETON).count()==2
                    && mobs.stream().filter(mob->mob.getType()==EntityType.ENDERMAN).count()==2,
                    "Hard native activation creates exactly two skeletons and two Endermen");
            targets(h,f);
            for(var mob:mobs) if(mob instanceof EnderMan enderman) h.assertTrue(
                    enderman.getPersistentAngerTarget()!=null && enderman.getPersistentAngerTarget().matches(f.player)
                    && enderman.isAngry() && enderman.getPersistentAngerEndTime()>h.getLevel().getGameTime(),
                    "actual Enderman persistent anger belongs to activator");
        }
        h.succeed();
    }

    @GameTest
    public void peacefulUseSpawnsNothing(GameTestHelper h) {
        try(var f=new Fixture(h,4,Difficulty.PEACEFUL,false)) {
            f.activateDifficulty();
            h.assertTrue(h.getLevel().getDifficulty()==Difficulty.PEACEFUL,"Peaceful native setup");
            f.use();
            h.assertTrue(f.created().isEmpty(),"Peaceful actual item use admits no mobs");
            h.assertTrue(!f.player.getCooldowns().isOnCooldown(f.player.getItemInHand(InteractionHand.MAIN_HAND)),
                    "Peaceful no-spawn use does not enter cooldown");
        }
        h.succeed();
    }

    @GameTest
    public void blockedLoadedSpaceCreatesNoPartialBatch(GameTestHelper h) {
        try(var f=new Fixture(h,5,Difficulty.NORMAL,true)) {
            f.activateDifficulty();
            var before=f.h.getLevel().getBlockState(f.player.blockPosition().offset(2,0,0));
            f.use();
            h.assertTrue(f.created().isEmpty(),"fully blocked loaded native space admits no partial batch");
            h.assertTrue(f.h.getLevel().getBlockState(f.player.blockPosition().offset(2,0,0)).equals(before),
                    "blocked item activation does not carve terrain");
            h.assertTrue(!f.player.getCooldowns().isOnCooldown(f.player.getItemInHand(InteractionHand.MAIN_HAND)),
                    "blocked failed use does not enter cooldown");
        }
        h.succeed();
    }

    @GameTest
    public void nativeCooldownPreventsSecondBatch(GameTestHelper h) {
        try(var f=new Fixture(h,6,Difficulty.NORMAL,false)) {
            f.activateDifficulty();
            f.use();
            var first=f.created().stream().map(Entity::getUUID).collect(java.util.stream.Collectors.toSet());
            h.assertTrue(first.size()==8,"first native cooldown activation creates its exact eight-mob batch");
            h.assertTrue(f.player.getCooldowns().isOnCooldown(f.player.getItemInHand(InteractionHand.MAIN_HAND)),
                    "real native cooldown is active before repeated use");
            f.use();
            h.assertTrue(f.created().stream().map(Entity::getUUID).collect(java.util.stream.Collectors.toSet()).equals(first),
                    "synchronous second real use cannot create a second batch");
            for(int tick=0;tick<19;tick++) f.player.getCooldowns().tick();
            h.assertTrue(f.player.getCooldowns().isOnCooldown(f.player.getItemInHand(InteractionHand.MAIN_HAND)),
                    "native cooldown remains active through its nineteenth tick");
            f.player.getCooldowns().tick();
            h.assertTrue(!f.player.getCooldowns().isOnCooldown(f.player.getItemInHand(InteractionHand.MAIN_HAND)),
                    "native cooldown ends at its exact twentieth tick");
        }
        h.succeed();
    }

    @GameTest
    public void alienFlowerUseRestoresFullHungerWithoutConsumption(GameTestHelper h) {
        var player=h.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        player.getFoodData().setFoodLevel(3);
        var flower=new ItemStack(ModAnomalyItems.ALIEN_FLOWER,2);
        var expected=flower.copy();
        player.setItemInHand(InteractionHand.MAIN_HAND,flower);
        player.gameMode.useItem(player,h.getLevel(),flower,InteractionHand.MAIN_HAND);
        h.assertTrue(player.getFoodData().getFoodLevel()==20,"actual alien flower sniff restores full hunger");
        h.assertTrue(ItemStack.isSameItemSameComponents(player.getItemInHand(InteractionHand.MAIN_HAND),expected)
                && player.getItemInHand(InteractionHand.MAIN_HAND).getCount()==2,"smelling does not consume or transform flower");
        h.assertTrue(player.getActiveEffects().isEmpty(),"full-hunger branch adds no potion effects");
        h.succeed();
    }

    @GameTest
    public void fullHungerAndCandiedFlowerContractsPreserved(GameTestHelper h) {
        var player=h.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.getFoodData().setFoodLevel(20);
        var flower=new ItemStack(ModAnomalyItems.ALIEN_FLOWER,2);
        var expected=flower.copy();
        player.setItemInHand(InteractionHand.MAIN_HAND,flower);
        player.gameMode.useItem(player,h.getLevel(),flower,InteractionHand.MAIN_HAND);
        h.assertTrue(player.getFoodData().getFoodLevel()==20 && flower.getCount()==2
                && ItemStack.isSameItemSameComponents(flower,expected),"full hunger sniff is an idempotent non-consuming action");
        h.assertTrue(player.getActiveEffects().isEmpty(),"sniff adds no effect before separate candied control");
        ModItems.CANDIED_FLOWER.finishUsingItem(new ItemStack(ModItems.CANDIED_FLOWER),h.getLevel(),player);
        var effect=player.getEffect(MobEffects.REGENERATION);
        h.assertTrue(effect!=null && effect.getAmplifier()==1 && effect.getDuration()==300,
                "existing actual candied flower retains Regeneration II for fifteen seconds");
        h.succeed();
    }
}
