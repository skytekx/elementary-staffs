package com.skytekx.elementarystaffs.item;

import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Breathes fire in front of the player. Hits set the target on fire. */
public final class FireStaff extends StaffBase {
    public FireStaff() {
        super(400, "fire");
    }

    @Override
    protected boolean onAttack(Player player, Entity target, ItemStack stack) {
        if (!player.level().isClientSide()) {
            target.setSecondsOnFire(4);
            wear(stack, player, InteractionHand.MAIN_HAND);
        }
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isInWater()) {
            return InteractionResultHolder.pass(stack);
        }
        if (level instanceof ServerLevel server) {
            cooldown(player, 5);
            int power = power(stack);
            float rangeMultiplier = Math.max(1.0F, (float) (power / 2) + 0.2F);
            float damage = power != 0 ? power / 2.0F + 0.5F : 0.0F;
            double distance = 4.0D * rangeMultiplier;
            Vec3 look = player.getLookAngle();
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().expandTowards(look.x * distance, look.y * distance, look.z * distance).inflate(1.0D),
                    entity -> entity != player && !entity.isSpectator());
            for (LivingEntity target : targets) {
                if (target.distanceTo(player) <= distance + 2.0D) {
                    target.setSecondsOnFire(4);
                    target.push(look.x / 5.0D, 0.25D, look.z / 5.0D);
                    target.hurt(target.damageSources().inFire(), damage);
                }
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 5.0F, 1.0F);
            double vx = -Math.sin(Math.toRadians(player.getYRot()));
            double vz = Math.cos(Math.toRadians(player.getYRot()));
            double vy = -Math.sin(Math.toRadians(player.getXRot()));
            for (int i = 0; i < 8; i++) {
                particle(server, ParticleTypes.FLAME, player.getX(), player.getY() + 1.0D, player.getZ(),
                        (vx + level.random.nextFloat() - 0.5D) / 2.0D,
                        (vy + level.random.nextFloat() - 0.5D) / 8.0D,
                        (vz + level.random.nextFloat() - 0.5D) / 2.0D);
            }
            wear(stack, player, hand);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
