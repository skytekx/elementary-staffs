package com.skytekx.elementarystaffs.enchantment;

import com.skytekx.elementarystaffs.StaffsConfig;
import com.skytekx.elementarystaffs.item.StaffBase;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * Magical Power I to III raises the range, strength or duration of the staffs. As in 2.0.0 it only
 * goes on staffs. Its own category keeps it off swords and tools at the enchanting table.
 */
public final class MagicalPowerEnchantment extends Enchantment {
    public static final EnchantmentCategory STAFF = EnchantmentCategory.create("ELEMENTARY_STAFFS_STAFF", item -> item instanceof StaffBase);

    public MagicalPowerEnchantment() {
        super(Rarity.VERY_RARE, STAFF, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    private static boolean enabled() {
        return StaffsConfig.MAGICAL_POWER_ENCHANTMENT.get();
    }

    @Override
    public int getMinLevel() {
        return 1;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return enabled() && stack.getItem() instanceof StaffBase;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return canEnchant(stack);
    }

    @Override
    public boolean isAllowedOnBooks() {
        return enabled();
    }

    @Override
    public boolean isTradeable() {
        return enabled();
    }

    @Override
    public boolean isDiscoverable() {
        return enabled();
    }
}
