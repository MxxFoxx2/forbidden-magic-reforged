package dev.visfox.forbiddenreforged.core.recipe;

import dev.visfox.forbiddenreforged.core.aspect.AspectDefinition;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Host-neutral recipe descriptions used by the common content model.
 *
 * <p>These are not Minecraft Recipe implementations. Adapters translate them into the recipe
 * serializers and registration mechanisms provided by the selected host.
 */
public sealed interface RecipeDefinition
    permits RecipeDefinition.Infusion, RecipeDefinition.Crucible, RecipeDefinition.Arcane {

    ContentId id();

    Output result();

    AspectDefinition.Cost aspects();

    Optional<ResearchGate> research();

    enum IngredientKind {
        ITEM,
        TAG
    }

    record Ingredient(ContentId id, IngredientKind kind, int count) {
        public Ingredient {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(kind, "kind");
            if (count <= 0) {
                throw new IllegalArgumentException("Ingredient count must be positive");
            }
        }

        public static Ingredient item(ContentId id) {
            return new Ingredient(id, IngredientKind.ITEM, 1);
        }

        public static Ingredient tag(ContentId id) {
            return new Ingredient(id, IngredientKind.TAG, 1);
        }
    }

    record Output(ContentId item, int count) {
        public Output {
            Objects.requireNonNull(item, "item");
            if (count <= 0) {
                throw new IllegalArgumentException("Output count must be positive");
            }
        }
    }

    record ResearchGate(ContentId entry, int minimumStage) {
        public ResearchGate {
            Objects.requireNonNull(entry, "entry");
            if (minimumStage < 0) {
                throw new IllegalArgumentException("Minimum research stage cannot be negative");
            }
        }
    }

    record Infusion(
        ContentId id,
        Output result,
        Ingredient catalyst,
        List<Ingredient> components,
        AspectDefinition.Cost aspects,
        int instability,
        Optional<ResearchGate> research
    ) implements RecipeDefinition {
        public Infusion {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(result, "result");
            Objects.requireNonNull(catalyst, "catalyst");
            Objects.requireNonNull(components, "components");
            Objects.requireNonNull(aspects, "aspects");
            Objects.requireNonNull(research, "research");
            components = List.copyOf(components);
            if (instability < 0) {
                throw new IllegalArgumentException("Instability cannot be negative");
            }
        }
    }

    record Crucible(
        ContentId id,
        Output result,
        Ingredient catalyst,
        AspectDefinition.Cost aspects,
        Optional<ResearchGate> research
    ) implements RecipeDefinition {
        public Crucible {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(result, "result");
            Objects.requireNonNull(catalyst, "catalyst");
            Objects.requireNonNull(aspects, "aspects");
            Objects.requireNonNull(research, "research");
        }
    }

    /**
     * Pattern cells are row-major and have exactly width * height entries. Empty cells are
     * represented explicitly so adapters do not need to infer shape from a string format.
     */
    record Arcane(
        ContentId id,
        Output result,
        int width,
        int height,
        List<Optional<Ingredient>> pattern,
        AspectDefinition.Cost aspects,
        int visCost,
        Optional<ResearchGate> research
    ) implements RecipeDefinition {
        public Arcane {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(result, "result");
            Objects.requireNonNull(pattern, "pattern");
            Objects.requireNonNull(aspects, "aspects");
            Objects.requireNonNull(research, "research");
            if (width <= 0 || width > 3 || height <= 0 || height > 3) {
                throw new IllegalArgumentException("Arcane pattern dimensions must be between 1 and 3");
            }
            pattern = List.copyOf(pattern);
            if (pattern.size() != width * height) {
                throw new IllegalArgumentException("Pattern cell count must equal width * height");
            }
            if (pattern.stream().noneMatch(Optional::isPresent)) {
                throw new IllegalArgumentException("Arcane pattern cannot be empty");
            }
            if (visCost < 0) {
                throw new IllegalArgumentException("Vis cost cannot be negative");
            }
        }
    }
}
