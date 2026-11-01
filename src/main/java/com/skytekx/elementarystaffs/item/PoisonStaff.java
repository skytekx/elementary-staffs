package com.skytekx.elementarystaffs.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Poisons every living entity around the player. Hits poison the target. */
public final class PoisonStaff extends AreaEffectStaff {
    public PoisonStaff() {
        super(1024, "poison");
    }

    @Override
    protected void affect(ServerLevel level, Player player, LivingEntity target, int power) {
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 300, 1 + power), player);
        for (int i = 0; i < 6; i++) {
            particle(level, ParticleTypes.AMBIENT_ENTITY_EFFECT,
                    target.getX() + level.random.nextFloat() - 0.5D,
                    target.getY() + 1.0D + level.random.nextFloat(),
                    target.getZ() + level.random.nextFloat() - 0.5D,
                    0.0D, 0.4D, 0.0D);
        }
    }

    @Override
    protected boolean onAttack(Player player, Entity target, ItemStack stack) {
        if (!player.level().isClientSide()) {
            if (target instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, 300, 1 + power(stack)), player);
            }
            wear(stack, player, InteractionHand.MAIN_HAND);
        }
        return true;
    }
}
