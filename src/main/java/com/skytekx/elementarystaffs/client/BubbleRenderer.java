package com.skytekx.elementarystaffs.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.skytekx.elementarystaffs.entity.BubbleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.Entity;

/** Draws the water ball larger, and large enough to hold its passengers while it carries them. */
public final class BubbleRenderer extends ThrownItemRenderer<BubbleEntity> {
    public BubbleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(BubbleEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light) {
        float scale = 1.5F;
        if (entity.isVehicle()) {
            double height = 0.0D;
            for (Entity passenger : entity.getPassengers()) {
                height += passenger.getBbHeight();
            }
            scale = 4.0F + (float) height;
        }
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        super.render(entity, yaw, partialTick, poseStack, buffer, light);
        poseStack.popPose();
    }
}
