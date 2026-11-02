package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.FmConfig;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.recipe.ForbiddenMagicRecipeCatalog;
import dev.visfox.forbiddenreforged.core.research.ForbiddenMagicResearchCatalog;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.Aspect;

public final class Tc4BootstrapAdapter {
    private Tc4BootstrapAdapter() {
    }

    public static void initialize() {
        logAspectRegistry();
        Tc4WarpAdapter.registerWarpItem(ContentId.parse("forbiddenmagic:wrath_cage"), 5);
        Tc4WarpAdapter.registerWarpItem(ContentId.parse("forbiddenmagic:taint_sapling"), 1);

        Tc4WandAdapter.registerWands();
        Tc4WandTriggerAdapter.register();

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
                    Tc4HostAdapter.INSTANCE.isArsMagicaLoaded(),
                    crystalwellUsesShardTag
                )
            )
        );

        // Optional integration remains on its original supported 1.21.1 API,
        // but registration is owned by the selected host adapter.
        Tc4TinkererAdapter.register();
    }

    /**
     * Adds the TC4-backed wand samples to the shared creative-tab stack list.
     * Host wand objects are resolved only in this adapter.
     */
    public static void addCreativeWands(List<ItemStack> stacks) {
        Tc4WandAdapter.addCreativeWands(stacks);
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
