package com.skytekx.elementarystaffs;

import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * The settings of 2.0.0 with their defaults. They live in the common config file
 * {@code config/elementary_staffs-common.toml}, which is loaded before the loot tables.
 */
public final class StaffsConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue DUNGEON_LOOT;
    public static final ForgeConfigSpec.BooleanValue MAGICAL_POWER_ENCHANTMENT;
    public static final ForgeConfigSpec.BooleanValue COOLDOWN_STAFFS;
    public static final ForgeConfigSpec.BooleanValue COOLDOWN_GRENADES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_MOD_IDS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        DUNGEON_LOOT = builder.comment("Should the essences be dungeon loot?")
                .define("dungeonLoot", true);
        MAGICAL_POWER_ENCHANTMENT = builder.comment("Do you want magical power enchantments?")
                .define("magicalPowerEnchantment", true);
        COOLDOWN_STAFFS = builder.comment("Should there be a cooldown on the staffs?")
                .define("cooldownStaffs", false);
        COOLDOWN_GRENADES = builder.comment("Should there be a dynamic cooldown on the grenades?")
                .define("cooldownGrenades", false);
        BLACKLISTED_MOD_IDS = builder.comment("Mod IDs whose chest loot tables get no essences.")
                .defineListAllowEmpty(List.of("blacklistedModIds"), List::of, value -> value instanceof String);
        SPEC = builder.build();
    }

    private StaffsConfig() {
    }
}
