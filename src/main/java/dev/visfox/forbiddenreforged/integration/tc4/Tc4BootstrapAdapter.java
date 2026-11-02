package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.FmConfig;
import dev.visfox.forbiddenreforged.FmTinkerer;
import dev.visfox.forbiddenreforged.FmWandTriggers;
import dev.visfox.forbiddenreforged.FmWands;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.recipe.ForbiddenMagicRecipeCatalog;
import dev.visfox.forbiddenreforged.core.research.ForbiddenMagicResearchCatalog;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import java.util.List;
import thaumcraft.aspect.Aspect;

public final class Tc4BootstrapAdapter {
    private Tc4BootstrapAdapter() {
    }

    public static void initialize() {
        logAspectRegistry();
        Tc4WarpAdapter.registerWarpItem(ContentId.parse("forbiddenmagic:wrath_cage"), 5);
        Tc4WarpAdapter.registerWarpItem(ContentId.parse("forbiddenmagic:taint_sapling"), 1);

        // Keep the existing, known-compatible 1.21.1 wand implementations.
        FmWands.register();
        FmWandTriggers.register();

        var host = HostAdapterRegistry.require();
        boolean crystalwellUsesShardTag = host.crystalwellUsesShardTag();
        host.registerRecipes(ForbiddenMagicRecipeCatalog.definitions(
            new ForbiddenMagicRecipeCatalog.Options(
                FmConfig.enchantmentsEnabled(),
                FmConfig.greedyEnchantmentEnabled(),
                FmConfig.emeraldTrans(),
                FmConfig.gluttonyMode(),
                FmConfig.lustDisabled(),
                crystalwellUsesShardTag
            )
        ));
        host.registerResearch(
            List.of(ForbiddenMagicResearchCatalog.category()),
            ForbiddenMagicResearchCatalog.definitions(
                new ForbiddenMagicResearchCatalog.Options(
                    FmConfig.enchantmentsEnabled(),
                    FmConfig.greedyEnchantmentEnabled(),
                    FmConfig.lustDisabled(),
                    FmConfig.emeraldTrans(),
                    FmWands.isArsMagicaLoaded(),
                    crystalwellUsesShardTag
                )
            )
        );

        // Optional integration remains on its original supported 1.21.1 API.
        FmTinkerer.register();
    }

    private static void logAspectRegistry() {
        int fmAddonCount = Tc4AspectDiagnostics.addonLines().size();
        int expectedAddonCount = FmConfig.lustDisabled() ? 6 : 7;
        String luxuriaStatus;
        if (FmConfig.lustDisabled()) {
            luxuriaStatus = "disabled-by-config";
        } else if (Aspect.getAspect("luxuria") == null) {
            luxuriaStatus = "missing-registration-error";
        } else {
            luxuriaStatus = "registered";
        }
        ForbiddenMagic.LOGGER.info(
            "Aspect registry summary: total={} builtin={} registeredAddon={} fmAddon={} expectedFmAddon={} luxuria={}",
            Tc4AspectDiagnostics.totalCount(),
            Tc4AspectDiagnostics.BUILTIN_ASPECT_COUNT,
            Tc4AspectDiagnostics.totalCount() - Tc4AspectDiagnostics.BUILTIN_ASPECT_COUNT,
            fmAddonCount,
            expectedAddonCount,
            luxuriaStatus
        );
        for (String line : Tc4AspectDiagnostics.registryLines()) {
            ForbiddenMagic.LOGGER.info("Aspect registry: {}", line);
        }
    }
}
