package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.FmConfig;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import net.neoforged.fml.ModList;

public final class Tc4TinkererAdapter {
    private static int registered;

    private Tc4TinkererAdapter() {
    }

    public static synchronized void register() {
        registered = 0;
        if (!FmConfig.enchantmentsEnabled() || !ModList.get().isLoaded("thaumictinkerer")) {
            return;
        }

        try {
            registered = Tc4TinkererLinkedAdapter.register();
        } catch (LinkageError | RuntimeException error) {
            registered = 0;
            ForbiddenMagic.LOGGER.warn(
                "Thaumic Tinkerer is installed, but its pinned 1.21.1 API could not be linked; skipping optional enchantment costs",
                error
            );
        }
    }

    public static int registeredCount() {
        return registered;
    }
}
