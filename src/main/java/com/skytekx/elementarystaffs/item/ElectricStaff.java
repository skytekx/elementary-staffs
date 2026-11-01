package com.skytekx.elementarystaffs.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Launches the player in the direction of view and catches the next fall. */
public final class ElectricStaff extends StaffBase {
    public ElectricStaff() {
        super(100, "electric");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        float power = Math.max(1.0F, power(stack) / 2.0F + 0.5F);
        double yaw = Math.toRadians(player.getYRot());
        double pitch = Math.toRadians(player.getXRot());
        // The client moves its own player, so the push runs on both sides as in 2.0.0.
        player.push(-Math.sin(yaw) * Math.cos(pitch) * power, -Math.sin(pitch) * power, Math.cos(yaw) * Math.cos(pitch) * power);
        player.fallDistance = -25.0F;
        if (!level.isClientSide()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 10.0F, -1.0F);
            wear(stack, player, hand);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
