package dev.visfox.forbiddenreforged.integration.api;

import dev.visfox.forbiddenreforged.core.aspect.AspectDefinition;
import dev.visfox.forbiddenreforged.core.recipe.RecipeDefinition;
import dev.visfox.forbiddenreforged.core.research.ResearchDefinition;
import java.util.List;

/**
 * Registration boundary implemented by a concrete host integration.
 *
 * <p>Calls are deliberately split by domain and lifecycle phase:
 * <ol>
 *   <li>registerAspects runs before common content that captures aspect references is initialized;</li>
 *   <li>registerRecipes runs after item and recipe registries are available;</li>
 *   <li>registerResearch runs after recipe identifiers and research categories are available.</li>
 * </ol>
 *
 * <p>This SPI contains no host classes. Adapter implementations may depend on their selected
 * host's public API, but common model and gameplay code must not.
 *
 * <p>Runtime operations such as warp, wand vis and essentia transport will be separate narrow
 * ports when the first common mechanics are migrated; they are intentionally not bundled into
 * this registration interface.
 */
public interface HostAdapter {
    HostProfile profile();

    void registerAspects(List<AspectDefinition> aspects);

    void registerRecipes(List<RecipeDefinition> recipes);

    void registerResearch(List<ResearchDefinition> research);
}
