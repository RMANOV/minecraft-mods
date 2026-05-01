package com.erik.medievalconquest.entity;

import com.erik.medievalconquest.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Overworld Dragon — a fearsome FLYING hostile mob.
 * Circles the sky, swoops down to attack players, then climbs back up.
 * Uses FlyingPathNavigation + FlyingMoveControl on a Monster base.
 */
public class OverworldDragonEntity extends Monster {

	public OverworldDragonEntity(EntityType<? extends Monster> entityType, Level level) {
		super(entityType, level);
		this.moveControl = new FlyingMoveControl(this, 20, true);
		this.setNoGravity(true);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
		nav.setCanOpenDoors(false);
		nav.setCanFloat(true);
		return nav;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new DragonSwoopAttackGoal(this));
		this.goalSelector.addGoal(2, new DragonFlyAroundGoal(this));
		this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 48.0f));
		this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	public void travel(Vec3 travelVector) {
		if (this.isInWater()) {
			this.moveRelative(0.02f, travelVector);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(0.8));
		} else {
			this.travelFlying(travelVector, 0.91f);
		}
		this.calculateEntityAnimation(false);
	}

	@Override
	public boolean onClimbable() {
		return false;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 180.0)
				.add(Attributes.ATTACK_DAMAGE, 16.0)
				.add(Attributes.MOVEMENT_SPEED, 0.35)
				.add(Attributes.FLYING_SPEED, 0.52)
				.add(Attributes.FOLLOW_RANGE, 80.0)
				.add(Attributes.ARMOR, 10.0)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.9);
	}

	@Override
	public boolean doHurtTarget(ServerLevel level, Entity target) {
		boolean hit = super.doHurtTarget(level, target);
		if (hit) {
			target.igniteForSeconds(6.0f);
		}
		return hit;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
		super.dropCustomDeathLoot(level, damageSource, recentlyHit);
		int count = 2 + this.getRandom().nextInt(3);
		this.spawnAtLocation(level, new ItemStack(ModItems.DRAGON_MEAT, count));
	}

	// ─── Dragon AI: Fly around randomly ───────────────────────────

	static class DragonFlyAroundGoal extends Goal {
		private final OverworldDragonEntity dragon;

		DragonFlyAroundGoal(OverworldDragonEntity dragon) {
			this.dragon = dragon;
			this.setFlags(EnumSet.of(Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			MoveControl control = this.dragon.getMoveControl();
			if (!control.hasWanted()) {
				return true;
			}
			double dx = control.getWantedX() - this.dragon.getX();
			double dy = control.getWantedY() - this.dragon.getY();
			double dz = control.getWantedZ() - this.dragon.getZ();
			return dx * dx + dy * dy + dz * dz < 1.0;
		}

		@Override
		public boolean canContinueToUse() {
			return false;
		}

		@Override
		public void start() {
			RandomSource random = this.dragon.getRandom();
			double x = this.dragon.getX() + (random.nextFloat() * 2.0 - 1.0) * 16.0;
			double z = this.dragon.getZ() + (random.nextFloat() * 2.0 - 1.0) * 16.0;

			// Pick altitude: 15-40 blocks above current ground level
			// Approximate ground as y=64, target between 79-104
			double y = this.dragon.getY() + (random.nextFloat() * 2.0 - 1.0) * 8.0;
			y = Mth.clamp(y, 75.0, 140.0);

			this.dragon.getMoveControl().setWantedPosition(x, y, z, 1.0);
		}
	}

	// ─── Dragon AI: Swoop attack ──────────────────────────────────

	static class DragonSwoopAttackGoal extends Goal {
		private final OverworldDragonEntity dragon;
		private int ticksInPhase;
		private int fireCooldown;

		private enum Phase { CIRCLING, DIVING, CLIMBING }
		private Phase phase = Phase.CIRCLING;

		DragonSwoopAttackGoal(OverworldDragonEntity dragon) {
			this.dragon = dragon;
			this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			LivingEntity target = this.dragon.getTarget();
			return target != null && target.isAlive();
		}

		@Override
		public boolean canContinueToUse() {
			return canUse();
		}

		@Override
		public void start() {
			this.ticksInPhase = 0;
			this.fireCooldown = 20;
			this.phase = Phase.CIRCLING;
		}

		@Override
		public void tick() {
			LivingEntity target = this.dragon.getTarget();
			if (target == null || !target.isAlive()) return;

			this.dragon.getLookControl().setLookAt(target);
			double distSq = this.dragon.distanceToSqr(target);
			this.ticksInPhase++;
			this.fireCooldown--;

			switch (this.phase) {
				case CIRCLING -> {
					// Circle above the target at ~15 blocks altitude
					double angle = this.dragon.tickCount * 0.05;
					double circleX = target.getX() + Math.cos(angle) * 12.0;
					double circleZ = target.getZ() + Math.sin(angle) * 12.0;
					double circleY = target.getY() + 15.0;
					this.dragon.getMoveControl().setWantedPosition(circleX, circleY, circleZ, 1.25);
					shootFireballAt(target);

					if (this.ticksInPhase > 40) {
						this.phase = Phase.DIVING;
						this.ticksInPhase = 0;
					}
				}
				case DIVING -> {
					// Dive toward the target
					this.dragon.getMoveControl().setWantedPosition(
							target.getX(), target.getY() + 1.0, target.getZ(), 1.8);
					shootFireballAt(target);

					if (distSq < 9.0) {
						// Hit the target
						float damage = (float) this.dragon.getAttributeValue(Attributes.ATTACK_DAMAGE);
						target.hurt(this.dragon.damageSources().mobAttack(this.dragon), damage);
						target.igniteForSeconds(6.0f);
						this.phase = Phase.CLIMBING;
						this.ticksInPhase = 0;
					}

					if (this.ticksInPhase > 40) {
						// Timeout — missed the target, climb back up
						this.phase = Phase.CLIMBING;
						this.ticksInPhase = 0;
					}
				}
				case CLIMBING -> {
					// Climb back up after attack
					this.dragon.getMoveControl().setWantedPosition(
						this.dragon.getX(), this.dragon.getY() + 20.0, this.dragon.getZ(), 1.0);

					if (this.ticksInPhase > 30) {
						this.phase = Phase.CIRCLING;
						this.ticksInPhase = 0;
					}
				}
			}
		}

		private void shootFireballAt(LivingEntity target) {
			if (this.fireCooldown > 0 || this.dragon.level().isClientSide()) {
				return;
			}
			if (this.dragon.distanceToSqr(target) > 72.0 * 72.0) {
				return;
			}

			Vec3 targetCenter = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
			Vec3 direction = targetCenter.subtract(this.dragon.position()).normalize();
			SmallFireball fireball = new SmallFireball(this.dragon.level(), this.dragon, direction);
			fireball.setPos(this.dragon.getX(), this.dragon.getEyeY() - 0.2, this.dragon.getZ());
			((ServerLevel) this.dragon.level()).addFreshEntity(fireball);
			this.fireCooldown = 35 + this.dragon.getRandom().nextInt(25);
		}

		@Override
		public void stop() {
			this.ticksInPhase = 0;
			this.fireCooldown = 0;
			this.phase = Phase.CIRCLING;
		}
	}
}
