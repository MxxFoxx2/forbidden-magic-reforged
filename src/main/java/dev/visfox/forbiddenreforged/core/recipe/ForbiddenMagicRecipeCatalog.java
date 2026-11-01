package dev.visfox.forbiddenreforged.core.recipe;

import dev.visfox.forbiddenreforged.core.aspect.AspectDefinition;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dev.visfox.forbiddenreforged.core.recipe.RecipeDefinition.Ingredient;
import static dev.visfox.forbiddenreforged.core.recipe.RecipeDefinition.Output;

public final class ForbiddenMagicRecipeCatalog {
    public record Options(
        boolean enchantmentsEnabled,
        boolean greedyEnchantmentEnabled,
        boolean emeraldTransmutationEnabled,
        int gluttonyMode,
        boolean lustDisabled,
        boolean crystalwellUsesShardTag
    ) {
        public Options {
            if (gluttonyMode < 0 || gluttonyMode > 2) {
                throw new IllegalArgumentException("Gluttony mode must be 0, 1 or 2");
            }
        }
    }

    private ForbiddenMagicRecipeCatalog() {
    }

    public static List<RecipeDefinition> definitions(Options options) {
        List<RecipeDefinition> recipes = new ArrayList<>();

        recipes.add(infusion("skullaxe", "skullaxe", "forbiddenmagic:skull_axe", 1,
            cost(custom("ira", 8), a("weapon", 8), custom("infernus", 8)),
            item("thaumcraft:thaumium_axe"),
            item("forbiddenmagic:wrath"), item("forbiddenmagic:wrath"),
            item("minecraft:diamond"), item("minecraft:wither_skeleton_skull")));

        recipes.add(infusion("fork", "fork", "forbiddenmagic:diabolist_fork", 1,
            cost(custom("infernus", 8), a("mechanism", 8), a("energy", 8)),
            item("thaumcraft:thaumium_sword"),
            item("minecraft:quartz"), item("minecraft:quartz"),
            item("minecraft:quartz"), item("minecraft:redstone")));

        if (options.enchantmentsEnabled()) {
            recipes.add(enchantment("wrath", "wrath", 8,
                cost(custom("ira", 16), a("weapon", 16), custom("infernus", 8)),
                item("thaumcraft:salis_mundus"), item("minecraft:diamond_sword"),
                item("forbiddenmagic:wrath"), item("forbiddenmagic:wrath"),
                item("forbiddenmagic:wrath")));

            if (options.greedyEnchantmentEnabled()) {
                recipes.add(enchantment("greedy", "greedy", 4,
                    cost(custom("infernus", 16), a("weapon", 8), a("greed", 16)),
                    item("minecraft:golden_sword"), item("minecraft:diamond"),
                    item("forbiddenmagic:greed"), item("forbiddenmagic:greed"),
                    item("thaumcraft:salis_mundus")));
            }

            recipes.add(enchantment("consuming", "consuming", 3,
                cost(a("void", 8), a("tool", 8), a("hunger", 8)),
                item("minecraft:iron_pickaxe"), item("minecraft:lava_bucket"),
                item("thaumcraft:salis_mundus")));

            recipes.add(enchantment("educational", "educational", 3,
                cost(a("magic", 4), a("weapon", 4), a("mind", 8)),
                item("thaumcraft:zombie_brain"), item("minecraft:book"),
                item("thaumcraft:salis_mundus")));

            recipes.add(enchantment("corrupting", "corrupting", 4,
                cost(custom("infernus", 16), a("exchange", 16), a("crystal", 8)),
                item("minecraft:nether_wart"), item("minecraft:soul_sand"),
                item("thaumcraft:salis_mundus")));

            recipes.add(enchantment("cluster", "cluster", 3,
                cost(a("fire", 4), a("metal", 4), a("greed", 4)),
                item("thaumcraft:elemental_pickaxe"), item("thaumcraft:salis_mundus")));

            recipes.add(enchantment("impact", "impact", 4,
                cost(a("entropy", 16), a("mine", 16)),
                item("thaumcraft:salis_mundus"), item("forbiddenmagic:envy"),
                item("forbiddenmagic:envy"), item("thaumcraft:elemental_shovel")));

            recipes.add(enchantment("voidtouched", "voidtouched", 8,
                cost(a("void", 16), a("darkness", 16), custom("invidia", 24), a("eldritch", 16)),
                item("thaumcraft:salis_mundus"), item("thaumcraft:void_seed"),
                item("thaumcraft:void_seed"), item("thaumcraft:void_seed"),
                item("forbiddenmagic:envy"), item("forbiddenmagic:envy"),
                item("forbiddenmagic:envy")));
        }

        addMorphToolRecipes(recipes);

        if (options.emeraldTransmutationEnabled()) {
            recipes.add(crucible("emerald_transmutation", "transemerald",
                "forbiddenmagic:emerald_fragment", 4, tag("c:nuggets/emerald"),
                cost(a("crystal", 2), a("greed", 2))));
        }

        if (options.gluttonyMode() != 1) {
            Ingredient[] cakeParts = options.gluttonyMode() == 2
                ? new Ingredient[] {
                    item("thaumcraft:salis_mundus"), item("forbiddenmagic:star_block"),
                    item("forbiddenmagic:star_block"), item("minecraft:egg"),
                    item("forbiddenmagic:gluttony_shard"), item("forbiddenmagic:gluttony_shard")
                }
                : new Ingredient[] {
                    item("thaumcraft:salis_mundus"), item("minecraft:egg"),
                    item("minecraft:milk_bucket"), item("minecraft:egg"),
                    item("forbiddenmagic:gluttony_shard"), item("forbiddenmagic:gluttony_shard")
                };
            recipes.add(infusion("arcanecake", "arcanecake", "forbiddenmagic:arcane_cake", 3,
                cost(custom("gula", 12), a("hunger", 24), a("craft", 24)),
                item("minecraft:cake"), cakeParts));
        }

        recipes.add(crucible("tainttree", "tainttree", "forbiddenmagic:taint_sapling", 1,
            item("minecraft:oak_sapling"), cost(a("taint", 10), a("poison", 4))));

        recipes.add(crucible("blackflower", "blackflower", "forbiddenmagic:umbral_rose_bush", 1,
            item("minecraft:rose_bush"), cost(a("darkness", 8), a("life", 5))));

        recipes.add(craftingReference("black_ink", "blackflower",
            "forbiddenmagic:ink_from_rose", "forbiddenmagic:black_dye", 2));

        recipes.add(craftingReference("riding_crop", "ridingcrop",
            "forbiddenmagic:riding_crop", "forbiddenmagic:riding_crop", 1));

        if (!options.lustDisabled()) {
            recipes.add(infusion("sub_collar", "subcollar", "forbiddenmagic:sub_collar", 1,
                cost(custom("luxuria", 8), a("trap", 8), custom("infernus", 4), a("flesh", 4)),
                item("thaumcraft:vis_amulet"),
                item("forbiddenmagic:lust"), item("forbiddenmagic:lust"),
                item("forbiddenmagic:lust"), item("minecraft:lead"),
                item("thaumcraft:mundane_belt")));
        }

        recipes.add(shaped("taint_stone", "taintstone", "forbiddenmagic:taint_stone_smooth", 9,
            cost(a("entropy", 2), a("order", 1)), 3, 3,
            item("minecraft:stone"), item("minecraft:stone"), item("minecraft:stone"),
            item("minecraft:stone"), tag("forbiddenmagic:taint_ingredients"), item("minecraft:stone"),
            item("minecraft:stone"), item("minecraft:stone"), item("minecraft:stone")));

        recipes.add(craftingReference("taint_brick", "taintstone",
            "forbiddenmagic:taint_stone_brick", "forbiddenmagic:taint_stone_brick", 4));

        recipes.add(infusion("wrathcage", "wrathcage", "forbiddenmagic:wrath_cage", 10,
            cost(custom("ira", 32), a("magic", 32), a("beast", 32), a("mechanism", 16)),
            item("thaumcraft:thaumium_block"),
            item("forbiddenmagic:wrath"), item("forbiddenmagic:wrath"),
            item("forbiddenmagic:wrath"), item("forbiddenmagic:wrath"),
            item("minecraft:diamond"), item("minecraft:diamond"),
            item("minecraft:diamond"), item("minecraft:diamond"),
            item("thaumcraft:warded_jar"), item("thaumcraft:warded_jar"),
            item("thaumcraft:warded_jar")));

        recipes.add(crucible("mobcrystal", "wrathcage", "forbiddenmagic:mob_crystal", 1,
            item("minecraft:diamond"), cost(a("mind", 10), a("energy", 10))));

        recipes.add(infusion("rod_tainted", "rod_tainted", "forbiddenmagic:tainted_rod", 5,
            cost(a("taint", 24), a("magic", 12), a("entropy", 12)),
            item("thaumcraft:tainted_goo"),
            item("thaumcraft:salis_mundus"),
            item("forbiddenmagic:taint"), item("forbiddenmagic:taint"),
            item("forbiddenmagic:taint"), item("forbiddenmagic:taint"),
            item("forbiddenmagic:taint"), item("forbiddenmagic:taint")));

        recipes.add(infusion("rod_infernal", "rod_infernal", "forbiddenmagic:infernal_rod", 5,
            cost(custom("infernus", 32), a("magic", 12), custom("superbia", 12)),
            item("minecraft:blaze_rod"),
            item("thaumcraft:salis_mundus"),
            item("forbiddenmagic:pride"), item("forbiddenmagic:pride"),
            item("minecraft:soul_sand"), item("minecraft:wither_skeleton_skull"),
            item("minecraft:quartz"), item("minecraft:blaze_powder")));

        recipes.add(infusion("taint_shovel", "taintshovel", "forbiddenmagic:taint_shovel", 5,
            cost(a("heal", 8), a("crystal", 24), a("mine", 12)),
            item("thaumcraft:thaumium_shovel"),
            item("thaumcraft:balanced_shard"), item("thaumcraft:balanced_shard"),
            item("minecraft:diamond"), item("thaumcraft:silverwood_log")));

        recipes.add(infusion("taint_pick", "taintpick", "forbiddenmagic:taint_pickaxe", 1,
            cost(a("entropy", 8), a("mine", 8), a("taint", 12)),
            item("thaumcraft:thaumium_pickaxe"),
            item("thaumcraft:entropy_shard"), item("thaumcraft:entropy_shard"),
            item("minecraft:diamond"), item("thaumcraft:greatwood_log")));

        if (options.crystalwellUsesShardTag()) {
            recipes.add(shapeless("crystalwell", "crystalwell", "forbiddenmagic:crystalwell", 1,
                cost(a("water", 1), a("order", 1)),
                item("thaumcraft:scribing_tools"), tag("c:dyes/black"),
                tag("thaumcraft:shards"), tag("thaumcraft:shards")));
        } else {
            recipes.add(shapeless("crystalwell", "crystalwell", "forbiddenmagic:crystalwell", 1,
                cost(a("water", 1), a("order", 1)),
                item("thaumcraft:scribing_tools"), tag("c:dyes/black"),
                item("thaumcraft:mixed_crystal_cluster"),
                item("thaumcraft:balanced_shard"), item("thaumcraft:balanced_shard"),
                item("thaumcraft:balanced_shard")));
        }

        recipes.add(shapeless("primewell", "primewell", "forbiddenmagic:primewell", 1,
            cost(a("water", 50), a("earth", 50), a("fire", 50), a("air", 50), a("order", 50), a("entropy", 50)),
            item("minecraft:feather"), item("thaumcraft:primordial_pearl"),
            item("minecraft:glass_bottle")));

        if (options.gluttonyMode() != 1) {
            String topIngredient = options.gluttonyMode() == 2
                ? "forbiddenmagic:star_block"
                : "minecraft:diamond";
            recipes.add(shaped("ring_food", "ringfood", "forbiddenmagic:ring_nutrition", 1,
                cost(a("entropy", 10), a("water", 20), a("earth", 20)), 3, 3,
                empty(), item(topIngredient), empty(),
                item("forbiddenmagic:gluttony_shard"), item("thaumcraft:mundane_ring"),
                item("forbiddenmagic:gluttony_shard"),
                empty(), item("forbiddenmagic:gluttony_shard"), empty()));
        }

        recipes.add(infusion("focus_blink", "focusblink", "forbiddenmagic:blink_focus", 3,
            cost(a("travel", 25), custom("infernus", 10), custom("desidia", 10), a("entropy", 25)),
            item("minecraft:ender_pearl"),
            item("minecraft:quartz"), item("forbiddenmagic:sloth"),
            item("minecraft:quartz"), item("forbiddenmagic:sloth"),
            item("minecraft:quartz"), item("forbiddenmagic:sloth")));

        return List.copyOf(recipes);
    }

