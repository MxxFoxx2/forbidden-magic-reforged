package dev.visfox.forbiddenreforged.integration.api;

import dev.visfox.forbiddenreforged.core.aspect.AspectDefinition;
import dev.visfox.forbiddenreforged.core.recipe.RecipeDefinition;
import dev.visfox.forbiddenreforged.core.research.ResearchCategoryDefinition;
import dev.visfox.forbiddenreforged.core.research.ResearchDefinition;
import java.util.List;

public interface HostAdapter {
    HostProfile profile();

    /** Typed composition of this host's runtime capability ports. */
    HostServices services();

    /** Registers host-native aspect scan targets after custom aspects exist. */
    void registerAspectTags();

    /** Returns the host's configured Crystalwell ingredient mode. */
    boolean crystalwellUsesShardTag();

    /**
     * Performs host-specific runtime bootstrap after common mod registration.
     */
    void initializeRuntime();

    /**
     * Returns human-readable host diagnostics for the shared admin command.
     */
    default List<String> aspectDiagnostics() {
        return List.of();
    }

    void registerAspects(List<AspectDefinition> aspects);

    void registerRecipes(List<RecipeDefinition> recipes);

    void registerResearch(
        List<ResearchCategoryDefinition> categories,
        List<ResearchDefinition> research
    );
}
