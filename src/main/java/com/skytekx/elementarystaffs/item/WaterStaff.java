package com.skytekx.elementarystaffs.item;

import com.skytekx.elementarystaffs.entity.BubbleEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Shoots a water ball that traps what it hits in a rising bubble. */
public final class WaterStaff extends StaffBase {
    public WaterStaff() {
        super(400, "water");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            cooldown(player, 10);
            BubbleEntity bubble = new BubbleEntity(player, level, power(stack));
            bubble.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.2F, 0.0F);
            level.addFreshEntity(bubble);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_SWIM, SoundSource.PLAYERS, 4.0F, 0.0F);
            wear(stack, player, hand);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
