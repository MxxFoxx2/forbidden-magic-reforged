package dev.visfox.forbiddenreforged.test;

import java.util.List;
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
