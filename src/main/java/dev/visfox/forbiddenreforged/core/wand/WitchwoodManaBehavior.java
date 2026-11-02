package dev.visfox.forbiddenreforged.core.wand;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Pure Witchwood wand/staff mana conversion rules shared by host adapters. */
public final class WitchwoodManaBehavior {
    public record VisDeficit(ContentId aspect, int missingInternalUnits) {
        public VisDeficit {
            Objects.requireNonNull(aspect, "aspect");
        }
    }

    public record RechargePlan(List<ContentId> aspects, double remainingMana) {
        public RechargePlan {
            aspects = List.copyOf(aspects);
        }
    }

    private WitchwoodManaBehavior() {
    }

    public static boolean supportsArsMagicaModId(String modId) {
        return "arsmagicalegacy".equals(modId) || "arsmagica2".equals(modId);
    }

    public static int rechargeInterval(boolean staff) {
        return staff ? 40 : 100;
    }

    /** Cost for one full vis point before partial-deficit scaling. */
    public static float manaCost(boolean staff, WandCapKind capKind) {
        Objects.requireNonNull(capKind, "capKind");
        if (staff) {
            return capKind == WandCapKind.VINTEUM ? 50.0F : 100.0F;
        }
        return capKind == WandCapKind.VINTEUM ? 80.0F : 140.0F;
    }

    /** Costs scale to stored vis units, where 100 internal units equal one vis point. */
    public static float manaCost(boolean staff, WandCapKind capKind, int deficit) {
        if (deficit <= 0) {
            return 0.0F;
        }
        return manaCost(staff, capKind) * Math.min(deficit, 100) / 100.0F;
    }

    /**
     * Plans one Witchwood update in input order. Each selected aspect gains one
     * Vis point, and the corresponding mana is reserved before the host applies
     * any Vis writes. The strict greater-than check preserves the original
     * behavior at an exact mana-cost boundary.
     */
    public static RechargePlan planRecharge(
        boolean staff,
        WandCapKind capKind,
        double currentMana,
        List<VisDeficit> deficits
    ) {
        Objects.requireNonNull(capKind, "capKind");
        Objects.requireNonNull(deficits, "deficits");
        if (currentMana <= 0.0D) {
            return new RechargePlan(List.of(), currentMana);
        }

        double remaining = currentMana;
        List<ContentId> toRecharge = new ArrayList<>();
        for (VisDeficit deficit : deficits) {
            Objects.requireNonNull(deficit, "deficit");
            if (deficit.missingInternalUnits() <= 0) {
                continue;
            }
            double cost = manaCost(staff, capKind, deficit.missingInternalUnits());
            if (remaining > cost) {
                remaining -= cost;
                toRecharge.add(deficit.aspect());
            }
        }
        return new RechargePlan(toRecharge, remaining);
    }
}
