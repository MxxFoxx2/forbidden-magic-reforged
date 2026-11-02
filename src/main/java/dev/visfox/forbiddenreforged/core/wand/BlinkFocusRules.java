package dev.visfox.forbiddenreforged.core.wand;

import java.util.List;

/** Host-independent Blink Focus upgrade progression and cost-profile rules. */
public final class BlinkFocusRules {
    public enum Upgrade {
        FRUGAL,
        HELLFIRE,
        PANDEMONIUM,
        POTENCY,
        ENLARGE
    }

    public enum CostProfile {
        BASE,
        HELLFIRE,
        PANDEMONIUM
    }

    private BlinkFocusRules() {
    }

    public static CostProfile costProfile(boolean hellfire, boolean pandemonium) {
        if (hellfire) {
            return CostProfile.HELLFIRE;
        }
        return pandemonium ? CostProfile.PANDEMONIUM : CostProfile.BASE;
    }

    public static List<Upgrade> possibleUpgradesByRank(int rank, boolean hellfire, boolean pandemonium) {
        return switch (rank) {
            case 1 -> List.of(Upgrade.FRUGAL);
            case 2 -> List.of(Upgrade.HELLFIRE, Upgrade.PANDEMONIUM, Upgrade.FRUGAL);
            case 3, 4, 5 -> hellfire
                ? List.of(Upgrade.POTENCY, Upgrade.FRUGAL, Upgrade.ENLARGE)
                : pandemonium
                    ? List.of(Upgrade.FRUGAL, Upgrade.ENLARGE)
                    : List.of(Upgrade.FRUGAL);
            default -> List.of();
        };
    }
}
