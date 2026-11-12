package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.core.research.ForbiddenMagicResearchCatalog;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import java.util.List;

public final class FmResearch {
    private FmResearch() {
    }

    public static void register() {
        ForbiddenMagicResearchCatalog.Options options = new ForbiddenMagicResearchCatalog.Options(
            FmConfig.enchantmentsEnabled(),
            FmConfig.greedyEnchantmentEnabled(),
            FmConfig.lustDisabled(),
            FmConfig.emeraldTrans(),
            FmArsMagica.loaded(),
            HostAdapterRegistry.require().crystalwellUsesShardTag()
        );
        HostAdapterRegistry.require().registerResearch(
            List.of(ForbiddenMagicResearchCatalog.category()),
            ForbiddenMagicResearchCatalog.definitions(options)
        );
    }
}
