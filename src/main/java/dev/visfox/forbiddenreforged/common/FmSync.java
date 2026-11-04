package dev.visfox.forbiddenreforged.common;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Synchronizes content-gating startup flags from server to client.
 *
 * NeoForge does not sync STARTUP configs, and these flags change registered
 * content (aspects, items, recipes), so a mismatch would desync registries.
 * The server registers the payload and sends its authoritative flags on login;
 * the client-side handler lives in {@code client/FmClientSync} and disconnects
 * on mismatch, since registries cannot be reconciled after the fact.
 */
@EventBusSubscriber(modid = FmIds.MODID)
public final class FmSync {
    private FmSync() {
    }

    @SubscribeEvent
    static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(FmContentFlagsPayload.TYPE, FmContentFlagsPayload.STREAM_CODEC);
    }

    @SubscribeEvent
    static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, FmContentFlagsPayload.ofConfig());
        }
    }
}
