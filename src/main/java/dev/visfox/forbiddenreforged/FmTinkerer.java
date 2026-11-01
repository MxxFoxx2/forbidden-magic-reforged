package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.integration.tc4.Tc4TinkererAdapter;

/** Compatibility entry point for the optional Thaumic Tinkerer integration. */
public final class FmTinkerer {
    private FmTinkerer() {
    }

    public static void register() {
        Tc4TinkererAdapter.register();
    }

    public static int registeredCount() {
        return Tc4TinkererAdapter.registeredCount();
    }
}
