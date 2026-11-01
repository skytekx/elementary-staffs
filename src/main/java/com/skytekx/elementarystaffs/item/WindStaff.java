package com.skytekx.elementarystaffs.item;

import com.skytekx.elementarystaffs.entity.WindEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Sends out a whirlwind that pulls entities in and hurts those close to its center. */
public final class WindStaff extends StaffBase {
    public WindStaff() {
        super(150, "wind");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            cooldown(player, 10);
            level.addFreshEntity(new WindEntity(player, level, power(stack)));
            wear(stack, player, hand);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
