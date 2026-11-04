package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.wand.BlinkFocusRules;
import dev.visfox.forbiddenreforged.core.wand.BlinkFocusRules.VisCost;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.AspectList;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.focus.FocusItem;
import thaumcraft.wand.focus.FocusUpgradeType;

/** Converts shared Blink Focus rules into the pinned Thaumcraft focus API. */
public final class Tc4FocusAdapter {
    private Tc4FocusAdapter() {}

    public static boolean hasUpgrade(ItemStack focus, BlinkFocusRules.Upgrade upgrade) {
        if (!(focus.getItem() instanceof FocusItem focusItem)) return false;
        return focusItem.isUpgradedWith(focus, toHostUpgrade(upgrade));
    }

    /** Converts a shared semantic upgrade into the corresponding pinned TC4 type. */
    public static FocusUpgradeType toHostUpgrade(BlinkFocusRules.Upgrade upgrade) {
        return switch (upgrade) {
            case FRUGAL -> FocusUpgradeType.FRUGAL;
            case HELLFIRE -> Tc4BlinkFocusItem.HELLFIRE;
            case PANDEMONIUM -> Tc4BlinkFocusItem.PANDEMONIUM;
            case POTENCY -> FocusUpgradeType.POTENCY;
            case ENLARGE -> FocusUpgradeType.ENLARGE;
        };
    }

    /** Preserves the order supplied by the shared progression rules. */
    public static FocusUpgradeType[] toHostUpgrades(List<BlinkFocusRules.Upgrade> upgrades) {
        return upgrades.stream().map(Tc4FocusAdapter::toHostUpgrade).toArray(FocusUpgradeType[]::new);
    }

    public static AspectList toAspectList(List<VisCost> costs) {
        AspectList result = new AspectList();
        for (VisCost cost : costs) result.add(Tc4AspectAdapter.resolveAspect(cost.aspect()), cost.amount());
        return result;
    }

    public static boolean consumeVis(ItemStack wand, Player player, List<VisCost> costs, boolean apply) {
        if (!(wand.getItem() instanceof WandItem wandItem)) return false;
        return wandItem.consumeAllVis(wand, player, toAspectList(costs), apply, false);
    }
}
