package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.warp.WarpKind;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import thaumcraft.research.Warp;
import thaumcraft.research.WarpType;

public final class Tc4WarpAdapter {
    private Tc4WarpAdapter() {
    }

    public static void registerWarpItem(ContentId item, int amount) {
        Objects.requireNonNull(item, "item");
        if (amount < 0) {
            throw new IllegalArgumentException("Registered item warp cannot be negative");
        }
        Warp.registerItem(
            ResourceLocation.fromNamespaceAndPath(item.namespace(), item.path()),
            amount
        );
    }

    public static WarpType toHostType(WarpKind kind) {
        return switch (Objects.requireNonNull(kind, "kind")) {
            case PERMANENT -> WarpType.PERMANENT;
            case STICKY -> WarpType.STICKY;
            case TEMPORARY -> WarpType.TEMPORARY;
        };
    }

    public static void changeWarp(ServerPlayer player, int amount, WarpKind kind) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(kind, "kind");
        if (amount != 0) {
            Warp.add(player, amount, toHostType(kind));
        }
    }
}
