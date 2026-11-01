package com.skytekx.elementarystaffs.entity;

import com.skytekx.elementarystaffs.registry.ModEntities;
import com.skytekx.elementarystaffs.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/**
 * The ball of the water staff. The first entity it hits rides it as in a bubble, which rises for
 * 105 ticks, puts out fire and then pops.
 */
public final class BubbleEntity extends StaffProjectile {
    private int maxAge = 300;
    private int maxMountTime = 105;
    private int mountTime;

    public BubbleEntity(EntityType<? extends BubbleEntity> type, Level level) {
        super(type, level);
    }

    public BubbleEntity(LivingEntity owner, Level level, int power) {
        super(ModEntities.BUBBLE.get(), owner, level);
        float multiplier = multiplier(power);
        // 2.0.0 scaled the times on the first tick with these factors, also without the enchantment.
        this.maxAge = (int) (300 * multiplier);
        this.maxMountTime = (int) (70 * (multiplier + 0.5F));
        setNoGravity(true);
    }

    public int maxMountTime() {
        return maxMountTime;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.BUBBLE.get();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        BlockPos pos = result.getBlockPos();
        if (!level().isClientSide() && level().getBlockState(pos).isRedstoneConductor(level(), pos)) {
            ejectPassengers();
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide() || isRemoved()) {
            return;
        }
        Entity entity = result.getEntity();
        if (!(entity instanceof EndCrystal) && !(entity instanceof EnderDragonPart) && mountTime == 0 && !entity.isPassenger()) {
            entity.clearFire();
            entity.startRiding(this);
        }
    }

    @Override
    public void tick() {
        super.tick();
        boolean server = !level().isClientSide();
        if (server) {
            getPassengers().forEach(Entity::clearFire);
            speedUpInWater();
            if (tickCount >= maxAge && mountTime == 0) {
                discard();
            }
        }
        if (isVehicle()) {
            mountTime++;
            setDeltaMovement(0.0D, 0.1D, 0.0D);
            if (!server) {
                level().addParticle(ParticleTypes.SPLASH, getX(), getY() - 1.0D, getZ(), 0.0D, -1.0D, 0.0D);
                level().addParticle(ParticleTypes.SPLASH, getX() + random.nextFloat() - 0.5D, getY() + random.nextFloat() - 0.5D,
                        getZ() + random.nextFloat() - 0.5D, 0.0D, -1.0D, 0.0D);
            }
            if (server && mountTime >= maxMountTime) {
                ejectPassengers();
                discard();
            }
        } else if (server && mountTime > 0) {
            discard();
        }
    }

    @Override
    public double getPassengersRidingOffset() {
        return -0.2D;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("MaxAge", maxAge);
        tag.putInt("MaxMountTime", maxMountTime);
        tag.putInt("MountTime", mountTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("MaxAge")) {
            maxAge = tag.getInt("MaxAge");
            maxMountTime = tag.getInt("MaxMountTime");
            mountTime = tag.getInt("MountTime");
        }
    }
}
