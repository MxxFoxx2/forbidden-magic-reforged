package dev.visfox.forbiddenreforged.test;

import java.util.List;
import java.util.Map;
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
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.integration.api.HostProfile;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class RecipeGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
        DeferredRegister.create(Registries.TEST_FUNCTION, ForbiddenMagic.MODID);

    private static final String[] TESTS = {
        "every_ported_recipe_resolves_its_inputs",
        "chain_from_netherrack_to_wrath_shard",
        "enchantment_recipes_cover_five",
        "morph_recipes_match_the_original",
        "materials_have_crafting_recipes",
        "plant_research_and_recipes",
        "wand_rod_recipes_registered",
        "taintstone_research_and_arcane_recipe",
        "ore_dictionary_entries_became_tags",
        "emerald_transmutation_matches_original",
        "ring_food_recipe_matches_original",
        "subcollar_matches_original",
        "warp_values_match_original",
        "research_layout_matches_original",
        "common_recipe_catalog_matches_tc4_registration",
        "common_research_catalog_matches_tc4_registration",
        "research_catalog_preserves_flags_pages_and_progression",
        "tc4_warp_adapter_preserves_semantic_types_and_item_costs",
        "host_content_identifiers_are_resolved_by_the_tc4_adapter",
        "host_adapter_registry_exposes_tc4_profile",
        "profane_wand_behavior_uses_core_ports"
    };

    static {
        FUNCTIONS.register(TESTS[0], () -> RecipeParityGameTests::everyPortedRecipeResolvesItsInputs);
        FUNCTIONS.register(TESTS[1], () -> RecipeParityGameTests::chainFromNetherrackToWrathShard);
        FUNCTIONS.register(TESTS[2], () -> RecipeParityGameTests::enchantmentRecipesCoverFive);
        FUNCTIONS.register(TESTS[3], () -> RecipeParityGameTests::morphRecipesMatchTheOriginal);
        FUNCTIONS.register(TESTS[4], () -> RecipeParityGameTests::materialsHaveCraftingRecipes);
        FUNCTIONS.register(TESTS[5], () -> RecipeParityGameTests::plantResearchAndRecipes);
        FUNCTIONS.register(TESTS[6], () -> RecipeParityGameTests::wandRodRecipesRegistered);
        FUNCTIONS.register(TESTS[7], () -> RecipeParityGameTests::taintstoneResearchAndArcaneRecipe);
        FUNCTIONS.register(TESTS[8], () -> RecipeParityGameTests::oreDictionaryEntriesBecameTags);
        FUNCTIONS.register(TESTS[9], () -> RecipeParityGameTests::emeraldTransmutationMatchesOriginal);
        FUNCTIONS.register(TESTS[10], () -> RecipeParityGameTests::ringFoodRecipeMatchesOriginal);
        FUNCTIONS.register(TESTS[11], () -> RecipeParityGameTests::subcollarMatchesOriginal);
        FUNCTIONS.register(TESTS[12], () -> RecipeParityGameTests::warpValuesMatchOriginal);
        FUNCTIONS.register(TESTS[13], () -> RecipeParityGameTests::researchLayoutMatchesOriginal);
        FUNCTIONS.register(TESTS[14], () -> HostIntegrationGameTests::commonRecipeCatalogMatchesTc4Registration);
        FUNCTIONS.register(TESTS[15], () -> HostIntegrationGameTests::commonResearchCatalogMatchesTc4Registration);
        FUNCTIONS.register(TESTS[16], () -> HostIntegrationGameTests::researchCatalogPreservesFlagsPagesAndProgression);
        FUNCTIONS.register(TESTS[17], () -> HostIntegrationGameTests::tc4WarpAdapterPreservesSemanticTypesAndItemCosts);
        FUNCTIONS.register(TESTS[18], () -> HostIntegrationGameTests::hostContentIdentifiersAreResolvedByTc4Adapter);
        FUNCTIONS.register(TESTS[19], () -> HostIntegrationGameTests::hostAdapterRegistryExposesTc4Profile);
        FUNCTIONS.register(TESTS[20], () -> HostIntegrationGameTests::profaneWandBehaviorUsesCorePorts);
    }

    private RecipeGameTests() {}

    @SubscribeEvent
    static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
            Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, "fm_recipes"), new TestEnvironmentDefinition.AllOf());
        for (String name : TESTS) {
            ResourceKey<Consumer<GameTestHelper>> function =
                ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, name));
            event.registerTest(
                Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, name),
                new FunctionGameTestInstance(function, new TestData<>(environment, Identifier.parse("thaumcraft:test/platform"), 100, 0, true))
            );
        }
    }

    

    

    private static void assertTc4FeatureSupport(GameTestHelper helper, HostProfile profile) {
        Map<HostProfile.Feature, HostProfile.SupportLevel> expected = Map.ofEntries(
            Map.entry(HostProfile.Feature.ASPECT_REGISTRATION, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.ASPECT_RECIPE_DERIVATION, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.ASPECT_SCAN_TARGETS, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.ASPECT_DIAGNOSTICS, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.ASPECT_PRESENTATION, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.RUNTIME_BOOTSTRAP, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.MORPH_TOOL_RUNTIME, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.WAND_INSPECTION, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.ACCESSORY_INTEGRATION, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.TAINT_BLOCK_SERVICES, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.ENCHANTMENT_EFFECTS, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.SCRIBING_RUNTIME, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.ITEM_PROPERTIES, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.ITEM_FACTORIES, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.BLOCK_ENTITY_IMPLEMENTATION, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.RESEARCH_ENTRIES, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.RESEARCH_STAGES, HostProfile.SupportLevel.PARTIAL),
            Map.entry(HostProfile.Feature.ARCANE_RECIPES, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.CRAFTING_RECIPE_REFERENCES, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.CRUCIBLE_RECIPES, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.INFUSION_RECIPES, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.INFUSION_ENCHANTMENTS, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.WAND_RODS, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.WAND_CAPS, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.WAND_TRIGGERS, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.OPTIONAL_ENCHANTMENT_INTEGRATION, HostProfile.SupportLevel.PARTIAL),
            Map.entry(HostProfile.Feature.WAND_VIS_STORAGE, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.WAND_PARTS, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.FOCI_OR_SPELLS, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.ESSENTIA_STORAGE, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.ESSENTIA_TRANSPORT, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.WARP_POOLS, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.STICKY_WARP, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.TAINT_ORIGINS, HostProfile.SupportLevel.UNSUPPORTED),
            Map.entry(HostProfile.Feature.ENTITY_ASPECTS, HostProfile.SupportLevel.ADAPTED),
            Map.entry(HostProfile.Feature.ENTITY_CLASSIFICATION, HostProfile.SupportLevel.ADAPTED)
        );
        helper.assertValueEqual(expected.size(), HostProfile.Feature.values().length,
            "every HostProfile feature has exactly one expected support level");
        for (Map.Entry<HostProfile.Feature, HostProfile.SupportLevel> feature : expected.entrySet()) {
            helper.assertValueEqual(
                profile.supportOf(feature.getKey()).level(),
                feature.getValue(),
                "TC4 host feature support: " + feature.getKey()
            );
        }
    }

    

    

    

    

    

    

    

    private static void checkIngredient(TcIngredient ingredient, List<String> missing) {
        if (!ingredient.exists()) {
            missing.add(ingredient.toString());
        }
    }

    private static void checkStack(TcStack stack, List<String> missing) {
        if (!stack.exists()) {
            missing.add(stack.item().toString());
        }
    }

    

    

    

    

    

    

    

    

    
    
    

    

    private static void assertResearchLayout(GameTestHelper helper, String id, int column, int row, int complexity) {
        ResearchItem research = ResearchCategories.getResearch(id);
        helper.assertTrue(research != null, id + " research exists");
        helper.assertValueEqual(research.column(), column, id + " column");
        helper.assertValueEqual(research.row(), row, id + " row");
        helper.assertValueEqual(research.complexity(), complexity, id + " complexity");
    }

    

    

}
