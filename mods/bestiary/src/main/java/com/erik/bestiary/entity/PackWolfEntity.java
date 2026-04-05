package com.erik.bestiary.entity;

import com.erik.bestiary.entity.ai.*;
import com.erik.bestiary.entity.ai.behavior.*;
import com.erik.bestiary.morale.MoraleSystem;
import com.erik.bestiary.signal.SignalPropagator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * PackWolfEntity — a hostile wolf that fights in coordinated packs.
 *
 * ★ KEY DESIGN DECISIONS ★
 *
 * 1. EXTENDS Monster (not Wolf) because:
 *    - Wolf is a tameable, neutral mob with complex tame/sit state
 *    - We want ALWAYS-HOSTILE, coordinated hunter behavior
 *    - Monster gives us: dark-spawn conditions, player targeting, proper aggro
 *    - We use the wolf MODEL/TEXTURE for appearance (renderer handles that)
 *
 * 2. PackRole uses SEALED INTERFACE (not enum) because:
 *    - Each role carries different data (Alpha has packMembers list, Scout has range)
 *    - Pattern matching deconstruction works on records, not enum constants
 *    - See PackRole.java for the full explanation
 *
 * 3. MoraleState is IMMUTABLE (record):
 *    - Always replaced via withFear()/withCourage() — never mutated in place
 *    - Makes morale changes explicit and traceable
 *
 * 4. BehaviorTree integration:
 *    - In addition to GoalSelector, we maintain a BehaviorTree for complex decisions
 *    - BehaviorTree handles: flee decision, signal response, role switching
 *    - GoalSelector handles: movement, attacking, targeting
 *
 * ─── Pack Formation on Spawn ──────────────────────────────────────────────────
 * When an Alpha spawns via finalizeSpawn(), it creates 2-4 additional wolves
 * with random roles (Scout, Flanker, Guard) nearby. These wolves register
 * as listeners on the Alpha's SignalPropagator.
 *
 * When a non-Alpha spawns directly (e.g., from spawn egg), it creates its own
 * mini-pack as an alpha (so spawn egg always creates a pack).
 */
public class PackWolfEntity extends Monster {

    // ─── Pack AI State ────────────────────────────────────────────────────────

    /**
     * ★ JAVA 21: PackRole is a sealed interface — set once at spawn ★
     * Defaults to Alpha with empty pack until finalizeSpawn() assigns roles.
     */
    private PackRole role;

    /**
     * ★ JAVA 21: MoraleState is an immutable record ★
     * Replaced (not mutated) when morale changes.
     */
    private MoraleState moraleState;

    /**
     * Observer pattern: this propagator broadcasts signals to packmates.
     * Each wolf has its own propagator; Alpha's propagator is the "pack radio."
     */
    private final SignalPropagator signalPropagator;

    /**
     * ★ JAVA 21: BehaviorTree<PackWolfEntity> for complex decision-making ★
     * Runs alongside the GoalSelector to handle flee/rally decisions.
     */
    private final BehaviorTree<PackWolfEntity> behaviorTree;

    // ─── Constructors ──────────────────────────────────────────────────────────

    public PackWolfEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        // Initialize with default Alpha role (empty pack) until finalizeSpawn
        this.role = new PackRole.Alpha(new ArrayList<>());
        this.moraleState = MoraleState.neutral();
        this.signalPropagator = new SignalPropagator(this.position());

        // Build the behavior tree for this wolf
        this.behaviorTree = buildBehaviorTree();

