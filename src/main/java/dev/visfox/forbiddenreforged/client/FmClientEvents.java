package dev.visfox.forbiddenreforged.client;

import dev.visfox.forbiddenreforged.FmBlockEntities;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = ForbiddenMagic.MODID, value = Dist.CLIENT)
public final class FmClientEvents {
    private FmClientEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(FmBlockEntities.WRATH_CAGE.get(), WrathCageRenderer::new);
    }
}
