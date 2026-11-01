package dev.visfox.forbiddenreforged.core.recipe;

import dev.visfox.forbiddenreforged.core.aspect.AspectDefinition;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Host-neutral descriptions of Forbidden Magic recipe content.
 *
 * <p>These are domain data, not Minecraft Recipe implementations. A host adapter translates each
 * variant into the recipe serializers, registries and research links provided by that host.
 */
public sealed interface RecipeDefinition
    permits RecipeDefinition.Infusion, RecipeDefinition.InfusionEnchantment,
        RecipeDefinition.Crucible, RecipeDefinition.ArcaneShaped,
        RecipeDefinition.ArcaneShapeless, RecipeDefinition.CraftingReference {

    /** Stable, host-independent identifier of this recipe definition. */
    ContentId id();

    /** Semantic research entry that owns this recipe, if any. */
    Optional<ContentId> research();

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

        public static Ingredient item(ContentId id, int count) {
            return new Ingredient(id, IngredientKind.ITEM, count);
        }

        public static Ingredient tag(ContentId id) {
            return new Ingredient(id, IngredientKind.TAG, 1);
        }

        public static Ingredient tag(ContentId id, int count) {
            return new Ingredient(id, IngredientKind.TAG, count);
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

    record Infusion(
        ContentId id,
        Optional<ContentId> research,
        Output result,
        int instability,
        AspectDefinition.Cost aspects,
        Ingredient central,
        List<Ingredient> components
    ) implements RecipeDefinition {
        public Infusion {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(research, "research");
            Objects.requireNonNull(result, "result");
            Objects.requireNonNull(aspects, "aspects");
            Objects.requireNonNull(central, "central");
            Objects.requireNonNull(components, "components");
            components = List.copyOf(components);
            if (instability < 0) {
                throw new IllegalArgumentException("Instability cannot be negative");
            }
        }
    }

    /**
     * Recipe that applies a Forbidden Magic enchantment to a compatible target. The target is
     * selected by the host's infusion-enchantment system, so this definition has no fixed output.
     */
    record InfusionEnchantment(
        ContentId id,
        Optional<ContentId> research,
        ContentId enchantment,
        int instability,
        AspectDefinition.Cost aspects,
        List<Ingredient> components
    ) implements RecipeDefinition {
        public InfusionEnchantment {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(research, "research");
            Objects.requireNonNull(enchantment, "enchantment");
            Objects.requireNonNull(aspects, "aspects");
            Objects.requireNonNull(components, "components");
            components = List.copyOf(components);
            if (instability < 0) {
                throw new IllegalArgumentException("Instability cannot be negative");
            }
        }
    }

    record Crucible(
        ContentId id,
        Optional<ContentId> research,
        Output result,
        Ingredient catalyst,
        AspectDefinition.Cost aspects
    ) implements RecipeDefinition {
        public Crucible {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(research, "research");
            Objects.requireNonNull(result, "result");
            Objects.requireNonNull(catalyst, "catalyst");
            Objects.requireNonNull(aspects, "aspects");
        }
    }

    /**
     * Pattern cells are row-major and have exactly width * height entries. Empty cells are
     * represented explicitly so adapters do not need to infer shape from a string format.
     */
    record ArcaneShaped(
        ContentId id,
        Optional<ContentId> research,
        Output result,
        int width,
        int height,
        List<Optional<Ingredient>> pattern,
        AspectDefinition.Cost aspects
    ) implements RecipeDefinition {
        public ArcaneShaped {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(research, "research");
            Objects.requireNonNull(result, "result");
            Objects.requireNonNull(pattern, "pattern");
            Objects.requireNonNull(aspects, "aspects");
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
        }
    }

    record ArcaneShapeless(
        ContentId id,
        Optional<ContentId> research,
        Output result,
        List<Ingredient> ingredients,
        AspectDefinition.Cost aspects
    ) implements RecipeDefinition {
        public ArcaneShapeless {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(research, "research");
            Objects.requireNonNull(result, "result");
            Objects.requireNonNull(ingredients, "ingredients");
            Objects.requireNonNull(aspects, "aspects");
            ingredients = List.copyOf(ingredients);
            if (ingredients.isEmpty()) {
                throw new IllegalArgumentException("Arcane shapeless recipe cannot be empty");
            }
        }
    }

    /**
     * Reference to a regular Minecraft crafting recipe that is displayed under a research entry.
     * The host does not own or execute the referenced vanilla recipe.
     */
    record CraftingReference(
        ContentId id,
        Optional<ContentId> research,
        ContentId recipe,
        Output result
    ) implements RecipeDefinition {
        public CraftingReference {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(research, "research");
            Objects.requireNonNull(recipe, "recipe");
            Objects.requireNonNull(result, "result");
        }
    }
}
