package com.skytekx.elementarystaffs.item;

import com.skytekx.elementarystaffs.Element;
import com.skytekx.elementarystaffs.entity.ElementalArrow;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** An arrow that bows and crossbows take through the tag minecraft:arrows. */
public final class ElementalArrowItem extends ArrowItem {
    private final Element element;

    public ElementalArrowItem(Element element) {
        super(new Properties());
        this.element = element;
    }

    public Element element() {
        return element;
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity shooter) {
        return new ElementalArrow(level, shooter, element);
    }
}
