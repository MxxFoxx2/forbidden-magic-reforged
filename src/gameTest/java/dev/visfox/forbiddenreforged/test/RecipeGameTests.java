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
import dev.visfox.forbiddenreforged.common.FmIds;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcStack;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;

@EventBusSubscriber(modid = FmIds.MODID)
public final class RecipeGameTests {

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

    public static void registerFunctions(net.minecraft.core.Registry<java.util.function.Consumer<net.minecraft.gametest.framework.GameTestHelper>> registry) {
        FmGameTestBootstrap.register(registry, "every_ported_recipe_resolves_its_inputs", RecipeParityGameTests::everyPortedRecipeResolvesItsInputs);
        FmGameTestBootstrap.register(registry, "chain_from_netherrack_to_wrath_shard", RecipeParityGameTests::chainFromNetherrackToWrathShard);
        FmGameTestBootstrap.register(registry, "enchantment_recipes_cover_five", RecipeParityGameTests::enchantmentRecipesCoverFive);
        FmGameTestBootstrap.register(registry, "morph_recipes_match_the_original", RecipeParityGameTests::morphRecipesMatchTheOriginal);
        FmGameTestBootstrap.register(registry, "materials_have_crafting_recipes", RecipeParityGameTests::materialsHaveCraftingRecipes);
        FmGameTestBootstrap.register(registry, "plant_research_and_recipes", RecipeParityGameTests::plantResearchAndRecipes);
        FmGameTestBootstrap.register(registry, "wand_rod_recipes_registered", RecipeParityGameTests::wandRodRecipesRegistered);
        FmGameTestBootstrap.register(registry, "taintstone_research_and_arcane_recipe", RecipeParityGameTests::taintstoneResearchAndArcaneRecipe);
        FmGameTestBootstrap.register(registry, "ore_dictionary_entries_became_tags", RecipeParityGameTests::oreDictionaryEntriesBecameTags);
        FmGameTestBootstrap.register(registry, "emerald_transmutation_matches_original", RecipeParityGameTests::emeraldTransmutationMatchesOriginal);
        FmGameTestBootstrap.register(registry, "ring_food_recipe_matches_original", RecipeParityGameTests::ringFoodRecipeMatchesOriginal);
        FmGameTestBootstrap.register(registry, "subcollar_matches_original", RecipeParityGameTests::subcollarMatchesOriginal);
        FmGameTestBootstrap.register(registry, "warp_values_match_original", RecipeParityGameTests::warpValuesMatchOriginal);
        FmGameTestBootstrap.register(registry, "research_layout_matches_original", RecipeParityGameTests::researchLayoutMatchesOriginal);
        FmGameTestBootstrap.register(registry, "common_recipe_catalog_matches_tc4_registration", HostIntegrationGameTests::commonRecipeCatalogMatchesTc4Registration);
        FmGameTestBootstrap.register(registry, "common_research_catalog_matches_tc4_registration", HostIntegrationGameTests::commonResearchCatalogMatchesTc4Registration);
        FmGameTestBootstrap.register(registry, "research_catalog_preserves_flags_pages_and_progression", HostIntegrationGameTests::researchCatalogPreservesFlagsPagesAndProgression);
        FmGameTestBootstrap.register(registry, "tc4_warp_adapter_preserves_semantic_types_and_item_costs", HostIntegrationGameTests::tc4WarpAdapterPreservesSemanticTypesAndItemCosts);
        FmGameTestBootstrap.register(registry, "host_content_identifiers_are_resolved_by_the_tc4_adapter", HostIntegrationGameTests::hostContentIdentifiersAreResolvedByTc4Adapter);
        FmGameTestBootstrap.register(registry, "host_adapter_registry_exposes_tc4_profile", HostIntegrationGameTests::hostAdapterRegistryExposesTc4Profile);
        FmGameTestBootstrap.register(registry, "profane_wand_behavior_uses_core_ports", HostIntegrationGameTests::profaneWandBehaviorUsesCorePorts);
    }

    private RecipeGameTests() {}

    @SubscribeEvent
    static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
            Identifier.fromNamespaceAndPath(FmIds.MODID, "fm_recipes"), new TestEnvironmentDefinition.AllOf());
        for (String name : TESTS) {
            ResourceKey<Consumer<GameTestHelper>> function =
                ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath(FmIds.MODID, name));
            event.registerTest(
                Identifier.fromNamespaceAndPath(FmIds.MODID, name),
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
