package com.skytekx.elementarystaffs.item;

import com.skytekx.elementarystaffs.entity.VampireEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Shoots a vampire ball that drains life from what it hits and carries it back to the player. */
public final class DarknessStaff extends StaffBase {
    public DarknessStaff() {
        super(100, "darkness");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            cooldown(player, 10);
            VampireEntity vampire = new VampireEntity(player, level, power(stack));
            vampire.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.2F, 0.0F);
            level.addFreshEntity(vampire);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CROSSBOW_HIT, SoundSource.PLAYERS, 4.0F, 0.0F);
            wear(stack, player, hand);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
