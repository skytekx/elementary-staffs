package com.skytekx.elementarystaffs.client;

import com.skytekx.elementarystaffs.ElementaryStaffsMod;
import com.skytekx.elementarystaffs.registry.ModEntities;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** The renderers. Forge loads this class on the client only. */
@Mod.EventBusSubscriber(modid = ElementaryStaffsMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientSetup {
    private ClientSetup() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.VAMPIRE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.HEARTH.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.BUBBLE.get(), BubbleRenderer::new);
        // The whirlwind shows only its particles.
        event.registerEntityRenderer(ModEntities.WIND.get(), NoopRenderer::new);
        ModEntities.GRENADES.values().forEach(type -> event.registerEntityRenderer(type.get(), ThrownItemRenderer::new));
        ModEntities.ARROWS.values().forEach(type -> event.registerEntityRenderer(type.get(), ElementalArrowRenderer::new));
    }
}
