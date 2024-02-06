
package net.sashakyotoz.variousworld.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import net.sashakyotoz.variousworld.init.VariousWorldModEntities;

public class ZanyVilerWitchEntity extends Witch {
	public int texture;

	public ZanyVilerWitchEntity(PlayMessages.SpawnEntity packet, Level world) {
		this(VariousWorldModEntities.ZANY_VILER_WITCH.get(), world);
	}

	public ZanyVilerWitchEntity(EntityType<ZanyVilerWitchEntity> type, Level world) {
		super(type, world);
		xpReward = 0;
		setNoAi(false);
		setPersistenceRequired();
		texture = (int) Math.round((Math.random()) * 2);
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.5, false) {
			@Override
			protected double getAttackReachSqr(LivingEntity entity) {
				return this.mob.getBbWidth() * this.mob.getBbWidth() + entity.getBbWidth();
			}
		});
		this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, IronGolem.class, (float) 6, 1, 1.2));
		this.goalSelector.addGoal(3, new RandomStrollGoal(this, 1));
		this.targetSelector.addGoal(4, new HurtByTargetGoal(this).setAlertOthers());
		this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
		this.goalSelector.addGoal(6, new FloatGoal(this));
	}

	public void performRangedAttack(LivingEntity p_34143_, float p_34144_) {
		if (!this.isDrinkingPotion()) {
			Vec3 vec3 = p_34143_.getDeltaMovement();
			double d0 = p_34143_.getX() + vec3.x - this.getX();
			double d1 = p_34143_.getEyeY() - (double) 1.1F - this.getY();
			double d2 = p_34143_.getZ() + vec3.z - this.getZ();
			double d3 = Math.sqrt(d0 * d0 + d2 * d2);
			Potion potion = Potions.HARMING;
			if (p_34143_ instanceof Raider) {
				if (p_34143_.getHealth() <= 4.0F) {
					potion = Potions.HEALING;
				} else {
					potion = Potions.REGENERATION;
				}
				this.setTarget(null);
			} else if (d3 >= 8.0D && !p_34143_.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
				potion = Potions.SLOWNESS;
			} else if (p_34143_.getHealth() >= 8.0F && !p_34143_.hasEffect(MobEffects.POISON)) {
				potion = Potions.POISON;
			} else if (d3 <= 3.0D && !p_34143_.hasEffect(MobEffects.WEAKNESS) && this.random.nextFloat() < 0.25F) {
				potion = Potions.WEAKNESS;
			}
			ThrownPotion thrownpotion = new ThrownPotion(this.level(), this);
			thrownpotion.setItem(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), potion));
			thrownpotion.setXRot(thrownpotion.getXRot() - -20.0F);
			thrownpotion.shoot(d0, d1 + d3 * 0.2D, d2, 0.75F, 8.0F);
			if (!this.isSilent()) {
				this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_THROW, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
			}
			this.level().addFreshEntity(thrownpotion);
		}
	}

	@Override
	public MobType getMobType() {
		return MobType.ILLAGER;
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	public SoundEvent getAmbientSound() {
		return SoundEvents.WITCH_AMBIENT;
	}

	@Override
	public void playStepSound(BlockPos pos, BlockState blockIn) {
		this.playSound(SoundEvents.ROOTED_DIRT_STEP, 0.15f, 1);
	}

	@Override
	public SoundEvent getHurtSound(DamageSource ds) {
		return SoundEvents.WITCH_HURT;
	}

	@Override
	public SoundEvent getDeathSound() {
		return SoundEvents.WITCH_DEATH;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (source.getDirectEntity() instanceof ThrownPotion || source.getDirectEntity() instanceof AreaEffectCloud)
			return false;
		if (source.is(DamageTypes.CACTUS))
			return false;
		return super.hurt(source, amount);
	}

	public static void init() {
	}

	public static AttributeSupplier.Builder createAttributes() {
		AttributeSupplier.Builder builder = Mob.createMobAttributes();
		builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3);
		builder = builder.add(Attributes.MAX_HEALTH, 30);
		builder = builder.add(Attributes.ARMOR, 2);
		builder = builder.add(Attributes.ATTACK_DAMAGE, 5);
		builder = builder.add(Attributes.FOLLOW_RANGE, 24);
		return builder;
	}
}