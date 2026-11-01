package dev.visfox.forbiddenreforged.core.research;

import dev.visfox.forbiddenreforged.core.aspect.AspectDefinition;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record ResearchDefinition(
    ContentId id,
    ContentId category,
    Icon icon,
    int column,
    int row,
    int complexity,
    AspectDefinition.Cost noteCost,
    List<ContentId> parents,
    List<ContentId> hiddenParents,
    Set<Flag> flags,
    List<ContentId> aspectTriggers,
    List<Stage> stages
) {
    public ResearchDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(icon, "icon");
        Objects.requireNonNull(noteCost, "noteCost");
        Objects.requireNonNull(parents, "parents");
        Objects.requireNonNull(hiddenParents, "hiddenParents");
        Objects.requireNonNull(flags, "flags");
        Objects.requireNonNull(aspectTriggers, "aspectTriggers");
        Objects.requireNonNull(stages, "stages");
        parents = List.copyOf(parents);
        hiddenParents = List.copyOf(hiddenParents);
        flags = Set.copyOf(flags);
        aspectTriggers = List.copyOf(aspectTriggers);
        stages = List.copyOf(stages);
        if (complexity < 0) {
            throw new IllegalArgumentException("Research complexity cannot be negative");
        }
        if (parents.contains(id) || hiddenParents.contains(id)) {
            throw new IllegalArgumentException("Research cannot depend on itself");
        }
        if (stages.isEmpty()) {
            throw new IllegalArgumentException("Research must have at least one stage");
        }
    }

    public enum Flag {
        SPECIAL,
        SECONDARY,
        ROUND_ICON,
        STUB,
        VIRTUAL,
        CONCEALED,
        HIDDEN,
        LOST,
        AUTO_UNLOCK
    }

    public enum IconKind {
        ITEM,
        TEXTURE
    }

    public record Icon(IconKind kind, ContentId value) {
        public Icon {
            Objects.requireNonNull(kind, "kind");
            Objects.requireNonNull(value, "value");
        }

        public static Icon item(ContentId item) {
            return new Icon(IconKind.ITEM, item);
        }

        public static Icon texture(ContentId texture) {
            return new Icon(IconKind.TEXTURE, texture);
        }
    }

    public enum PageKind {
        TEXT,
        CRAFTING_RECIPE,
        ARCANE_RECIPE,
        CRUCIBLE_RECIPE,
        INFUSION_RECIPE,
        INFUSION_ENCHANTMENT
    }

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
            if (pages.isEmpty()) {
                throw new IllegalArgumentException("Research stage must have at least one page");
            }
        }
    }
}
