package com.skytekx.elementarystaffs.registry;

import com.skytekx.elementarystaffs.Element;
import com.skytekx.elementarystaffs.ElementaryStaffsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** The four creative tabs of 2.0.0. */
public final class ModTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ElementaryStaffsMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> STAFFS = TABS.register("staffs", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.elementary_staffs.staffs"))
            .icon(() -> new ItemStack(ModItems.STAFF.get()))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.STAFF.get());
                ModItems.STAFFS.forEach(staff -> output.accept(staff.get()));
            })
            .build());

    public static final RegistryObject<CreativeModeTab> ESSENCES = TABS.register("essences", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.elementary_staffs.essences"))
            .icon(() -> new ItemStack(ModItems.essence("magic")))
            .displayItems((parameters, output) -> ModItems.ESSENCES.values().forEach(essence -> output.accept(essence.get())))
            .build());

    public static final RegistryObject<CreativeModeTab> ARROWS = TABS.register("arrows", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.elementary_staffs.arrows"))
            .icon(() -> new ItemStack(ModItems.ARROWS.get(Element.DARK).get()))
            .displayItems((parameters, output) -> ModItems.ARROWS.values().forEach(arrow -> output.accept(arrow.get())))
            .build());

    public static final RegistryObject<CreativeModeTab> GRENADES = TABS.register("grenade", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.elementary_staffs.grenade"))
            .icon(() -> new ItemStack(ModItems.GRENADES.get(Element.DARK).get()))
            .displayItems((parameters, output) -> ModItems.GRENADES.values().forEach(grenade -> output.accept(grenade.get())))
            .build());

    private ModTabs() {
    }
}
