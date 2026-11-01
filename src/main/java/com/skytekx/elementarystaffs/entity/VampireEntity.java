package com.skytekx.elementarystaffs.entity;

import com.skytekx.elementarystaffs.registry.ModEntities;
import com.skytekx.elementarystaffs.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/**
 * The ball of the darkness staff. It drains up to 2 health from the first living entity it hits and
 * sends a heal ball with that amount back to its owner.
 */
public final class VampireEntity extends StaffProjectile {
    public static final int MAX_AGE = 300;

    private float damage = 2.0F;
    private float boost = 0.2F;

    public VampireEntity(EntityType<? extends VampireEntity> type, Level level) {
        super(type, level);
    }

    public VampireEntity(LivingEntity owner, Level level, int power) {
        super(ModEntities.VAMPIRE.get(), owner, level);
        float multiplier = multiplier(power);
        this.damage = 2.0F * multiplier;
        this.boost = 0.2F * multiplier;
        setNoGravity(true);
    }

    public float damage() {
        return damage;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.VAMPIRE.get();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        BlockPos pos = result.getBlockPos();
        if (!level().isClientSide() && level().getBlockState(pos).isRedstoneConductor(level(), pos)) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide() || isRemoved() || !(getOwner() instanceof LivingEntity owner)) {
            return;
        }
        Entity entity = result.getEntity();
        if (!(entity instanceof LivingEntity target)) {
            return;
        }
        float amount = Math.min(target.getHealth(), damage);
        target.hurt(damageSources().mobAttack(owner), amount);
        HearthEntity hearth = new HearthEntity(owner, level(), amount, boost);
        hearth.setPos(position());
        level().addFreshEntity(hearth);
        discard();
    }

    @Override
    public void tick() {
        super.tick();
        speedUpInWater();
        if (!level().isClientSide() && tickCount >= MAX_AGE) {
            discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Damage", damage);
        tag.putFloat("Boost", boost);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Damage")) {
            damage = tag.getFloat("Damage");
            boost = tag.getFloat("Boost");
        }
    }
}
