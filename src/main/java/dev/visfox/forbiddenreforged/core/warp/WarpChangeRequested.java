package dev.visfox.forbiddenreforged.core.warp;

import java.util.Objects;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

public final class WarpChangeRequested extends Event {
    private final ServerPlayer player;
    private final int amount;
    private final WarpKind kind;

    public WarpChangeRequested(ServerPlayer player, int amount, WarpKind kind) {
        this.player = Objects.requireNonNull(player, "player");
        this.amount = amount;
        this.kind = Objects.requireNonNull(kind, "kind");
    }

    public ServerPlayer player() {
        return player;
    }

    public int amount() {
        return amount;
    }

    public WarpKind kind() {
        return kind;
    }

    public static void request(ServerPlayer player, int amount, WarpKind kind) {
        if (amount != 0) {
            NeoForge.EVENT_BUS.post(new WarpChangeRequested(player, amount, kind));
        }
    }
}
