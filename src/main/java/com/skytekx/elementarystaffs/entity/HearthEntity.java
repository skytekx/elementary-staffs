package com.skytekx.elementarystaffs.entity;

import com.skytekx.elementarystaffs.registry.ModEntities;
import com.skytekx.elementarystaffs.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** The heal ball that flies back from a vampire ball hit and heals its owner on arrival. */
public final class HearthEntity extends StaffProjectile {
    /** A heal ball without an owner, for example after a restart, falls apart after this many ticks. */
    public static final int ORPHAN_AGE = 100;

    private float health = 2.0F;
    private float boost = 0.2F;

    public HearthEntity(EntityType<? extends HearthEntity> type, Level level) {
        super(type, level);
    }

    public HearthEntity(LivingEntity owner, Level level, float health, float boost) {
        super(ModEntities.HEARTH.get(), owner, level);
        this.health = health;
        this.boost = boost;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.HEARTH.get();
    }

    private void arrive(LivingEntity owner) {
        if (!level().isClientSide() && !isRemoved()) {
            owner.heal(health);
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity() == getOwner() && getOwner() instanceof LivingEntity owner) {
            arrive(owner);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (isRemoved()) {
            return;
        }
        Entity owner = getOwner();
        if (owner == null) {
            if (!level().isClientSide() && tickCount > ORPHAN_AGE) {
                discard();
            }
            return;
        }
        Vec3 toOwner = new Vec3(owner.getX() - getX(), owner.getY() + 0.4D - getY(), owner.getZ() - getZ());
        if (toOwner.length() < 1.0D && owner instanceof LivingEntity living) {
            arrive(living);
        }
        setDeltaMovement(toOwner.normalize().scale(boost));
        if (!level().isClientSide() && !owner.isAlive()) {
            discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Health", health);
        tag.putFloat("Boost", boost);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Health")) {
            health = tag.getFloat("Health");
            boost = tag.getFloat("Boost");
        }
    }
}
