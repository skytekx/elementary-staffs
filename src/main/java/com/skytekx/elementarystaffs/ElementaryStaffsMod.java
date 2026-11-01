package com.skytekx.elementarystaffs;

import com.skytekx.elementarystaffs.entity.ElementalArrow;
import com.skytekx.elementarystaffs.loot.LootInjector;
import com.skytekx.elementarystaffs.registry.ModEnchantments;
import com.skytekx.elementarystaffs.registry.ModEntities;
import com.skytekx.elementarystaffs.registry.ModItems;
import com.skytekx.elementarystaffs.registry.ModSounds;
import com.skytekx.elementarystaffs.registry.ModTabs;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ElementaryStaffsMod.MOD_ID)
public final class ElementaryStaffsMod {
    public static final String MOD_ID = "elementary_staffs";

    // FMLJavaModLoadingContext.get() and new ResourceLocation(...) still work on every Forge 47
    // build. Their replacements only exist in newer builds than some servers run.
    @SuppressWarnings("removal")
    public ElementaryStaffsMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModEnchantments.ENCHANTMENTS.register(modBus);
        ModSounds.SOUND_EVENTS.register(modBus);
        ModTabs.TABS.register(modBus);
        modBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.addListener(LootInjector::onLootTableLoad);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, StaffsConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // The dispenser registry is a plain map, so it is filled on the main thread.
        event.enqueueWork(() -> {
            for (Element element : Element.values()) {
                DispenserBlock.registerBehavior(ModItems.ARROWS.get(element).get(), new AbstractProjectileDispenseBehavior() {
                    @Override
                    protected Projectile getProjectile(Level level, Position position, ItemStack stack) {
                        ElementalArrow arrow = new ElementalArrow(level, position.x(), position.y(), position.z(), element);
                        arrow.pickup = AbstractArrow.Pickup.ALLOWED;
                        return arrow;
                    }
                });
            }
        });
    }
}