    private static void addMorphToolRecipes(List<RecipeDefinition> recipes) {
        recipes.add(morph("morphpick", "morph_pickaxe", "thaumcraft:thaumium_pickaxe", false));
        recipes.add(morph("morphsword", "morph_sword", "thaumcraft:thaumium_sword", true));
        recipes.add(morph("morphshovel", "morph_shovel", "thaumcraft:thaumium_shovel", false));
        recipes.add(morph("morphaxe", "morph_axe", "thaumcraft:thaumium_axe", false));
    }

    private static RecipeDefinition.Infusion morph(String key, String output, String base, boolean weapon) {
        AspectDefinition.Cost aspects = weapon
            ? cost(a("exchange", 32), custom("invidia", 16), a("weapon", 16))
            : cost(a("exchange", 32), custom("invidia", 16), a("tool", 16));
        return infusion(key, "morphtools", "forbiddenmagic:" + output, 6, aspects,
            item(base),
            item("forbiddenmagic:envy"), item("forbiddenmagic:envy"),
            item("minecraft:diamond"), item("thaumcraft:enchanted_fabric"),
            item("thaumcraft:silverwood_log"));
    }

    private static RecipeDefinition.Infusion infusion(String key, String research, String output,
                                                        int instability, AspectDefinition.Cost aspects,
                                                        Ingredient central, Ingredient... components) {
        return new RecipeDefinition.Infusion(recipeId(key), researchId(research),
            output(output, 1), instability, aspects, central, List.of(components));
    }

