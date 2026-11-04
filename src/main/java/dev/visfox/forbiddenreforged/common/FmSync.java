package dev.visfox.forbiddenreforged.common;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.PayloadRegistrar;

/**
 * Synchronizes content-gating startup flags from server to client.
 *
 * NeoForge does not sync STARTUP configs, and these flags change registered
 * content (aspects, items, recipes), so a mismatch would desync registries.
 * The server sends its authoritative flags on login; the client disconnects
 * itself when they differ, since it cannot reconcile registry differences.
 */
@EventBusSubscriber(modid = FmIds.MODID)
public final class FmSync {
    private FmSync() {
    }

    @SubscribeEvent
    static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(FmContentFlagsPayload.TYPE, FmContentFlagsPayload.STREAM_CODEC, FmSync::handleOnClient);
    }

    private static void handleOnClient(FmContentFlagsPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        if (!payload.matchesClient()) {
            context.disconnect(net.minecraft.network.chat.Component.literal(
                "Forbidden Magic content flags differ between client and server "
                    + "(no_lust, emerald_transmutation, enchanting, greedy_ench, wrath_cage). "
                    + "Align forbiddenmagic-startup.toml on both sides."
            ));
        }
    }

    @SubscribeEvent
    static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, FmContentFlagsPayload.ofConfig());
        }
    }
}
