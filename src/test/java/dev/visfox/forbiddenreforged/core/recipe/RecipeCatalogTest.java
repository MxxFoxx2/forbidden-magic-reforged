package dev.visfox.forbiddenreforged.core.recipe;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Structural and gating checks for the host-neutral recipe catalog. Runtime resolution
 * of these recipes against TC4 stays covered by the GameTests; this suite verifies the
 * catalog data and option wiring without a server.
 */
class RecipeCatalogTest {
    private static ForbiddenMagicRecipeCatalog.Options allOn() {
        return new ForbiddenMagicRecipeCatalog.Options(true, true, true, 1, false, true);
    }

    private static boolean has(List<RecipeDefinition> recipes, String key) {
        return recipes.stream().anyMatch(r -> r.id().path().equals("recipe/" + key));
    }

    @Test
    void idsAreUnique() {
        List<RecipeDefinition> recipes = ForbiddenMagicRecipeCatalog.definitions(allOn());
        Set<String> seen = new HashSet<>();
        for (RecipeDefinition recipe : recipes) {
            assertTrue(seen.add(recipe.id().toString()), "duplicate recipe id: " + recipe.id());
        }
    }

    @Test
    void baseRecipesAreAlwaysPresent() {
        ForbiddenMagicRecipeCatalog.Options minimal =
            new ForbiddenMagicRecipeCatalog.Options(false, false, false, 1, true, false);
        List<RecipeDefinition> recipes = ForbiddenMagicRecipeCatalog.definitions(minimal);
        assertTrue(has(recipes, "skullaxe"));
        assertTrue(has(recipes, "fork"));
    }

    @Test
    void enchantmentsRequireEnchantmentsEnabled() {
        assertTrue(has(ForbiddenMagicRecipeCatalog.definitions(allOn()), "wrath"));

        ForbiddenMagicRecipeCatalog.Options noEnchants =
            new ForbiddenMagicRecipeCatalog.Options(false, false, true, 1, false, true);
        List<RecipeDefinition> recipes = ForbiddenMagicRecipeCatalog.definitions(noEnchants);
        assertFalse(has(recipes, "wrath"));
        assertFalse(has(recipes, "consuming"));
        assertFalse(has(recipes, "educational"));
        assertFalse(has(recipes, "corrupting"));
        assertFalse(has(recipes, "greedy"));
    }

    @Test
    void greedyRequiresItsOwnFlag() {
        ForbiddenMagicRecipeCatalog.Options noGreedy =
            new ForbiddenMagicRecipeCatalog.Options(true, false, true, 1, false, true);
        List<RecipeDefinition> recipes = ForbiddenMagicRecipeCatalog.definitions(noGreedy);
        assertTrue(has(recipes, "wrath"));
        assertFalse(has(recipes, "greedy"));
    }

    @Test
    void emeraldTransmutationIsGated() {
        assertTrue(has(ForbiddenMagicRecipeCatalog.definitions(allOn()), "emerald_transmutation"));

        ForbiddenMagicRecipeCatalog.Options noEmerald =
            new ForbiddenMagicRecipeCatalog.Options(true, true, false, 1, false, true);
        assertFalse(has(ForbiddenMagicRecipeCatalog.definitions(noEmerald), "emerald_transmutation"));
    }

    @Test
    void ringFoodIsOmittedInGluttonyModeOne() {
        assertFalse(has(ForbiddenMagicRecipeCatalog.definitions(allOn()), "ring_food"));

        ForbiddenMagicRecipeCatalog.Options modeZero =
            new ForbiddenMagicRecipeCatalog.Options(true, true, true, 0, false, true);
        assertTrue(has(ForbiddenMagicRecipeCatalog.definitions(modeZero), "ring_food"));

        ForbiddenMagicRecipeCatalog.Options modeTwo =
            new ForbiddenMagicRecipeCatalog.Options(true, true, true, 2, false, true);
        assertTrue(has(ForbiddenMagicRecipeCatalog.definitions(modeTwo), "ring_food"));
    }

    @Test
    void gluttonyModeIsValidated() {
        assertThrows(IllegalArgumentException.class,
            () -> new ForbiddenMagicRecipeCatalog.Options(true, true, true, 3, false, true));
        assertThrows(IllegalArgumentException.class,
            () -> new ForbiddenMagicRecipeCatalog.Options(true, true, true, -1, false, true));
    }
}