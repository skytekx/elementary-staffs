package com.skytekx.elementarystaffs.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Slows every living entity around the player. Hits slow the target for longer. */
public final class IceStaff extends AreaEffectStaff {
    public IceStaff() {
        super(1024, "ice");
    }

    @Override
    protected void affect(ServerLevel level, Player player, LivingEntity target, int power) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, power), player);
        level.levelEvent(2002, target.blockPosition(), MobEffects.MOVEMENT_SPEED.getColor());
    }

    @Override
    protected boolean onAttack(Player player, Entity target, ItemStack stack) {
        if (!player.level().isClientSide()) {
            if (target instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 230, 1 + power(stack)), player);
            }
            wear(stack, player, InteractionHand.MAIN_HAND);
        }
        return true;
    }
}
