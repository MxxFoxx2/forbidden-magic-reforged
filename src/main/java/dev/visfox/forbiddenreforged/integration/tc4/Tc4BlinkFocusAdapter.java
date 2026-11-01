package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.BlinkFocusItem;
import dev.visfox.forbiddenreforged.core.focus.BlinkFocusAccess;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.AspectList;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.Wands;
import thaumcraft.wand.focus.FocusItem;

public final class Tc4BlinkFocusAdapter implements BlinkFocusAccess {
    public static final Tc4BlinkFocusAdapter INSTANCE = new Tc4BlinkFocusAdapter();

    private Tc4BlinkFocusAdapter() {
    }

    @Override
    public boolean consumeVis(ItemStack wand, Player player, boolean apply) {
        if (!(wand.getItem() instanceof WandItem wandItem)) {
            return false;
        }
        FocusItem focus = Wands.getFocus(wand);
        ItemStack focusStack = Wands.getFocusItem(wand);
        if (focus == null || focusStack.isEmpty()) {
            return false;
        }
        AspectList cost = focus.getVisCost(focusStack);
        return cost != null && wandItem.consumeAllVis(wand, player, cost, apply, false);
    }

    @Override
    public boolean hasUpgrade(ItemStack wand, ContentId upgrade) {
        FocusItem focus = Wands.getFocus(wand);
        ItemStack focusStack = Wands.getFocusItem(wand);
        if (focus == null || focusStack.isEmpty()) {
            return false;
        }
        if (BlinkFocusItem.HELLFIRE_ID.equals(upgrade)) {
            return focus.isUpgradedWith(focusStack, BlinkFocusItem.HELLFIRE);
        }
        if (BlinkFocusItem.PANDEMONIUM_ID.equals(upgrade)) {
            return focus.isUpgradedWith(focusStack, BlinkFocusItem.PANDEMONIUM);
        }
        return false;
    }

    @Override
    public int potency(ItemStack wand) {
        return Wands.getFocusPotency(wand);
    }

    @Override
    public int enlarge(ItemStack wand) {
        return Wands.getFocusEnlarge(wand);
    }
}
