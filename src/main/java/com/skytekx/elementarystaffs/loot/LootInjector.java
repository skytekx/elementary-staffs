package com.skytekx.elementarystaffs.loot;

import com.skytekx.elementarystaffs.ElementaryStaffsMod;
import com.skytekx.elementarystaffs.StaffsConfig;
import java.util.Locale;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.event.LootTableLoadEvent;

/**
 * Adds the essence pool of 2.0.0 to every chest loot table that comes from the game or a mod jar.
 * Forge fires the event only for those, not for tables that a data pack replaces, which matches
 * the check for built in tables in 2.0.0.
 */
public final class LootInjector {
    public static final String POOL_NAME = ElementaryStaffsMod.MOD_ID + ":essences";
    public static final ResourceLocation ESSENCES = new ResourceLocation(ElementaryStaffsMod.MOD_ID, "chests/essences");
    public static final ResourceLocation MAGICAL_POWER = new ResourceLocation(ElementaryStaffsMod.MOD_ID, "chests/magical_power");

    private LootInjector() {
    }

    public static boolean appliesTo(ResourceLocation table) {
        String namespace = table.getNamespace().toLowerCase(Locale.ROOT);
        // The own tables are left out, so the essence table does not refer to itself.
        if (namespace.equals(ElementaryStaffsMod.MOD_ID) || StaffsConfig.BLACKLISTED_MOD_IDS.get().contains(namespace)) {
            return false;
        }
        return table.getPath().toLowerCase(Locale.ROOT).contains("chests/");
    }

    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (!StaffsConfig.DUNGEON_LOOT.get() || !appliesTo(event.getName())) {
            return;
        }
        LootPool.Builder pool = LootPool.lootPool()
                .name(POOL_NAME)
                .setRolls(ConstantValue.exactly(1.0F))
                .when(LootItemRandomChanceCondition.randomChance(1.0F))
                .add(LootTableReference.lootTableReference(ESSENCES).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))));
        if (StaffsConfig.MAGICAL_POWER_ENCHANTMENT.get()) {
            pool.add(LootTableReference.lootTableReference(MAGICAL_POWER).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))));
        }
        event.getTable().addPool(pool.build());
    }
}
