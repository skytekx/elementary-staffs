package com.skytekx.elementarystaffs.gametest;

import com.mojang.authlib.GameProfile;
import com.skytekx.elementarystaffs.Element;
import com.skytekx.elementarystaffs.ElementaryStaffsMod;
import com.skytekx.elementarystaffs.StaffsConfig;
import com.skytekx.elementarystaffs.entity.BubbleEntity;
import com.skytekx.elementarystaffs.entity.ElementalArrow;
import com.skytekx.elementarystaffs.entity.ElementalGrenade;
import com.skytekx.elementarystaffs.entity.HearthEntity;
import com.skytekx.elementarystaffs.entity.VampireEntity;
import com.skytekx.elementarystaffs.entity.WindEntity;
import com.skytekx.elementarystaffs.item.GrenadeItem;
import com.skytekx.elementarystaffs.item.StaffBase;
import com.skytekx.elementarystaffs.loot.LootInjector;
import com.skytekx.elementarystaffs.registry.ModEnchantments;
import com.skytekx.elementarystaffs.registry.ModEntities;
import com.skytekx.elementarystaffs.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * Server side tests of the port. They run on the Forge game test server with
 * {@code ./gradlew runGameTestServer}, which exits with the number of failed tests. Every test
 * gets a 15 by 15 stone floor at relative height 1, so effects with a range of up to 6.5 blocks
 * stay inside it.
 */
