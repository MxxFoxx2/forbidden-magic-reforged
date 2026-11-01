package dev.visfox.forbiddenreforged.integration.api;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.Map;
import java.util.Objects;

public record HostProfile(
    ContentId id,
    String hostVersion,
    String minecraftVersion,
    Map<Feature, FeatureSupport> features
) {
    public HostProfile {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(hostVersion, "hostVersion");
        Objects.requireNonNull(minecraftVersion, "minecraftVersion");
        Objects.requireNonNull(features, "features");
        if (hostVersion.isBlank() || minecraftVersion.isBlank()) {
            throw new IllegalArgumentException("Host and Minecraft versions cannot be blank");
        }
        features = Map.copyOf(features);
    }

    public FeatureSupport supportOf(Feature feature) {
        Objects.requireNonNull(feature, "feature");
        return features.getOrDefault(feature, FeatureSupport.unsupported("Not declared by this adapter"));
    }

    public enum Feature {
        ASPECT_REGISTRATION,
        ASPECT_DIAGNOSTICS,
        ASPECT_PRESENTATION,
        RUNTIME_BOOTSTRAP,
        MORPH_TOOL_RUNTIME,
        WAND_INSPECTION,
        ACCESSORY_INTEGRATION,
        TAINT_RUNTIME,
        SCRIBING_RUNTIME,
        ITEM_PROPERTIES,
        ITEM_FACTORIES,
        BLOCK_ENTITY_IMPLEMENTATION,
        ASPECT_RECIPE_DERIVATION,
        ASPECT_SCAN_TARGETS,
        RESEARCH_ENTRIES,
        RESEARCH_STAGES,
        ARCANE_RECIPES,
        CRAFTING_RECIPE_REFERENCES,
        CRUCIBLE_RECIPES,
        INFUSION_RECIPES,
        INFUSION_ENCHANTMENTS,
        WAND_RODS,
        WAND_CAPS,
        WAND_TRIGGERS,
        OPTIONAL_ENCHANTMENT_INTEGRATION,
        WAND_VIS_STORAGE,
        WAND_PARTS,
        FOCI_OR_SPELLS,
        ESSENTIA_STORAGE,
        ESSENTIA_TRANSPORT,
        WARP_POOLS,
        STICKY_WARP,
        TAINT_ORIGINS,
        ENTITY_ASPECTS,
        ENTITY_CLASSIFICATION
    }

    public enum SupportLevel {
        NATIVE,
        ADAPTED,
        PARTIAL,
        UNSUPPORTED
    }

    public record FeatureSupport(SupportLevel level, String notes) {
        public FeatureSupport {
            Objects.requireNonNull(level, "level");
            Objects.requireNonNull(notes, "notes");
        }

        public static FeatureSupport nativeSupport(String notes) {
            return new FeatureSupport(SupportLevel.NATIVE, notes);
        }

        public static FeatureSupport adapted(String notes) {
            return new FeatureSupport(SupportLevel.ADAPTED, notes);
        }

        public static FeatureSupport partial(String notes) {
            return new FeatureSupport(SupportLevel.PARTIAL, notes);
        }

        public static FeatureSupport unsupported(String notes) {
            return new FeatureSupport(SupportLevel.UNSUPPORTED, notes);
        }
    }
}
