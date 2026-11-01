package com.skytekx.elementarystaffs.registry;

import com.skytekx.elementarystaffs.Element;
import com.skytekx.elementarystaffs.ElementaryStaffsMod;
import com.skytekx.elementarystaffs.entity.BubbleEntity;
import com.skytekx.elementarystaffs.entity.ElementalArrow;
import com.skytekx.elementarystaffs.entity.ElementalGrenade;
import com.skytekx.elementarystaffs.entity.HearthEntity;
import com.skytekx.elementarystaffs.entity.VampireEntity;
import com.skytekx.elementarystaffs.entity.WindEntity;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The entity types of 2.0.0 with their ids, such as {@code elementary_staffs:arrow/fire}. */
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ElementaryStaffsMod.MOD_ID);

    public static final RegistryObject<EntityType<VampireEntity>> VAMPIRE = ENTITY_TYPES.register("vampire",
            () -> EntityType.Builder.<VampireEntity>of(VampireEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).build("vampire"));
    public static final RegistryObject<EntityType<HearthEntity>> HEARTH = ENTITY_TYPES.register("hearth",
            () -> EntityType.Builder.<HearthEntity>of(HearthEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).build("hearth"));
    public static final RegistryObject<EntityType<BubbleEntity>> BUBBLE = ENTITY_TYPES.register("bubble",
            () -> EntityType.Builder.<BubbleEntity>of(BubbleEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).build("bubble"));
    public static final RegistryObject<EntityType<WindEntity>> WIND = ENTITY_TYPES.register("wind",
            () -> EntityType.Builder.<WindEntity>of(WindEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).build("wind"));

    public static final Map<Element, RegistryObject<EntityType<ElementalArrow>>> ARROWS = new EnumMap<>(Element.class);
    public static final Map<Element, RegistryObject<EntityType<ElementalGrenade>>> GRENADES = new EnumMap<>(Element.class);

    static {
        for (Element element : Element.values()) {
            String name = "arrow/" + element.id();
            // The tracking settings of the vanilla arrow.
            ARROWS.put(element, ENTITY_TYPES.register(name, () -> EntityType.Builder.<ElementalArrow>of(
                            (type, level) -> new ElementalArrow(type, level, element), MobCategory.MISC)
                    .sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20).build(name)));
        }
        for (Element element : Element.values()) {
            String name = "grenade/" + element.id();
            GRENADES.put(element, ENTITY_TYPES.register(name, () -> EntityType.Builder.<ElementalGrenade>of(
                            (type, level) -> new ElementalGrenade(type, level, element), MobCategory.MISC)
                    .sized(0.5F, 0.5F).build(name)));
        }
    }

    private ModEntities() {
    }
}
