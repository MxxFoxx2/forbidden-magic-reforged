package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.core.research.ForbiddenMagicResearchCatalog;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import java.util.List;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4RecipeAdapter;
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
        HostAdapterRegistry.require().registerResearch(
            List.of(ForbiddenMagicResearchCatalog.category()),
            ForbiddenMagicResearchCatalog.definitions(options)
        );
    }
}
