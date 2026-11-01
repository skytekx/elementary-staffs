package com.skytekx.elementarystaffs.entity;

import com.skytekx.elementarystaffs.Element;
import com.skytekx.elementarystaffs.registry.ModEntities;
import com.skytekx.elementarystaffs.registry.ModItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * One class for the eight grenades of 2.0.0. The grenade bursts on the first block or entity it
 * hits and affects the living entities within 4 blocks across and 2 blocks up and down.
 */
public final class ElementalGrenade extends ThrowableItemProjectile {
    private final Element element;

    public ElementalGrenade(EntityType<? extends ElementalGrenade> type, Level level, Element element) {
        super(type, level);
        this.element = element;
    }

    public ElementalGrenade(Level level, LivingEntity owner, Element element) {
        super(ModEntities.GRENADES.get(element).get(), owner, level);
        this.element = element;
        setDeltaMovement(owner.getLookAngle());
    }

    public ElementalGrenade(Level level, double x, double y, double z, Element element) {
        super(ModEntities.GRENADES.get(element).get(), x, y, z, level);
        this.element = element;
    }

    public Element element() {
        return element;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.GRENADES.get(element).get();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide() || isRemoved()) {
            return;
        }
        burst();
        Vec3 position = position().add(0.0D, 1.0D, 0.0D);
        level().levelEvent(2002, BlockPos.containing(position), PotionUtils.getColor(splashColor()));
        discard();
    }

    /** Applies the effect of the element around the grenade. Public for the GameTests. */
    public void burst() {
        AABB area = getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, area, entity -> !entity.isSpectator());
        if (element == Element.WIND) {
            if (!targets.isEmpty()) {
                repel();
            }
            return;
        }
        Entity owner = getOwner();
        for (LivingEntity target : targets) {
            boolean isOwner = target == owner;
            switch (element) {
                case DARK -> {
                    if (!isOwner) {
                        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 1), owner);
                        target.hurt(damage(), 8.0F);
                    }
                }
                case ELECTRIC -> {
                    if (!isOwner) {
                        float amount = 4.0F;
                        if (target.hasEffect(MobEffects.CONFUSION) || target.isInWaterRainOrBubble()) {
                            amount = 8.0F;
                            target.removeEffect(MobEffects.CONFUSION);
                        }
                        target.hurt(damageSources().indirectMagic(this, owner), amount);
                    }
                }
                case FIRE -> {
                    if (!isOwner) {
                        target.setSecondsOnFire(8);
                    }
                }
                case ICE -> {
                    if (!isOwner) {
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1), owner);
                        target.hurt(damage(), 8.0F);
                    }
                }
                case LIFE -> {
                    if (!isOwner) {
                        target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1), owner);
                        target.heal(1.5F);
                    }
                }
                case POISON -> {
                    if (!isOwner) {
                        target.addEffect(new MobEffectInstance(MobEffects.POISON, 400, 0), owner);
                    }
                }
                case WATER -> {
                    // As in 2.0.0 the water grenade also puts out and confuses its thrower.
                    target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0), owner);
                    target.clearFire();
                    if (!isOwner) {
                        target.hurt(damage(), target.fireImmune() ? 16.0F : 8.0F);
                    }
                }
                default -> {
                }
            }
        }
    }

    /** Pushes every entity but the thrower away from the grenade and hurts the living ones. */
    private void repel() {
        double size = 3.0D;
        double strength = 2.0D;
        AABB area = new AABB(Math.floor(getX() - size - 1.0D), Math.floor(getY() - size - 1.0D), Math.floor(getZ() - size - 1.0D),
                Math.floor(getX() + size + 1.0D), Math.floor(getY() + size + 1.0D), Math.floor(getZ() + size + 1.0D));
        Entity owner = getOwner();
        for (Entity entity : level().getEntities(this, area)) {
            if (entity == owner || entity.isSpectator()) {
                continue;
            }
            Vec3 away = entity.position().subtract(position());
            entity.push(away.x * strength, 0.0D, away.z * strength);
            entity.hurtMarked = true;
            if (entity instanceof LivingEntity) {
                entity.hurt(damage(), 4.0F);
            }
        }
    }

    /** 2.0.0 used generic damage. Thrown damage names the thrower, so kills and claims count for them. */
    private DamageSource damage() {
        return damageSources().thrown(this, getOwner());
    }

    private Potion splashColor() {
        return switch (element) {
            case DARK -> Potions.HARMING;
            case ELECTRIC -> Potions.NIGHT_VISION;
            case FIRE -> Potions.FIRE_RESISTANCE;
            case ICE, WIND -> Potions.SWIFTNESS;
            case LIFE -> Potions.REGENERATION;
            case POISON -> Potions.POISON;
            case WATER -> Potions.WATER;
        };
    }
}
