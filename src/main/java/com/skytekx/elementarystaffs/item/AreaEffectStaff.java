package com.skytekx.elementarystaffs.item;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** The ice and poison staffs, which affect every living entity around the player. */
abstract class AreaEffectStaff extends StaffBase {
    protected AreaEffectStaff(int maxDamage, String essence) {
        super(maxDamage, essence);
    }

    protected abstract void affect(ServerLevel level, Player player, LivingEntity target, int power);

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int power = power(stack);
        float multiplier = Math.max(1.0F, power / 2.0F + 0.5F);
        double distance = 6.0D + 0.5D * multiplier;
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(distance + 0.5D),
                entity -> !entity.isSpectator() && entity != player);
        if (level instanceof ServerLevel server) {
            cooldown(player, 5);
            for (LivingEntity target : nearby) {
                if (target.distanceTo(player) <= distance) {
                    affect(server, player, target, power);
                }
            }
            if (!nearby.isEmpty()) {
                wear(stack, player, hand);
            }
        }
        return nearby.isEmpty() ? InteractionResultHolder.pass(stack) : InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
