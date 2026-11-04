package dev.visfox.forbiddenreforged.common;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.List;
import java.util.Map;

/**
 * Stores the Wrath Cage's three essentia channels and their selection mode.
 *
 * World access, persistence, and client synchronization stay in the block entity.
 */
public final class WrathCageEssentiaStore {
    static final int SUCTION = 256;

    private static final int MAX_STORED = 64;
    private static final ContentId WRATH_ASPECT = ContentId.parse("forbiddenmagic:ira");
    private static final ContentId SLOTH_ASPECT = ContentId.parse("forbiddenmagic:desidia");

    private int special;
    private int wrath;
    private int sloth;
    private int mode;

    public void clearMobEssentia() {
        special = 0;
    }

    public void cycleMode() {
        mode = mode >= 2 ? 0 : mode + 1;
    }

    public boolean hasRoom() {
        return special < MAX_STORED && wrath < MAX_STORED && sloth < MAX_STORED;
    }

    public int totalAmount() {
        return special + wrath + sloth;
    }

    /**
     * Pulls at most one essentia from adjacent inputs, preserving the original
     * priority and the rule that fallback channels only fill the current cost.
     */
    public boolean drawOne(
        int cost,
        ContentId mobAspect,
        List<WrathCageBlockEntity.EssentiaInput> inputs
    ) {
        for (WrathCageBlockEntity.EssentiaInput input : inputs) {
            if (special < MAX_STORED && input.take(mobAspect, 1) == 1) {
                special++;
                return true;
            }
            if (wrath < MAX_STORED && special < cost && input.take(WRATH_ASPECT, 1) == 1) {
                wrath++;
                return true;
            }
            if (sloth < MAX_STORED && special < cost && wrath < cost
                && input.take(SLOTH_ASPECT, 1) == 1) {
                sloth++;
                return true;
            }
        }
        return false;
    }

    public FuelChannel consumeFuel(int cost) {
        if (special >= cost) {
            special -= cost;
            return FuelChannel.MOB;
        }
        if (wrath >= cost) {
            wrath -= cost;
            return FuelChannel.WRATH;
        }
        if (sloth >= cost) {
            sloth -= cost;
            return FuelChannel.SLOTH;
        }
        return null;
    }

    Map<ContentId, Integer> visible(ContentId mobAspect) {
        return switch (mode) {
            case 0 -> Map.of(mobAspect, special);
            case 1 -> Map.of(WRATH_ASPECT, wrath);
            default -> Map.of(SLOTH_ASPECT, sloth);
        };
    }

    public boolean accepts(ContentId aspect, ContentId mobAspect) {
        return WRATH_ASPECT.equals(aspect)
            || SLOTH_ASPECT.equals(aspect)
            || mobAspect.equals(aspect);
    }

    public int addToContainer(ContentId aspect, int amount, ContentId mobAspect) {
        if (WRATH_ASPECT.equals(aspect)) {
            wrath += amount;
            return 0;
        }
        if (SLOTH_ASPECT.equals(aspect)) {
            sloth += amount;
            return 0;
        }
        if (mobAspect.equals(aspect)) {
            special += amount;
            return 0;
        }
        return amount;
    }

    public int amount(ContentId aspect, ContentId mobAspect) {
        if (WRATH_ASPECT.equals(aspect)) {
            return wrath;
        }
        if (SLOTH_ASPECT.equals(aspect)) {
            return sloth;
        }
        if (mobAspect.equals(aspect)) {
            return special;
        }
        return 0;
    }

    public int addTransport(ContentId aspect, int amount, ContentId mobAspect) {
        int accepted = Math.min(amount, MAX_STORED);
        if (mobAspect.equals(aspect)) {
            special += accepted;
            return accepted;
        }
        if (WRATH_ASPECT.equals(aspect)) {
            wrath += accepted;
            return accepted;
        }
        if (SLOTH_ASPECT.equals(aspect)) {
            sloth += accepted;
            return accepted;
        }
        return amount;
    }

    public ContentId suctionType(ContentId mobAspect) {
        return switch (mode) {
            case 0 -> mobAspect;
            case 1 -> WRATH_ASPECT;
            default -> SLOTH_ASPECT;
        };
    }

    public int selectedAmount() {
        return switch (mode) {
            case 0 -> special;
            case 1 -> wrath;
            default -> sloth;
        };
    }

    public int special() {
        return special;
    }

    public int wrath() {
        return wrath;
    }

    public int sloth() {
        return sloth;
    }

    public int mode() {
        return mode;
    }

    public void load(int special, int wrath, int sloth, int mode) {
        this.special = special;
        this.wrath = wrath;
        this.sloth = sloth;
        this.mode = mode;
    }

    public enum FuelChannel {
        MOB,
        WRATH,
        SLOTH
    }
}
