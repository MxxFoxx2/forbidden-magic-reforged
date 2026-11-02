package dev.visfox.forbiddenreforged.core.wand;

/** Pure Witchwood wand/staff mana conversion rules shared by host adapters. */
public final class WitchwoodManaBehavior {
    private WitchwoodManaBehavior() {
    }

    public static boolean supportsArsMagicaModId(String modId) {
        return "arsmagicalegacy".equals(modId) || "arsmagica2".equals(modId);
    }

    public static int rechargeInterval(boolean staff) {
        return staff ? 40 : 100;
    }

    /** Cost for one full vis point before partial-deficit scaling. */
    public static float manaCost(boolean staff, String capTag) {
        if (staff) {
            return "vinteum".equals(capTag) ? 50.0F : 100.0F;
        }
        return "vinteum".equals(capTag) ? 80.0F : 140.0F;
    }

    /** Costs scale to stored vis units, where 100 internal units equal one vis point. */
    public static float manaCost(boolean staff, String capTag, int deficit) {
        if (deficit <= 0) {
            return 0.0F;
        }
        return manaCost(staff, capTag) * Math.min(deficit, 100) / 100.0F;
    }
}
