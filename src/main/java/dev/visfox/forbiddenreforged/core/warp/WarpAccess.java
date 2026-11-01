package dev.visfox.forbiddenreforged.core.warp;

import net.minecraft.server.level.ServerPlayer;

/** Applies a semantic warp change without coupling shared wand behavior to an event bus. */
public interface WarpAccess {
    void changeWarp(ServerPlayer player, int amount, WarpKind kind);
}
