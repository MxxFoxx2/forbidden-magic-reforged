package dev.visfox.forbiddenreforged.core.warp;

import net.minecraft.server.level.ServerPlayer;

/**
 * Applies a semantic warp change without coupling shared behavior to an event bus.
 * A zero amount must be treated as a no-op by every host implementation.
 */
@FunctionalInterface
public interface WarpAccess {
    void changeWarp(ServerPlayer player, int amount, WarpKind kind);
}
