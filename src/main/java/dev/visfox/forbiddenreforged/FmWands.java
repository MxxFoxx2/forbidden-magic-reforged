package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import dev.visfox.forbiddenreforged.core.wand.WitchwoodManaBehavior;

/** Compatibility facade for host-owned wand registration and shared tuning values. */
public final class FmWands {
    private FmWands() {
    }

    public static void register() {
        HostAdapterRegistry.requireBootstrap().registerWands();
    }

    public static boolean supportsArsMagicaModId(String modId) {
        return WitchwoodManaBehavior.supportsArsMagicaModId(modId);
    }

    public static boolean isArsMagicaLoaded() {
        return HostAdapterRegistry.requireBootstrap().isArsMagicaLoaded();
    }

    public static int witchwoodRechargeInterval(boolean staff) {
        return WitchwoodManaBehavior.rechargeInterval(staff);
    }

    /** Cost for one full vis point before partial-deficit scaling. */
    public static float witchwoodManaCost(boolean staff, String capTag) {
        return WitchwoodManaBehavior.manaCost(staff, capTag);
    }

    /** Costs scale to stored vis units, where 100 internal units equal one vis point. */
    public static float witchwoodManaCost(boolean staff, String capTag, int deficit) {
        return WitchwoodManaBehavior.manaCost(staff, capTag, deficit);
    }
}
