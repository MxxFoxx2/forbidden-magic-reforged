package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.integration.tc4.Tc4AspectDiagnostics;
import dev.visfox.forbiddenreforged.FmAspects;
import dev.visfox.forbiddenreforged.FmConfig;
import dev.visfox.forbiddenreforged.FmItems;
import dev.visfox.forbiddenreforged.FmRecipes;
import dev.visfox.forbiddenreforged.FmResearch;
import dev.visfox.forbiddenreforged.FmWandTriggers;
import dev.visfox.forbiddenreforged.FmWands;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import net.minecraft.world.item.Items;
import thaumcraft.aspect.AddonAspects;
import thaumcraft.device.SmeltingBonuses;

public final class Tc4BootstrapAdapter {
    private Tc4BootstrapAdapter() {
    }

    public static void initialize() {
        AddonAspects.init();
        logAspectRegistry();
        Tc4WarpAdapter.register();
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
        } else if (FmAspects.LUST == null) {
            luxuriaStatus = "missing-registration-error";
        } else {
            luxuriaStatus = "registered";
        }
        ForbiddenMagic.LOGGER.info(
            "Aspect registry summary: total={} builtin={} registeredAddon={} fmAddon={} expectedFmAddon={} luxuria={}",
            FmAspects.count(),
            FmAspects.BUILTIN_ASPECT_COUNT,
            FmAspects.count() - FmAspects.BUILTIN_ASPECT_COUNT,
            fmAddonCount,
            expectedAddonCount,
            luxuriaStatus
        );
        for (String line : Tc4AspectDiagnostics.registryLines()) {
            ForbiddenMagic.LOGGER.info("Aspect registry: {}", line);
        }
    }
}
