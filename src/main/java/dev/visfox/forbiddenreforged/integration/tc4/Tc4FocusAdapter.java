package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.BlinkFocusItem;
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
        FocusUpgradeType hostUpgrade = switch (upgrade) {
            case FRUGAL -> FocusUpgradeType.FRUGAL;
            case HELLFIRE -> BlinkFocusItem.HELLFIRE;
            case PANDEMONIUM -> BlinkFocusItem.PANDEMONIUM;
            case POTENCY -> FocusUpgradeType.POTENCY;
            case ENLARGE -> FocusUpgradeType.ENLARGE;
        };
        return focusItem.isUpgradedWith(focus, hostUpgrade);
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
