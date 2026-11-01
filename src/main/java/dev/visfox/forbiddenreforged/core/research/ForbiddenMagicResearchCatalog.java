package dev.visfox.forbiddenreforged.core.research;

import dev.visfox.forbiddenreforged.core.aspect.AspectDefinition;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static dev.visfox.forbiddenreforged.core.research.ResearchDefinition.Flag.AUTO_UNLOCK;
import static dev.visfox.forbiddenreforged.core.research.ResearchDefinition.Flag.CONCEALED;
import static dev.visfox.forbiddenreforged.core.research.ResearchDefinition.Flag.HIDDEN;
import static dev.visfox.forbiddenreforged.core.research.ResearchDefinition.Flag.ROUND_ICON;
import static dev.visfox.forbiddenreforged.core.research.ResearchDefinition.Flag.SECONDARY;
import static dev.visfox.forbiddenreforged.core.research.ResearchDefinition.Flag.STUB;

public final class ForbiddenMagicResearchCatalog {
    private static final ContentId CATEGORY = id("category/forbidden");
    private static final Map<String, String> RECIPE_PATHS = Map.ofEntries(
        Map.entry("TaintStone", "taint_stone"),
        Map.entry("TaintBrick", "taint_brick"),
        Map.entry("BlackInk", "black_ink"),
        Map.entry("RidingCrop", "riding_crop"),
        Map.entry("TRANSEMERALD", "emerald_transmutation"),
        Map.entry("TAINTSHOVEL", "taint_shovel"),
        Map.entry("TAINTPICK", "taint_pick"),
        Map.entry("RINGFOOD", "ring_food"),
        Map.entry("FOCUSBLINK", "focus_blink"),
        Map.entry("SUBCOLLAR", "sub_collar"),
        Map.entry("ARCANECAKE", "arcanecake")
    );

    public record Options(
        boolean enchantmentsEnabled,
        boolean greedyEnchantmentEnabled,
        boolean lustDisabled,
        boolean emeraldTransmutationEnabled,
        boolean arsMagica2Loaded,
        boolean crystalwellUsesShardTag
    ) {
    }

    private ForbiddenMagicResearchCatalog() {
    }

    public static ResearchCategoryDefinition category() {
        return new ResearchCategoryDefinition(
            CATEGORY,
            id("textures/misc/forbidden.png"),
            id("textures/misc/runecircle.png")
        );
    }

