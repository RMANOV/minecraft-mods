package com.erik.medievalconquest;

import com.erik.medievalconquest.registry.ModAnomalyBlocks;
import com.erik.medievalconquest.registry.ModAnomalyItems;
import com.erik.medievalconquest.registry.ModItems;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Native use is synchronous while difficulty changes; no delayed shared-state assertions. Each orb case
 * builds a disposable far area and force-loads its chunks, waits until they are live (entity-ticking,
 * like the chunks around a real player), then runs its whole body, including difficulty, in one tick.
 */
public final class AnomalyItemGameTest {
    /**
     * Live chunks need generated neighbours; a CPU-capped run can take several seconds for that (one
     * capped run measured 173 ticks for the strip's last fixture), so allow about three times that.
     */
    private static final int SETTLE_TICKS = 600;
    /** Settle budget plus room for the synchronous body; keep above SETTLE_TICKS. */
    private static final int ORB_MAX_TICKS = 700;

    /** One shared strip far from the tests: own chunks per case (32 blocks apart), little extra terrain. */
    private static BlockPos center(int index) { return new BlockPos(-200000+index*32,64,-200000); }

    /** The 13x13 floor sits on a chunk corner, so it spans up to four chunks. */
    private static List<ChunkPos> chunks(BlockPos center) {
        return Stream.of(center.offset(-6,0,-6),center.offset(6,0,-6),center.offset(-6,0,6),center.offset(6,0,6))
                .map(ChunkPos::new).distinct().toList();
    }

    private static void release(GameTestHelper h, BlockPos center) {
        for(var chunk:chunks(center)) h.getLevel().setChunkForced(chunk.x,chunk.z,false);
    }

    /** Builds and force-loads the area, then runs {@code body} once in the first tick all of it is live. */
    private static void orbCase(GameTestHelper h, int index, Difficulty difficulty, boolean blocked, Consumer<Fixture> body) {
        var center=center(index);
        // Test setup may load its own disposable chunks, unlike the item adapter.
        for (int x=-6;x<=6;x++) for(int z=-6;z<=6;z++) {
            h.getLevel().setBlock(center.offset(x,0,z),Blocks.STONE.defaultBlockState(),2);
            for(int y=1;y<=4;y++) h.getLevel().setBlock(center.offset(x,y,z),
                    blocked ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(),2);
        }
        for(var chunk:chunks(center)) h.getLevel().setChunkForced(chunk.x,chunk.z,true);
        whenLive(h,center,0,()->{
            try(var f=new Fixture(h,center,difficulty)) { body.accept(f); }
            h.succeed();
        });
    }

    private static void whenLive(GameTestHelper h, BlockPos center, int waited, Runnable body) {
        if(chunks(center).stream().allMatch(chunk->h.getLevel().isPositionEntityTicking(
                chunk.getMiddleBlockPosition(center.getY())))) {
            MedievalConquestMod.LOGGER.info("orb fixture at {} live after {} ticks", center, waited);
            body.run();
        }
        else if(waited>=SETTLE_TICKS) {
            release(h,center);
            h.fail("HARNESS: forced fixture chunks at "+center+" never became entity-ticking");
        }
        else h.runAfterDelay(1,()->whenLive(h,center,waited+1,body));
    }

    private static final class Fixture implements AutoCloseable {
        final GameTestHelper h;
        final BlockPos center;
        final ServerPlayer player;
        final Difficulty oldDifficulty;
        final boolean oldLock;
        final Difficulty requested;
        final AABB region;
        final Set<UUID> before;

