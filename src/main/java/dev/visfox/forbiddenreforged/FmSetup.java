package dev.visfox.forbiddenreforged;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import thaumcraft.aspect.AddonAspects;

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class FmSetup {
    private FmSetup() {
    }

    @SubscribeEvent
    static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            AddonAspects.init();
            FmWands.register();
            FmWandTriggers.register();
            FmRecipes.register();
            FmResearch.register();
            FmTinkerer.register();
        });
    }
}
