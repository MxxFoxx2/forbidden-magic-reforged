package dev.visfox.forbiddenreforged.core.wand;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * The original cross-wand conversion from mana into primal vis.
 *
 * <p>Both the wand and the staff loop every primal aspect: for each aspect with room they
 * clamp the deficit to 100 and top the wand up by 1 vis only when the player can pay the
 * price for that deficit. The staff ticks three times as often and charges half as much per
 * point; a Vinteum cap halves the cost again. Mana is read once per tick and mirrored
 * locally, so the bridge is queried once instead of once per aspect.
 */
public final class ManaWandBehavior {
    private static final int INTERVAL_WAND = 100;
    private static final int INTERVAL_STAFF = 40;
    private static final double COST_WAND = 1.4;
    private static final double COST_STAFF = 1.0;
    private static final double COST_VINTEUM_WAND = 0.8;
    private static final double COST_VINTEUM_STAFF = 0.5;
    private static final int MAX_DEFICIT = 100;

    private ManaWandBehavior() {
    }

    /** Ticks between conversions: the original {@code ticksExisted % 100} for the wand. */
    public static int interval(boolean staff) {
        return staff ? INTERVAL_STAFF : INTERVAL_WAND;
    }

    /** Mana for one vis point before deficit scaling; a Vinteum cap halves it. */
    public static double pointCost(boolean staff, boolean vinteum) {
        if (staff) {
            return vinteum ? COST_VINTEUM_STAFF : COST_STAFF;
        }
        return vinteum ? COST_VINTEUM_WAND : COST_WAND;
    }

    /**
     * Mana to top one aspect up by a single vis. The original scales the price by how empty
     * the aspect is, capped at 100, so a nearly full aspect costs less than an empty one.
     */
    public static double price(boolean staff, boolean vinteum, int deficit) {
        if (deficit <= 0) {
            return 0;
        }
        return pointCost(staff, vinteum) * Math.min(deficit, MAX_DEFICIT);
    }

    public static void wand(ItemStack wand, Player player, WandVisAccess vis, ManaSource source, boolean vinteum) {
        convert(wand, player, vis, source, vinteum, false);
    }

    public static void staff(ItemStack wand, Player player, WandVisAccess vis, ManaSource source, boolean vinteum) {
        convert(wand, player, vis, source, vinteum, true);
    }

    private static void convert(
        ItemStack wand,
        Player player,
        WandVisAccess vis,
        ManaSource source,
        boolean vinteum,
        boolean staff
    ) {
        if (player.tickCount % interval(staff) != 0 || !source.available()) {
            return;
        }
        double mana = source.mana(player);
        if (mana <= 0) {
            return;
        }
        int maximum = vis.maximumVis(wand);
        for (ContentId aspect : vis.primalAspects()) {
            int deficit = Math.min(maximum - vis.vis(wand, aspect), MAX_DEFICIT);
            double price = price(staff, vinteum, deficit);
            if (deficit <= 0 || mana <= price || !source.spend(player, price)) {
                continue;
            }
            mana -= price;
            vis.addVis(wand, aspect, 1, true);
        }
    }
}
