package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.integration.tc4.Tc4AspectDiagnostics;
import dev.visfox.forbiddenreforged.common.FmConfig;
import dev.visfox.forbiddenreforged.common.FmItems;
import dev.visfox.forbiddenreforged.common.FmRecipes;
import dev.visfox.forbiddenreforged.common.FmResearch;
import dev.visfox.forbiddenreforged.common.FmWandTriggers;
import dev.visfox.forbiddenreforged.common.FmWands;
import dev.visfox.forbiddenreforged.common.FmIds;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import net.minecraft.world.item.Items;
import thaumcraft.aspect.Aspect;
import thaumcraft.device.SmeltingBonuses;

public final class Tc4BootstrapAdapter {
    private Tc4BootstrapAdapter() {
    }

    public static void initialize() {
        logAspectRegistry();
        Tc4WarpAdapter.registerWarpItem(ContentId.parse("forbiddenmagic:wrath_cage"), 5);
        Tc4WarpAdapter.registerWarpItem(ContentId.parse("forbiddenmagic:taint_sapling"), 1);
        FmWands.register();
        FmWandTriggers.register();
        FmRecipes.register();
        FmResearch.register();
        SmeltingBonuses.register(Items.EMERALD, FmItems.EMERALD_FRAGMENT.get());
        Tc4TinkererAdapter.register();
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
        FmIds.LOGGER.info(
            "Aspect registry summary: total={} builtin={} registeredAddon={} fmAddon={} expectedFmAddon={} luxuria={}",
            Tc4AspectDiagnostics.totalCount(),
            Tc4AspectDiagnostics.BUILTIN_ASPECT_COUNT,
            Tc4AspectDiagnostics.totalCount() - Tc4AspectDiagnostics.BUILTIN_ASPECT_COUNT,
            fmAddonCount,
            expectedAddonCount,
            luxuriaStatus
        );
        for (String line : Tc4AspectDiagnostics.registryLines()) {
            FmIds.LOGGER.info("Aspect registry: {}", line);
        }
    }
}
