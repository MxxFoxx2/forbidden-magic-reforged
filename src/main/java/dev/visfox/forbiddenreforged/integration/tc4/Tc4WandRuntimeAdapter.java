package dev.visfox.forbiddenreforged.integration.tc4;

import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.Aspect;
import thaumcraft.wand.Wands;

public final class Tc4WandRuntimeAdapter {
    private Tc4WandRuntimeAdapter() {
    }

    public static boolean isWand(ItemStack wand) {
        return Wands.isWand(wand);
    }

    public static String rodTag(ItemStack wand) {
        return Wands.isWand(wand) ? Wands.getRod(wand).tag() : null;
    }

    public static int focusTreasure(ItemStack wand) {
        return Wands.getFocusTreasure(wand);
    }

    public static int focusPotency(ItemStack wand) {
        return Wands.getFocusPotency(wand);
    }

    public static void addVis(ItemStack wand, Aspect aspect, int amount, boolean apply) {
        Wands.addVis(wand, aspect, amount, apply);
    }
}
