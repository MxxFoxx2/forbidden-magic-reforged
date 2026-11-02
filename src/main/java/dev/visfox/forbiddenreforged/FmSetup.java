package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class FmSetup {
    private FmSetup() {
    }

    @SubscribeEvent
    static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> HostAdapterRegistry.require().initializeRuntime());
    }
}
