package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import net.minecraft.world.item.ItemStack;
import thaumcraft.wand.VisStorage;

public final class Tc4VisAdapter {
    private Tc4VisAdapter() {
    }

    public static void addRealVis(ItemStack stack, ContentId aspect, int amount, boolean apply) {
        VisStorage.addRealVis(stack, Tc4AspectAdapter.resolveAspect(aspect), amount, apply);
    }
}
