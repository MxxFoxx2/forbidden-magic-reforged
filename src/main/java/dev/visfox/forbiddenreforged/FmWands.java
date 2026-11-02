package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;

/** Compatibility facade for host-owned wand registration and shared tuning values. */
public final class FmWands {
    private FmWands() {
    }

    public static void register() {
        HostAdapterRegistry.requireBootstrap().registerWands();
    }

    public static boolean supportsArsMagicaModId(String modId) {
        return "arsmagicalegacy".equals(modId) || "arsmagica2".equals(modId);
    }

    public static boolean isArsMagicaLoaded() {
        return HostAdapterRegistry.requireBootstrap().isArsMagicaLoaded();
    }

    public static int witchwoodRechargeInterval(boolean staff) {
        return staff ? 40 : 100;
    }

    /** Cost for one full vis point before partial-deficit scaling. */
    public static float witchwoodManaCost(boolean staff, String capTag) {
        if (staff) return "vinteum".equals(capTag) ? 50.0F : 100.0F;
        return "vinteum".equals(capTag) ? 80.0F : 140.0F;
    }

    /** Costs scale to stored vis units, where 100 internal units equal one vis point. */
    public static float witchwoodManaCost(boolean staff, String capTag, int deficit) {
        if (deficit <= 0) return 0.0F;
        return witchwoodManaCost(staff, capTag) * Math.min(deficit, 100) / 100.0F;
    }
}
