package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.aspect.AspectDefinition;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.recipe.RecipeDefinition;
import dev.visfox.forbiddenreforged.core.research.ResearchCategoryDefinition;
import dev.visfox.forbiddenreforged.core.research.ResearchDefinition;
import dev.visfox.forbiddenreforged.integration.api.HostAdapter;
import dev.visfox.forbiddenreforged.integration.api.HostProfile;
import dev.visfox.forbiddenreforged.integration.api.HostServices;
import java.util.List;
import java.util.Map;

/**
 * Thaumcraft 4: Reforged implementation of the host-neutral content contract.
 *
 * Registration/lifecycle methods stay here; every runtime capability port is
 * composed once in {@link Tc4Services} and exposed through {@link #services()}.
 */
public final class Tc4HostAdapter implements HostAdapter {
    public static final Tc4HostAdapter INSTANCE = new Tc4HostAdapter();

    private static final HostProfile PROFILE = new HostProfile(
        ContentId.parse("host:tc4-reforged"),
        "1.1.3",
        "26.3",
        Map.ofEntries(
            Map.entry(HostProfile.Feature.ASPECT_REGISTRATION,
                HostProfile.FeatureSupport.adapted("Uses the AddonAspects registration API")),
            Map.entry(HostProfile.Feature.ASPECT_RECIPE_DERIVATION,
                HostProfile.FeatureSupport.adapted("Passes semantic aspect component lists to the TC4 AddonAspects registrar")),
            Map.entry(HostProfile.Feature.ASPECT_SCAN_TARGETS,
                HostProfile.FeatureSupport.adapted("Registers item, block and entity scan targets through the TC4 AspectSync API")),
            Map.entry(HostProfile.Feature.ASPECT_DIAGNOSTICS,
                HostProfile.FeatureSupport.adapted("Reports native and add-on aspects from the TC4 registry")),
            Map.entry(HostProfile.Feature.ASPECT_PRESENTATION,
                HostProfile.FeatureSupport.adapted("Resolves display components for host aspects")),
            Map.entry(HostProfile.Feature.RUNTIME_BOOTSTRAP,
                HostProfile.FeatureSupport.adapted("Initializes TC4 warp, wand, recipe, research and add-on integrations")),
            Map.entry(HostProfile.Feature.MORPH_TOOL_RUNTIME,
                HostProfile.FeatureSupport.adapted("Provides morph-tool cycle sounds and area-mining behavior")),
            Map.entry(HostProfile.Feature.WAND_INSPECTION,
                HostProfile.FeatureSupport.adapted("Provides wand detection, rod tags, and focus-upgrade queries")),
            Map.entry(HostProfile.Feature.WAND_VIS_STORAGE,
                HostProfile.FeatureSupport.adapted("Adds semantic aspects to wand and real Vis stores")),
            Map.entry(HostProfile.Feature.ACCESSORY_INTEGRATION,
                HostProfile.FeatureSupport.adapted("Reads host accessory slots through the accessory provider API")),
            Map.entry(HostProfile.Feature.TAINT_BLOCK_SERVICES,
                HostProfile.FeatureSupport.adapted("Provides taint block classification and taint-shovel behavior")),
            Map.entry(HostProfile.Feature.ENCHANTMENT_EFFECTS,
                HostProfile.FeatureSupport.adapted("Provides host special-mining drops and taint damage-source classification")),
            Map.entry(HostProfile.Feature.TAINT_ORIGINS,
                HostProfile.FeatureSupport.unsupported("No host taint-origin registration or propagation adapter is implemented")),
            Map.entry(HostProfile.Feature.SMELTING_BONUSES,
                HostProfile.FeatureSupport.unsupported("TC4 keeps the smelting-bonus table internal; the add-on wakes up once the host publishes a registration API")),
            Map.entry(HostProfile.Feature.SCRIBING_RUNTIME,
                HostProfile.FeatureSupport.adapted("Provides research difficulty and player aspect-pool access")),
            Map.entry(HostProfile.Feature.WARP_POOLS,
                HostProfile.FeatureSupport.adapted("Routes semantic permanent, sticky, and temporary warp changes into TC4 Warp")),
            Map.entry(HostProfile.Feature.STICKY_WARP,
                HostProfile.FeatureSupport.adapted("Preserves sticky warp as the distinct TC4 WarpType.STICKY pool")),
            Map.entry(HostProfile.Feature.WAND_RODS,
                HostProfile.FeatureSupport.adapted("Registers TC4 wand rods and their update behaviors")),
            Map.entry(HostProfile.Feature.WAND_CAPS,
                HostProfile.FeatureSupport.adapted("Registers TC4 wand caps and material properties")),
            Map.entry(HostProfile.Feature.WAND_PARTS,
                HostProfile.FeatureSupport.adapted("Replaces wand rod parts through the TC4 WandParts component while preserving the installed cap")),
            Map.entry(HostProfile.Feature.WAND_TRIGGERS,
                HostProfile.FeatureSupport.adapted("Registers TC4 wand block triggers")),
            Map.entry(HostProfile.Feature.FOCI_OR_SPELLS,
                HostProfile.FeatureSupport.adapted("Integrates the Blink Focus with the TC4 wand/focus API")),
            Map.entry(HostProfile.Feature.OPTIONAL_ENCHANTMENT_INTEGRATION,
                HostProfile.FeatureSupport.partial("Thaumic Tinkerer enchantment costs are registered only when the optional mod is installed")),
            Map.entry(HostProfile.Feature.ITEM_PROPERTIES,
                HostProfile.FeatureSupport.adapted("Applies TC4 tool-material, food, focus, and repair Item.Properties")),
            Map.entry(HostProfile.Feature.ITEM_FACTORIES,
                HostProfile.FeatureSupport.adapted("Creates TC4-backed morph tools, baubles, wells, focus, collar, and primordial pearl items")),
            Map.entry(HostProfile.Feature.BLOCK_ENTITY_IMPLEMENTATION,
                HostProfile.FeatureSupport.adapted("Provides the TC4 EssentiaSource/EssentiaTransport Wrath Cage bridge implementation")),
            Map.entry(HostProfile.Feature.ESSENTIA_STORAGE,
                HostProfile.FeatureSupport.adapted("Wrath Cage stores aspect quantities through the TC4 AspectSource container contract")),
            Map.entry(HostProfile.Feature.ESSENTIA_TRANSPORT,
                HostProfile.FeatureSupport.adapted("Wrath Cage accepts and exposes aspect transport through the TC4 EssentiaTransport contract")),
            Map.entry(HostProfile.Feature.ENTITY_ASPECTS,
                HostProfile.FeatureSupport.adapted("Provides vanilla and TC4 entity-to-aspect mappings as semantic ContentId values")),
            Map.entry(HostProfile.Feature.ENTITY_CLASSIFICATION,
                HostProfile.FeatureSupport.adapted("Provides TC4 tainted-mob, boss, and golem predicates")),
            Map.entry(HostProfile.Feature.RESEARCH_ENTRIES,
                HostProfile.FeatureSupport.adapted("Translates common research definitions to ResearchItem")),
            Map.entry(HostProfile.Feature.RESEARCH_STAGES,
                HostProfile.FeatureSupport.partial("Only one page group is supported; multi-stage definitions are rejected")),
            Map.entry(HostProfile.Feature.ARCANE_RECIPES,
                HostProfile.FeatureSupport.adapted("Translates common arcane recipe definitions")),
            Map.entry(HostProfile.Feature.CRAFTING_RECIPE_REFERENCES,
                HostProfile.FeatureSupport.adapted("Translates common crafting recipe references")),
            Map.entry(HostProfile.Feature.CRUCIBLE_RECIPES,
                HostProfile.FeatureSupport.adapted("Translates common crucible recipe definitions")),
            Map.entry(HostProfile.Feature.INFUSION_RECIPES,
                HostProfile.FeatureSupport.adapted("Translates common infusion recipe definitions")),
            Map.entry(HostProfile.Feature.INFUSION_ENCHANTMENTS,
                HostProfile.FeatureSupport.adapted("Translates common infusion enchantment definitions"))
        )
    );

    private Tc4HostAdapter() {
    }

    @Override
    public HostProfile profile() {
        return PROFILE;
    }

    @Override
    public HostServices services() {
        return Tc4Services.INSTANCE;
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
    public void registerAspectTags() {
        Tc4AspectTagAdapter.register();
    }

    @Override
    public void registerAspects(List<AspectDefinition> aspects) {
        try {
            Tc4AspectAdapter.registerAspects(aspects);
        } catch (NoSuchMethodError | NoClassDefFoundError absent) {
            throw new IllegalStateException(
                "Forbidden Magic requires Thaumcraft 4: Reforged 1.1.3 or newer with the add-on aspect API",
                absent
            );
        }
    }

    @Override
    public boolean crystalwellUsesShardTag() {
        return Tc4RecipeAdapter.crystalwellUsesShardTag();
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
