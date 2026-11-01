package com.skytekx.elementarystaffs;

import com.skytekx.elementarystaffs.registry.ModEnchantments;
import com.skytekx.elementarystaffs.registry.ModEntities;
import com.skytekx.elementarystaffs.registry.ModItems;
import com.skytekx.elementarystaffs.registry.ModSounds;
import com.skytekx.elementarystaffs.registry.ModTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ElementaryStaffsMod.MOD_ID)
public final class ElementaryStaffsMod {
    public static final String MOD_ID = "elementary_staffs";

    // FMLJavaModLoadingContext.get() and new ResourceLocation(...) still work on every Forge 47
    // build. Their replacements only exist in newer builds than some servers run.
    public ElementaryStaffsMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModEnchantments.ENCHANTMENTS.register(modBus);
        ModSounds.SOUND_EVENTS.register(modBus);
        ModTabs.TABS.register(modBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, StaffsConfig.SPEC);
    }
}
