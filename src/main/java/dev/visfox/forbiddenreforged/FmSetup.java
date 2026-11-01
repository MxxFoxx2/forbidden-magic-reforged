package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.integration.tc4.Tc4BootstrapAdapter;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class FmSetup {
    private FmSetup() {
    }

    @SubscribeEvent
    static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(Tc4BootstrapAdapter::initialize);
    }
}