    private static RecipeDefinition.InfusionEnchantment enchantment(String key, String enchantment,
                                                                      int instability, AspectDefinition.Cost aspects,
                                                                      Ingredient... components) {
        return new RecipeDefinition.InfusionEnchantment(recipeId(key), researchId(key),
            new ContentId("forbiddenmagic", enchantment), instability, aspects, List.of(components));
    }

    private static RecipeDefinition.Crucible crucible(String key, String research, String output,
                                                       int count, Ingredient catalyst,
                                                       AspectDefinition.Cost aspects) {
        return new RecipeDefinition.Crucible(recipeId(key), researchId(research),
            output(output, count), catalyst, aspects);
    }

    private static RecipeDefinition.ArcaneShaped shaped(String key, String research, String output,
                                                        int count, AspectDefinition.Cost aspects,
                                                        int width, int height, Ingredient... cells) {
        List<Optional<Ingredient>> pattern = new ArrayList<>(cells.length);
        for (Ingredient cell : cells) {
            pattern.add(cell == null ? Optional.empty() : Optional.of(cell));
        }
        return new RecipeDefinition.ArcaneShaped(recipeId(key), researchId(research),
            output(output, count), width, height, pattern, aspects);
    }

    private static RecipeDefinition.ArcaneShapeless shapeless(String key, String research, String output,
                                                               int count, AspectDefinition.Cost aspects,
                                                               Ingredient... ingredients) {
        return new RecipeDefinition.ArcaneShapeless(recipeId(key), researchId(research),
            output(output, count), List.of(ingredients), aspects);
    }