        Fixture(GameTestHelper h, BlockPos center, Difficulty difficulty) {
            this.h=h;
            this.center=center;
            var server=h.getLevel().getServer();
            oldDifficulty=server.getWorldData().getDifficulty();
            oldLock=server.getWorldData().isDifficultyLocked();
            requested=difficulty;
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
                release(h,center);
            }
        }
    }

    private static void targets(GameTestHelper h, Fixture fixture) {
        for(var entity:fixture.created()) h.assertTrue(entity instanceof Mob mob && mob.getTarget()==fixture.player,
                "each newly admitted native mob targets the actual activator");
    }

    @GameTest(maxTicks=ORB_MAX_TICKS)
    public void easyUseSpawnsOneActualZombie(GameTestHelper h) {
        orbCase(h,1,Difficulty.EASY,false,f->{
            f.activateDifficulty();
            h.assertTrue(h.getLevel().getDifficulty()==Difficulty.EASY,"Easy native setup");
            f.use();
            h.assertTrue(f.created().size()==1 && f.created().getFirst().getType()==EntityType.ZOMBIE,
                    "Easy native activation creates exactly one actual zombie");
            targets(h,f);
            h.assertTrue(f.player.getCooldowns().isOnCooldown(f.player.getItemInHand(InteractionHand.MAIN_HAND)),
                    "successful native use enters item cooldown");
        });
    }

    @GameTest(maxTicks=ORB_MAX_TICKS)
    public void normalUseSpawnsEightActualZombies(GameTestHelper h) {
        orbCase(h,2,Difficulty.NORMAL,false,f->{
            f.activateDifficulty();
            h.assertTrue(h.getLevel().getDifficulty()==Difficulty.NORMAL,"Normal native setup");
            f.use();
            h.assertTrue(f.created().size()==8 && f.created().stream().allMatch(mob->mob.getType()==EntityType.ZOMBIE),
                    "Normal native activation creates exactly eight actual zombies");
            targets(h,f);
            for(var mob:f.created()) h.assertTrue(mob instanceof Zombie zombie && !zombie.isBaby()
                    && zombie.getPassengers().isEmpty() && zombie.getVehicle()==null,
                    "orb zombies are single grown-ups: no baby, no jockey, no mount: "+mob);
        });
    }

    @GameTest(maxTicks=ORB_MAX_TICKS)
    public void hardUseSpawnsTwoSkeletonsAndTwoAngryEndermen(GameTestHelper h) {
        orbCase(h,3,Difficulty.HARD,false,f->{
            f.activateDifficulty();
            h.assertTrue(h.getLevel().getDifficulty()==Difficulty.HARD,"Hard native setup");
            f.use();
            var mobs=f.created();
            h.assertTrue(mobs.size()==4 && mobs.stream().filter(mob->mob.getType()==EntityType.SKELETON).count()==2
                    && mobs.stream().filter(mob->mob.getType()==EntityType.ENDERMAN).count()==2,
                    "Hard native activation creates exactly two skeletons and two Endermen");
            targets(h,f);
            for(var mob:mobs) if(mob.getType()==EntityType.SKELETON) h.assertTrue(
                    ((Mob)mob).getMainHandItem().is(Items.BOW),"orb skeletons get their normal vanilla bow: "+mob);
            for(var mob:mobs) if(mob instanceof EnderMan enderman) h.assertTrue(
                    enderman.getPersistentAngerTarget()!=null && enderman.getPersistentAngerTarget().matches(f.player)
                    && enderman.isAngry() && enderman.getPersistentAngerEndTime()>h.getLevel().getGameTime(),
                    "actual Enderman persistent anger belongs to activator");
        });
    }

    @GameTest(maxTicks=ORB_MAX_TICKS)
    public void peacefulUseSpawnsNothing(GameTestHelper h) {
        orbCase(h,4,Difficulty.PEACEFUL,false,f->{
            f.activateDifficulty();
            h.assertTrue(h.getLevel().getDifficulty()==Difficulty.PEACEFUL,"Peaceful native setup");
            f.use();
            h.assertTrue(f.created().isEmpty(),"Peaceful actual item use admits no mobs");
            h.assertTrue(!f.player.getCooldowns().isOnCooldown(f.player.getItemInHand(InteractionHand.MAIN_HAND)),
                    "Peaceful no-spawn use does not enter cooldown");
        });
    }

    @GameTest(maxTicks=ORB_MAX_TICKS)
    public void blockedLoadedSpaceCreatesNoPartialBatch(GameTestHelper h) {
        orbCase(h,5,Difficulty.NORMAL,true,f->{
            f.activateDifficulty();
            var before=f.h.getLevel().getBlockState(f.player.blockPosition().offset(2,0,0));
            f.use();
            h.assertTrue(f.created().isEmpty(),"fully blocked loaded native space admits no partial batch");
            h.assertTrue(f.h.getLevel().getBlockState(f.player.blockPosition().offset(2,0,0)).equals(before),
                    "blocked item activation does not carve terrain");
            h.assertTrue(!f.player.getCooldowns().isOnCooldown(f.player.getItemInHand(InteractionHand.MAIN_HAND)),
                    "blocked failed use does not enter cooldown");
        });
    }

    @GameTest(maxTicks=ORB_MAX_TICKS)
    public void nativeCooldownPreventsSecondBatch(GameTestHelper h) {
        orbCase(h,6,Difficulty.NORMAL,false,f->{
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
            f.use();
            h.assertTrue(f.created().size()==16,"after the cooldown the orb calls one more full batch");
        });
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

    @GameTest(maxTicks=ORB_MAX_TICKS)
    public void partlyOpenSpaceAdmitsNoPartialBatch(GameTestHelper h) {
        orbCase(h,8,Difficulty.NORMAL,true,f->{
            var feet=f.player.blockPosition();
            var pockets=List.of(feet.offset(2,0,0),feet.offset(-2,0,0),feet.offset(0,0,2));
            for(var pocket:pockets) {
                h.getLevel().setBlock(pocket,Blocks.AIR.defaultBlockState(),2);
                h.getLevel().setBlock(pocket.above(),Blocks.AIR.defaultBlockState(),2);
            }
            f.activateDifficulty();
            f.use();
            h.assertTrue(f.created().isEmpty(),"three free spots admit no part of the eight-zombie Normal batch");
            h.assertTrue(!f.player.getCooldowns().isOnCooldown(f.player.getItemInHand(InteractionHand.MAIN_HAND)),
                    "refused batch starts no cooldown");
            h.getLevel().getServer().setDifficulty(Difficulty.EASY,true);
            var orb=f.player.getItemInHand(InteractionHand.MAIN_HAND);
            f.player.gameMode.useItem(f.player,h.getLevel(),orb,InteractionHand.MAIN_HAND);
            var admitted=f.created();
            h.assertTrue(admitted.size()==1 && pockets.contains(admitted.getFirst().blockPosition()),
                    "the same free spots do admit the one-zombie Easy batch");
        });
    }

    @GameTest(maxTicks=ORB_MAX_TICKS)
    public void unsafePocketsAdmitNothingUntilOneGetsAFloor(GameTestHelper h) {
        orbCase(h,9,Difficulty.EASY,true,f->{
            var level=h.getLevel();
            var feet=f.player.blockPosition();
            var hole=feet.offset(2,0,0);
            var water=feet.offset(-2,0,0);
            var magma=feet.offset(0,0,2);
            var lowCeiling=feet.offset(0,0,-2);
            for(var pocket:List.of(hole,water,magma)) {
                level.setBlock(pocket,Blocks.AIR.defaultBlockState(),2);
                level.setBlock(pocket.above(),Blocks.AIR.defaultBlockState(),2);
            }
            for(int y=1;y<=5;y++) level.setBlock(hole.below(y),Blocks.AIR.defaultBlockState(),2);
            level.setBlock(water,Blocks.WATER.defaultBlockState(),2);
            level.setBlock(magma.below(),Blocks.MAGMA_BLOCK.defaultBlockState(),2);
            level.setBlock(lowCeiling,Blocks.AIR.defaultBlockState(),2);
            f.activateDifficulty();
            f.use();
            h.assertTrue(f.created().isEmpty(),
                    "no mob appears over a deep hole, in water, on magma or under a one-block ceiling");
            h.assertTrue(!f.player.getCooldowns().isOnCooldown(f.player.getItemInHand(InteractionHand.MAIN_HAND)),
                    "a use with no safe spot starts no cooldown");
            level.setBlock(hole.below(),Blocks.STONE.defaultBlockState(),2);
            f.use();
            var admitted=f.created();
            h.assertTrue(admitted.size()==1 && admitted.getFirst().blockPosition().equals(hole),
                    "once the hole has a floor the Easy zombie stands exactly there, got "
                            +admitted.stream().map(Entity::blockPosition).toList());
        });
    }

    @GameTest(maxTicks=ORB_MAX_TICKS)
    public void spawnedMobsStandFreeOnSolidGroundNearTheActivator(GameTestHelper h) {
        orbCase(h,7,Difficulty.HARD,false,f->{
            f.activateDifficulty();
            f.use();
            var mobs=f.created();
            var level=h.getLevel();
            var origin=f.player.blockPosition();
            h.assertTrue(mobs.size()==4,"open loaded space admits the whole Hard batch");
            for(var mob:mobs) {
                var feet=mob.blockPosition();
                int ring=Math.max(Math.abs(feet.getX()-origin.getX()),Math.abs(feet.getZ()-origin.getZ()));
                h.assertTrue(ring>=2 && ring<=6 && Math.abs(feet.getY()-origin.getY())<=2,
                        "mob appears two to six blocks from the activator, got "+feet+" for "+origin);
                h.assertTrue(level.noCollision(mob) && !mob.isInWall() && !level.containsAnyLiquid(mob.getBoundingBox()),
                        "mob is not inside blocks or liquid: "+mob);
                h.assertTrue(level.getBlockState(feet.below()).isFaceSturdy(level,feet.below(),Direction.UP),
                        "mob stands on solid ground: "+mob);
            }
            h.assertTrue(mobs.stream().map(Entity::blockPosition).distinct().count()==mobs.size(),
                    "every mob gets its own spot");
        });
    }

    @GameTest
    public void alienFlowerSmellFillsSaturationAndWaitsThirtySeconds(GameTestHelper h) {
        var player=h.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        var flower=new ItemStack(ModAnomalyItems.ALIEN_FLOWER,3);
        player.setItemInHand(InteractionHand.MAIN_HAND,flower);
        var food=player.getFoodData();
        food.setFoodLevel(20);
        food.setSaturation(20f);
        player.gameMode.useItem(player,h.getLevel(),flower,InteractionHand.MAIN_HAND);
        h.assertTrue(!player.getCooldowns().isOnCooldown(flower),"a fully fed smell changes nothing and starts no cooldown");
        food.setFoodLevel(4);
        food.setSaturation(0f);
        var result=player.gameMode.useItem(player,h.getLevel(),flower,InteractionHand.MAIN_HAND);
        h.assertTrue(result.consumesAction() && food.getFoodLevel()==20 && food.getSaturationLevel()==20f,
                "hungry smell fills hunger and saturation to the maximum");
        h.assertTrue(player.getCooldowns().isOnCooldown(flower),"smell starts the flower cooldown");
        food.setFoodLevel(4);
        food.setSaturation(0f);
        player.gameMode.useItem(player,h.getLevel(),flower,InteractionHand.MAIN_HAND);
        h.assertTrue(food.getFoodLevel()==4 && food.getSaturationLevel()==0f,"a second smell during the cooldown does nothing");
        for(int tick=0;tick<599;tick++) player.getCooldowns().tick();
        h.assertTrue(player.getCooldowns().isOnCooldown(flower),"cooldown still runs one tick before thirty seconds");
        player.getCooldowns().tick();
        h.assertTrue(!player.getCooldowns().isOnCooldown(flower),"cooldown ends at exactly thirty seconds (600 ticks)");
        player.gameMode.useItem(player,h.getLevel(),flower,InteractionHand.MAIN_HAND);
        h.assertTrue(food.getFoodLevel()==20,"the flower works again after the cooldown");
        h.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).is(ModAnomalyItems.ALIEN_FLOWER)
                && player.getItemInHand(InteractionHand.MAIN_HAND).getCount()==3,"smelling never uses up the flower");
        h.succeed();
    }

    @GameTest
    public void plantedAlienFlowerSmellsSharesCooldownAndDropsItself(GameTestHelper h) {
        h.setBlock(1,0,1,Blocks.STONE);
        h.setBlock(1,1,1,Blocks.AIR);
        var support=h.absolutePos(new BlockPos(1,0,1));
        var target=support.above();
        var level=h.getLevel();
        var player=h.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        player.setPos(target.getX()+2.5,target.getY(),target.getZ()+0.5);
        var flowers=new ItemStack(ModAnomalyItems.ALIEN_FLOWER,2);
        player.setItemInHand(InteractionHand.MAIN_HAND,flowers);
        var planted=player.gameMode.useItemOn(player,level,flowers,InteractionHand.MAIN_HAND,
                new BlockHitResult(new Vec3(support.getX()+0.5,support.getY()+1.0,support.getZ()+0.5),
                        Direction.UP,support,false));
        h.assertTrue(planted.consumesAction(),"right-click on solid ground plants the alien flower");
        h.assertBlockPresent(ModAnomalyBlocks.ALIEN_FLOWER,1,1,1);
        h.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).getCount()==1,"survival planting uses exactly one flower");
        h.assertTrue(ModAnomalyBlocks.ALIEN_FLOWER.asItem()==ModAnomalyItems.ALIEN_FLOWER,
                "the planted flower belongs to the alien flower item");

        var food=player.getFoodData();
        food.setFoodLevel(2);
        food.setSaturation(0f);
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        var flowerHit=new BlockHitResult(Vec3.atCenterOf(target),Direction.WEST,target,false);
        var smelled=player.gameMode.useItemOn(player,level,ItemStack.EMPTY,InteractionHand.MAIN_HAND,flowerHit);
        h.assertTrue(smelled.consumesAction() && food.getFoodLevel()==20 && food.getSaturationLevel()==20f,
                "right-click on the planted flower fills hunger and saturation");
        h.assertBlockPresent(ModAnomalyBlocks.ALIEN_FLOWER,1,1,1);
        var held=new ItemStack(ModAnomalyItems.ALIEN_FLOWER);
        h.assertTrue(player.getCooldowns().isOnCooldown(held),"planted smell starts the shared flower cooldown");

        food.setFoodLevel(2);
        food.setSaturation(0f);
        player.gameMode.useItemOn(player,level,ItemStack.EMPTY,InteractionHand.MAIN_HAND,flowerHit);
        player.setItemInHand(InteractionHand.MAIN_HAND,held);
        player.gameMode.useItem(player,level,held,InteractionHand.MAIN_HAND);
        player.gameMode.useItemOn(player,level,held,InteractionHand.MAIN_HAND,flowerHit);
        h.assertTrue(food.getFoodLevel()==2 && food.getSaturationLevel()==0f,
                "neither the planted nor the held flower can be smelled during the shared cooldown");
        h.assertTrue(held.getCount()==1,"clicking the planted flower during the cooldown plants nothing");

        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        h.assertTrue(player.gameMode.destroyBlock(target),"survival player breaks the planted flower");
        h.assertBlockNotPresent(ModAnomalyBlocks.ALIEN_FLOWER,1,1,1);
        int dropped=level.getEntitiesOfClass(ItemEntity.class,new AABB(target).inflate(1.5),
                item->item.getItem().is(ModAnomalyItems.ALIEN_FLOWER)).stream().mapToInt(item->item.getItem().getCount()).sum();
        h.assertTrue(dropped==1,"breaking the planted flower returns exactly one flower, got "+dropped);
        h.succeed();
    }
}
