package com.skytekx.elementarystaffs.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Shared parts of the projectiles that the staffs shoot. */
abstract class StaffProjectile extends ThrowableItemProjectile {
    /**
     * 2.0.0 sped vampire and water balls up by 17 percent per tick in water without a limit. Past
     * a few blocks per tick a projectile loads every chunk on its way, so the speed stops here.
     */
    static final double MAX_WATER_SPEED = 3.0D;

    StaffProjectile(EntityType<? extends StaffProjectile> type, Level level) {
        super(type, level);
    }

    StaffProjectile(EntityType<? extends StaffProjectile> type, LivingEntity owner, Level level) {
        super(type, owner, level);
    }

    /** The multiplier of 2.0.0 for Magical Power, at least 1. */
    static float multiplier(int power) {
        return Math.max(1.0F, power / 2.0F + 0.5F);
    }

    /**
     * Speeds the projectile up in water as 2.0.0 did, or removes it when one axis of its motion is
     * zero.
     */
    void speedUpInWater() {
        if (level().isClientSide() || !isInWater()) {
            return;
        }
        Vec3 motion = getDeltaMovement();
        if (motion.x != 0.0D && motion.y != 0.0D && motion.z != 0.0D) {
            Vec3 faster = motion.scale(1.17D);
            if (faster.length() > MAX_WATER_SPEED) {
                faster = faster.normalize().scale(MAX_WATER_SPEED);
            }
            setDeltaMovement(faster);
        } else {
            discard();
        }
    }
}
