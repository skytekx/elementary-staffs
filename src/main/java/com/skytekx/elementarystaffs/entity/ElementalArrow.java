package com.skytekx.elementarystaffs.entity;

import com.skytekx.elementarystaffs.Element;
import com.skytekx.elementarystaffs.registry.ModEntities;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * One class for the eight arrows of 2.0.0. The element follows from the entity type. A hit
 * applies the effect of the element on the server, the flight trail is drawn on the client.
 */
public final class ElementalArrow extends AbstractArrow {
    /** The extra knockback of the wind arrow, as two levels of Punch. */
    public static final int WIND_KNOCKBACK = 2;

    private final Element element;

    public ElementalArrow(EntityType<? extends ElementalArrow> type, Level level, Element element) {
        super(type, level);
        this.element = element;
    }

    public ElementalArrow(Level level, LivingEntity shooter, Element element) {
        super(ModEntities.ARROWS.get(element).get(), shooter, level);
        this.element = element;
    }

    public ElementalArrow(Level level, double x, double y, double z, Element element) {
        super(ModEntities.ARROWS.get(element).get(), x, y, z, level);
        this.element = element;
    }

    public Element element() {
        return element;
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        switch (element) {
            case DARK -> {
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 1), getEffectSource());
                target.hurt(target.damageSources().magic(), 1.0F);
            }
            case ELECTRIC -> target.hurt(target.damageSources().magic(), target.hasEffect(MobEffects.CONFUSION) ? 6.0F : 2.0F);
            case FIRE -> {
                target.hurt(target.damageSources().inFire(), 4.0F);
                target.setSecondsOnFire(2);
            }
            case ICE -> {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1, true, false, false), getEffectSource());
                target.setTicksFrozen(10);
            }
            case LIFE -> {
                float amount = (float) Math.ceil(Math.max(getDeltaMovement().length() * getBaseDamage(), 0.0D));
                target.heal(amount + 2.0F);
            }
            case POISON -> target.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0, true, false, false), getEffectSource());
            case WATER -> {
                target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0, true, false, false), getEffectSource());
                target.clearFire();
            }
            case WIND -> {
                // 2.0.0 raised the knockback when the spawn packet was built, which added two more
                // for every player that saw the arrow. The push here is that of two Punch levels.
                double resistance = Math.max(0.0D, 1.0D - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                Vec3 push = getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale(WIND_KNOCKBACK * 0.6D * resistance);
                if (push.lengthSqr() > 0.0D) {
                    target.push(push.x, 0.1D, push.z);
                }
                target.push(0.0D, 0.2D, 0.0D);
            }
        }
    }

    @Override
    public void tick() {
        if (level().isClientSide() && !inGround) {
            ParticleOptions trail = trail();
            Vec3 motion = getDeltaMovement();
            for (int i = 0; i < 4; i++) {
                level().addParticle(trail, true, getX() + motion.x * i / 4.0D, getY() + motion.y * i / 4.0D, getZ() + motion.z * i / 4.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        super.tick();
    }

    private ParticleOptions trail() {
        return switch (element) {
            case DARK -> ParticleTypes.PORTAL;
            case ELECTRIC -> ParticleTypes.CRIT;
            case FIRE -> ParticleTypes.FLAME;
            case ICE -> ParticleTypes.ITEM_SNOWBALL;
            case LIFE -> ParticleTypes.HEART;
            case POISON -> ParticleTypes.ENTITY_EFFECT;
            case WATER -> ParticleTypes.SPLASH;
            case WIND -> ParticleTypes.MYCELIUM;
        };
    }

    /** As in 2.0.0 a picked up elemental arrow turns into a plain arrow. */
    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