    private static RecipeDefinition.CraftingReference craftingReference(String key, String research,
                                                                          String recipe, String output, int count) {
        return new RecipeDefinition.CraftingReference(recipeId(key), researchId(research),
            id(recipe), output(output, count));
    }

    private static Ingredient item(String id) {
        return Ingredient.item(id(id));
    }

    private static Ingredient tag(String id) {
        return Ingredient.tag(id(id));
    }

    private static Ingredient empty() {
        return null;
    }

    private static Output output(String item, int count) {
        return new Output(id(item), count);
    }

    private static AspectDefinition.Cost cost(AspectDefinition.Amount... amounts) {
        return AspectDefinition.Cost.of(amounts);
    }

    private static AspectDefinition.Amount a(String aspect, int amount) {
        return new AspectDefinition.Amount(new ContentId("forbiddenmagic", "concept/" + aspect), amount);
    }

    private static AspectDefinition.Amount custom(String aspect, int amount) {
        return new AspectDefinition.Amount(new ContentId("forbiddenmagic", aspect), amount);
    }

    private static ContentId recipeId(String key) {
        return new ContentId("forbiddenmagic", "recipe/" + key);
    }

    private static Optional<ContentId> researchId(String key) {
        return Optional.of(new ContentId("forbiddenmagic", "research/" + key));
    }

    private static ContentId id(String value) {
        return ContentId.parse(value);
    }
}
