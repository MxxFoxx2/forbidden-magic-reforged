package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.Aspect;
import thaumcraft.wand.Wands;
import org.jetbrains.annotations.Nullable;

public final class Tc4WandRuntimeAdapter {
    private Tc4WandRuntimeAdapter() {
    }

    public static boolean isWand(ItemStack wand) {
        return Wands.isWand(wand);
    }

    public static @Nullable String rodTag(ItemStack wand) {
        return Wands.isWand(wand) ? Wands.getRod(wand).tag() : null;
    }

    public static int focusTreasure(ItemStack wand) {
        return Wands.getFocusTreasure(wand);
    }

    public static int focusPotency(ItemStack wand) {
        return Wands.getFocusPotency(wand);
    }

    public static ItemStack focusItem(ItemStack wand) {
        return Wands.getFocusItem(wand);
    }

    public static int focusEnlarge(ItemStack wand) {
        return Wands.getFocusEnlarge(wand);
    }

    public static void addVis(ItemStack wand, Aspect aspect, int amount, boolean apply) {
        Wands.addVis(wand, aspect, amount, apply);
    }

    public static void addVis(ItemStack wand, ContentId aspect, int amount, boolean apply) {
        Wands.addVis(wand, Tc4AspectAdapter.resolveAspect(aspect), amount, apply);
    }
}
