package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.integration.tc4.Tc4WandAdapter;

public final class FmWands {
    private FmWands() {
    }

    public static void register() {
        Tc4WandAdapter.registerWands();
    }
}
