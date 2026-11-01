package com.skytekx.elementarystaffs.registry;

import com.skytekx.elementarystaffs.ElementaryStaffsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ElementaryStaffsMod.MOD_ID);

    public static final RegistryObject<SoundEvent> MAGIC = register("magic");
    public static final RegistryObject<SoundEvent> WIND = register("wind");

    private ModSounds() {
    }

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ElementaryStaffsMod.MOD_ID, name)));
    }
}
