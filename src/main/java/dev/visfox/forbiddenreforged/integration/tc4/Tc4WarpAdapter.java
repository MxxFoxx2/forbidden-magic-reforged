package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.warp.WarpChangeRequested;
import dev.visfox.forbiddenreforged.core.warp.WarpKind;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForge;
import thaumcraft.research.Warp;
import thaumcraft.research.WarpType;

public final class Tc4WarpAdapter {
    private static boolean registered;

    private Tc4WarpAdapter() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(Tc4WarpAdapter::onWarpChangeRequested);
        registered = true;
    }

    public static void registerWarpItem(ContentId item, int amount) {
        Objects.requireNonNull(item, "item");
        if (amount < 0) {
            throw new IllegalArgumentException("Registered item warp cannot be negative");
        }
        Warp.registerItem(
            Identifier.fromNamespaceAndPath(item.namespace(), item.path()),
            amount
        );
    }

    public static WarpType toHostType(WarpKind kind) {
        return switch (Objects.requireNonNull(kind, "kind")) {
            case NORMAL -> WarpType.NORMAL;
            case STICKY -> WarpType.STICKY;
            case TEMPORARY -> WarpType.TEMPORARY;
        };
    }

    private static void onWarpChangeRequested(WarpChangeRequested event) {
        Warp.add(event.player(), event.amount(), toHostType(event.kind()));
    }
}
