package com.skytekx.elementarystaffs.item;

import com.skytekx.elementarystaffs.StaffsConfig;
import com.skytekx.elementarystaffs.registry.ModEnchantments;
import com.skytekx.elementarystaffs.registry.ModItems;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * A staff with durability that its essence repairs in an anvil. The game logic of every staff runs
 * on the server only, sounds and particles are sent from there to all players nearby.
 */
public abstract class StaffBase extends Item {
    private final String essence;

    protected StaffBase(int maxDamage, String essence) {
        super(new Item.Properties().stacksTo(1).durability(maxDamage));
        this.essence = essence;
    }

    public String essence() {
        return essence;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        return repair.is(ModItems.essence(essence));
    }

    /**
     * Runs when the player hits an entity with this staff in the main hand, on both sides.
     *
     * @return false to cancel the attack, as the healing staff does
     */
    protected boolean onAttack(Player player, Entity target, ItemStack stack) {
        return true;
    }

    // Forge calls this at the start of every player attack. In 2.0.0 a mixin into Player.attack did the same.
    @Override
    public final boolean onLeftClickEntity(ItemStack stack, Player player, Entity target) {
        if (player.isSpectator()) {
            return false;
        }
        return !onAttack(player, target, stack);
    }

    /** The level of Magical Power on the stack, 0 to 3. */
    public static int power(ItemStack stack) {
        return EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.MAGICAL_POWER.get(), stack);
    }

    protected static void wear(ItemStack stack, Player player, InteractionHand hand) {
        stack.hurtAndBreak(1, player, broken -> broken.broadcastBreakEvent(hand));
    }

    protected void cooldown(Player player, int ticks) {
        if (StaffsConfig.COOLDOWN_STAFFS.get()) {
            player.getCooldowns().addCooldown(this, ticks);
        }
    }

    /** Sends one particle with the given motion, as Level.addParticle does on the client. */
    protected static void particle(ServerLevel level, ParticleOptions particle, double x, double y, double z, double dx, double dy, double dz) {
        level.sendParticles(particle, x, y, z, 0, dx, dy, dz, 1.0D);
    }
}
