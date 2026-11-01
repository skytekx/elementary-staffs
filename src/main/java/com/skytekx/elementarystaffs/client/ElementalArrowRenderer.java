package com.skytekx.elementarystaffs.client;

import com.skytekx.elementarystaffs.Element;
import com.skytekx.elementarystaffs.ElementaryStaffsMod;
import com.skytekx.elementarystaffs.entity.ElementalArrow;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public final class ElementalArrowRenderer extends ArrowRenderer<ElementalArrow> {
    private static final Map<Element, ResourceLocation> TEXTURES = new EnumMap<>(Element.class);

    static {
        for (Element element : Element.values()) {
            TEXTURES.put(element, new ResourceLocation(ElementaryStaffsMod.MOD_ID, "textures/entity/arrow/" + element.id() + ".png"));
        }
    }

    public ElementalArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ElementalArrow arrow) {
        return TEXTURES.get(arrow.element());
    }
}