    public static List<ResearchDefinition> definitions(Options options) {
        List<ResearchDefinition> entries = new ArrayList<>();

        entries.add(entry("nethershards", item("wrath_shard"), AspectDefinition.Cost.EMPTY,
            -8, -5, 0, noParents(), noParents(), flags(STUB, ROUND_ICON, AUTO_UNLOCK), List.of(), 0,
            text("forbiddenmagic.research_page.NETHERSHARDS.1"),
            text(options.lustDisabled()
                ? "forbiddenmagic.research_page.NETHERSHARDS.2b"
                : "forbiddenmagic.research_page.NETHERSHARDS.2"),
            text("forbiddenmagic.research_page.NETHERSHARDS.3")));

        entries.add(entry("hellfire", item("minecraft:flint_and_steel"),
            cost(a("fire", 3), custom("infernus", 4), a("travel", 2)),
            -8, -8, 1, parents("nethershards"), noParents(), flags(ROUND_ICON, HIDDEN),
            triggers(custom("infernus")), 0,
            text("forbiddenmagic.research_page.HELLFIRE.1")));

        entries.add(entry("skullaxe", item("skull_axe"),
            cost(a("weapon", 3), custom("ira", 4), custom("infernus", 1)),
            -11, -7, 2, parents("nethershards", "thaumium", "infusion"), noParents(), flags(CONCEALED), List.of(), 1,
            text("forbiddenmagic.research_page.SKULLAXE.1"),
            recipe(ResearchDefinition.PageKind.INFUSION_RECIPE, "SKULLAXE")));

        entries.add(entry("fork", item("diabolist_fork"),
            cost(custom("infernus", 3), a("mechanism", 1), a("tool", 1)),
            -6, -7, 0, parents("infusion", "nethershards", "thaumium"), noParents(),
            flags(SECONDARY, CONCEALED), List.of(), 0,
            text("forbiddenmagic.research_page.FORK.1"),
            text("forbiddenmagic.research_page.FORK.wc"),
            recipe(ResearchDefinition.PageKind.INFUSION_RECIPE, "FORK")));

        entries.add(entry("morphtools", item("morph_pickaxe"),
            cost(a("tool", 2), custom("invidia", 3), a("exchange", 2)),
            -9, -3, 4, parents("nethershards"), parents("thaumium", "infusionenchantment"),
            flags(CONCEALED), List.of(), 4,
            text("forbiddenmagic.research_page.MORPHTOOLS.1"),
            recipe(ResearchDefinition.PageKind.INFUSION_RECIPE, "MORPHPICK"),
            recipe(ResearchDefinition.PageKind.INFUSION_RECIPE, "MORPHSWORD"),
            recipe(ResearchDefinition.PageKind.INFUSION_RECIPE, "MORPHSHOVEL"),
            recipe(ResearchDefinition.PageKind.INFUSION_RECIPE, "MORPHAXE")));

        if (options.enchantmentsEnabled()) {
            entries.add(entry("cluster", texture("textures/misc/lucrative.png"),
                cost(a("metal", 1), a("fire", 4), custom("invidia", 3)),
                -10, -1, 3, parents("morphtools", "elementalpick"), noParents(),
                flags(SECONDARY, CONCEALED), List.of(), 0,
                text("forbiddenmagic.research_page.CLUSTER.1"),
                recipe(ResearchDefinition.PageKind.INFUSION_ENCHANTMENT, "CLUSTER")));

            entries.add(entry("impact", texture("textures/misc/impact.png"),
                cost(a("entropy", 8), a("tool", 10), a("mine", 16), custom("invidia", 10)),
                -9, -1, 3, parents("morphtools", "elementalshovel"), noParents(),
                flags(CONCEALED), List.of(), 0,
                text("forbiddenmagic.research_page.IMPACT.1"),
                recipe(ResearchDefinition.PageKind.INFUSION_ENCHANTMENT, "IMPACT")));

            entries.add(entry("voidtouched", texture("textures/misc/voidtouched.png"),
                cost(a("eldritch", 16), a("tool", 10), a("craft", 8), custom("invidia", 32)),
                -8, -1, 4, parents("morphtools", "voidmetal"), noParents(),
                flags(CONCEALED), List.of(), 3,
                text("forbiddenmagic.research_page.VOIDTOUCHED.1"),
                recipe(ResearchDefinition.PageKind.INFUSION_ENCHANTMENT, "VOIDTOUCHED")));
        }

        entries.add(entry("arcanecake", item("arcane_cake"),
            cost(custom("gula", 6), a("hunger", 3), a("craft", 2)),
            -12, -6, 2, parents("nethershards", "infusion"), noParents(),
            flags(CONCEALED), triggers(custom("gula")), 0,
            text("forbiddenmagic.research_page.ARCANECAKE.1"),
            recipe(ResearchDefinition.PageKind.INFUSION_RECIPE, "ARCANECAKE")));

        entries.add(entry("wrathcage", item("wrath_cage"),
            cost(custom("ira", 5), a("mechanism", 3), a("beast", 2)),
            -6, -9, 4, parents("fork"), parents("infusion", "thaumium", "distilessentia"),
            flags(CONCEALED), List.of(), 3,
            text("forbiddenmagic.research_page.WRATHCAGE.1"),
            recipe(ResearchDefinition.PageKind.INFUSION_RECIPE, "WRATHCAGE"),
            text("forbiddenmagic.research_page.WRATHCAGE.2"),
            text("forbiddenmagic.research_page.WRATHCAGE.3"),
            recipe(ResearchDefinition.PageKind.CRUCIBLE_RECIPE, "MOBCRYSTAL")));

        entries.add(entry("tainttree", item("taint_sapling"),
            cost(a("tree", 4), a("taint", 6), a("poison", 2), a("plant", 3)),
            -11, 3, 3, parents("thaumium", "infusion", "etherealbloom"), noParents(),
            flags(CONCEALED), List.of(), 1,
            text("forbiddenmagic.research_page.TAINTTREE.1"),
            recipe(ResearchDefinition.PageKind.CRUCIBLE_RECIPE, "TAINTTREE")));

        entries.add(entry("taintstone", item("taint_stone_brick"),
            cost(a("earth", 4), a("taint", 6), a("order", 2)),
            -10, 3, 2, parents("thaumium", "infusion", "etherealbloom"), noParents(),
            flags(CONCEALED), List.of(), 0,
            text("forbiddenmagic.research_page.TAINTSTONE.1"),
            recipe(ResearchDefinition.PageKind.ARCANE_RECIPE, "TaintStone"),
            recipe(ResearchDefinition.PageKind.CRAFTING_RECIPE, "TaintBrick")));

        entries.add(entry("blackflower", item("umbral_rose"),
            cost(a("plant", 3), a("senses", 2), a("darkness", 4)),
            -2, -6, 1, noParents(), noParents(), flags(), triggers(a("senses")), 0,
            text("forbiddenmagic.research_page.BLACKFLOWER.1"),
            recipe(ResearchDefinition.PageKind.CRUCIBLE_RECIPE, "BLACKFLOWER"),
            recipe(ResearchDefinition.PageKind.CRAFTING_RECIPE, "BlackInk")));

        if (options.enchantmentsEnabled()) {
            entries.add(entry("wrath", texture("textures/misc/wrath.png"),
                cost(custom("ira", 16), a("weapon", 20), a("magic", 10)),
                -6, -3, 4, parents("nethershards", "infusionenchantment"), noParents(),
                flags(CONCEALED), List.of(), 3,
                text("forbiddenmagic.research_page.WRATH.1"),
                recipe(ResearchDefinition.PageKind.INFUSION_ENCHANTMENT, "WRATH")));

            if (options.greedyEnchantmentEnabled()) {
                entries.add(entry("greedy", texture("textures/misc/greedy.png"),
                    cost(a("magic", 2), a("weapon", 1), a("greed", 3)),
                    -6, -5, 2, parents("nethershards", "infusionenchantment"), noParents(),
                    flags(CONCEALED), List.of(), 0,
                    text("forbiddenmagic.research_page.GREEDY.1"),
                    recipe(ResearchDefinition.PageKind.INFUSION_ENCHANTMENT, "GREEDY")));
            }

            entries.add(entry("corrupting", texture("textures/misc/corrupting.png"),
                cost(custom("infernus", 5), a("crystal", 2), a("exchange", 1)),
                -6, -4, 1, parents("nethershards", "infusionenchantment"), noParents(),
                flags(CONCEALED), List.of(), 0,
                text("forbiddenmagic.research_page.CORRUPTING.1"),
                recipe(ResearchDefinition.PageKind.INFUSION_ENCHANTMENT, "CORRUPTING")));

            entries.add(entry("consuming", texture("textures/misc/consuming.png"),
                cost(a("void", 4), a("entropy", 3), a("magic", 2)),
                -4, -8, 1, parents("infusionenchantment"), noParents(),
                flags(SECONDARY, CONCEALED), List.of(), 0,
                text("forbiddenmagic.research_page.CONSUMING.1"),
                recipe(ResearchDefinition.PageKind.INFUSION_ENCHANTMENT, "CONSUMING")));

            entries.add(entry("educational", texture("textures/misc/educational.png"),
                cost(a("mind", 5), a("weapon", 1), a("magic", 3)),
                -4, -7, 2, parents("infusionenchantment"), noParents(),
                flags(SECONDARY, CONCEALED), List.of(), 0,
                text("forbiddenmagic.research_page.EDUCATIONAL.1"),
                recipe(ResearchDefinition.PageKind.INFUSION_ENCHANTMENT, "EDUCATIONAL")));
        }

        entries.add(entry("ridingcrop", item("riding_crop"), AspectDefinition.Cost.EMPTY,
            0, -8, 0, noParents(), noParents(), flags(STUB, ROUND_ICON, AUTO_UNLOCK), List.of(), 0,
            text("forbiddenmagic.research_page.RIDINGCROP.1"),
            recipe(ResearchDefinition.PageKind.CRAFTING_RECIPE, "RidingCrop")));

        entries.add(entry("ringfood", item("ring_nutrition"),
            cost(custom("gula", 4), a("hunger", 3), a("life", 2)),
            -11, -6, 2, parents("nethershards"), noParents(), flags(), List.of(), 0,
            text("forbiddenmagic.research_page.RINGFOOD.1"),
            recipe(ResearchDefinition.PageKind.ARCANE_RECIPE, "RINGFOOD")));

        entries.add(entry("rod_tainted", item("tainted_rod"),
            cost(a("magic", 4), a("taint", 5), a("tool", 2)),
            -8, 3, 3, parents("rod_silverwood", "taintshovel", "infusion"), noParents(),
            flags(CONCEALED), List.of(), 2,
            text("forbiddenmagic.research_page.ROD_tainted.1"),
            recipe(ResearchDefinition.PageKind.INFUSION_RECIPE, "ROD_TAINTED")));

        entries.add(entry("rod_infernal", item("infernal_rod"),
            cost(custom("infernus", 4), a("fire", 3), a("tool", 1)),
            -11, -4, 3, parents("rod_silverwood", "infusion", "nethershards"), noParents(),
            flags(CONCEALED), List.of(), 2,
            text("forbiddenmagic.research_page.ROD_infernal.1"),
            text("forbiddenmagic.research_page.ROD_infernal.2")));

        if (!options.lustDisabled()) {
            entries.add(entry("subcollar", item("sub_collar"),
                cost(a("aura", 3), custom("luxuria", 8), a("magic", 3), a("trap", 6), a("flesh", 4)),
                -11, -5, 2, parents("nethershards", "visamulet", "infusion"), noParents(),
                flags(HIDDEN), triggers(custom("luxuria")), 0,
                text("forbiddenmagic.research_page.SUBCOLLAR.1"),
                recipe(ResearchDefinition.PageKind.INFUSION_RECIPE, "SUBCOLLAR")));
        }

        if (options.arsMagica2Loaded()) {
            entries.add(entry("rod_witchwood", item("witchwood_rod"),
                cost(a("tree", 3), a("magic", 2)),
                -3, 1, 3, parents("nethershards"), noParents(), flags(HIDDEN), List.of(), 2,
                text("forbiddenmagic.research_page.ROD_witchwood.1")));
        }

        entries.add(entry("rod_profane", item("profane_rod"), AspectDefinition.Cost.EMPTY,
            -9, -8, 0, parents("nethershards"), noParents(), flags(ROUND_ICON, HIDDEN, STUB), List.of(), 0,
            text("forbiddenmagic.research_page.ROD_profane.1")));

        if (options.arsMagica2Loaded()) {
            entries.add(entry("rod_witchwood_staff", item("witchwood_staff_rod"),
                cost(a("tree", 4), a("magic", 3)),
                -3, -1, 2, parents("rod_witchwood"), noParents(), flags(HIDDEN), List.of(), 0,
                text("forbiddenmagic.research_page.ROD_witchwood_staff.1")));
        }

        if (options.emeraldTransmutationEnabled()) {
            entries.add(entry("transemerald", item("emerald_fragment"),
                cost(a("crystal", 2), a("exchange", 5), a("greed", 4)),
                0, -6, 3, parents("transgold"), noParents(), flags(SECONDARY, CONCEALED), List.of(), 0,
                text("forbiddenmagic.research_page.TRANSEMERALD.1"),
                recipe(ResearchDefinition.PageKind.CRUCIBLE_RECIPE, "TRANSEMERALD")));
        }

        entries.add(entry("crystalwell", item("crystalwell"),
            cost(a("mind", 3), a("crystal", 2), a("magic", 1)),
            -2, -8, 1, parents("research"), noParents(), flags(), List.of(), 0,
            text(options.crystalwellUsesShardTag()
                ? "forbiddenmagic.research_page.CRYSTALWELL.1b"
                : "forbiddenmagic.research_page.CRYSTALWELL.1"),
            recipe(ResearchDefinition.PageKind.ARCANE_RECIPE, "CRYSTALWELL")));

        entries.add(entry("primewell", item("primewell"),
            cost(a("mind", 3), a("eldritch", 6), a("craft", 1)),
            2, -8, 1, parents("primpearl"), noParents(), flags(CONCEALED), List.of(), 0,
            text("forbiddenmagic.research_page.PRIMEWELL.1"),
            recipe(ResearchDefinition.PageKind.ARCANE_RECIPE, "PRIMEWELL")));

        entries.add(entry("taintshovel", item("taint_shovel"),
            cost(a("crystal", 3), a("taint", 2), a("tool", 1)),
            -8, 1, 2, parents("thaumium", "infusion", "etherealbloom"), noParents(),
            flags(CONCEALED), List.of(), 0,
            text("forbiddenmagic.research_page.TAINTSHOVEL.1"),
            recipe(ResearchDefinition.PageKind.INFUSION_RECIPE, "TAINTSHOVEL"),
            text("forbiddenmagic.research_page.TAINTSHOVEL.2")));

        entries.add(entry("taintpick", item("taint_pickaxe"),
            cost(a("tool", 2), a("taint", 4), a("entropy", 3)),
            -10, 1, 2, parents("thaumium", "infusion"), noParents(), flags(CONCEALED), List.of(), 0,
            text("forbiddenmagic.research_page.TAINTPICK.1"),
            recipe(ResearchDefinition.PageKind.INFUSION_RECIPE, "TAINTPICK")));

        entries.add(entry("focusblink", item("blink_focus"),
            cost(a("travel", 3), a("entropy", 3), custom("infernus", 3), custom("desidia", 6)),
            -11, -3, 2, parents("nethershards", "infusion"), noParents(), flags(CONCEALED), List.of(), 0,
            text("forbiddenmagic.research_page.FOCUSBLINK.1"),
            recipe(ResearchDefinition.PageKind.INFUSION_RECIPE, "FOCUSBLINK")));

        return List.copyOf(entries);
    }

