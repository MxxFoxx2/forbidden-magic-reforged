package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.aspect.AspectDefinition;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.recipe.RecipeDefinition;
import dev.visfox.forbiddenreforged.core.research.ResearchCategoryDefinition;
import dev.visfox.forbiddenreforged.core.research.ResearchDefinition;
import dev.visfox.forbiddenreforged.integration.api.HostAdapter;
import dev.visfox.forbiddenreforged.integration.api.HostProfile;
import java.util.List;
import java.util.Map;

/**
 * Thaumcraft 4: Reforged implementation of the host-neutral content contract.
 */
public final class Tc4HostAdapter implements HostAdapter {
    public static final Tc4HostAdapter INSTANCE = new Tc4HostAdapter();

    private static final HostProfile PROFILE = new HostProfile(
        ContentId.parse("host:tc4-reforged"),
        "1.1.2",
        "26.3",
        Map.of(
            HostProfile.Feature.ASPECT_REGISTRATION,
            HostProfile.FeatureSupport.adapted("Uses the AddonAspects registration API"),
            HostProfile.Feature.ASPECT_DIAGNOSTICS,
            HostProfile.FeatureSupport.adapted("Reports native and add-on aspects from the TC4 registry"),
            HostProfile.Feature.RUNTIME_BOOTSTRAP,
            HostProfile.FeatureSupport.adapted("Initializes TC4 warp, wand, recipe, research and add-on integrations"),
            HostProfile.Feature.RESEARCH_ENTRIES,
            HostProfile.FeatureSupport.adapted("Translates common research definitions to ResearchItem"),
            HostProfile.Feature.RESEARCH_STAGES,
            HostProfile.FeatureSupport.partial("Only one page group is supported; multi-stage definitions are rejected"),
            HostProfile.Feature.ARCANE_RECIPES,
            HostProfile.FeatureSupport.adapted("Translates common arcane recipe definitions"),
            HostProfile.Feature.CRAFTING_RECIPE_REFERENCES,
            HostProfile.FeatureSupport.adapted("Translates common crafting recipe references"),
            HostProfile.Feature.CRUCIBLE_RECIPES,
            HostProfile.FeatureSupport.adapted("Translates common crucible recipe definitions"),
            HostProfile.Feature.INFUSION_RECIPES,
            HostProfile.FeatureSupport.adapted("Translates common infusion recipe definitions"),
            HostProfile.Feature.INFUSION_ENCHANTMENTS,
            HostProfile.FeatureSupport.adapted("Translates common infusion enchantment definitions")
        )
    );

    private Tc4HostAdapter() {
    }

    @Override
    public HostProfile profile() {
        return PROFILE;
    }

    @Override
    public void initializeRuntime() {
        Tc4BootstrapAdapter.initialize();
    }

    @Override
    public List<String> aspectDiagnostics() {
        return Tc4AspectDiagnostics.registryLines();
    }

    @Override
    public void registerAspects(List<AspectDefinition> aspects) {
        try {
            Tc4AspectAdapter.registerAspects(aspects);
        } catch (NoSuchMethodError | NoClassDefFoundError absent) {
            throw new IllegalStateException(
                "Forbidden Magic requires Thaumcraft 4: Reforged 1.1.2 or newer with the add-on aspect API",
                absent
            );
        }
    }

    @Override
    public void registerRecipes(List<RecipeDefinition> recipes) {
        Tc4RecipeAdapter.registerRecipes(recipes);
    }

    @Override
    public void registerResearch(
        List<ResearchCategoryDefinition> categories,
        List<ResearchDefinition> research
    ) {
        Tc4ResearchAdapter.register(categories, research);
    }
}
