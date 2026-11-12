package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import dev.visfox.forbiddenreforged.core.wand.WitchwoodManaBehavior;
import dev.visfox.forbiddenreforged.core.wand.WandCapKind;

/** Compatibility facade for host-owned wand registration and shared tuning values. */
public final class FmWands {
    private FmWands() {
    }

    public static void register() {
        HostAdapterRegistry.requireWandRegistration().registerWands();
    }

    public static boolean supportsArsMagicaModId(String modId) {
        return WitchwoodManaBehavior.supportsArsMagicaModId(modId);
    }

    public static boolean isArsMagicaLoaded() {
        return HostAdapterRegistry.requireWandRegistration().isArsMagicaLoaded();
    }

    public static int witchwoodRechargeInterval(boolean staff) {
        return WitchwoodManaBehavior.rechargeInterval(staff);
    }

    /** Cost for one full vis point before partial-deficit scaling. */
    public static float witchwoodManaCost(boolean staff, WandCapKind capKind) {
        return WitchwoodManaBehavior.manaCost(staff, capKind);
    }

    /** Costs scale to stored vis units, where 100 internal units equal one vis point. */
    public static float witchwoodManaCost(boolean staff, WandCapKind capKind, int deficit) {
        return WitchwoodManaBehavior.manaCost(staff, capKind, deficit);
    }

    /** @deprecated Prefer the semantic {@link WandCapKind} overloads. */
    @Deprecated
    public static float witchwoodManaCost(boolean staff, String legacyCapTag) {
        return witchwoodManaCost(staff, legacyCapKind(legacyCapTag));
    }

    /** @deprecated Prefer the semantic {@link WandCapKind} overloads. */
    @Deprecated
    public static float witchwoodManaCost(boolean staff, String legacyCapTag, int deficit) {
        return witchwoodManaCost(staff, legacyCapKind(legacyCapTag), deficit);
    }

    private static WandCapKind legacyCapKind(String tag) {
        if ("iron".equals(tag)) {
            return WandCapKind.IRON;
        }
        if ("vinteum".equals(tag)) {
            return WandCapKind.VINTEUM;
        }
        return WandCapKind.OTHER;
    }
}
