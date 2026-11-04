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

    public static void registerFunctions(net.minecraft.core.Registry<java.util.function.Consumer<net.minecraft.gametest.framework.GameTestHelper>> registry) {
        FmGameTestBootstrap.register(registry, "skull_axe_infusion_recipe_matches_original", ItemMechanicsGameTests::skullAxeInfusionRecipeMatchesOriginal);
        FmGameTestBootstrap.register(registry, "diabolist_fork_research_and_recipe", ScribingResearchGameTests::diabolistForkResearchAndRecipe);
        FmGameTestBootstrap.register(registry, "taint_coal_is_registered_and_is_fuel", ItemRegistrationGameTests::taintCoalIsRegisteredAndIsFuel);
        FmGameTestBootstrap.register(registry, "taint_coal_burn_time_varies_within_original_range", ItemRegistrationGameTests::taintCoalBurnTimeVariesWithinOriginalRange);
        FmGameTestBootstrap.register(registry, "emerald_fragment_is_registered", ItemRegistrationGameTests::emeraldFragmentIsRegistered);
        FmGameTestBootstrap.register(registry, "riding_crop_is_registered_as_sword", ItemRegistrationGameTests::ridingCropIsRegisteredAsSword);
        FmGameTestBootstrap.register(registry, "wand_rods_register_as_functional_cores", WandGameTests::wandRodsRegisterAsFunctionalCores);
        FmGameTestBootstrap.register(registry, "creative_tab_is_registered_with_fork_icon", ItemRegistrationGameTests::creativeTabIsRegisteredWithForkIcon);
        FmGameTestBootstrap.register(registry, "creative_tab_lists_every_registered_item", ItemRegistrationGameTests::creativeTabListsEveryRegisteredItem);
        FmGameTestBootstrap.register(registry, "orichalcum_cap_is_registered", ItemRegistrationGameTests::orichalcumCapIsRegistered);
        FmGameTestBootstrap.register(registry, "diabolist_fork_switches_to_the_spork_model", ItemRegistrationGameTests::diabolistForkSwitchesToTheSporkModel);
        FmGameTestBootstrap.register(registry, "lust_shard_switches_to_the_despair_model", ItemRegistrationGameTests::lustShardSwitchesToTheDespairModel);
        FmGameTestBootstrap.register(registry, "creative_wand_family_matches_original", WandGameTests::creativeWandFamilyMatchesOriginal);
        FmGameTestBootstrap.register(registry, "morph_phase_tints_match_original", ItemMechanicsGameTests::morphPhaseTintsMatchOriginal);
        FmGameTestBootstrap.register(registry, "tool_and_skull_axe_chance_rules_preserve_original_boundaries", ItemMechanicsGameTests::toolAndSkullAxeChanceRulesPreserveOriginalBoundaries);
        FmGameTestBootstrap.register(registry, "silverfish_emerald_drop_thresholds_match_original", ItemMechanicsGameTests::silverfishEmeraldDropThresholdsMatchOriginal);
        FmGameTestBootstrap.register(registry, "wand_tooltip_kinds_preserve_original_hints", WandGameTests::wandTooltipKindsPreserveOriginalHints);
        FmGameTestBootstrap.register(registry, "crystalwell_is_a_scribing_tool_with_original_durability", ScribingResearchGameTests::crystalwellIsAScribingToolWithOriginalDurability);
        FmGameTestBootstrap.register(registry, "primewell_is_a_permanent_epic_scribing_tool", ScribingResearchGameTests::primewellIsAPermanentEpicScribingTool);
        FmGameTestBootstrap.register(registry, "scribe_wells_keep_original_recipes_and_research", ScribingResearchGameTests::scribeWellsKeepOriginalRecipesAndResearch);
        FmGameTestBootstrap.register(registry, "research_prefix_respects_tag_research", ScribingResearchGameTests::researchPrefixRespectsTagResearch);
        FmGameTestBootstrap.register(registry, "item_translation_keys_are_not_nested_under_research_pages", ScribingResearchGameTests::itemTranslationKeysAreNotNestedUnderResearchPages);
        FmGameTestBootstrap.register(registry, "research_translation_keys_are_complete", ScribingResearchGameTests::researchTranslationKeysAreComplete);
        FmGameTestBootstrap.register(registry, "crystalwell_research_page_matches_difficulty", ScribingResearchGameTests::crystalwellResearchPageMatchesDifficulty);
        FmGameTestBootstrap.register(registry, "infernal_and_tainted_wand_damage_behaviors_match_original", WandGameTests::infernalAndTaintedWandDamageBehaviorsMatchOriginal);
        FmGameTestBootstrap.register(registry, "hellfire_trigger_targets_netherrack_and_obsidian", WandGameTests::hellfireTriggerTargetsNetherrackAndObsidian);
        FmGameTestBootstrap.register(registry, "subcollar_places_on_another_player_and_stores_damage_vis", ItemMechanicsGameTests::subcollarPlacesOnAnotherPlayerAndStoresDamageVis);
        FmGameTestBootstrap.register(registry, "riding_crop_effects_match_original", ItemMechanicsGameTests::ridingCropEffectsMatchOriginal);
        FmGameTestBootstrap.register(registry, "heretic_trades_match_original_listing", ItemMechanicsGameTests::hereticTradesMatchOriginalListing);
        FmGameTestBootstrap.register(registry, "witchwood_requires_ars_magica", ItemMechanicsGameTests::witchwoodRequiresArsMagica);
        FmGameTestBootstrap.register(registry, "wrath_shard_formula_matches_original", ItemMechanicsGameTests::wrathShardFormulaMatchesOriginal);
        FmGameTestBootstrap.register(registry, "alchemical_cap_and_equivalent_rod_match_original", WandGameTests::alchemicalCapAndEquivalentRodMatchOriginal);
        FmGameTestBootstrap.register(registry, "ring_of_nutrition_only_works_in_ring_slots", ItemRegistrationGameTests::ringOfNutritionOnlyWorksInRingSlots);
        FmGameTestBootstrap.register(registry, "blink_focus_upgrades_use_original_icon_assets", WandGameTests::blinkFocusUpgradesUseOriginalIconAssets);
        FmGameTestBootstrap.register(registry, "research_icons_resolve_to_real_content", ScribingResearchGameTests::researchIconsResolveToRealContent);
        FmGameTestBootstrap.register(registry, "blink_focus_hellfire_applies_through_focal_manipulator", WandGameTests::blinkFocusHellfireAppliesThroughFocalManipulator);
        FmGameTestBootstrap.register(registry, "blink_focus_reaches_beyond_wand_reach_and_lands_like_original", WandGameTests::blinkFocusReachesBeyondWandReachAndLandsLikeOriginal);
        FmGameTestBootstrap.register(registry, "wand_vis_and_parts_use_neutral_access_ports", WandGameTests::wandVisAndPartsUseNeutralAccessPorts);
        FmGameTestBootstrap.register(registry, "hellfire_behavior_uses_host_neutral_access", WandGameTests::hellfireBehaviorUsesHostNeutralAccess);
        FmGameTestBootstrap.register(registry, "scribe_well_aspect_ids_stay_semantic", ScribingResearchGameTests::scribeWellAspectIdsStaySemantic);
        FmGameTestBootstrap.register(registry, "remaining_wand_caps_match_original_costs", WandGameTests::remainingWandCapsMatchOriginalCosts);
        FmGameTestBootstrap.register(registry, "item_data_components_keep_their_ids_and_serialize", ItemRegistrationGameTests::itemDataComponentsKeepTheirIdsAndSerialize);
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
