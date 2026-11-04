package dev.visfox.forbiddenreforged.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import dev.visfox.forbiddenreforged.common.FmIds;

@EventBusSubscriber(modid = FmIds.MODID)
public final class ItemGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
        DeferredRegister.create(Registries.TEST_FUNCTION, FmIds.MODID);

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
        "research_translation_keys_are_complete",
        "crystalwell_research_page_matches_difficulty",
        "infernal_and_tainted_wand_damage_behaviors_match_original",
        "hellfire_trigger_targets_netherrack_and_obsidian",
        "subcollar_places_on_another_player_and_stores_damage_vis",
        "riding_crop_effects_match_original",
        "heretic_trades_match_original_listing",
        "witchwood_requires_ars_magica",
        "wrath_shard_formula_matches_original",
        "alchemical_cap_and_equivalent_rod_match_original",
        "ring_of_nutrition_only_works_in_ring_slots",
        "blink_focus_upgrades_use_original_icon_assets",
        "research_icons_resolve_to_real_content",
        "blink_focus_hellfire_applies_through_focal_manipulator",
        "blink_focus_reaches_beyond_wand_reach_and_lands_like_original",
        "wand_vis_and_parts_use_neutral_access_ports",
        "hellfire_behavior_uses_host_neutral_access",
        "scribe_well_aspect_ids_stay_semantic",
        "remaining_wand_caps_match_original_costs",
        "item_data_components_keep_their_ids_and_serialize"
    };

    static {
        FUNCTIONS.register(TESTS[0], () -> ItemMechanicsGameTests::skullAxeInfusionRecipeMatchesOriginal);
        FUNCTIONS.register(TESTS[1], () -> ScribingResearchGameTests::diabolistForkResearchAndRecipe);
        FUNCTIONS.register(TESTS[2], () -> ItemRegistrationGameTests::taintCoalIsRegisteredAndIsFuel);
        FUNCTIONS.register(TESTS[3], () -> ItemRegistrationGameTests::taintCoalBurnTimeVariesWithinOriginalRange);
        FUNCTIONS.register(TESTS[4], () -> ItemRegistrationGameTests::emeraldFragmentIsRegistered);
        FUNCTIONS.register(TESTS[5], () -> ItemRegistrationGameTests::ridingCropIsRegisteredAsSword);
        FUNCTIONS.register(TESTS[6], () -> WandGameTests::wandRodsRegisterAsFunctionalCores);
        FUNCTIONS.register(TESTS[7], () -> ItemRegistrationGameTests::creativeTabIsRegisteredWithForkIcon);
        FUNCTIONS.register(TESTS[8], () -> ItemRegistrationGameTests::creativeTabListsEveryRegisteredItem);
        FUNCTIONS.register(TESTS[9], () -> ItemRegistrationGameTests::orichalcumCapIsRegistered);
        FUNCTIONS.register(TESTS[10], () -> ItemRegistrationGameTests::diabolistForkSwitchesToTheSporkModel);
        FUNCTIONS.register(TESTS[11], () -> ItemRegistrationGameTests::lustShardSwitchesToTheDespairModel);
        FUNCTIONS.register(TESTS[12], () -> WandGameTests::creativeWandFamilyMatchesOriginal);
        FUNCTIONS.register(TESTS[13], () -> ItemMechanicsGameTests::morphPhaseTintsMatchOriginal);
        FUNCTIONS.register(TESTS[14], () -> ItemMechanicsGameTests::toolAndSkullAxeChanceRulesPreserveOriginalBoundaries);
        FUNCTIONS.register(TESTS[15], () -> ItemMechanicsGameTests::silverfishEmeraldDropThresholdsMatchOriginal);
        FUNCTIONS.register(TESTS[16], () -> WandGameTests::wandTooltipKindsPreserveOriginalHints);
        FUNCTIONS.register(TESTS[17], () -> ScribingResearchGameTests::crystalwellIsAScribingToolWithOriginalDurability);
        FUNCTIONS.register(TESTS[18], () -> ScribingResearchGameTests::primewellIsAPermanentEpicScribingTool);
        FUNCTIONS.register(TESTS[19], () -> ScribingResearchGameTests::scribeWellsKeepOriginalRecipesAndResearch);
        FUNCTIONS.register(TESTS[20], () -> ScribingResearchGameTests::researchPrefixRespectsTagResearch);
        FUNCTIONS.register(TESTS[21], () -> ScribingResearchGameTests::itemTranslationKeysAreNotNestedUnderResearchPages);
        FUNCTIONS.register(TESTS[22], () -> ScribingResearchGameTests::researchTranslationKeysAreComplete);
        FUNCTIONS.register(TESTS[23], () -> ScribingResearchGameTests::crystalwellResearchPageMatchesDifficulty);
        FUNCTIONS.register(TESTS[24], () -> WandGameTests::infernalAndTaintedWandDamageBehaviorsMatchOriginal);
        FUNCTIONS.register(TESTS[25], () -> WandGameTests::hellfireTriggerTargetsNetherrackAndObsidian);
        FUNCTIONS.register(TESTS[26], () -> ItemMechanicsGameTests::subcollarPlacesOnAnotherPlayerAndStoresDamageVis);
        FUNCTIONS.register(TESTS[27], () -> ItemMechanicsGameTests::ridingCropEffectsMatchOriginal);
        FUNCTIONS.register(TESTS[28], () -> ItemMechanicsGameTests::hereticTradesMatchOriginalListing);
        FUNCTIONS.register(TESTS[29], () -> ItemMechanicsGameTests::witchwoodRequiresArsMagica);
        FUNCTIONS.register(TESTS[30], () -> ItemMechanicsGameTests::wrathShardFormulaMatchesOriginal);
        FUNCTIONS.register(TESTS[31], () -> WandGameTests::alchemicalCapAndEquivalentRodMatchOriginal);
        FUNCTIONS.register(TESTS[32], () -> ItemRegistrationGameTests::ringOfNutritionOnlyWorksInRingSlots);
        FUNCTIONS.register(TESTS[33], () -> WandGameTests::blinkFocusUpgradesUseOriginalIconAssets);
        FUNCTIONS.register(TESTS[34], () -> ScribingResearchGameTests::researchIconsResolveToRealContent);
        FUNCTIONS.register(TESTS[35], () -> WandGameTests::blinkFocusHellfireAppliesThroughFocalManipulator);
        FUNCTIONS.register(TESTS[36], () -> WandGameTests::blinkFocusReachesBeyondWandReachAndLandsLikeOriginal);
        FUNCTIONS.register(TESTS[37], () -> WandGameTests::wandVisAndPartsUseNeutralAccessPorts);
        FUNCTIONS.register(TESTS[38], () -> WandGameTests::hellfireBehaviorUsesHostNeutralAccess);
        FUNCTIONS.register(TESTS[39], () -> ScribingResearchGameTests::scribeWellAspectIdsStaySemantic);
        FUNCTIONS.register(TESTS[40], () -> WandGameTests::remainingWandCapsMatchOriginalCosts);
        FUNCTIONS.register(TESTS[41], () -> ItemRegistrationGameTests::itemDataComponentsKeepTheirIdsAndSerialize);
    }

    private ItemGameTests() {}

    @SubscribeEvent
    static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
            Identifier.fromNamespaceAndPath(FmIds.MODID, "fm_items"), new TestEnvironmentDefinition.AllOf());
        for (String name : TESTS) {
            ResourceKey<Consumer<GameTestHelper>> function =
                ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath(FmIds.MODID, name));
            event.registerTest(
                Identifier.fromNamespaceAndPath(FmIds.MODID, name),
                new FunctionGameTestInstance(function, new TestData<>(environment, Identifier.parse("thaumcraft:test/platform"), 100, 0, true))
            );
        }
    }

    

    

    

    

    

    

    

    

    

    

    

    

    

    

    

    

    

    

    

    

    

    

    

    

    

    

    

    

    

    

    private static JsonObject readJson(GameTestHelper helper, String path) {        try (InputStream stream = FmIds.class.getResourceAsStream(path)) {
            if (stream == null) {
                helper.fail("missing resource " + path);
            }
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException e) {
            helper.fail("could not read " + path + ": " + e);
            return new JsonObject();
        }
    }
    
    

    

    

    

    

    


    

    

    

    

    

}
