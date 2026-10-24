package dev.visfox.forbiddenreforged;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class FmConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;
    public static ModConfigSpec.BooleanValue noLust;
    public static ModConfigSpec.BooleanValue tagResearch;
    public static ModConfigSpec.IntValue gluttony;
    public static ModConfigSpec.BooleanValue enchanting;
    public static ModConfigSpec.BooleanValue greedyEnch;
    public static ModConfigSpec.BooleanValue wrathCage;
    public static ModConfigSpec.IntValue wrathCost;
    public static ModConfigSpec.IntValue wrathEff;
    public static ModConfigSpec.BooleanValue wrathCrazy;
    public static ModConfigSpec.BooleanValue spork;
    public static ModConfigSpec.BooleanValue silverfishEmeralds;
    public static ModConfigSpec.BooleanValue emeraldTrans;

    static {
        noLust = BUILDER.comment("Enable to remove Luxuria aspect and related items.").define("general.no_lust", false);
        tagResearch = BUILDER.comment("Show the [FM] prefix in Forbidden Magic research titles.").define("general.tag_research", true);
        gluttony = BUILDER.comment("0 = Enabled, 1 = Disabled, 2 = Hardcore").defineInRange("general.gluttony", 0, 0, 2);
        enchanting = BUILDER.comment("Enable Forbidden Magic enchantments.").define("general.enchanting", true);
        greedyEnch = BUILDER.comment("Enable the Capitalist (Greedy) enchantment, its recipe and effects.").define("general.greedy_ench", true);
        wrathCage = BUILDER.comment("Enable the Wrath Cage.").define("general.wrath_cage", true);
        wrathCost = BUILDER.comment("Essentia cost per spawn round.").defineInRange("general.wrath_cost", 5, 0, 64);
        wrathEff = BUILDER.comment("Spawns granted per essentia cost.").defineInRange("general.wrath_eff", 4, 1, 64);
        wrathCrazy = BUILDER.comment("Let the cage imprint on any non-boss mob.").define("general.wrath_crazy", false);
        spork = BUILDER.comment("Rename the Diabolist Fork to 'Diabolist Spork' and use the spork texture.").define("general.spork", false);
        silverfishEmeralds = BUILDER.comment("Allow silverfish to drop emerald nuggets.").define("general.silverfish_emeralds", true);
        emeraldTrans = BUILDER.comment("Enable Emerald Transmutation research and recipe.").define("general.emerald_transmutation", true);
        SPEC = BUILDER.build();
    }

    private FmConfig() {
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.STARTUP, SPEC);
    }

    public static boolean lustDisabled() {
        return SPEC.isLoaded() ? noLust.get() : noLust.getDefault();
    }

    public static boolean tagResearchEnabled() {
        return SPEC.isLoaded() ? tagResearch.get() : tagResearch.getDefault();
    }

    public static int gluttonyMode() {
        return SPEC.isLoaded() ? gluttony.get() : gluttony.getDefault();
    }

    public static boolean enchantmentsEnabled() {
        return SPEC.isLoaded() ? enchanting.get() : enchanting.getDefault();
    }

    public static boolean greedyEnchantmentEnabled() {
        return SPEC.isLoaded() ? greedyEnch.get() : greedyEnch.getDefault();
    }

    public static boolean wrathCage() {
        return SPEC.isLoaded() ? wrathCage.get() : wrathCage.getDefault();
    }

    public static int wrathCost() {
        return SPEC.isLoaded() ? wrathCost.get() : wrathCost.getDefault();
    }

    public static int wrathEff() {
        return SPEC.isLoaded() ? wrathEff.get() : wrathEff.getDefault();
    }

    public static boolean wrathCrazy() {
        return SPEC.isLoaded() ? wrathCrazy.get() : wrathCrazy.getDefault();
    }

    public static boolean spork() {
        return SPEC.isLoaded() ? spork.get() : spork.getDefault();
    }

    public static boolean silverfishEmeralds() {
        return SPEC.isLoaded() ? silverfishEmeralds.get() : silverfishEmeralds.getDefault();
    }

    public static boolean emeraldTrans() {
        return SPEC.isLoaded() ? emeraldTrans.get() : emeraldTrans.getDefault();
    }
}
