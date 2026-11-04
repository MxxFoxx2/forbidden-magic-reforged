package dev.visfox.forbiddenreforged.common;

import dev.visfox.forbiddenreforged.core.recipe.ForbiddenMagicRecipeCatalog;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import java.util.Set;

public final class FmRecipes {
    public static final Set<String> OWN_KEYS = Set.of(
        "SKULLAXE", "FORK", "WRATH", "GREEDY", "CONSUMING", "EDUCATIONAL", "CORRUPTING",
        "MORPHPICK", "MORPHSWORD", "MORPHSHOVEL", "MORPHAXE", "CLUSTER", "IMPACT", "VOIDTOUCHED",
        "TAINTTREE", "BLACKFLOWER", "ARCANECAKE", "WRATHCAGE", "MOBCRYSTAL", "ROD_TAINTED",
        "ROD_INFERNAL", "TAINTSHOVEL", "TAINTPICK", "CRYSTALWELL", "PRIMEWELL", "TRANSEMERALD",
        "FOCUSBLINK", "RINGFOOD", "TaintStone", "TaintBrick", "BlackInk", "SUBCOLLAR", "RidingCrop"
    );

    private FmRecipes() {
    }

    public static void register() {
        ForbiddenMagicRecipeCatalog.Options options = new ForbiddenMagicRecipeCatalog.Options(
            FmConfig.enchantmentsEnabled(),
            FmConfig.greedyEnchantmentEnabled(),
            FmConfig.emeraldTrans(),
            FmConfig.gluttonyMode(),
            FmConfig.lustDisabled(),
            HostAdapterRegistry.require().crystalwellUsesShardTag()
        );
        HostAdapterRegistry.require().registerRecipes(ForbiddenMagicRecipeCatalog.definitions(options));
    }
}
