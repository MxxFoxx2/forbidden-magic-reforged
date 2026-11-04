package dev.visfox.forbiddenreforged.core.wand;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.List;

/** Host-independent Blink Focus upgrade progression and cost rules. */
public final class BlinkFocusRules {
    public enum Upgrade { FRUGAL, HELLFIRE, PANDEMONIUM, POTENCY, ENLARGE }
    public enum CostProfile { BASE, HELLFIRE, PANDEMONIUM }

    public record VisCost(ContentId aspect, int amount) {
        public VisCost {
            if (aspect == null) throw new NullPointerException("aspect");
            if (amount <= 0) throw new IllegalArgumentException("Vis cost must be positive");
        }
    }

    private static final ContentId ENTROPY = concept("entropy");
    private static final ContentId FIRE = concept("fire");
    private static final ContentId ORDER = concept("order");

    private BlinkFocusRules() {}

    public static CostProfile costProfile(boolean hellfire, boolean pandemonium) {
        if (hellfire) return CostProfile.HELLFIRE;
        return pandemonium ? CostProfile.PANDEMONIUM : CostProfile.BASE;
    }

    public static List<VisCost> visCosts(boolean hellfire, boolean pandemonium) {
        return switch (costProfile(hellfire, pandemonium)) {
            case BASE -> List.of(new VisCost(ENTROPY, 300));
            case HELLFIRE -> List.of(new VisCost(ENTROPY, 300), new VisCost(FIRE, 100));
            case PANDEMONIUM -> List.of(new VisCost(ENTROPY, 300), new VisCost(ORDER, 100));
        };
    }

    public static List<Upgrade> possibleUpgradesByRank(int rank, boolean hellfire, boolean pandemonium) {
        return switch (rank) {
            case 1 -> List.of(Upgrade.FRUGAL);
            case 2 -> List.of(Upgrade.HELLFIRE, Upgrade.PANDEMONIUM, Upgrade.FRUGAL);
            case 3, 4, 5 -> hellfire
                ? List.of(Upgrade.POTENCY, Upgrade.FRUGAL, Upgrade.ENLARGE)
                : pandemonium ? List.of(Upgrade.FRUGAL, Upgrade.ENLARGE) : List.of(Upgrade.FRUGAL);
            default -> List.of();
        };
    }

    private static ContentId concept(String name) {
        return new ContentId("forbiddenmagic", "concept/" + name);
    }
}
