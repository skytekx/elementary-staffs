package com.skytekx.elementarystaffs.registry;

import com.skytekx.elementarystaffs.Element;
import com.skytekx.elementarystaffs.ElementaryStaffsMod;
import com.skytekx.elementarystaffs.item.DarknessStaff;
import com.skytekx.elementarystaffs.item.ElectricStaff;
import com.skytekx.elementarystaffs.item.ElementalArrowItem;
import com.skytekx.elementarystaffs.item.FireStaff;
import com.skytekx.elementarystaffs.item.GrenadeItem;
import com.skytekx.elementarystaffs.item.HealStaff;
import com.skytekx.elementarystaffs.item.IceStaff;
import com.skytekx.elementarystaffs.item.PoisonStaff;
import com.skytekx.elementarystaffs.item.StaffBase;
import com.skytekx.elementarystaffs.item.WaterStaff;
import com.skytekx.elementarystaffs.item.WindStaff;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The items of 2.0.0 with their ids, such as {@code elementary_staffs:staff/fire}. */
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ElementaryStaffsMod.MOD_ID);

    /** The essences in the order of 2.0.0. Magic essence is crafted from the other eight. */
    public static final List<String> ESSENCE_NAMES = List.of("magic", "darkness", "electric", "fire", "ice", "life", "poison", "water", "wind");

    public static final RegistryObject<Item> STAFF = ITEMS.register("staff", () -> new Item(new Item.Properties()));

    public static final RegistryObject<StaffBase> DARKNESS_STAFF = ITEMS.register("staff/darkness", DarknessStaff::new);
    public static final RegistryObject<StaffBase> ELECTRIC_STAFF = ITEMS.register("staff/electric", ElectricStaff::new);
    public static final RegistryObject<StaffBase> FIRE_STAFF = ITEMS.register("staff/fire", FireStaff::new);
    public static final RegistryObject<StaffBase> ICE_STAFF = ITEMS.register("staff/ice", IceStaff::new);
    public static final RegistryObject<StaffBase> HEAL_STAFF = ITEMS.register("staff/heal", HealStaff::new);
    public static final RegistryObject<StaffBase> POISON_STAFF = ITEMS.register("staff/poison", PoisonStaff::new);
    public static final RegistryObject<StaffBase> WATER_STAFF = ITEMS.register("staff/water", WaterStaff::new);
    public static final RegistryObject<StaffBase> WIND_STAFF = ITEMS.register("staff/wind", WindStaff::new);

    public static final List<RegistryObject<StaffBase>> STAFFS = List.of(DARKNESS_STAFF, ELECTRIC_STAFF, FIRE_STAFF, ICE_STAFF,
            HEAL_STAFF, POISON_STAFF, WATER_STAFF, WIND_STAFF);

    /** Items that only give the staff projectiles their look. No tab lists them. */
    public static final RegistryObject<Item> VAMPIRE = ITEMS.register("special/vampire", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HEARTH = ITEMS.register("special/hearth", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BUBBLE = ITEMS.register("special/bubble", () -> new Item(new Item.Properties()));

    public static final Map<String, RegistryObject<Item>> ESSENCES = new LinkedHashMap<>();
    public static final Map<Element, RegistryObject<ElementalArrowItem>> ARROWS = new EnumMap<>(Element.class);
    public static final Map<Element, RegistryObject<GrenadeItem>> GRENADES = new EnumMap<>(Element.class);

    static {
        for (String name : ESSENCE_NAMES) {
            ESSENCES.put(name, ITEMS.register("essence/" + name, () -> new Item(new Item.Properties())));
        }
        for (Element element : Element.values()) {
            ARROWS.put(element, ITEMS.register("arrow/" + element.id(), () -> new ElementalArrowItem(element)));
        }
        for (Element element : Element.values()) {
            GRENADES.put(element, ITEMS.register("grenade/" + element.id(), () -> new GrenadeItem(element)));
        }
    }

    private ModItems() {
    }

    public static Item essence(String name) {
        return ESSENCES.get(name).get();
    }
}
