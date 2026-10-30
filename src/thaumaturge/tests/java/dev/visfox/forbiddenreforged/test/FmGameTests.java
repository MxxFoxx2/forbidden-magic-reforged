package dev.visfox.forbiddenreforged.test;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.taint.ITaintBlock;
import dev.visfox.forbiddenreforged.FmAspects;
import dev.visfox.forbiddenreforged.FmDrops;
import dev.visfox.forbiddenreforged.FmEnchantmentEffects;
import dev.visfox.forbiddenreforged.FmEnchantments;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.WrathCageBlockEntity;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class FmGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
        DeferredRegister.create(Registries.TEST_FUNCTION, ForbiddenMagic.MODID);

    private static final String[] TESTS = {
        "thaumaturge_host_is_present",
        "sin_aspects_are_registered",
        "shards_are_registered",
        "forbidden_research_entry_and_category_resolve",
        "tainted_blocks_are_registered",
        "tainted_blocks_use_taint_contract",
        "simple_items_are_registered",
        "rose_and_cake_blocks_are_registered",
        "recipes_are_loaded",
        "taint_tools_are_registered",
        "drop_rules_match_original",
        "mob_crystal_and_wrath_cage_are_registered",
        "cage_rules_match_original",
        "enchantments_are_loaded",
        "enchantment_rules_match_original",
        "morph_tools_are_registered",
        "morph_state_cycles"
    };

    static {
        FUNCTIONS.register(TESTS[0], () -> FmGameTests::thaumaturgeHostIsPresent);
        FUNCTIONS.register(TESTS[1], () -> FmGameTests::sinAspectsAreRegistered);
        FUNCTIONS.register(TESTS[2], () -> FmGameTests::shardsAreRegistered);
        FUNCTIONS.register(TESTS[3], () -> FmGameTests::forbiddenResearchResolves);
        FUNCTIONS.register(TESTS[4], () -> FmGameTests::taintedBlocksAreRegistered);
        FUNCTIONS.register(TESTS[5], () -> FmGameTests::taintedBlocksUseTaintContract);
        FUNCTIONS.register(TESTS[6], () -> FmGameTests::simpleItemsAreRegistered);
        FUNCTIONS.register(TESTS[7], () -> FmGameTests::roseAndCakeBlocksAreRegistered);
        FUNCTIONS.register(TESTS[8], () -> FmGameTests::recipesAreLoaded);
        FUNCTIONS.register(TESTS[9], () -> FmGameTests::taintToolsAreRegistered);
        FUNCTIONS.register(TESTS[10], () -> FmGameTests::dropRulesMatchOriginal);
        FUNCTIONS.register(TESTS[11], () -> FmGameTests::mobCrystalAndWrathCageAreRegistered);
        FUNCTIONS.register(TESTS[12], () -> FmGameTests::cageRulesMatchOriginal);
        FUNCTIONS.register(TESTS[13], () -> FmGameTests::enchantmentsAreLoaded);
        FUNCTIONS.register(TESTS[14], () -> FmGameTests::enchantmentRulesMatchOriginal);
        FUNCTIONS.register(TESTS[15], () -> FmGameTests::morphToolsAreRegistered);
        FUNCTIONS.register(TESTS[16], () -> FmGameTests::morphStateCycles);
    }

    private FmGameTests() {
    }

    @SubscribeEvent
    static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
            ForbiddenMagic.id("default"), new TestEnvironmentDefinition.AllOf());
        for (String name : TESTS) {
            ResourceKey<Consumer<GameTestHelper>> function =
                ResourceKey.create(Registries.TEST_FUNCTION, ForbiddenMagic.id(name));
            event.registerTest(
                ForbiddenMagic.id(name),
                new FunctionGameTestInstance(function,
                    new TestData<>(environment, Identifier.parse("thaumaturge:empty_5x5x7"), 100, 0, true, Rotation.NONE))
            );
        }
    }

    private static void thaumaturgeHostIsPresent(GameTestHelper helper) {
        helper.assertTrue(ModList.get().isLoaded("thaumaturge"), "thaumaturge must be loaded");
        helper.succeed();
    }

    private static void sinAspectsAreRegistered(GameTestHelper helper) {
        HolderLookup.RegistryLookup<IAspect> aspects = aspects(helper);
        for (ResourceKey<IAspect> key : List.of(FmAspects.LUXURIA, FmAspects.INFERNUS, FmAspects.SUPERBIA,
            FmAspects.GULA, FmAspects.INVIDIA, FmAspects.DESIDIA, FmAspects.IRA)) {
            IAspect aspect = aspects.get(key).map(Holder.Reference::value).orElse(null);
            helper.assertTrue(aspect != null, "missing aspect " + key.identifier());
            helper.assertTrue(aspect.components().size() == 2, key.identifier() + " must have two components");
            helper.assertTrue(aspect.texture().getPath().endsWith(key.identifier().getPath() + ".png"),
                key.identifier() + " must use its own texture");
        }
        helper.succeed();
    }

    private static void shardsAreRegistered(GameTestHelper helper) {
        for (String vice : List.of("wrath", "envy", "taint", "pride", "lust", "sloth", "greed", "gluttony")) {
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(ForbiddenMagic.id(vice + "_shard")),
                vice + "_shard must be registered");
        }
        helper.succeed();
    }

    private static void forbiddenResearchResolves(GameTestHelper helper) {
        HolderLookup.RegistryLookup<IResearchCategory> categories =
            helper.getLevel().registryAccess().lookup(IResearchCategory.REGISTRY_KEY).orElse(null);
        helper.assertTrue(categories != null, "research category registry must exist");
        helper.assertTrue(categories.get(ResourceKey.create(IResearchCategory.REGISTRY_KEY, ForbiddenMagic.id("forbidden"))).isPresent(),
            "forbiddenmagic:forbidden category must exist");

        HolderLookup.RegistryLookup<IResearchEntry> entries =
            helper.getLevel().registryAccess().lookup(IResearchEntry.REGISTRY_KEY).orElse(null);
        helper.assertTrue(entries != null, "research entry registry must exist");
        for (String id : List.of("nether_shards", "tainted_tree", "tainted_stone", "blackflower", "arcane_cake",
            "emerald_transmutation", "taint_shovel", "taint_pickaxe", "wrath_cage", "crystalwell")) {
            helper.assertTrue(entries.get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, ForbiddenMagic.id(id))).isPresent(),
                "forbiddenmagic:" + id + " entry must exist");
        }
        helper.succeed();
    }

    private static void taintedBlocksAreRegistered(GameTestHelper helper) {
        for (String id : List.of("taint_log", "taint_planks", "taint_stone_smooth", "taint_stone_brick",
            "star_block", "taint_leaves", "taint_sapling")) {
            helper.assertTrue(BuiltInRegistries.BLOCK.containsKey(ForbiddenMagic.id(id)), id + " block must be registered");
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(ForbiddenMagic.id(id)), id + " block item must be registered");
        }
        helper.succeed();
    }

    private static void taintedBlocksUseTaintContract(GameTestHelper helper) {
        for (String id : List.of("taint_log", "taint_planks", "taint_stone_smooth", "taint_stone_brick",
            "taint_leaves", "taint_sapling")) {
            Block block = BuiltInRegistries.BLOCK.getValue(ForbiddenMagic.id(id));
            helper.assertTrue(block instanceof ITaintBlock, id + " must implement ITaintBlock");
        }
        helper.succeed();
    }

    private static void simpleItemsAreRegistered(GameTestHelper helper) {
        for (String id : List.of("emerald_fragment", "taint_coal", "taint_fruit", "black_dye", "diabolist_fork",
            "skull_axe", "riding_crop", "ring_nutrition", "crystalwell")) {
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(ForbiddenMagic.id(id)), id + " must be registered");
        }
        helper.assertTrue(dev.visfox.forbiddenreforged.RidingCropItem.dropsLustShard(1), "lust shard roll inside");
        helper.assertFalse(dev.visfox.forbiddenreforged.RidingCropItem.dropsLustShard(2), "lust shard roll outside");
        helper.succeed();
    }

    private static void roseAndCakeBlocksAreRegistered(GameTestHelper helper) {
        for (String id : List.of("umbral_rose", "umbral_rose_bush", "arcane_cake")) {
            helper.assertTrue(BuiltInRegistries.BLOCK.containsKey(ForbiddenMagic.id(id)), id + " block must be registered");
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(ForbiddenMagic.id(id)), id + " block item must be registered");
        }
        helper.succeed();
    }

    private static void recipesAreLoaded(GameTestHelper helper) {
        for (String id : List.of("taint_planks", "taint_stone_brick", "star_block", "star_from_block", "taint_coal",
            "ink_from_rose", "taint_sapling_crucible", "umbral_rose_crucible", "emerald_fragment_crucible",
            "arcane_cake_infusion", "wrath_cage_infusion", "mob_crystal_crucible", "taint_shovel_infusion",
            "taint_pickaxe_infusion")) {
            helper.assertTrue(helper.getLevel().getServer().getRecipeManager()
                .byKey(ResourceKey.create(Registries.RECIPE, ForbiddenMagic.id(id))).isPresent(), id + " recipe must load");
        }
        helper.succeed();
    }

    private static void taintToolsAreRegistered(GameTestHelper helper) {
        for (String id : List.of("taint_shovel", "taint_pickaxe")) {
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(ForbiddenMagic.id(id)), id + " must be registered");
        }
        helper.succeed();
    }

    private static void dropRulesMatchOriginal(GameTestHelper helper) {
        helper.assertValueEqual(FmDrops.heldWrathDamage(new ItemStack(Items.WOODEN_SWORD)), 4, "wooden sword adds four wrath");
        helper.assertValueEqual(FmDrops.heldWrathDamage(new ItemStack(Items.IRON_SWORD)), 6, "iron sword adds six wrath");
        helper.assertValueEqual(FmDrops.heldWrathDamage(new ItemStack(Items.DIAMOND_SWORD)), 7, "diamond sword adds seven wrath");
        helper.assertValueEqual(FmDrops.heldWrathDamage(new ItemStack(Items.IRON_PICKAXE)), 0, "tools add no wrath damage");
        helper.assertTrue(FmDrops.rollWrath(5, 5), "roll equal to bonus succeeds");
        helper.assertFalse(FmDrops.rollWrath(5, 6), "roll above bonus fails");
        helper.assertValueEqual(FmDrops.prideCount(0, 0), 2, "pride base count");
        helper.assertValueEqual(FmDrops.prideCount(3, 2), 5, "pride count adds roll");
        helper.assertTrue(FmDrops.rollSloth(3), "sloth boundary inside");
        helper.assertFalse(FmDrops.rollSloth(4), "sloth boundary outside");
        helper.assertValueEqual(FmDrops.slothCount(2), 3, "sloth count adds roll");
        helper.assertFalse(FmDrops.rollGreed(0, 0), "zero greed never rolls");
        helper.assertTrue(FmDrops.rollGreed(2, 2), "greed boundary inside");
        helper.assertFalse(FmDrops.rollGreed(2, 3), "greed boundary outside");
        helper.assertTrue(FmDrops.rollGluttony(1), "gluttony inside");
        helper.assertFalse(FmDrops.rollGluttony(2), "gluttony outside");
        helper.assertTrue(FmDrops.rollQuartzGreed(1, 1), "quartz greed inside");
        helper.assertFalse(FmDrops.rollQuartzGreed(0, 0), "quartz greed needs fortune");
        helper.assertTrue(FmDrops.rollEnvy(1), "envy inside");
        helper.assertFalse(FmDrops.rollEnvy(2), "envy outside");
        helper.succeed();
    }

    private static void mobCrystalAndWrathCageAreRegistered(GameTestHelper helper) {
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(ForbiddenMagic.id("mob_crystal")), "mob crystal must be registered");
        helper.assertTrue(BuiltInRegistries.BLOCK.containsKey(ForbiddenMagic.id("wrath_cage")), "wrath cage must be registered");
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(ForbiddenMagic.id("wrath_cage")), "wrath cage item must be registered");
        helper.succeed();
    }

    private static void cageRulesMatchOriginal(GameTestHelper helper) {
        helper.assertTrue(WrathCageBlockEntity.shouldSpawn(false, true, 0, 5, 5), "available essentia starts a round");
        helper.assertFalse(WrathCageBlockEntity.shouldSpawn(true, true, 5, 5, 5), "powered cage stays idle");
        helper.assertFalse(WrathCageBlockEntity.shouldSpawn(false, false, 5, 5, 5), "cage without a mob stays idle");
        helper.assertFalse(WrathCageBlockEntity.shouldSpawn(false, true, 0, 5, 4), "insufficient essentia stays idle");
        helper.assertTrue(WrathCageBlockEntity.shouldSpawn(false, true, 0, 0, 0), "zero cost always runs");
        var random = net.minecraft.util.RandomSource.create(7L);
        for (int i = 0; i < 20; i++) {
            int delay = WrathCageBlockEntity.nextDelay(random, false);
            helper.assertTrue(delay >= 200 && delay < 300, "normal delay in range");
            int slothDelay = WrathCageBlockEntity.nextDelay(random, true);
            helper.assertTrue(slothDelay >= 400 && slothDelay < 500, "slothful delay in range");
        }
        helper.succeed();
    }

    private static void enchantmentsAreLoaded(GameTestHelper helper) {
        var enchantments = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        for (var key : List.of(FmEnchantments.WRATH, FmEnchantments.GREEDY, FmEnchantments.CONSUMING,
            FmEnchantments.EDUCATIONAL, FmEnchantments.CORRUPTING, FmEnchantments.CLUSTER,
            FmEnchantments.IMPACT, FmEnchantments.VOIDTOUCHED)) {
            helper.assertTrue(enchantments.get(key).isPresent(), key.identifier() + " must be registered");
        }
        helper.succeed();
    }

    private static void enchantmentRulesMatchOriginal(GameTestHelper helper) {
        helper.assertValueEqual(FmEnchantmentEffects.educationalExperience(10, 2), 70, "educational triples per level");
        helper.assertValueEqual(FmEnchantmentEffects.wrathDamage(2), 2.5F, "wrath damage per level");
        helper.assertValueEqual(FmEnchantmentEffects.clusterChance(1), 0.275F, "cluster chance per level");
        helper.assertTrue(FmEnchantmentEffects.silverfishEmeraldRoll(true, 6, 2), "windswept boundary inside");
        helper.assertFalse(FmEnchantmentEffects.silverfishEmeraldRoll(true, 7, 2), "windswept boundary outside");
        helper.assertTrue(FmEnchantmentEffects.silverfishEmeraldRoll(false, 4, 1), "normal boundary inside");
        helper.assertFalse(FmEnchantmentEffects.silverfishEmeraldRoll(false, 5, 1), "normal boundary outside");
        helper.assertTrue(FmEnchantmentEffects.skullAxeRoll(5, 2, 3, 1), "skull axe boundary inside");
        helper.assertFalse(FmEnchantmentEffects.skullAxeRoll(6, 2, 3, 1), "skull axe boundary outside");
        helper.succeed();
    }

    private static void morphToolsAreRegistered(GameTestHelper helper) {
        for (String id : List.of("morph_pickaxe", "morph_sword", "morph_shovel", "morph_axe")) {
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(ForbiddenMagic.id(id)), id + " must be registered");
        }
        helper.succeed();
    }

    private static void morphStateCycles(GameTestHelper helper) {
        var state = dev.visfox.forbiddenreforged.MorphState.EMPTY;
        helper.assertValueEqual(state.phase(), 0, "empty state starts at phase zero");
        helper.assertValueEqual(state.advance().phase(), 1, "advance moves to phase one");
        helper.assertValueEqual(state.advance().advance().phase(), 2, "two advances reach phase two");
        helper.assertValueEqual(state.advance().advance().advance().phase(), 0, "third advance wraps to zero");
        helper.assertValueEqual(state.tintColor(), 0xFF980000, "phase zero tint");
        helper.assertValueEqual(state.advance().tintColor(), 0xFF0010CC, "phase one tint");
        helper.assertValueEqual(state.advance().advance().tintColor(), 0xFFE5DA00, "phase two tint");
        var stored = state.store(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY, "test");
        helper.assertValueEqual(stored.currentName(), "test", "store keeps the name in the active slot");
        helper.succeed();
    }

    private static HolderLookup.RegistryLookup<IAspect> aspects(GameTestHelper helper) {
        return helper.getLevel().registryAccess().lookup(IAspect.REGISTRY_KEY).orElseThrow();
    }
}
