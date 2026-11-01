package dev.visfox.forbiddenreforged.integration.api;

import dev.visfox.forbiddenreforged.core.aspect.AspectDefinition;
import dev.visfox.forbiddenreforged.core.recipe.RecipeDefinition;
import dev.visfox.forbiddenreforged.core.research.ResearchCategoryDefinition;
import dev.visfox.forbiddenreforged.core.research.ResearchDefinition;
import java.util.List;

public interface HostAdapter {
    HostProfile profile();

    void registerAspects(List<AspectDefinition> aspects);

    void registerRecipes(List<RecipeDefinition> recipes);

    void registerResearch(
        List<ResearchCategoryDefinition> categories,
        List<ResearchDefinition> research
    );
}