    private static ResearchDefinition entry(
        String key,
        ResearchDefinition.Icon icon,
        AspectDefinition.Cost noteCost,
        int column,
        int row,
        int complexity,
        List<ContentId> parents,
        List<ContentId> hiddenParents,
        Set<ResearchDefinition.Flag> flags,
        List<ContentId> aspectTriggers,
        int warp,
        ResearchDefinition.Page... pages
    ) {
        return new ResearchDefinition(
            researchId(key),
            CATEGORY,
            icon,
            column,
            row,
            complexity,
            noteCost,
            parents,
            hiddenParents,
            flags,
            aspectTriggers,
            List.of(new ResearchDefinition.Stage(List.of(pages), List.of(), warp))
        );
    }

    private static ResearchDefinition.Icon item(String path) {
        return ResearchDefinition.Icon.item(id(path.contains(":") ? path : path));
    }

    private static ResearchDefinition.Icon texture(String path) {
        return ResearchDefinition.Icon.texture(id(path));
    }

    private static ResearchDefinition.Page text(String translationKey) {
        return ResearchDefinition.Page.text(translationKey);
    }

    private static ResearchDefinition.Page recipe(ResearchDefinition.PageKind kind, String key) {
        return ResearchDefinition.Page.recipe(kind, recipeId(key));
    }