        // Register to receive signals from our own propagator (self-awareness)
        this.signalPropagator.addListener(this::onSignalReceived);
    }

    // ─── Attribute Registration ────────────────────────────────────────────────

    /**
     * Base attributes for all PackWolfEntity.
     * Role-specific HP bonuses are applied in finalizeSpawn().
     *
     * Follow v3 pattern: static createAttributes() returning Builder.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)          // Base: 20 HP (same as player)
                .add(Attributes.ATTACK_DAMAGE, 4.0)         // Base: 4 damage per bite
                .add(Attributes.MOVEMENT_SPEED, 0.35)       // Faster than player (0.1)
                .add(Attributes.FOLLOW_RANGE, 16.0)         // Default detection range
                .add(Attributes.ARMOR, 2.0)                 // Light natural armor
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1); // Slight knockback resistance
    }

    // ─── Goal Registration ─────────────────────────────────────────────────────

    /**
     * registerGoals() — adds UNIVERSAL goals only.
     *
     * IMPORTANT: registerGoals() is called by the Mob super constructor,
     * BEFORE our constructor body runs, so this.role is still the default value.
     * Role-specific goals are added later in registerRoleGoals(), called from
     * finalizeSpawn() after the role has been set.
     *
     * Universal goals apply to all pack wolf roles regardless of assignment.
     */
    @Override
    protected void registerGoals() {
        // Universal goals (all roles)
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        // Universal target selection (all roles attack back when hit, seek players)
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        // Default melee attack (overridden/supplemented by role-specific goals)
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.2, true));
    }

    /**
     * ★ JAVA 21 FEATURE: Switch on Sealed Interface (Exhaustive) ★
     *
     * registerRoleGoals() adds role-specific goals AFTER the role is set.
     * Called from finalizeSpawn() once we know what role this wolf has.
     *
     * The switch expression is exhaustive — no default needed because the
     * compiler knows the SEALED set: Alpha, Scout, Flanker, Guard are ALL variants.
     * If we add a new role, this switch won't compile until we handle it.
     *
     * This is the power of sealed interfaces: forced completeness at compile time.
     */
    private void registerRoleGoals() {
        switch (this.role) {
            case PackRole.Alpha alpha -> {
                // Alpha commands the pack and attacks directly
                this.goalSelector.addGoal(1, new PackCoordinatorGoal(this));
                this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.3, true));
            }
            case PackRole.Scout scout -> {
                // Scout detects enemies and broadcasts signals; faster movement
                this.goalSelector.addGoal(1, new ScoutGoal(this));
                this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.5, true));
            }
            case PackRole.Flanker flanker -> {
                // Flanker positions itself around the target — no extra melee goal
                // (FlankingGoal handles attack when in position)
                this.goalSelector.addGoal(1, new FlankingGoal(this));
            }
            case PackRole.Guard guard -> {
                // Guard protects the alpha above all else
                this.goalSelector.addGoal(1, new GuardGoal(this));
                this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
            }
        }
    }

    // ─── Spawn Initialization ─────────────────────────────────────────────────

    /**
     * ★ PACK SPAWNING LOGIC ★
     *
     * Called once when the entity first spawns into the world.
     * This is where we:
     * 1. Assign the role (if not already set externally)
     * 2. Apply HP bonus for the role
     * 3. If Alpha: spawn packmates around us
     *
     * v3 pattern: @Nullable SpawnGroupData return.
     */
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        EntitySpawnReason spawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);

        // Register role-specific goals now that role is finalized
        registerRoleGoals();

        // Only Alpha wolves spawn packs (prevent infinite recursion)
        if (this.role instanceof PackRole.Alpha(var members) && members.isEmpty()) {
            // Apply alpha HP bonus
            applyRoleHealthBonus();

            // Spawn 2-4 packmates
            int packSize = 2 + this.random.nextInt(3); // 2, 3, or 4
            for (int i = 0; i < packSize; i++) {
                spawnPackmate(level, difficulty, spawnType, i, packSize);
            }
        } else {
            // Non-alpha role: apply HP bonus only
            applyRoleHealthBonus();
        }

        return data;
    }

    /**
     * Apply the role-based HP bonus using pattern matching (getHealthBonus).
     * Called in finalizeSpawn so the wolf starts with correct max HP.
     */
    private void applyRoleHealthBonus() {
        float bonus = PackRole.getHealthBonus(this.role);
        if (bonus > 0) {
            double currentMax = this.getAttributeValue(Attributes.MAX_HEALTH);
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(currentMax + bonus);
            this.setHealth(this.getMaxHealth()); // Heal to new max
        }
    }

    /**
     * Spawn a packmate with a role appropriate to its index.
     *
     * Role distribution:
     * - index 0: Scout (first packmate is always the scout)
     * - index 1: Guard (protects the alpha)
     * - index 2+: Flanker (with alternating angles: +90°, -90°, +45°...)
     */
    private void spawnPackmate(ServerLevelAccessor level, DifficultyInstance difficulty,
                                EntitySpawnReason spawnType, int index, int totalPack) {
        // Calculate spawn offset (spread around alpha)
        double angle = (2 * Math.PI * index) / totalPack;
        double spawnDist = 2.0 + this.random.nextDouble() * 2.0;
        double spawnX = this.getX() + Math.cos(angle) * spawnDist;
        double spawnY = this.getY();
        double spawnZ = this.getZ() + Math.sin(angle) * spawnDist;

        // Determine role for this packmate
        PackRole packRole = switch (index) {
            case 0 -> new PackRole.Scout(24.0); // Wide detection range
            case 1 -> new PackRole.Guard(this); // Guard protects THIS alpha
            default -> new PackRole.Flanker(Math.PI / 2.0 * (index % 2 == 0 ? 1 : -1)); // Alternating flanks
        };

        // Create the packmate entity using the same entity type as this wolf
        // Cast is safe: this entity's type always produces PackWolfEntity instances
        @SuppressWarnings("unchecked")
        EntityType<? extends Monster> wolfType = (EntityType<? extends Monster>) this.getType();
        PackWolfEntity packmate = new PackWolfEntity(wolfType, this.level());
        // Set role BEFORE finalizeSpawn so registerRoleGoals() uses the correct role
        packmate.role = packRole;
        packmate.snapTo(spawnX, spawnY, spawnZ, this.random.nextFloat() * 360.0f, 0.0f);

        // finalizeSpawn BEFORE addFreshEntity (canonical Minecraft pattern)
        // Safe from recursion: packmate is non-Alpha role (Scout/Guard/Flanker),
        // so finalizeSpawn will NOT attempt to spawn another pack
        packmate.finalizeSpawn(level, difficulty, spawnType, null);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.addFreshEntity(packmate);
        }

        // Add packmate to our pack list
        if (this.role instanceof PackRole.Alpha(var members)) {
            members.add(packmate);
        }

        // Register packmate as a signal listener on our propagator
        this.signalPropagator.addListener(packmate::onSignalReceived);

        // Give packmate a reference to our propagator (so it can also broadcast)
        // In a full implementation, packmates would have their own propagator chain
    }

    // ─── Morale System Integration ────────────────────────────────────────────

    /**
     * Override hurtServer() to update morale when the wolf takes damage.
     * This is how external events (taking hits) feed into the morale system.
     *
     * In 1.21.11, hurt(DamageSource, float) is final in Entity.
     * The overridable damage hook is hurtServer(ServerLevel, DamageSource, float).
     */
    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        boolean wasHurt = super.hurtServer(serverLevel, source, amount);
        if (wasHurt) {
            updateMoraleOnHurt(amount);
        }
        return wasHurt;
    }

    /**
     * Called when this wolf takes damage. Updates the morale state using
     * immutable record operations (withFear, withCause).
     *
     * ★ JAVA 21: Using record wither methods and pattern matching ★
     */
    private void updateMoraleOnHurt(float damage) {
        float healthPercent = this.getHealth() / this.getMaxHealth();
        MoraleCause cause = new MoraleCause.Injury(damage, healthPercent);

        // Fear increases with damage severity relative to max health
        float fearIncrease = damage / this.getMaxHealth() * 2.0f;
        MoraleState newMorale = this.moraleState
                .withFear(this.moraleState.fear() + fearIncrease)
                .withCause(cause);

        this.moraleState = newMorale;

        // Check if we should flee based on new morale
        MoraleSystem.MoraleAction action = MoraleSystem.decide(newMorale);
        if (action == MoraleSystem.MoraleAction.FLEE) {
            startFleeing();
        }
    }

    /**
     * Make the wolf flee from its current target.
     */
    private void startFleeing() {
        LivingEntity target = this.getTarget();
        if (target != null) {
            // Move away from target at high speed
            Vec3 fleeDir = this.position().subtract(target.position()).normalize();
            Vec3 fleeTarget = this.position().add(fleeDir.scale(16.0));
            this.getNavigation().moveTo(fleeTarget.x, fleeTarget.y, fleeTarget.z, 1.5);
            this.setTarget(null); // Drop aggressive target while fleeing
        }
    }

    // ─── Signal System ───────────────────────────────────────────────────────

    /**
     * ★ JAVA CONCEPT: Consumer<Signal> callback — called by SignalPropagator ★
     *
     * This method is registered as a Consumer<Signal> via:
     *   propagator.addListener(this::onSignalReceived)
     *
     * It's a method reference — Java converts it to Consumer<Signal> automatically.
     * This is the Observer pattern in action: no polling, just event-driven.
     *
     * @param signal the received signal (already attenuated by propagation)
     */
    public void onSignalReceived(Signal signal) {
        // Pattern matching switch on SignalType enum
        switch (signal.type()) {
            case DANGER -> {
                // Enemy nearby — increase fear based on urgency
                MoraleCause cause = new MoraleCause.PlayerArmor(signal.urgency() * 10.0f);
                this.moraleState = this.moraleState
                        .withFear(this.moraleState.fear() + signal.urgency() * 0.3f)
                        .withCause(cause);
            }
            case FOOD -> {
                // Prey detected — increase courage
                this.moraleState = this.moraleState
                        .withCourage(this.moraleState.courage() + signal.urgency() * 0.2f);
            }
            case RALLY -> {
                // Alpha calling — reduce fear, boost courage
                this.moraleState = this.moraleState
                        .withFear(this.moraleState.fear() * 0.5f)
                        .withCourage(this.moraleState.courage() + 0.3f)
                        .withCause(new MoraleCause.PackPresence(2));
            }
        }

        // Update signal propagator position
        this.signalPropagator.updateAnchor(this.position());
    }

    // ─── Behavior Tree ───────────────────────────────────────────────────────

    /**
     * ★ JAVA 21 FEATURE: Building a BehaviorTree with Generic Records ★
     *
     * This method constructs the behavior tree using the composable nodes.
     * The tree reads like a specification:
     *
     *   Selector[
     *     Sequence[isPanicking, flee],     ← if panicking, flee
     *     Sequence[isRallied, charge],     ← if rallied, charge
     *     Sequence[hasTarget, attack]      ← otherwise, attack if has target
     *   ]
     *
     * The generic type flows: BehaviorTree<PackWolfEntity> holds
     * Selector<PackWolfEntity> which holds Sequence<PackWolfEntity>...
     * Type safety guaranteed from root to leaf.
     *
     * @return the constructed behavior tree
     */
    private BehaviorTree<PackWolfEntity> buildBehaviorTree() {
        // ─── Flee branch: panicking wolf runs away ────────────────────────────
        var fleeSequence = new Sequence<PackWolfEntity>(List.of(
                // Condition: check if wolf is panicking (high fear, low courage)
                new Condition<>(wolf -> wolf.getMoraleState().isPanicking()),
                // Action: move away from target
                new Action<>(wolf -> {
                    wolf.startFleeing();
                    return true;
                })
        ));

        // ─── Rally branch: rallied wolf charges aggressively ─────────────────
        var rallySequence = new Sequence<PackWolfEntity>(List.of(
                // Condition: wolf is rallied (high pack presence, high courage)
                new Condition<>(wolf -> wolf.getMoraleState().isRallied()),
                // Condition: wolf has a target to charge
                new Condition<>(wolf -> wolf.getTarget() != null && wolf.getTarget().isAlive()),
                // Action: sprint toward target
                new Action<>(wolf -> {
                    LivingEntity target = wolf.getTarget();
                    if (target != null) {
                        wolf.getNavigation().moveTo(target, 1.5); // Sprint speed
                        return true;
                    }
                    return false;
                })
        ));

        // ─── Normal attack: default when no special morale state ─────────────
        var normalAttackSequence = new Sequence<PackWolfEntity>(List.of(
                new Condition<>(wolf -> wolf.getTarget() != null),
                new Action<>(wolf -> {
                    LivingEntity target = wolf.getTarget();
                    if (target != null && wolf.distanceToSqr(target) < 4.0) {
                        // Use the standard Minecraft hurt() API (same as v3 dragon)
                        float damage = (float) wolf.getAttributeValue(
                                net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                        target.hurt(wolf.damageSources().mobAttack(wolf), damage);
                        return true;
                    }
                    return false;
                })
        ));

        // ─── Root: Selector tries branches in priority order ─────────────────
        var root = new Selector<PackWolfEntity>(List.of(
                fleeSequence,
                rallySequence,
                normalAttackSequence
        ));

        return new BehaviorTree<>(root);
    }

    /**
     * Tick the behavior tree each game tick.
     * Called from the standard mob tick cycle.
     */
    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            // Tick behavior tree every 5 ticks (server-side only)
            if (this.tickCount % 5 == 0) {
                behaviorTree.tick(this);
                // Update signal propagator position
                signalPropagator.updateAnchor(this.position());
            }
        }
    }

    // ─── Getters / Setters ────────────────────────────────────────────────────

    public PackRole getPackRole() { return role; }
    public void setPackRole(PackRole role) { this.role = role; }

    public MoraleState getMoraleState() { return moraleState; }
    public void setMoraleState(MoraleState state) { this.moraleState = state; }

    public SignalPropagator getSignalPropagator() { return signalPropagator; }
    public BehaviorTree<PackWolfEntity> getBehaviorTree() { return behaviorTree; }
}
