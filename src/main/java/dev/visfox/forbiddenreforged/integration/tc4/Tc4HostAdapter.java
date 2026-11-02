package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.FmAspectTags;
import dev.visfox.forbiddenreforged.core.aspect.AspectDefinition;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.recipe.RecipeDefinition;
import dev.visfox.forbiddenreforged.core.research.ResearchCategoryDefinition;
import dev.visfox.forbiddenreforged.core.research.ResearchDefinition;
import dev.visfox.forbiddenreforged.core.warp.WarpKind;
import dev.visfox.forbiddenreforged.integration.api.HostAdapter;
import dev.visfox.forbiddenreforged.integration.api.HostProfile;
import dev.visfox.forbiddenreforged.integration.runtime.HostAspectServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostWarpServices;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Thaumcraft 4: Reforged adapter for the Minecraft 1.21.1 release track.
 *
 * Shared semantic catalogs are registered through this boundary. Existing
 * 1.21.1 gameplay integrations remain intact while their extraction into
 * focused host capabilities proceeds incrementally.
 */
public final class Tc4HostAdapter implements HostAdapter, HostAspectServices, HostWarpServices {
    public static final Tc4HostAdapter INSTANCE = new Tc4HostAdapter();

    private static final String LEGACY_RUNTIME_NOTE =
        "Still implemented by the 1.21.1 legacy gameplay path; extraction behind this host capability is pending";

    private static final HostProfile PROFILE = createProfile();

    private Tc4HostAdapter() {
    }

    private static HostProfile createProfile() {
        Map<HostProfile.Feature, HostProfile.FeatureSupport> support =
            new EnumMap<>(HostProfile.Feature.class);
        for (HostProfile.Feature feature : HostProfile.Feature.values()) {
            support.put(feature, HostProfile.FeatureSupport.partial(LEGACY_RUNTIME_NOTE));
        }

        adapted(support, HostProfile.Feature.ASPECT_REGISTRATION,
            "Registers shared semantic aspect definitions through AddonAspects");
        adapted(support, HostProfile.Feature.ASPECT_DIAGNOSTICS,
            "Reads the TC4 aspect registry for startup diagnostics");
        adapted(support, HostProfile.Feature.ASPECT_PRESENTATION,
            "Resolves semantic aspect identifiers to TC4 display components");
        adapted(support, HostProfile.Feature.RUNTIME_BOOTSTRAP,
            "Bootstraps shared recipe and research catalogs and preserves 1.21.1 wand registration");
        adapted(support, HostProfile.Feature.ASPECT_RECIPE_DERIVATION,
            "Uses semantic aspect definitions and component identifiers");
        adapted(support, HostProfile.Feature.ASPECT_SCAN_TARGETS,
            "Registers scan targets through the existing 1.21.1 AspectSync implementation");
        adapted(support, HostProfile.Feature.RESEARCH_ENTRIES,
            "Translates shared research definitions to TC4 ResearchItem entries");
        support.put(HostProfile.Feature.RESEARCH_STAGES,
            HostProfile.FeatureSupport.partial("The TC4 adapter accepts one page group; multi-stage definitions are rejected"));
        adapted(support, HostProfile.Feature.ARCANE_RECIPES, "Translates shared arcane recipe definitions");
        adapted(support, HostProfile.Feature.CRAFTING_RECIPE_REFERENCES,
            "Translates shared crafting recipe references");
        adapted(support, HostProfile.Feature.CRUCIBLE_RECIPES, "Translates shared crucible recipe definitions");
        adapted(support, HostProfile.Feature.INFUSION_RECIPES, "Translates shared infusion recipe definitions");
        adapted(support, HostProfile.Feature.INFUSION_ENCHANTMENTS,
            "Translates shared infusion enchantment definitions");
        adapted(support, HostProfile.Feature.WARP_POOLS,
            "Maps semantic warp kinds to the three TC4 warp pools");
        adapted(support, HostProfile.Feature.STICKY_WARP, "Preserves sticky warp as WarpType.STICKY");
        support.put(HostProfile.Feature.TAINT_ORIGINS,
            HostProfile.FeatureSupport.unsupported("No TC4 taint-origin registration or propagation adapter exists"));

        return new HostProfile(
            ContentId.parse("host:tc4-reforged"),
            "1.1.2",
            "1.21.1",
            support
        );
    }

    private static void adapted(
        Map<HostProfile.Feature, HostProfile.FeatureSupport> support,
        HostProfile.Feature feature,
        String notes
    ) {
        support.put(feature, HostProfile.FeatureSupport.adapted(notes));
    }

    @Override
    public HostProfile profile() {
        return PROFILE;
    }

    @Override
    public void registerAspectTags() {
        FmAspectTags.register();
    }

    @Override
    public boolean crystalwellUsesShardTag() {
        return Tc4RecipeAdapter.crystalwellUsesShardTag();
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
        Tc4AspectAdapter.registerAspects(aspects);
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

    @Override
    public Component aspectDescription(ContentId aspect) {
        return Tc4AspectAdapter.description(aspect);
    }

    @Override
    public void changeWarp(ServerPlayer player, int amount, WarpKind kind) {
        Tc4WarpAdapter.changeWarp(player, amount, kind);
    }
}
