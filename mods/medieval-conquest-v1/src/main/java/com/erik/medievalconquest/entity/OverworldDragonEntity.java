package com.erik.medievalconquest.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Overworld Dragon — a fearsome hostile mob that spawns in the overworld.
 * NOT the Ender Dragon — this is a new, custom dragon.
 *
 * Current implementation: ground-based dragon (walks and attacks).
 * TODO: Add flying AI and breath attack in future phases.
 */
public class OverworldDragonEntity extends Monster {

	public OverworldDragonEntity(EntityType<? extends Monster> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected void registerGoals() {
		// Survival
		this.goalSelector.addGoal(0, new FloatGoal(this));

		// Combat
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false));

		// Movement
		this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8));

		// Awareness
		this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 16.0f));
		this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

		// Targeting
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 100.0)        // 50 hearts — tough boss
				.add(Attributes.ATTACK_DAMAGE, 12.0)       // hits hard
				.add(Attributes.MOVEMENT_SPEED, 0.3)        // slightly fast
				.add(Attributes.FOLLOW_RANGE, 48.0)         // sees you from far
				.add(Attributes.ARMOR, 8.0)                 // natural armor
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.8); // barely flinches
	}
}
