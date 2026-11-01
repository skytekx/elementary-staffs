package com.skytekx.elementarystaffs.entity;

import com.skytekx.elementarystaffs.registry.ModEntities;
import com.skytekx.elementarystaffs.registry.ModSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The whirlwind of the wind staff. It drifts in the direction of view, pulls entities within 4
 * blocks towards its center and hurts living ones closer than 1.7 blocks. Two whirlwinds of
 * different owners that meet call down lightning.
 */
public final class WindEntity extends StaffProjectile {
    /** 2.0.0 called lightning every tick while two whirlwinds touched. Here it is once a second at most. */
    public static final int LIGHTNING_COOLDOWN = 20;

    private int maxAge = 180;
    private float boostOnGround = 0.2F;
    private int lightningCooldown;

    public WindEntity(EntityType<? extends WindEntity> type, Level level) {
        super(type, level);
    }

    public WindEntity(LivingEntity owner, Level level, int power) {
        super(ModEntities.WIND.get(), owner, level);
        float multiplier = multiplier(power);
        this.maxAge = (int) (180 * multiplier);
        this.boostOnGround = 0.2F * multiplier;
        setDeltaMovement(owner.getLookAngle());
    }

    @Override
    protected Item getDefaultItem() {
        return Items.AIR;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!(result.getEntity() instanceof LivingEntity)) {
            setDeltaMovement(getDeltaMovement().x, 0.0D, getDeltaMovement().z);
        }
    }

    /**
     * Whether this side moves the entity. The server moves everything but players, each client
     * moves its own player, as Fabric did by running the same code on both sides.
     */
    private boolean movesOnThisSide(Entity entity) {
        if (entity instanceof Player player) {
            return level().isClientSide() && player.isLocalPlayer();
        }
        return !level().isClientSide();
    }

    @Override
    public void tick() {
        super.tick();
        boolean client = level().isClientSide();
        if (!client && tickCount >= maxAge) {
            discard();
            return;
        }
        if (lightningCooldown > 0) {
            lightningCooldown--;
        }
        if (onGround()) {
            setDeltaMovement(getDeltaMovement().multiply(random.nextFloat() * boostOnGround, random.nextFloat() * boostOnGround,
                    random.nextFloat() * boostOnGround));
        }
        if (client && isInWater()) {
            Vec3 motion = getDeltaMovement();
            for (int i = 0; i < 4; i++) {
                level().addParticle(ParticleTypes.BUBBLE, getX() - motion.x * 0.25D, getY() - motion.y * 0.25D, getZ() - motion.z * 0.25D,
                        motion.x, motion.y, motion.z);
            }
        }
        if (client && tickCount % 30 == 0) {
            level().playLocalSound(getX(), getY(), getZ(), ModSounds.WIND.get(), SoundSource.AMBIENT, 1.0F, 1.0F, true);
        }
        setDeltaMovement(getDeltaMovement().scale(0.9D));
        BlockPos here = blockPosition();
        if (level().getBlockState(here).isRedstoneConductor(level(), here)) {
            setDeltaMovement(getDeltaMovement().x, 0.1D, getDeltaMovement().z);
        }

        Entity owner = getOwner();
        List<Entity> nearby = level().getEntities(this, getBoundingBox().inflate(4.0D, 2.0D, 4.0D), entity -> entity != owner);
        for (Entity entity : nearby) {
            // 2.0.0 normalized nothing here, so the pull grows with the distance up to a cap.
            Vec3 toCenter = new Vec3(getX() - entity.getX(), getY() - entity.getY(), getZ() - entity.getZ());
            double horizontal = Math.max(15.0D, (toCenter.x * toCenter.x + toCenter.z * toCenter.z) * 10.0D);
            double pullX = toCenter.x / horizontal;
            double pullY = toCenter.y / Math.max(15.0D, toCenter.y * toCenter.y * 10.0D);
            double pullZ = toCenter.z / horizontal;
            if (entity instanceof LivingEntity living) {
                if (living.isPushable() && movesOnThisSide(living)) {
                    living.push(pullX, pullY, pullZ);
                }
                if (!client && distanceToSqr(living) <= 3.0D) {
                    living.hurt(damage(owner), 1.0F);
                }
                living.fallDistance = 0.0F;
            } else if (entity instanceof WindEntity other) {
                if (other.getOwner() != owner && !client) {
                    if (lightningCooldown == 0) {
                        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level());
                        if (lightning != null) {
                            lightning.moveTo(getX(), getY() - 1.0D, getZ());
                            level().addFreshEntity(lightning);
                        }
                        lightningCooldown = LIGHTNING_COOLDOWN;
                    }
                    other.push(pullX, pullY, pullZ);
                }
            } else if (movesOnThisSide(entity)) {
                entity.push(pullX, pullY, pullZ);
            }
        }

        if (client) {
            BlockState ground = level().getBlockState(BlockPos.containing(getX(), getY() - 2.0D, getZ()));
            if (ground.isAir()) {
                ground = Blocks.GLASS.defaultBlockState();
            }
            for (int i = 0; i < 4; i++) {
                level().addDestroyBlockEffect(BlockPos.containing(getX() + random.nextFloat() - 0.5D, getY() + random.nextFloat() - 0.5D,
                        getZ() + random.nextFloat() - 0.5D), ground);
            }
        }
    }

    private DamageSource damage(Entity owner) {
        return owner instanceof LivingEntity living ? damageSources().mobAttack(living) : damageSources().generic();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("MaxAge", maxAge);
        tag.putFloat("BoostOnGround", boostOnGround);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("MaxAge")) {
            maxAge = tag.getInt("MaxAge");
            boostOnGround = tag.getFloat("BoostOnGround");
        }
    }
}
