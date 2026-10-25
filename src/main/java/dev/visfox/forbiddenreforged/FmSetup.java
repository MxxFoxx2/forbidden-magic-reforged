package dev.visfox.forbiddenreforged;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import thaumcraft.aspect.AddonAspects;
import thaumcraft.aspect.Aspect;
import thaumcraft.research.Warp;

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class FmSetup {
    private FmSetup() {
    }

    @SubscribeEvent
    static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            AddonAspects.init();
            logAspectRegistry();
            FmWands.register();
            FmWandTriggers.register();
            FmRecipes.register();
            FmResearch.register();
            Warp.registerItem(ForbiddenMagic.id("wrath_cage"), 5);
            Warp.registerItem(ForbiddenMagic.id("taint_sapling"), 1);
            FmTinkerer.register();
        });
    }
    private static void logAspectRegistry() {
            int fmAddonCount = FmAspectDiagnostics.addonLines().size();
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
                Aspect.count(),
                Aspect.builtinCount(),
                Aspect.count() - Aspect.builtinCount(),
                fmAddonCount,
                expectedAddonCount,
                luxuriaStatus
            );
            for (String line : FmAspectDiagnostics.registryLines()) {
                ForbiddenMagic.LOGGER.info("Aspect registry: {}", line);
            }
        }
}
