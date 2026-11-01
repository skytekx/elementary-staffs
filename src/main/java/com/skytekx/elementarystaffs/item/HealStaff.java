package com.skytekx.elementarystaffs.item;

import com.skytekx.elementarystaffs.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Hitting a living entity heals it instead of hurting it. */
public final class HealStaff extends StaffBase {
    public HealStaff() {
        super(1024, "life");
    }

    @Override
    protected boolean onAttack(Player player, Entity target, ItemStack stack) {
        if (target instanceof LivingEntity living && player.level() instanceof ServerLevel server) {
            float multiplier = Math.max(1.0F, power(stack) / 2.0F + 0.5F);
            living.heal(2.0F * multiplier);
            server.playSound(null, living.getX(), living.getY(), living.getZ(), ModSounds.MAGIC.get(), SoundSource.PLAYERS, 1.0F, 0.5F);
            RandomSource random = server.random;
            for (int i = 0; i < 5; i++) {
                particle(server, ParticleTypes.HEART,
                        living.getX() + random.nextFloat() - 0.5D,
                        living.getY() + living.getBbHeight() + random.nextFloat(),
                        living.getZ() + random.nextFloat() - 0.5D,
                        random.nextFloat() - 0.5D, random.nextFloat() - 0.5D, random.nextFloat() - 0.5D);
            }
            wear(stack, player, InteractionHand.MAIN_HAND);
        }
        // The staff never hurts what it hits.
        return false;
    }
}
