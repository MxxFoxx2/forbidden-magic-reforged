package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.core.recipe.ForbiddenMagicRecipeCatalog;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import java.util.Set;

public final class FmRecipes {
    public static final Set<String> OWN_KEYS = Set.of(
        "SKULLAXE", "FORK", "WRATH", "GREEDY", "CONSUMING", "EDUCATIONAL",
        "MORPHPICK", "MORPHSWORD", "MORPHSHOVEL", "MORPHAXE", "CLUSTER", "IMPACT", "VOIDTOUCHED",
        "CORRUPTING",
        "TAINTTREE", "BLACKFLOWER", "ARCANECAKE", "WRATHCAGE", "MOBCRYSTAL",
        "ROD_WITCHWOOD", "CAP_VINTEUM", "ROD_WITCHWOOD_STAFF",
        "ROD_TAINTED", "ROD_INFERNAL", "TAINTSHOVEL", "TAINTPICK",
        "CRYSTALWELL", "PRIMEWELL", "TRANSEMERALD", "FOCUSBLINK", "RINGFOOD",
        "TaintStone", "TaintBrick", "BlackInk", "SUBCOLLAR", "RidingCrop"
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
            FmArsMagica.loaded(),
            HostAdapterRegistry.require().crystalwellUsesShardTag()
        );
        HostAdapterRegistry.require().registerRecipes(ForbiddenMagicRecipeCatalog.definitions(options));
    }
}
