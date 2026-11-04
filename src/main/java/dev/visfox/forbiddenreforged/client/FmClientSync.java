package dev.visfox.forbiddenreforged.client;

import dev.visfox.forbiddenreforged.common.FmContentFlagsPayload;
import dev.visfox.forbiddenreforged.common.FmIds;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client side of the content-flag sync: disconnects when the server's
 * content-gating flags differ from the local startup config, because the
 * registries would be incompatible.
 */
@EventBusSubscriber(modid = FmIds.MODID, value = Dist.CLIENT)
public final class FmClientSync {
    private FmClientSync() {
    }

    @SubscribeEvent
    static void register(RegisterClientPayloadHandlersEvent event) {
        event.register(FmContentFlagsPayload.TYPE, FmClientSync::handle);
    }

    private static void handle(FmContentFlagsPayload payload, IPayloadContext context) {
        if (!payload.matchesClient()) {
            context.disconnect(Component.literal(
                "Forbidden Magic content flags differ between client and server "
                    + "(no_lust, emerald_transmutation, enchanting, greedy_ench, wrath_cage). "
                    + "Align forbiddenmagic-startup.toml on both sides."
            ));
        }
    }
}
