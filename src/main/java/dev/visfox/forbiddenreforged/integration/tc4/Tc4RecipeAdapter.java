package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.recipe.RecipeDefinition;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.resources.Identifier;
import thaumcraft.crafting.CraftingRecipeRef;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.InfusionEnchantmentRecipe;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.ShapedArcaneRecipe;
import thaumcraft.crafting.ShapelessArcaneRecipe;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.TcRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.crafting.TcStack;

/**
 * Translates common Forbidden Magic recipe definitions to Thaumcraft 4: Reforged recipes.
 *
 * <p>Legacy uppercase keys and host research identifiers are deliberately resolved here rather
 * than stored in the common recipe model.
 */
public final class Tc4RecipeAdapter {
    private static final Map<String, String> LEGACY_RECIPE_KEYS = Map.of(
        "emerald_transmutation", "TRANSEMERALD",
        "taint_stone", "TaintStone",
        "taint_brick", "TaintBrick",
        "black_ink", "BlackInk",
        "riding_crop", "RidingCrop",
        "taint_shovel", "TAINTSHOVEL",
        "taint_pick", "TAINTPICK",
        "ring_food", "RINGFOOD",
        "focus_blink", "FOCUSBLINK",
        "sub_collar", "SUBCOLLAR"
    );

    private Tc4RecipeAdapter() {
    }

    /**
     * Reads the legacy host option that selects the simpler Crystalwell recipe variant.
     * This is intentionally kept out of the common recipe catalog.
     */
    public static boolean crystalwellUsesShardTag() {
        return thaumcraft.Config.researchDifficulty() == -1;
    }

    public static void registerRecipes(List<RecipeDefinition> definitions) {
        for (RecipeDefinition definition : definitions) {
            TcRecipes.register(translate(definition));
        }
    }

    public static TcRecipe translate(RecipeDefinition definition) {
        String key = recipeKey(definition.id());
        String research = definition.research()
            .map(Tc4RecipeAdapter::researchKey)
            .orElse("");

        if (definition instanceof RecipeDefinition.Infusion infusion) {
            return new InfusionRecipe(
                key,
                research,
                output(infusion.result()),
                infusion.instability(),
                Tc4AspectAdapter.toAspectList(infusion.aspects()),
                ingredient(infusion.central()),
                infusion.components().stream().map(Tc4RecipeAdapter::ingredient).toList()
            );
        }
        if (definition instanceof RecipeDefinition.InfusionEnchantment enchantment) {
            return new InfusionEnchantmentRecipe(
                key,
                research,
                identifier(enchantment.enchantment()),
                enchantment.instability(),
                Tc4AspectAdapter.toAspectList(enchantment.aspects()),
                enchantment.components().stream().map(Tc4RecipeAdapter::ingredient).toList()
            );
        }
        if (definition instanceof RecipeDefinition.Crucible crucible) {
            return new CrucibleRecipe(
                key,
                research,
                output(crucible.result()),
                ingredient(crucible.catalyst()),
                Tc4AspectAdapter.toAspectList(crucible.aspects())
            );
        }
        if (definition instanceof RecipeDefinition.ArcaneShaped shaped) {
            List<TcIngredient> pattern = shaped.pattern().stream()
                .map(cell -> cell.map(Tc4RecipeAdapter::ingredient).orElse(TcIngredient.EMPTY))
                .toList();
            return new ShapedArcaneRecipe(
                key,
                research,
                output(shaped.result()),
                Tc4AspectAdapter.toAspectList(shaped.aspects()),
                shaped.width(),
                shaped.height(),
                pattern
            );
        }
        if (definition instanceof RecipeDefinition.ArcaneShapeless shapeless) {
            return new ShapelessArcaneRecipe(
                key,
                research,
                output(shapeless.result()),
                Tc4AspectAdapter.toAspectList(shapeless.aspects()),
                shapeless.ingredients().stream().map(Tc4RecipeAdapter::ingredient).toList()
            );
        }
        if (definition instanceof RecipeDefinition.CraftingReference reference) {
            return new CraftingRecipeRef(
                key,
                research,
                identifier(reference.recipe()),
                output(reference.result())
            );
        }
        throw new IllegalArgumentException("Unsupported common recipe definition: " + definition.getClass().getName());
    }

    private static TcIngredient ingredient(RecipeDefinition.Ingredient ingredient) {
        if (ingredient.kind() == RecipeDefinition.IngredientKind.TAG) {
            if (ingredient.count() != 1) {
                throw new IllegalArgumentException("TC4 tag ingredients do not support stack counts: " + ingredient.id());
            }
            return TcIngredient.tag(ingredient.id().toString());
        }
        if (ingredient.count() == 1) {
            return TcIngredient.item(ingredient.id().toString());
        }
        return TcIngredient.of(new TcStack(identifier(ingredient.id()), ingredient.count(), null));
    }

    private static TcStack output(RecipeDefinition.Output output) {
        return TcStack.of(output.item().toString(), output.count());
    }

    private static Identifier identifier(ContentId id) {
        return Identifier.fromNamespaceAndPath(id.namespace(), id.path());
    }

    private static String recipeKey(ContentId id) {
        String path = definitionPath(id, "recipe/");
        return LEGACY_RECIPE_KEYS.getOrDefault(path, path.toUpperCase(Locale.ROOT));
    }

    private static String researchKey(ContentId id) {
        return definitionPath(id, "research/").toUpperCase(Locale.ROOT);
    }

    private static String definitionPath(ContentId id, String prefix) {
        if (!"forbiddenmagic".equals(id.namespace()) || !id.path().startsWith(prefix)) {
            throw new IllegalArgumentException("Unexpected TC4 adapter identifier: " + id);
        }
        String path = id.path().substring(prefix.length());
        if (path.isBlank()) {
            throw new IllegalArgumentException("Empty adapter key: " + id);
        }
        return path;
    }
}