@GameTestHolder(ElementaryStaffsMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StaffGameTests {
    private static final String ARENA = "arena";
    /** Where the player stands, looking south towards {@link #TARGET}. */
    private static final Vec3 PLAYER = new Vec3(7.5D, 2.0D, 4.5D);
    private static final Vec3 TARGET = new Vec3(7.5D, 2.0D, 8.5D);

    private StaffGameTests() {
    }

    // Helpers

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private static FakePlayer player(GameTestHelper helper, Item held) {
        UUID id = UUID.randomUUID();
        FakePlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(id, "es" + id.toString().substring(0, 8)));
        Vec3 position = helper.absoluteVec(PLAYER);
        player.moveTo(position.x, position.y, position.z, 0.0F, 0.0F);
        player.setYHeadRot(0.0F);
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
        player.setHealth(player.getMaxHealth());
        if (held != null) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(held));
        }
        return player;
    }

    private static Pig pig(GameTestHelper helper, Vec3 relative) {
        return helper.spawnWithNoFreeWill(EntityType.PIG, relative);
    }

    private static void aim(FakePlayer player, Pig pig) {
        player.lookAt(EntityAnchorArgument.Anchor.EYES, pig.getBoundingBox().getCenter());
    }

    private static ItemStack use(GameTestHelper helper, FakePlayer player) {
        ItemStack stack = player.getMainHandItem();
        stack.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        return stack;
    }

    private static <T extends net.minecraft.world.entity.Entity> List<T> around(GameTestHelper helper, Class<T> type) {
        Vec3 center = helper.absoluteVec(new Vec3(7.5D, 3.0D, 7.5D));
        return helper.getLevel().getEntitiesOfClass(type, new AABB(center, center).inflate(8.0D, 6.0D, 8.0D));
    }

    private static Optional<ItemStack> craft(ServerLevel level, ItemStack... grid) {
        NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
        for (int i = 0; i < grid.length; i++) {
            items.set(i, grid[i].copy());
        }
        // The container is never changed here, so it needs no menu.
        TransientCraftingContainer container = new TransientCraftingContainer(null, 3, 3, items);
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, container, level)
                .map(recipe -> recipe.assemble(container, level.registryAccess()));
    }

    private static void expectCraft(ServerLevel level, Item result, int count, ItemStack... grid) {
        Optional<ItemStack> crafted = craft(level, grid);
        check(crafted.isPresent(), "No recipe makes " + result);
        check(crafted.get().is(result) && crafted.get().getCount() == count,
                "Expected " + count + " " + result + ", got " + crafted.get());
    }

    private static ItemStack[] ring(Item outer, Item center) {
        ItemStack o = new ItemStack(outer);
        return new ItemStack[]{o, o, o, o, new ItemStack(center), o, o, o, o};
    }

    /** Spawns an arrow 1.5 blocks in front of the pig, flying straight at it. */
    private static ElementalArrow shootAt(GameTestHelper helper, Pig pig, Element element) {
        Vec3 target = pig.getBoundingBox().getCenter();
        Vec3 start = target.add(0.0D, 0.0D, -1.5D);
        ElementalArrow arrow = new ElementalArrow(helper.getLevel(), start.x, start.y, start.z, element);
        arrow.shoot(0.0D, 0.0D, 1.0D, 2.0F, 0.0F);
        helper.getLevel().addFreshEntity(arrow);
        return arrow;
    }

    /** Drops a grenade onto the floor right next to the pig. */
    private static ElementalGrenade dropGrenade(GameTestHelper helper, Pig pig, Element element) {
        Vec3 start = pig.position().add(1.0D, 2.0D, 0.0D);
        ElementalGrenade grenade = new ElementalGrenade(helper.getLevel(), start.x, start.y, start.z, element);
        grenade.setDeltaMovement(0.0D, -0.6D, 0.0D);
        helper.getLevel().addFreshEntity(grenade);
        return grenade;
    }

    // Registration, recipes and loot

    @GameTest(template = ARENA)
    public static void allItemsAreRegistered(GameTestHelper helper) {
        check(ModItems.STAFFS.size() == 8, "Expected 8 elemental staffs");
        check(ModItems.ESSENCES.size() == 9, "Expected 9 essences");
        check(ModItems.ARROWS.size() == 8, "Expected 8 arrows");
        check(ModItems.GRENADES.size() == 8, "Expected 8 grenades");
        check(ModEnchantments.MAGICAL_POWER.get().getMaxLevel() == 3, "Magical Power should go up to III");
        for (Element element : Element.values()) {
            check(new ItemStack(ModItems.ARROWS.get(element).get()).is(net.minecraft.tags.ItemTags.ARROWS),
                    "The " + element.id() + " arrow is not in minecraft:arrows");
        }
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void recipesAreLoaded(GameTestHelper helper) {
        List<ResourceLocation> own = new ArrayList<>();
        for (Recipe<?> recipe : helper.getLevel().getRecipeManager().getRecipes()) {
            if (recipe.getId().getNamespace().equals(ElementaryStaffsMod.MOD_ID)) {
                own.add(recipe.getId());
            }
        }
        check(own.size() == 26, "Expected the 26 recipes of 2.0.0, found " + own.size() + ": " + own);
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void recipesCraftEveryItem(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        expectCraft(level, ModItems.STAFF.get(), 1,
                new ItemStack(ModItems.essence("magic")), new ItemStack(Items.STICK), new ItemStack(Items.DIAMOND));
        List<ItemStack> elements = new ArrayList<>();
        for (String name : ModItems.ESSENCE_NAMES) {
            if (!name.equals("magic")) {
                elements.add(new ItemStack(ModItems.essence(name)));
            }
        }
        expectCraft(level, ModItems.essence("magic"), 1, elements.toArray(ItemStack[]::new));
        for (var staff : ModItems.STAFFS) {
            expectCraft(level, staff.get(), 1, new ItemStack(ModItems.STAFF.get()), new ItemStack(ModItems.essence(staff.get().essence())));
        }
        for (Element element : Element.values()) {
            Item essence = ModItems.essence(element.essence());
            expectCraft(level, ModItems.ARROWS.get(element).get(), 8, ring(Items.ARROW, essence));
            expectCraft(level, ModItems.GRENADES.get(element).get(), 8, ring(Items.GLASS_BOTTLE, essence));
        }
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void chestLootGetsTheEssencePool(GameTestHelper helper) {
        var lootData = helper.getLevel().getServer().getLootData();
        check(lootData.getLootTable(BuiltInLootTables.SIMPLE_DUNGEON).getPool(LootInjector.POOL_NAME) != null,
                "The dungeon chest has no essence pool");
        check(lootData.getLootTable(BuiltInLootTables.VILLAGE_WEAPONSMITH).getPool(LootInjector.POOL_NAME) != null,
                "The weaponsmith chest has no essence pool");
        check(lootData.getLootTable(LootInjector.ESSENCES).getPool(LootInjector.POOL_NAME) == null,
                "The essence table refers to itself");
        check(lootData.getLootTable(BuiltInLootTables.FISHING).getPool(LootInjector.POOL_NAME) == null,
                "The fishing table got the essence pool");
        LootTable essences = lootData.getLootTable(LootInjector.ESSENCES);
        LootParams params = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, helper.absoluteVec(PLAYER))
                .create(LootContextParamSets.CHEST);
        boolean found = false;
        for (int i = 0; i < 100 && !found; i++) {
            for (ItemStack stack : essences.getRandomItems(params)) {
                check(ModItems.ESSENCES.values().stream().anyMatch(essence -> stack.is(essence.get())), "Not an essence: " + stack);
                found = true;
            }
        }
        check(found, "The essence table gave nothing in 100 rolls");
        helper.succeed();
    }

    // Durability, repair and enchantment

    @GameTest(template = ARENA)
    public static void staffsKeepTheirDurabilityAndRepairItems(GameTestHelper helper) {
        Map<String, Integer> durability = Map.of("darkness", 100, "electric", 100, "fire", 400, "life", 1024,
                "ice", 1024, "poison", 1024, "water", 400, "wind", 150);
        for (var entry : ModItems.STAFFS) {
            StaffBase staff = entry.get();
            ItemStack stack = new ItemStack(staff);
            check(stack.getMaxDamage() == durability.get(staff.essence()), staff + " has durability " + stack.getMaxDamage());
            check(staff.isValidRepairItem(stack, new ItemStack(ModItems.essence(staff.essence()))), staff + " is not repaired by its essence");
            check(!staff.isValidRepairItem(stack, new ItemStack(ModItems.essence("magic"))), staff + " is repaired by magic essence");
        }
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void magicalPowerOnlyGoesOnStaffs(GameTestHelper helper) {
        var power = ModEnchantments.MAGICAL_POWER.get();
        for (var staff : ModItems.STAFFS) {
            check(power.canEnchant(new ItemStack(staff.get())), "Magical Power does not go on " + staff.get());
        }
        check(!power.canEnchant(new ItemStack(Items.DIAMOND_SWORD)), "Magical Power goes on a sword");
        check(!power.canApplyAtEnchantingTable(new ItemStack(Items.DIAMOND_PICKAXE)), "The table offers Magical Power for a pickaxe");
        check(!power.canEnchant(new ItemStack(ModItems.STAFF.get())), "Magical Power goes on the plain staff");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void staffCooldownFollowsTheConfig(GameTestHelper helper) {
        FakePlayer player = player(helper, ModItems.ELECTRIC_STAFF.get());
        use(helper, player);
        check(!player.getCooldowns().isOnCooldown(ModItems.ELECTRIC_STAFF.get()), "Cooldown without the setting");
        FakePlayer other = player(helper, ModItems.FIRE_STAFF.get());
        StaffsConfig.COOLDOWN_STAFFS.set(true);
        try {
            use(helper, other);
        } finally {
            StaffsConfig.COOLDOWN_STAFFS.set(false);
        }
        check(other.getCooldowns().isOnCooldown(ModItems.FIRE_STAFF.get()), "No cooldown with cooldownStaffs");
        helper.succeed();
    }

    // Staffs

    @GameTest(template = ARENA)
    public static void fireStaffBurnsWhatIsInFront(GameTestHelper helper) {
        Pig pig = pig(helper, new Vec3(7.5D, 2.0D, 7.5D));
        Pig behind = pig(helper, new Vec3(7.5D, 2.0D, 0.5D));
        FakePlayer player = player(helper, ModItems.FIRE_STAFF.get());
        ItemStack stack = use(helper, player);
        check(pig.isOnFire(), "The pig in front is not burning");
        check(!behind.isOnFire(), "The pig behind the player is burning");
        check(stack.getDamageValue() == 1, "The staff took " + stack.getDamageValue() + " damage");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void magicalPowerLetsTheFireStaffHurt(GameTestHelper helper) {
        Pig plain = pig(helper, new Vec3(7.5D, 2.0D, 7.5D));
        FakePlayer player = player(helper, ModItems.FIRE_STAFF.get());
        use(helper, player);
        check(plain.getHealth() == plain.getMaxHealth(), "The fire staff hurt without Magical Power");
        plain.clearFire();
        plain.invulnerableTime = 0;
        ItemStack stack = player.getMainHandItem();
        stack.enchant(ModEnchantments.MAGICAL_POWER.get(), 2);
        use(helper, player);
        check(plain.getHealth() < plain.getMaxHealth(), "Magical Power II did not add damage");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void iceStaffSlowsWhatIsAround(GameTestHelper helper) {
        Pig near = pig(helper, new Vec3(3.5D, 2.0D, 4.5D));
        Pig far = pig(helper, new Vec3(14.5D, 2.0D, 14.5D));
        FakePlayer player = player(helper, ModItems.ICE_STAFF.get());
        ItemStack stack = use(helper, player);
        check(near.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "The near pig is not slowed");
        check(near.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getDuration() <= 70, "Slowness lasts too long");
        check(!far.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "The far pig is slowed");
        check(stack.getDamageValue() == 1, "The staff took " + stack.getDamageValue() + " damage");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void poisonStaffPoisonsWhatIsAround(GameTestHelper helper) {
        Pig near = pig(helper, new Vec3(11.5D, 2.0D, 4.5D));
        FakePlayer player = player(helper, ModItems.POISON_STAFF.get());
        ItemStack stack = use(helper, player);
        check(near.hasEffect(MobEffects.POISON), "The pig is not poisoned");
        check(near.getEffect(MobEffects.POISON).getAmplifier() == 1, "Poison should be level II");
        check(stack.getDamageValue() == 1, "The staff took " + stack.getDamageValue() + " damage");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void areaStaffWithoutTargetsCostsNothing(GameTestHelper helper) {
        FakePlayer player = player(helper, ModItems.ICE_STAFF.get());
        ItemStack stack = use(helper, player);
        check(stack.getDamageValue() == 0, "The staff took damage without a target");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void electricStaffLaunchesThePlayer(GameTestHelper helper) {
        FakePlayer player = player(helper, ModItems.ELECTRIC_STAFF.get());
        player.setXRot(-45.0F);
        ItemStack stack = use(helper, player);
        Vec3 motion = player.getDeltaMovement();
        check(motion.length() > 0.99D && motion.y > 0.5D && motion.z > 0.5D, "Unexpected launch " + motion);
        check(player.fallDistance == -25.0F, "The next fall is not caught");
        check(stack.getDamageValue() == 1, "The staff took " + stack.getDamageValue() + " damage");
        helper.succeed();
    }

    @GameTest(template = ARENA, timeoutTicks = 200)
    public static void darknessStaffDrainsLifeBackToThePlayer(GameTestHelper helper) {
        Pig pig = pig(helper, TARGET);
        FakePlayer player = player(helper, ModItems.DARKNESS_STAFF.get());
        player.setHealth(10.0F);
        aim(player, pig);
        ItemStack stack = use(helper, player);
        check(stack.getDamageValue() == 1, "The staff took " + stack.getDamageValue() + " damage");
        check(!around(helper, VampireEntity.class).isEmpty(), "No vampire ball was shot");
        AtomicBoolean sawHearth = new AtomicBoolean();
        helper.onEachTick(() -> {
            if (!around(helper, HearthEntity.class).isEmpty()) {
                sawHearth.set(true);
            }
        });
        helper.succeedWhen(() -> {
            check(pig.getHealth() == pig.getMaxHealth() - 2.0F, "The pig lost " + (pig.getMaxHealth() - pig.getHealth()));
            check(sawHearth.get(), "No heal ball flew back");
            check(player.getHealth() == 12.0F, "The player has " + player.getHealth() + " health");
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 300)
    public static void waterStaffLiftsTheTargetInABubble(GameTestHelper helper) {
        Pig pig = pig(helper, TARGET);
        FakePlayer player = player(helper, ModItems.WATER_STAFF.get());
        aim(player, pig);
        ItemStack stack = use(helper, player);
        check(stack.getDamageValue() == 1, "The staff took " + stack.getDamageValue() + " damage");
        double startY = pig.getY();
        AtomicBoolean rode = new AtomicBoolean();
        double[] highest = {startY};
        helper.onEachTick(() -> {
            if (pig.getVehicle() instanceof BubbleEntity) {
                rode.set(true);
                highest[0] = Math.max(highest[0], pig.getY());
            }
        });
        helper.succeedWhen(() -> {
            check(rode.get(), "The pig never rode a bubble");
            check(!pig.isPassenger(), "The bubble has not popped yet");
            check(highest[0] > startY + 2.0D, "The pig rose only to " + (highest[0] - startY));
            check(around(helper, BubbleEntity.class).isEmpty(), "The bubble is still there");
        });
    }

    // The whirlwind drifts up to 9 blocks and pulls from 4 more, so it runs alone.
    @GameTest(template = ARENA, timeoutTicks = 200, batch = "whirlwind")
    public static void windStaffWhirlwindPullsAndHurts(GameTestHelper helper) {
        Pig pig = pig(helper, new Vec3(7.5D, 2.0D, 6.5D));
        FakePlayer player = player(helper, ModItems.WIND_STAFF.get());
        player.setXRot(20.0F);
        ItemStack stack = use(helper, player);
        check(stack.getDamageValue() == 1, "The staff took " + stack.getDamageValue() + " damage");
        check(!around(helper, WindEntity.class).isEmpty(), "No whirlwind was made");
        helper.succeedWhen(() -> check(pig.getHealth() < pig.getMaxHealth(), "The whirlwind did not hurt the pig"));
    }

    @GameTest(template = ARENA)
    public static void healStaffHealsInsteadOfHurting(GameTestHelper helper) {
        Pig pig = pig(helper, new Vec3(7.5D, 2.0D, 6.0D));
        pig.setHealth(4.0F);
        FakePlayer player = player(helper, ModItems.HEAL_STAFF.get());
        player.attack(pig);
        check(pig.getHealth() == 6.0F, "The pig has " + pig.getHealth() + " health");
        check(pig.hurtTime == 0, "The pig was hurt");
        check(player.getMainHandItem().getDamageValue() == 1, "The staff took no damage");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void staffHitsApplyTheirElement(GameTestHelper helper) {
        Pig burning = pig(helper, new Vec3(5.5D, 2.0D, 6.0D));
        Pig slowed = pig(helper, new Vec3(7.5D, 2.0D, 6.0D));
        Pig poisoned = pig(helper, new Vec3(9.5D, 2.0D, 6.0D));
        FakePlayer fire = player(helper, ModItems.FIRE_STAFF.get());
        fire.attack(burning);
        FakePlayer ice = player(helper, ModItems.ICE_STAFF.get());
        ice.attack(slowed);
        FakePlayer poison = player(helper, ModItems.POISON_STAFF.get());
        poison.attack(poisoned);
        check(burning.isOnFire() && burning.getHealth() < burning.getMaxHealth(), "The fire staff hit did not burn and hurt");
        check(slowed.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && slowed.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier() == 1,
                "The ice staff hit did not slow");
        check(poisoned.hasEffect(MobEffects.POISON), "The poison staff hit did not poison");
        for (FakePlayer player : List.of(fire, ice, poison)) {
            check(player.getMainHandItem().getDamageValue() == 1, player.getMainHandItem() + " took no damage from the hit");
        }
        helper.succeed();
    }

    // Arrows

    @GameTest(template = ARENA)
    public static void bowsShootElementalArrows(GameTestHelper helper) {
        for (Element element : Element.values()) {
            FakePlayer player = player(helper, Items.BOW);
            player.getInventory().add(new ItemStack(ModItems.ARROWS.get(element).get(), 2));
            ItemStack bow = player.getMainHandItem();
            List<ElementalArrow> before = around(helper, ElementalArrow.class);
            bow.releaseUsing(helper.getLevel(), player, bow.getUseDuration() - 20);
            List<ElementalArrow> after = around(helper, ElementalArrow.class);
            after.removeAll(before);
            check(after.size() == 1, "The bow shot " + after.size() + " arrows for " + element.id());
            ElementalArrow arrow = after.get(0);
            check(arrow.element() == element && arrow.getType() == ModEntities.ARROWS.get(element).get(),
                    "The bow shot a " + arrow.element() + " arrow for " + element.id());
            check(player.getInventory().countItem(ModItems.ARROWS.get(element).get()) == 1, "The bow did not use up the arrow");
            arrow.discard();
        }
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void dispensersShootElementalArrows(GameTestHelper helper) {
        BlockPos pos = new BlockPos(7, 2, 3);
        helper.setBlock(pos, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.SOUTH));
        DispenserBlockEntity dispenser = (DispenserBlockEntity) helper.getBlockEntity(pos);
        dispenser.setItem(0, new ItemStack(ModItems.ARROWS.get(Element.FIRE).get()));
        helper.setBlock(pos.above(), Blocks.REDSTONE_BLOCK);
        helper.succeedWhen(() -> {
            List<ElementalArrow> arrows = around(helper, ElementalArrow.class);
            check(arrows.size() == 1, "Found " + arrows.size() + " arrows");
            check(arrows.get(0).element() == Element.FIRE, "Wrong arrow " + arrows.get(0).element());
            check(arrows.get(0).pickup == AbstractArrow.Pickup.ALLOWED, "A dispensed arrow should be pickable");
            check(dispenser.isEmpty(), "The dispenser still holds the arrow");
        });
    }

    @GameTest(template = ARENA, timeoutTicks = 100)
    public static void arrowsApplyTheirElement(GameTestHelper helper) {
        Map<Element, Pig> pigs = new java.util.EnumMap<>(Element.class);
        int i = 0;
        for (Element element : Element.values()) {
            Pig pig = pig(helper, new Vec3(1.5D + (i % 4) * 4.0D, 2.0D, 4.5D + (i / 4) * 6.0D));
            pigs.put(element, pig);
            i++;
        }
        pigs.get(Element.WATER).setSecondsOnFire(10);
        pigs.get(Element.ELECTRIC).addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.CONFUSION, 200));
        Map<Element, Double> startZ = new java.util.EnumMap<>(Element.class);
        pigs.forEach((element, pig) -> {
            startZ.put(element, pig.getZ());
            shootAt(helper, pig, element);
        });
        AtomicBoolean lifeHurt = new AtomicBoolean();
        AtomicBoolean blinded = new AtomicBoolean();
        helper.onEachTick(() -> {
            if (pigs.get(Element.LIFE).hurtTime > 0) {
                lifeHurt.set(true);
            }
            // Blindness lasts 80 ticks, which may end before the slower checks pass.
            if (pigs.get(Element.DARK).hasEffect(MobEffects.BLINDNESS)) {
                blinded.set(true);
            }
        });
        helper.succeedWhen(() -> {
            check(blinded.get(), "No blindness from the dark arrow");
            check(pigs.get(Element.ELECTRIC).getHealth() < pigs.get(Element.ELECTRIC).getMaxHealth() - 5.0F,
                    "The electric arrow did too little against a confused pig: " + pigs.get(Element.ELECTRIC).getHealth());
            check(pigs.get(Element.FIRE).isOnFire(), "The fire arrow did not burn");
            check(pigs.get(Element.ICE).hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "No slowness from the ice arrow");
            Pig life = pigs.get(Element.LIFE);
            check(lifeHurt.get() && life.getHealth() == life.getMaxHealth(), "The life arrow did not heal back what it hurt: " + life.getHealth());
            check(pigs.get(Element.POISON).hasEffect(MobEffects.POISON), "No poison from the poison arrow");
            check(pigs.get(Element.WATER).hasEffect(MobEffects.CONFUSION) && !pigs.get(Element.WATER).isOnFire(),
                    "The water arrow did not confuse and put out the pig");
            Pig wind = pigs.get(Element.WIND);
            check(wind.getZ() - startZ.get(Element.WIND) > 1.0D, "The wind arrow pushed the pig by " + (wind.getZ() - startZ.get(Element.WIND)));
        });
    }

    // Grenades

    @GameTest(template = ARENA)
    public static void grenadesAreThrownByHolding(GameTestHelper helper) {
        FakePlayer player = player(helper, ModItems.GRENADES.get(Element.FIRE).get());
        player.getMainHandItem().setCount(3);
        ItemStack stack = player.getMainHandItem();
        stack.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        check(player.isUsingItem(), "The grenade is not drawn");
        stack.releaseUsing(helper.getLevel(), player, stack.getUseDuration() - 30);
        List<ElementalGrenade> grenades = around(helper, ElementalGrenade.class);
        check(grenades.size() == 1, "Found " + grenades.size() + " grenades");
        check(Math.abs(grenades.get(0).getDeltaMovement().length() - GrenadeItem.speed(30)) < 0.01D,
                "Unexpected speed " + grenades.get(0).getDeltaMovement().length());
        check(stack.getCount() == 2, "The throw did not use up a grenade");
        check(GrenadeItem.speed(100000) == GrenadeItem.MAX_SPEED, "The throw speed has no limit");
        grenades.get(0).discard();
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void grenadesBurstOnImpact(GameTestHelper helper) {
        Pig pig = pig(helper, TARGET);
        ElementalGrenade grenade = dropGrenade(helper, pig, Element.POISON);
        helper.succeedWhen(() -> {
            check(grenade.isRemoved(), "The grenade did not burst");
            check(pig.hasEffect(MobEffects.POISON), "The pig is not poisoned");
        });
    }

    @GameTest(template = ARENA)
    public static void grenadesSpareTheirThrower(GameTestHelper helper) {
        Pig target = pig(helper, TARGET);
        Pig thrower = pig(helper, TARGET.add(2.0D, 0.0D, 0.0D));
        ElementalGrenade grenade = new ElementalGrenade(helper.getLevel(), target.getX(), target.getY() + 0.5D, target.getZ(), Element.DARK);
        grenade.setOwner(thrower);
        grenade.burst();
        check(target.hasEffect(MobEffects.BLINDNESS) && target.getHealth() < target.getMaxHealth(), "The target was spared");
        check(!thrower.hasEffect(MobEffects.BLINDNESS) && thrower.getHealth() == thrower.getMaxHealth(), "The thrower was hit");
        helper.succeed();
    }

    @GameTest(template = ARENA)
    public static void everyGrenadeAppliesItsElement(GameTestHelper helper) {
        Map<Element, Pig> pigs = new java.util.EnumMap<>(Element.class);
        Map<Element, ItemEntity> items = new java.util.EnumMap<>(Element.class);
        for (Element element : Element.values()) {
            // Each grenade bursts alone in the middle of the arena, so the pigs do not share effects.
            Pig pig = pig(helper, TARGET);
            pigs.put(element, pig);
            if (element == Element.LIFE) {
                pig.setHealth(4.0F);
            }
            if (element == Element.WATER) {
                pig.setSecondsOnFire(10);
            }
            if (element == Element.ELECTRIC) {
                pig.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.CONFUSION, 200));
            }
            ItemEntity item = null;
            if (element == Element.WIND) {
                item = new ItemEntity(helper.getLevel(), pig.getX() + 2.0D, pig.getY(), pig.getZ(), new ItemStack(Items.STONE));
                helper.getLevel().addFreshEntity(item);
                items.put(element, item);
            }
            ElementalGrenade grenade = new ElementalGrenade(helper.getLevel(), pig.getX(), pig.getY() + 0.5D, pig.getZ(), element);
            grenade.burst();
            check(checkGrenade(element, pig, item), "The " + element.id() + " grenade did not work");
            pig.discard();
            if (item != null) {
                item.discard();
            }
        }
        helper.succeed();
    }

    private static boolean checkGrenade(Element element, Pig pig, ItemEntity item) {
        float lost = pig.getMaxHealth() - pig.getHealth();
        return switch (element) {
            case DARK -> pig.hasEffect(MobEffects.BLINDNESS) && lost == 8.0F;
            case ELECTRIC -> lost == 8.0F && !pig.hasEffect(MobEffects.CONFUSION);
            case FIRE -> pig.isOnFire();
            case ICE -> pig.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && lost == 8.0F;
            case LIFE -> pig.hasEffect(MobEffects.REGENERATION) && pig.getHealth() == 5.5F;
            case POISON -> pig.hasEffect(MobEffects.POISON);
            case WATER -> pig.hasEffect(MobEffects.CONFUSION) && !pig.isOnFire() && lost == 8.0F;
            case WIND -> lost == 4.0F && item.getDeltaMovement().x > 1.0D;
        };
    }
}
