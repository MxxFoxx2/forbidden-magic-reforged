package dev.visfox.forbiddenreforged.test;

import com.google.gson.JsonArray;
import dev.visfox.forbiddenreforged.CrystalwellItem;
import dev.visfox.forbiddenreforged.PrimewellItem;
import net.minecraft.world.item.Rarity;
import thaumcraft.item.ScribingToolsItem;
import thaumcraft.crafting.ShapelessArcaneRecipe;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModItems;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.FmAspects;
import dev.visfox.forbiddenreforged.FmItems;
import dev.visfox.forbiddenreforged.FmConfig;
import dev.visfox.forbiddenreforged.FmResearchItem;
import dev.visfox.forbiddenreforged.MorphState;
import dev.visfox.forbiddenreforged.FmEnchantmentEffects;
import dev.visfox.forbiddenreforged.FmToolEvents;
import dev.visfox.forbiddenreforged.RidingCropItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.WandRod;
import thaumcraft.wand.Wands;
import thaumcraft.wand.WandTriggers;

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class ItemGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
        DeferredRegister.create(Registries.TEST_FUNCTION, ForbiddenMagic.MODID);

    private static final String[] TESTS = {
        "skull_axe_infusion_recipe_matches_original",
        "diabolist_fork_research_and_recipe",
        "taint_coal_is_registered_and_is_fuel",
        "taint_coal_burn_time_varies_within_original_range",
        "emerald_fragment_is_registered",
        "riding_crop_is_registered_as_sword",
        "wand_rods_register_as_functional_cores",
        "creative_tab_is_registered_with_fork_icon",
        "creative_tab_lists_every_registered_item",
        "orichalcum_cap_is_registered",
        "diabolist_fork_switches_to_the_spork_model",
        "lust_shard_switches_to_the_despair_model",
        "creative_wand_family_matches_original",
        "morph_phase_tints_match_original",
        "tool_and_skull_axe_chance_rules_preserve_original_boundaries",
        "silverfish_emerald_drop_thresholds_match_original",
        "wand_tooltip_kinds_preserve_original_hints",
        "crystalwell_is_a_scribing_tool_with_original_durability",
        "primewell_is_a_permanent_epic_scribing_tool",
        "scribe_wells_keep_original_recipes_and_research",
        "research_prefix_respects_tag_research",
        "item_translation_keys_are_not_nested_under_research_pages",
        "infernal_and_tainted_wand_damage_behaviors_match_original",
        "hellfire_trigger_targets_netherrack_and_obsidian"
    };

    static {
        FUNCTIONS.register(TESTS[0], () -> ItemGameTests::skullAxeInfusionRecipeMatchesOriginal);
        FUNCTIONS.register(TESTS[1], () -> ItemGameTests::diabolistForkResearchAndRecipe);
        FUNCTIONS.register(TESTS[2], () -> ItemGameTests::taintCoalIsRegisteredAndIsFuel);
        FUNCTIONS.register(TESTS[3], () -> ItemGameTests::taintCoalBurnTimeVariesWithinOriginalRange);
        FUNCTIONS.register(TESTS[4], () -> ItemGameTests::emeraldFragmentIsRegistered);
        FUNCTIONS.register(TESTS[5], () -> ItemGameTests::ridingCropIsRegisteredAsSword);
        FUNCTIONS.register(TESTS[6], () -> ItemGameTests::wandRodsRegisterAsFunctionalCores);
        FUNCTIONS.register(TESTS[7], () -> ItemGameTests::creativeTabIsRegisteredWithForkIcon);
        FUNCTIONS.register(TESTS[8], () -> ItemGameTests::creativeTabListsEveryRegisteredItem);
        FUNCTIONS.register(TESTS[9], () -> ItemGameTests::orichalcumCapIsRegistered);
        FUNCTIONS.register(TESTS[10], () -> ItemGameTests::diabolistForkSwitchesToTheSporkModel);
        FUNCTIONS.register(TESTS[11], () -> ItemGameTests::lustShardSwitchesToTheDespairModel);
        FUNCTIONS.register(TESTS[12], () -> ItemGameTests::creativeWandFamilyMatchesOriginal);
        FUNCTIONS.register(TESTS[13], () -> ItemGameTests::morphPhaseTintsMatchOriginal);
        FUNCTIONS.register(TESTS[14], () -> ItemGameTests::toolAndSkullAxeChanceRulesPreserveOriginalBoundaries);
        FUNCTIONS.register(TESTS[15], () -> ItemGameTests::silverfishEmeraldDropThresholdsMatchOriginal);
        FUNCTIONS.register(TESTS[16], () -> ItemGameTests::wandTooltipKindsPreserveOriginalHints);
        FUNCTIONS.register(TESTS[17], () -> ItemGameTests::crystalwellIsAScribingToolWithOriginalDurability);
        FUNCTIONS.register(TESTS[18], () -> ItemGameTests::primewellIsAPermanentEpicScribingTool);
        FUNCTIONS.register(TESTS[19], () -> ItemGameTests::scribeWellsKeepOriginalRecipesAndResearch);
        FUNCTIONS.register(TESTS[20], () -> ItemGameTests::researchPrefixRespectsTagResearch);
        FUNCTIONS.register(TESTS[21], () -> ItemGameTests::itemTranslationKeysAreNotNestedUnderResearchPages);
        FUNCTIONS.register(TESTS[22], () -> ItemGameTests::infernalAndTaintedWandDamageBehaviorsMatchOriginal);
        FUNCTIONS.register(TESTS[23], () -> ItemGameTests::hellfireTriggerTargetsNetherrackAndObsidian);
    }

    private ItemGameTests() {}

    @SubscribeEvent
    static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
            Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, "fm_items"), new TestEnvironmentDefinition.AllOf());
        for (String name : TESTS) {
            ResourceKey<Consumer<GameTestHelper>> function =
                ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, name));
            event.registerTest(
                Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, name),
                new FunctionGameTestInstance(function, new TestData<>(environment, Identifier.parse("thaumcraft:test/platform"), 100, 0, true))
            );
        }
    }

    private static void skullAxeInfusionRecipeMatchesOriginal(GameTestHelper helper) {
        InfusionRecipe recipe = (InfusionRecipe) TcRecipes.get("SKULLAXE");
        helper.assertTrue(recipe != null, "skull axe recipe must be registered");
        helper.assertValueEqual(recipe.instability(), 1, "instability");
        helper.assertValueEqual(recipe.aspects().getAmount(FmAspects.WRATH), 8, "wrath cost");
        helper.assertValueEqual(recipe.aspects().getAmount(Aspect.WEAPON), 8, "weapon cost");
        helper.assertValueEqual(recipe.aspects().getAmount(FmAspects.NETHER), 8, "nether cost");
        helper.assertValueEqual(recipe.components().size(), 4, "four components");
        helper.succeed();
    }

    private static void diabolistForkResearchAndRecipe(GameTestHelper helper) {
        ResearchItem fork = ResearchCategories.getResearch("FORK");
        helper.assertTrue(fork != null, "FORK research must be registered");
        helper.assertTrue(fork.isSecondary(), "FORK is secondary");
        helper.assertTrue(fork.isConcealed(), "FORK is concealed");
        helper.assertValueEqual(fork.warp(), 0, "FORK has no warp");
        helper.assertValueEqual(fork.parents(), List.of("INFUSION", "NETHERSHARDS", "THAUMIUM"), "FORK parents");
        InfusionRecipe recipe = (InfusionRecipe) TcRecipes.get("FORK");
        helper.assertTrue(recipe != null, "FORK infusion recipe exists");
        helper.assertValueEqual(recipe.instability(), 1, "FORK instability");
        helper.assertValueEqual(recipe.aspects().getAmount(Aspect.FIRE), 8, "FORK fire cost");
        helper.assertValueEqual(recipe.aspects().getAmount(Aspect.MECHANISM), 8, "FORK mechanism cost");
        helper.assertValueEqual(recipe.aspects().getAmount(Aspect.ENERGY), 8, "FORK energy cost");
        helper.assertValueEqual(recipe.components().size(), 4, "FORK has 4 components");
        helper.succeed();
    }

    private static void taintCoalIsRegisteredAndIsFuel(GameTestHelper helper) {
        ItemStack stack = new ItemStack(FmItems.TAINT_COAL.get());
        helper.assertFalse(stack.isEmpty(), "taint coal must exist");
        helper.assertValueEqual(BuiltInRegistries.ITEM.getKey(stack.getItem()), Identifier.parse("forbiddenmagic:taint_coal"), "taint coal id");
        helper.assertTrue(stack.has(DataComponents.COOKING_FUEL), "taint coal is a fuel");
        helper.succeed();
    }

    private static void taintCoalBurnTimeVariesWithinOriginalRange(GameTestHelper helper) {
        ItemStack stack = new ItemStack(FmItems.TAINT_COAL.get());
        CookingFuel fuel = stack.get(DataComponents.COOKING_FUEL);
        helper.assertTrue(fuel != null, "taint coal carries a cooking fuel component");
        LootContext context = new LootContext.Builder(
            new LootParams.Builder(helper.getLevel()).create(LootContextParamSets.EMPTY)
        ).create(Optional.empty());
        Set<Integer> drawn = new HashSet<>();
        for (int i = 0; i < 40; i++) {
            int burn = fuel.burnTime().get(context, 0);
            helper.assertTrue(burn >= 1 && burn <= 2400, "burn time " + burn + " must stay within 1..2400");
            drawn.add(burn);
        }
        helper.assertTrue(drawn.size() > 1, "burn time must be drawn per use, not a constant");
        helper.assertTrue(ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, stack).result().isPresent(), "taint coal must serialize its fuel component");
        helper.succeed();
    }

    private static void emeraldFragmentIsRegistered(GameTestHelper helper) {
        ItemStack stack = new ItemStack(FmItems.EMERALD_FRAGMENT.get());
        helper.assertFalse(stack.isEmpty(), "emerald fragment must exist");
        helper.assertValueEqual(BuiltInRegistries.ITEM.getKey(stack.getItem()), Identifier.parse("forbiddenmagic:emerald_fragment"), "emerald fragment id");
        helper.succeed();
    }

    private static void ridingCropIsRegisteredAsSword(GameTestHelper helper) {
        Item crop = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, "riding_crop"));
        helper.assertTrue(crop != Items.AIR, "riding crop must be registered");
        helper.assertTrue(crop instanceof RidingCropItem, "riding crop must be RidingCropItem");
        helper.succeed();
    }

    private static void wandRodsRegisterAsFunctionalCores(GameTestHelper helper) {
        WandRod tainted = WandRegistry.findRod("tainted");
        helper.assertTrue(tainted != null, "tainted rod must be registered in WandRegistry");
        helper.assertValueEqual(tainted.capacity(), 150, "tainted capacity");
        helper.assertValueEqual(tainted.craftCost(), 12, "tainted craft cost");
        helper.assertTrue(tainted.onUpdate() != null, "tainted rod has an update behavior");

        WandRod infernal = WandRegistry.findRod("infernal");
        helper.assertTrue(infernal != null && infernal.capacity() == 150, "infernal rod registered with capacity 150");
        helper.assertTrue(infernal.onUpdate() != null, "infernal rod has an update behavior");

        WandRod profane = WandRegistry.findRod("profane");
        helper.assertTrue(profane != null && profane.capacity() == 50, "profane rod registered with capacity 50");
        helper.assertTrue(profane.onUpdate() != null, "profane rod has an update behavior");

        helper.assertTrue(WandRegistry.findRod("profaned") != null, "profaned burnout rod registered");

        ItemStack wand = Wands.create(WandRegistry.CAP_THAUMIUM, tainted, false);
        helper.assertTrue(Wands.isWand(wand), "tainted wand is created");
        helper.assertValueEqual(Wands.getRod(wand).tag(), "tainted", "created wand uses the tainted rod");
        helper.succeed();
    }

    private static void creativeTabIsRegisteredWithForkIcon(GameTestHelper helper) {
        CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(ForbiddenMagic.id("forbidden"))
            .map(holder -> holder.value())
            .orElse(null);
        helper.assertTrue(tab != null, "the addon registers its own creative tab");
        helper.assertValueEqual(tab.getIconItem().getItem(), FmItems.DIABOLIST_FORK.get(), "the tab icon is the diabolist fork");
        helper.succeed();
    }

    private static void creativeTabListsEveryRegisteredItem(GameTestHelper helper) {
        List<ItemStack> stacks = dev.visfox.forbiddenreforged.FmCreativeTab.stacks();
        java.util.Set<Item> listed = new java.util.HashSet<>();
        for (ItemStack stack : stacks) {
            listed.add(stack.getItem());
        }
        for (var holder : FmItems.ITEMS.getEntries()) {
            helper.assertTrue(listed.contains(holder.get()), "the tab is missing the item " + holder.getId());
        }
        for (var holder : dev.visfox.forbiddenreforged.FmBlocks.BLOCKS.getEntries()) {
            Item blockItem = holder.get().asItem();
            if (blockItem != Items.AIR) {
                helper.assertTrue(listed.contains(blockItem), "the tab is missing the block " + holder.getId());
            }
        }
        long unique = stacks.stream().map(stack -> stack.getItem() + "#" + stack.getComponents()).distinct().count();
        helper.assertValueEqual(stacks.size(), (int) unique, "no duplicate entries in the tab list");
        helper.succeed();
    }

    private static void orichalcumCapIsRegistered(GameTestHelper helper) {
        thaumcraft.wand.WandCap cap = WandRegistry.findCap("orichalcum");
        helper.assertTrue(cap != null, "the creative orichalcum cap is registered");
        helper.assertValueEqual(cap.baseCostModifier(), 0.0F, "orichalcum charges no vis");
        helper.assertValueEqual(cap.craftCost(), 1000, "orichalcum craft cost is 1000");
        helper.assertTrue(cap.specialAspects().isEmpty(), "orichalcum has no special aspects");
        helper.succeed();
    }

    private static void diabolistForkSwitchesToTheSporkModel(GameTestHelper helper) {
        JsonObject definition = readJson(helper, "/assets/forbiddenmagic/items/diabolist_fork.json");
        JsonObject model = definition.getAsJsonObject("model");
        helper.assertValueEqual(model.get("type").getAsString(), "minecraft:condition", "the fork model is conditional");
        helper.assertValueEqual(model.get("property").getAsString(), "forbiddenmagic:spork", "condition reads the spork config");
        helper.assertValueEqual(model.getAsJsonObject("on_true").get("model").getAsString(), "forbiddenmagic:item/diabolist_fork_spork", "on_true is the spork");
        helper.assertValueEqual(model.getAsJsonObject("on_false").get("model").getAsString(), "forbiddenmagic:item/diabolist_fork", "on_false is the ordinary fork");
        JsonObject spork = readJson(helper, "/assets/forbiddenmagic/models/item/diabolist_fork_spork.json");
        helper.assertValueEqual(spork.getAsJsonObject("textures").get("layer0").getAsString(), "forbiddenmagic:item/spork", "spork model uses the spork texture");
        helper.assertTrue(ForbiddenMagic.class.getResourceAsStream("/assets/forbiddenmagic/textures/item/spork.png") != null, "spork texture ships with the mod");
        helper.succeed();
    }

    private static void lustShardSwitchesToTheDespairModel(GameTestHelper helper) {
        JsonObject definition = readJson(helper, "/assets/forbiddenmagic/items/lust_shard.json");
        JsonObject model = definition.getAsJsonObject("model");
        helper.assertValueEqual(model.get("type").getAsString(), "minecraft:condition", "the lust shard model is conditional");
        helper.assertValueEqual(model.get("property").getAsString(), "forbiddenmagic:no_lust", "condition reads the no_lust config");
        helper.assertValueEqual(model.getAsJsonObject("on_true").get("model").getAsString(), "forbiddenmagic:item/lust_shard_despair", "on_true is the despair shard");
        helper.assertValueEqual(model.getAsJsonObject("on_false").get("model").getAsString(), "forbiddenmagic:item/lust_shard", "on_false is the lust shard");
        JsonObject despair = readJson(helper, "/assets/forbiddenmagic/models/item/lust_shard_despair.json");
        helper.assertValueEqual(despair.getAsJsonObject("textures").get("layer0").getAsString(), "forbiddenmagic:item/despair_shard", "despair model uses the despair texture");
        helper.assertTrue(ForbiddenMagic.class.getResourceAsStream("/assets/forbiddenmagic/textures/item/despair_shard.png") != null, "despair texture ships with the mod");
        helper.succeed();
    }

    private static void creativeWandFamilyMatchesOriginal(GameTestHelper helper) {
        WandRod neutronium = WandRegistry.findRod("neutronium");
        WandRod staff = WandRegistry.findRod("neutronium_staff");
        helper.assertTrue(neutronium != null, "neutronium rod is registered");
        helper.assertTrue(staff != null, "neutronium staff is registered");
        helper.assertValueEqual(neutronium.capacity(), 9001, "neutronium rod capacity");
        helper.assertValueEqual(staff.capacity(), 9002, "neutronium staff capacity");
        helper.assertTrue(neutronium.glowing() && staff.glowing(), "neutronium rod and staff glow");
        helper.assertTrue(!neutronium.staff() && staff.staff(), "only the second neutronium rod is a staff");
        helper.assertValueEqual(neutronium.craftCost(), 1000, "neutronium rod craft cost");
        helper.assertValueEqual(staff.craftCost(), 1000, "neutronium staff craft cost");
        helper.assertTrue(neutronium.onUpdate() != null && staff.onUpdate() != null, "creative rods refill vis on update");
        helper.assertValueEqual(neutronium.item(), Identifier.withDefaultNamespace("bedrock"), "creative rod uses bedrock");
        helper.assertValueEqual(staff.item(), Identifier.withDefaultNamespace("bedrock"), "creative staff uses bedrock");
        helper.assertTrue(ForbiddenMagic.class.getResourceAsStream("/assets/forbiddenmagic/textures/model/wand_rod_neutronium.png") != null, "neutronium rod texture ships with the mod");

        List<ItemStack> stacks = dev.visfox.forbiddenreforged.FmCreativeTab.stacks();
        helper.assertTrue(stacks.stream().anyMatch(stack -> Wands.isWand(stack)
            && Wands.getCap(stack).tag().equals("thaumium") && Wands.getRod(stack).tag().equals("tainted")), "tainted wand is in the creative tab");
        helper.assertTrue(stacks.stream().anyMatch(stack -> Wands.isWand(stack)
            && Wands.getCap(stack).tag().equals("thaumium") && Wands.getRod(stack).tag().equals("infernal")), "infernal wand is in the creative tab");
        helper.assertTrue(stacks.stream().anyMatch(stack -> Wands.isWand(stack)
            && Wands.getCap(stack).tag().equals("orichalcum") && Wands.getRod(stack).tag().equals("neutronium")), "creative neutronium wand is in the tab");
        helper.assertTrue(stacks.stream().anyMatch(stack -> Wands.isWand(stack)
            && Wands.getCap(stack).tag().equals("orichalcum") && Wands.getRod(stack).tag().equals("neutronium_staff")), "creative neutronium staff is in the tab");

        ItemStack neutroniumWand = stacks.stream().filter(stack -> Wands.isWand(stack)
            && Wands.getCap(stack).tag().equals("orichalcum") && Wands.getRod(stack).tag().equals("neutronium"))
            .findFirst().orElse(ItemStack.EMPTY);
        ItemStack staffWand = stacks.stream().filter(stack -> Wands.isWand(stack)
            && Wands.getCap(stack).tag().equals("orichalcum") && Wands.getRod(stack).tag().equals("neutronium_staff"))
            .findFirst().orElse(ItemStack.EMPTY);
        helper.assertFalse(neutroniumWand.isEmpty(), "creative neutronium wand can be located");
        helper.assertFalse(staffWand.isEmpty(), "creative neutronium staff can be located");
        var creativePlayer = helper.makeMockServerPlayer(net.minecraft.world.level.GameType.CREATIVE);
        Wands.getRod(neutroniumWand).onUpdate().onUpdate(neutroniumWand, creativePlayer);
        Wands.getRod(staffWand).onUpdate().onUpdate(staffWand, creativePlayer);
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            helper.assertValueEqual(Wands.getVis(neutroniumWand, aspect), Wands.getMaxVis(neutroniumWand), "neutronium rod fills " + aspect);
            helper.assertValueEqual(Wands.getVis(staffWand, aspect), Wands.getMaxVis(staffWand), "neutronium staff fills " + aspect);
        }

        ItemStack profaneWand = stacks.stream().filter(stack -> Wands.isWand(stack)
            && Wands.getCap(stack).tag().equals("iron") && Wands.getRod(stack).tag().equals("profane"))
            .findFirst().orElse(ItemStack.EMPTY);
        helper.assertFalse(profaneWand.isEmpty(), "pre-filled profane wand is in the creative tab");
        for (Aspect aspect : Aspect.getPrimalAspects()) {
            helper.assertValueEqual(Wands.getVis(profaneWand, aspect), 5000, "profane creative wand starts with 5000 " + aspect);
        }
        helper.succeed();
    }

    private static void morphPhaseTintsMatchOriginal(GameTestHelper helper) {
        MorphState state = MorphState.EMPTY;
        int[] colors = {0xFF980000, 0xFF0010CC, 0xFFE5DA00};
        for (int i = 0; i < colors.length; i++) {
            helper.assertValueEqual(state.tintColor(), colors[i], "morph phase " + i + " tint");
            state = state.advance();
        }
        for (String name : List.of("morph_pickaxe", "morph_sword", "morph_shovel", "morph_axe")) {
            JsonObject definition = readJson(helper, "/assets/forbiddenmagic/items/" + name + ".json");
            JsonObject model = definition.getAsJsonObject("model");
            JsonArray tints = model.getAsJsonArray("tints");
            helper.assertValueEqual(tints.size(), 2, name + " has a base tint and a phase tint");
            helper.assertValueEqual(tints.get(0).getAsJsonObject().get("type").getAsString(), "minecraft:constant", name + " keeps the base layer white");
            helper.assertValueEqual(tints.get(0).getAsJsonObject().get("value").getAsInt(), 16777215, name + " base layer remains untinted");
            helper.assertValueEqual(tints.get(1).getAsJsonObject().get("type").getAsString(), "forbiddenmagic:morph_phase", name + " overlay tint uses the phase source");
        }
        helper.succeed();
    }

    private static void toolAndSkullAxeChanceRulesPreserveOriginalBoundaries(GameTestHelper helper) {
        helper.assertValueEqual(FmToolEvents.taintPickaxeSpeed(0.0F), 0.0F, "zero-hardness blocks remain instant");
        helper.assertValueEqual(FmToolEvents.taintPickaxeSpeed(2.0F), 0.1F, "hardness below five uses the original slow speed");
        helper.assertValueEqual(FmToolEvents.taintPickaxeSpeed(10.0F), 5.0F, "hardness below twenty is halved");
        helper.assertValueEqual(FmToolEvents.taintPickaxeSpeed(20.0F), 25.0F, "hardness twenty and above adds five");

        helper.assertTrue(FmToolEvents.taintShardRoll(1, 0, false), "taint shard roll succeeds at base threshold");
        helper.assertFalse(FmToolEvents.taintShardRoll(2, 0, false), "taint shard roll fails above base threshold");
        helper.assertTrue(FmToolEvents.taintShardRoll(7, 0, true), "silk touch adds six to taint shard threshold");
        helper.assertTrue(FmToolEvents.taintShardRoll(3, 1, false), "Fortune adds two per level to taint shard threshold");
        helper.assertFalse(FmToolEvents.taintShardRoll(4, 1, false), "taint shard threshold excludes the next roll");

        helper.assertTrue(FmEnchantmentEffects.skullAxeRoll(3, 0, 3, 1), "skeleton threshold includes roll three");
        helper.assertFalse(FmEnchantmentEffects.skullAxeRoll(4, 0, 3, 1), "skeleton threshold excludes roll four");
        helper.assertTrue(FmEnchantmentEffects.skullAxeRoll(4, 1, 3, 1), "Looting adds one to skeleton threshold");
        helper.assertTrue(FmEnchantmentEffects.skullAxeRoll(2, 0, 2, 2), "zombie threshold includes roll two");
        helper.assertFalse(FmEnchantmentEffects.skullAxeRoll(3, 0, 2, 2), "zombie threshold excludes roll three");
        helper.assertTrue(FmEnchantmentEffects.skullAxeRoll(4, 1, 2, 2), "Looting II raises zombie threshold by four");
        helper.assertTrue(FmEnchantmentEffects.skullAxeRoll(1, 0, 1, 1), "player head threshold includes roll one");
        helper.assertFalse(FmEnchantmentEffects.skullAxeRoll(3, 1, 1, 1), "player head threshold excludes roll three at Looting I");

        WandRod infernalRod = WandRegistry.findRod("infernal");
        helper.assertTrue(infernalRod != null, "infernal rod must be registered");
        ItemStack infernalWand = Wands.create(WandRegistry.CAP_IRON, infernalRod, false);
        helper.assertTrue(FmToolEvents.cancelsInfernalDamage(infernalWand, true),
            "infernal wand cancels incoming fire or wither damage");
        helper.assertFalse(FmToolEvents.cancelsInfernalDamage(infernalWand, false),
            "infernal wand does not cancel unrelated incoming damage");
        WandRod taintedRod = WandRegistry.findRod("tainted");
        ItemStack taintedWand = Wands.create(WandRegistry.CAP_IRON, taintedRod, false);
        helper.assertFalse(FmToolEvents.cancelsInfernalDamage(taintedWand, true),
            "tainted wand does not inherit infernal immunity");
        helper.assertFalse(FmToolEvents.cancelsInfernalDamage(new ItemStack(Items.STICK), true),
            "ordinary items do not cancel fire damage");
        helper.succeed();
    }

    private static void silverfishEmeraldDropThresholdsMatchOriginal(GameTestHelper helper) {
        helper.assertTrue(FmEnchantmentEffects.silverfishEmeraldRoll(true, 2, 0), "Windswept Hills includes roll 2 at Looting 0");
        helper.assertFalse(FmEnchantmentEffects.silverfishEmeraldRoll(true, 3, 0), "Windswept Hills excludes roll 3 at Looting 0");
        helper.assertTrue(FmEnchantmentEffects.silverfishEmeraldRoll(true, 4, 1), "Windswept Hills adds two per Looting level");
        helper.assertFalse(FmEnchantmentEffects.silverfishEmeraldRoll(true, 5, 1), "Windswept Hills excludes the next roll");

        helper.assertTrue(FmEnchantmentEffects.silverfishEmeraldRoll(false, 1, 0), "other biomes include roll 1 at Looting 0");
        helper.assertFalse(FmEnchantmentEffects.silverfishEmeraldRoll(false, 2, 0), "other biomes exclude roll 2 at Looting 0");
        helper.assertTrue(FmEnchantmentEffects.silverfishEmeraldRoll(false, 4, 1), "other biomes add three per Looting level");
        helper.assertFalse(FmEnchantmentEffects.silverfishEmeraldRoll(false, 5, 1), "other biomes exclude the next roll");
        helper.succeed();
    }

    private static void wandTooltipKindsPreserveOriginalHints(GameTestHelper helper) {
        helper.assertValueEqual(FmToolEvents.wandTooltipKind("neutronium"), "creativeonly",
            "neutronium wand exposes the creative-only tooltip");
        helper.assertValueEqual(FmToolEvents.wandTooltipKind("neutronium_staff"), "creativeonly",
            "neutronium staff exposes the creative-only tooltip");
        helper.assertValueEqual(FmToolEvents.wandTooltipKind("profane"), "profane_contract",
            "profane wand exposes its contract tooltip");
        helper.assertValueEqual(FmToolEvents.wandTooltipKind("profaned"), "profane_contract",
            "profaned wand exposes the contract tooltip");
        helper.assertValueEqual(FmToolEvents.wandTooltipKind("greatwood"), "",
            "ordinary rods do not receive special wand tooltips");
        helper.assertValueEqual(FmToolEvents.wandTooltipKind(new ItemStack(Items.STICK)), "",
            "non-wand items do not receive wand-specific tooltips");
        helper.succeed();
    }

    private static void crystalwellIsAScribingToolWithOriginalDurability(GameTestHelper helper) {
        ItemStack stack = new ItemStack(FmItems.CRYSTALWELL.get());
        helper.assertTrue(stack.getItem() instanceof CrystalwellItem, "crystalwell uses its behavior item");
        helper.assertTrue(stack.getItem() instanceof ScribingToolsItem, "crystalwell can occupy the research table scribe slot");
        int expectedMaxDamage = thaumcraft.Config.researchDifficulty() == -1 ? 50 : 100;
        helper.assertValueEqual(stack.getMaxDamage(), expectedMaxDamage, "crystalwell durability follows research difficulty");
        helper.assertValueEqual(stack.getCount(), 1, "crystalwell is unstackable");
        helper.succeed();
    }

    private static void primewellIsAPermanentEpicScribingTool(GameTestHelper helper) {
        ItemStack stack = new ItemStack(FmItems.PRIMEWELL.get());
        helper.assertTrue(stack.getItem() instanceof PrimewellItem, "primewell uses its behavior item");
        helper.assertTrue(stack.getItem() instanceof ScribingToolsItem, "primewell can occupy the research table scribe slot");
        helper.assertValueEqual(stack.getRarity(), Rarity.EPIC, "primewell retains epic rarity");
        helper.assertTrue(stack.getMaxDamage() > 1_000_000_000, "primewell does not wear out during research");
        stack.setDamageValue(1000);
        helper.assertFalse(stack.getItem().isBarVisible(stack), "primewell does not display a misleading durability bar");
        helper.assertTrue(stack.getDamageValue() < stack.getMaxDamage(), "primewell remains usable after repeated scribe operations");
        helper.succeed();
    }

    private static void scribeWellsKeepOriginalRecipesAndResearch(GameTestHelper helper) {
        ShapelessArcaneRecipe crystalwell = (ShapelessArcaneRecipe) TcRecipes.get("CRYSTALWELL");
        helper.assertTrue(crystalwell != null, "crystalwell arcane recipe is registered");
        helper.assertTrue(TcRecipes.get("PRIMEWELL") != null, "primewell arcane recipe is registered");
        helper.assertTrue(ResearchCategories.getResearch("CRYSTALWELL") != null, "crystalwell research is registered");
        helper.assertTrue(ResearchCategories.getResearch("PRIMEWELL") != null, "primewell research is registered");
        helper.assertTrue(crystalwell.ingredients().get(0).test(new ItemStack(ModItems.SCRIBING_TOOLS.get())),
            "crystalwell recipe uses the registered scribing tools item");
        if (thaumcraft.Config.researchDifficulty() == -1) {
            helper.assertValueEqual(crystalwell.ingredients().size(), 4, "easy research uses two arbitrary Thaumcraft shards");
            helper.assertTrue(crystalwell.ingredients().get(2).test(new ItemStack(ModItems.BALANCED_SHARD.get())),
                "the easy recipe accepts any shard through the shard tag");
        } else {
            helper.assertValueEqual(crystalwell.ingredients().size(), 6, "normal research uses a mixed crystal and three balanced shards");
            helper.assertTrue(crystalwell.ingredients().get(2).test(new ItemStack(ModBlocks.MIXED_CRYSTAL_CLUSTER.get().asItem())),
                "the hard recipe uses the mixed crystal cluster");
            helper.assertTrue(crystalwell.ingredients().get(3).test(new ItemStack(ModItems.BALANCED_SHARD.get())),
                "the hard recipe uses balanced shards");
        }
        helper.succeed();
    }

    private static void itemTranslationKeysAreNotNestedUnderResearchPages(GameTestHelper helper) {
            JsonObject english = readJson(helper, "/assets/forbiddenmagic/lang/en_us.json");
            JsonObject russian = readJson(helper, "/assets/forbiddenmagic/lang/ru_ru.json");
            List<String> expected = List.of(
                "item.forbiddenmagic.taint_coal",
                "item.forbiddenmagic.riding_crop",
                "item.forbiddenmagic.black_dye",
                "item.forbiddenmagic.taint_fruit",
                "item.forbiddenmagic.ring_nutrition",
                "item.forbiddenmagic.tainted_rod",
                "item.forbiddenmagic.infernal_rod",
                "item.forbiddenmagic.witchwood_rod",
                "item.forbiddenmagic.profane_rod",
                "item.forbiddenmagic.witchwood_staff_rod",
                "item.Wand.tainted.rod",
                "item.Wand.infernal.rod",
                "item.Wand.profane.rod",
                "item.Wand.profaned.rod",
                "item.forbiddenmagic.taint_shovel",
                "item.forbiddenmagic.taint_pickaxe",
                "item.forbiddenmagic.crystalwell",
                "item.forbiddenmagic.primewell",
                "item.forbiddenmagic.blink_focus",
                "entity.forbiddenmagic.villager.heretic",
                "tooltip.creativeonly",
                "tooltip.profane_contract"
            );
            List<String> pages = List.of(
                "RIDINGCROP", "RINGFOOD", "ROD_tainted", "ROD_infernal", "ROD_witchwood",
                "ROD_profane", "ROD_witchwood_staff", "TRANSEMERALD", "CRYSTALWELL",
                "PRIMEWELL", "TAINTSHOVEL", "TAINTPICK", "FOCUSBLINK"
            );
            for (JsonObject translations : List.of(english, russian)) {
                for (String key : expected) {
                    helper.assertTrue(translations.has(key), "missing translation key " + key);
                }
                for (String page : pages) {
                    helper.assertTrue(translations.has("forbiddenmagic.research_page." + page + ".1"),
                        "missing research page key " + page);
                }
                for (String key : translations.keySet()) {
                    helper.assertFalse(key.contains(".research_page.ARCANECAKE.item.")
                            || key.contains(".research_page.ARCANECAKE.entity.")
                            || key.contains(".research_page.ARCANECAKE.forbiddenmagic.")
                            || key.startsWith("itemGroup.tooltip."),
                        "translation key has an invalid nested prefix " + key);
                }
            }
            helper.succeed();
        }

    private static void infernalAndTaintedWandDamageBehaviorsMatchOriginal(GameTestHelper helper) {
        WandRod tainted = WandRegistry.findRod("tainted");
        WandRod infernal = WandRegistry.findRod("infernal");
        helper.assertTrue(tainted != null && infernal != null, "both behavior-bearing rods must be registered");
        ItemStack taintedWand = Wands.create(WandRegistry.CAP_THAUMIUM, tainted, false);
        ItemStack infernalWand = Wands.create(WandRegistry.CAP_THAUMIUM, infernal, false);

        helper.assertTrue(FmToolEvents.cancelsInfernalDamage(infernalWand, true),
            "held Infernal Wand cancels fire or wither damage");
        helper.assertFalse(FmToolEvents.cancelsInfernalDamage(infernalWand, false),
            "Infernal Wand does not cancel unrelated damage");
        helper.assertFalse(FmToolEvents.cancelsInfernalDamage(taintedWand, true),
            "Tainted Wand does not gain Infernal immunity");

        helper.assertTrue(FmToolEvents.restoresTaintedVis(taintedWand, true),
            "held Tainted Wand reacts to the taint damage type");
        helper.assertFalse(FmToolEvents.restoresTaintedVis(taintedWand, false),
            "Tainted Wand does not react to non-taint damage");
        helper.assertFalse(FmToolEvents.restoresTaintedVis(infernalWand, true),
            "Infernal Wand does not use the Tainted Wand reaction");

        List<Aspect> restored = FmToolEvents.taintedVisRolls(RandomSource.create(42L));
        helper.assertValueEqual(restored.size(), 3, "taint damage makes three independent primal-aspect rolls");
        for (Aspect aspect : restored) {
            helper.assertTrue(aspect.isPrimal(), "every restored aspect must be primal");
        }
        helper.succeed();
    }

    private static void hellfireTriggerTargetsNetherrackAndObsidian(GameTestHelper helper) {
        helper.assertTrue(WandTriggers.hasTrigger(Blocks.NETHERRACK.defaultBlockState()),
            "Hellfire trigger must be registered for netherrack");
        helper.assertTrue(WandTriggers.hasTrigger(Blocks.OBSIDIAN.defaultBlockState()),
            "Hellfire trigger must be registered for obsidian");
        helper.assertTrue(ResearchCategories.getResearch("HELLFIRE") != null,
            "the trigger's HELLFIRE research gate must exist");
        helper.succeed();
    }

    private static JsonObject readJson(GameTestHelper helper, String path) {        try (InputStream stream = ForbiddenMagic.class.getResourceAsStream(path)) {
            if (stream == null) {
                helper.fail("missing resource " + path);
            }
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException e) {
            helper.fail("could not read " + path + ": " + e);
            return new JsonObject();
        }
    }
    
    private static void researchPrefixRespectsTagResearch(GameTestHelper helper) {
        ResearchItem item = new FmResearchItem("TEST", "FORBIDDEN", new AspectList(), 0, 0, 1,
            ForbiddenMagic.id("textures/misc/forbidden.png"));
        String expected = FmConfig.tagResearchEnabled()
            ? "forbiddenmagic.research_text.TEST"
            : "tc.research_text.TEST";
        helper.assertValueEqual(item.textKey(), expected, "research text key follows tagResearch config");
        helper.succeed();
    }

}
