package com.skytekx.elementarystaffs.registry;

import com.skytekx.elementarystaffs.ElementaryStaffsMod;
import com.skytekx.elementarystaffs.enchantment.MagicalPowerEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, ElementaryStaffsMod.MOD_ID);

    public static final RegistryObject<MagicalPowerEnchantment> MAGICAL_POWER = ENCHANTMENTS.register("magical_power", MagicalPowerEnchantment::new);

    private ModEnchantments() {
    }
}
