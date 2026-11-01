package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.integration.tc4.Tc4WandTriggerAdapter;

public final class FmWandTriggers {
    private FmWandTriggers() {
    }

    public static void register() {
        Tc4WandTriggerAdapter.register();
    }
}
