package dev.visfox.forbiddenreforged.core.research;

import dev.visfox.forbiddenreforged.core.aspect.AspectDefinition;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Host-neutral research entry, stages and pages.
 *
 * <p>Pages reference translation keys or common recipe identifiers. A host adapter is responsible
 * for translating page kinds, progression rules and presentation into its own research system.
 */
public record ResearchDefinition(
    ContentId id,
    ContentId category,
    ContentId icon,
    int column,
    int row,
    int complexity,
    AspectDefinition.Cost noteCost,
    List<ContentId> parents,
    Set<Flag> flags,
    List<Stage> stages
) {
    public ResearchDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(icon, "icon");
        Objects.requireNonNull(noteCost, "noteCost");
        Objects.requireNonNull(parents, "parents");
        Objects.requireNonNull(flags, "flags");
        Objects.requireNonNull(stages, "stages");
        parents = List.copyOf(parents);
        flags = Set.copyOf(flags);
        stages = List.copyOf(stages);
        if (complexity < 0) {
            throw new IllegalArgumentException("Research complexity cannot be negative");
        }
        if (parents.contains(id)) {
            throw new IllegalArgumentException("Research cannot depend on itself");
        }
        if (stages.isEmpty()) {
            throw new IllegalArgumentException("Research must have at least one stage");
        }
    }

    public enum Flag {
        STUB,
        ROUND_ICON,
        HIDDEN,
        CONCEALED,
        SECONDARY,
        AUTO_UNLOCK
    }

    public enum PageKind {
        TEXT,
        CRAFTING_RECIPE,
        ARCANE_RECIPE,
        CRUCIBLE_RECIPE,
        INFUSION_RECIPE,
        INFUSION_ENCHANTMENT
    }

    /**
     * For TEXT pages, reference is a translation key. For recipe pages, it is a namespaced
     * ContentId string. The distinction stays explicit in pageKind.
     */
    public record Page(PageKind pageKind, String reference) {
        public Page {
            Objects.requireNonNull(pageKind, "pageKind");
            Objects.requireNonNull(reference, "reference");
            if (reference.isBlank()) {
                throw new IllegalArgumentException("Research page reference cannot be blank");
            }
            if (pageKind != PageKind.TEXT) {
                ContentId.parse(reference);
            }
        }

        public static Page text(String translationKey) {
            return new Page(PageKind.TEXT, translationKey);
        }

        public static Page recipe(PageKind kind, ContentId recipeId) {
            if (kind == PageKind.TEXT) {
                throw new IllegalArgumentException("Use Page.text for text pages");
            }
            return new Page(kind, Objects.requireNonNull(recipeId, "recipeId").toString());
        }
    }

    public record Stage(List<Page> pages, List<ContentId> prerequisites, int warp) {
        public Stage {
            Objects.requireNonNull(pages, "pages");
            Objects.requireNonNull(prerequisites, "prerequisites");
            pages = List.copyOf(pages);
            prerequisites = List.copyOf(prerequisites);
            if (warp < 0) {
                throw new IllegalArgumentException("Warp cannot be negative");
            }
        }
    }
}
