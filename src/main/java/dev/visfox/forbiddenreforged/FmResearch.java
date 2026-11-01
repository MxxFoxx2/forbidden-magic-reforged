package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.core.research.ForbiddenMagicResearchCatalog;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4RecipeAdapter;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4ResearchAdapter;
import net.neoforged.fml.ModList;

public final class FmResearch {
    private FmResearch() {
    }

    public static void register() {
        ForbiddenMagicResearchCatalog.Options options = new ForbiddenMagicResearchCatalog.Options(
            FmConfig.enchantmentsEnabled(),
            FmConfig.greedyEnchantmentEnabled(),
            FmConfig.lustDisabled(),
            FmConfig.emeraldTrans(),
            ModList.get().isLoaded("arsmagica2"),
            Tc4RecipeAdapter.crystalwellUsesShardTag()
        );
        Tc4ResearchAdapter.register(
            ForbiddenMagicResearchCatalog.category(),
            ForbiddenMagicResearchCatalog.definitions(options)
        );
    }
}
