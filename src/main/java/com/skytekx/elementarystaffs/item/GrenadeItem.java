package com.skytekx.elementarystaffs.item;

import com.skytekx.elementarystaffs.Element;
import com.skytekx.elementarystaffs.StaffsConfig;
import com.skytekx.elementarystaffs.entity.ElementalGrenade;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** A grenade that is drawn like a bow. The longer it is held, the farther it flies. */
public final class GrenadeItem extends Item {
    /** The highest speed in blocks per tick, that of a thrown snowball. */
    public static final double MAX_SPEED = 1.5D;

    private final Element element;

    public GrenadeItem(Element element) {
        super(new Item.Properties().stacksTo(32));
        this.element = element;
    }

    public Element element() {
        return element;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    /** The throw speed after holding the grenade for the given ticks. 2.0.0 reached 1 after 50 ticks and did not stop there. */
    public static double speed(int ticksHeld) {
        return Math.min(ticksHeld * 2 / 100.0D, MAX_SPEED);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeLeft) {
        int ticksHeld = getUseDuration(stack) - timeLeft;
        if (level.isClientSide()) {
            return;
        }
        ElementalGrenade grenade = new ElementalGrenade(level, user, element);
        grenade.setDeltaMovement(user.getLookAngle().scale(speed(ticksHeld)));
        level.addFreshEntity(grenade);
        level.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (user instanceof Player player) {
            if (StaffsConfig.COOLDOWN_GRENADES.get()) {
                player.getCooldowns().addCooldown(this, Math.min((int) (15 * ticksHeld * 2 * 0.03D), 50));
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        } else {
            stack.shrink(1);
        }
    }
}
