package dev.visfox.forbiddenreforged;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import thaumcraft.aspect.AddonAspects;
import thaumcraft.device.SmeltingBonuses;
import thaumcraft.aspect.Aspect;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4WarpAdapter;

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class FmSetup {
    private FmSetup() {
    }

    @SubscribeEvent
    static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            AddonAspects.init();
            logAspectRegistry();
            Tc4WarpAdapter.register();
            Tc4WarpAdapter.registerWarpItem(ContentId.parse("forbiddenmagic:wrath_cage"), 5);
            Tc4WarpAdapter.registerWarpItem(ContentId.parse("forbiddenmagic:taint_sapling"), 1);
            FmWands.register();
            FmWandTriggers.register();
            FmRecipes.register();
            FmResearch.register();
            thaumcraft.device.SmeltingBonuses.register(
                net.minecraft.world.item.Items.EMERALD,
                FmItems.EMERALD_FRAGMENT.get());
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
                FmAspects.count(),
                FmAspects.BUILTIN_ASPECT_COUNT,
                FmAspects.count() - FmAspects.BUILTIN_ASPECT_COUNT,
                fmAddonCount,
                expectedAddonCount,
                luxuriaStatus
            );
            for (String line : FmAspectDiagnostics.registryLines()) {
                ForbiddenMagic.LOGGER.info("Aspect registry: {}", line);
            }
        }
}