    private static List<ContentId> parents(String... keys) {
        return researchIds(keys);
    }

    private static List<ContentId> noParents() {
        return List.of();
    }

    private static List<ContentId> triggers(AspectDefinition.Amount amount) {
        return List.of(amount.aspect());
    }

    private static Set<ResearchDefinition.Flag> flags(ResearchDefinition.Flag... values) {
        if (values.length == 0) {
            return Set.of();
        }
        EnumSet<ResearchDefinition.Flag> flags = EnumSet.noneOf(ResearchDefinition.Flag.class);
        flags.addAll(List.of(values));
        return Set.copyOf(flags);
    }

    private static AspectDefinition.Cost cost(AspectDefinition.Amount... amounts) {
        return AspectDefinition.Cost.of(amounts);
    }

    private static AspectDefinition.Amount a(String aspect, int amount) {
        return new AspectDefinition.Amount(id("concept/" + aspect), amount);
    }

    private static AspectDefinition.Amount custom(String aspect, int amount) {
        return new AspectDefinition.Amount(id(aspect), amount);
    }

    private static List<ContentId> researchIds(String... keys) {
        return List.of(keys).stream().map(ForbiddenMagicResearchCatalog::researchId).toList();
    }

    private static ContentId researchId(String key) {
        return id("research/" + key);
    }

    private static ContentId recipeId(String key) {
        String path = RECIPE_PATHS.getOrDefault(key, key.toLowerCase());
        return id("recipe/" + path);
    }

    private static ContentId id(String path) {
        return new ContentId("forbiddenmagic", path);
    }
}
