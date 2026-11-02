package dev.visfox.forbiddenreforged.integration.runtime;

import dev.visfox.forbiddenreforged.core.wand.WandRodKind;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Read-only wand and focus queries exposed by the selected magic host. */
public interface HostWandInspectionServices {
    boolean isWand(ItemStack stack);
    WandRodKind wandRodKind(ItemStack stack);
    ItemStack wandFocusItem(ItemStack stack);
    int wandFocusTreasure(ItemStack stack);
    int wandFocusPotency(ItemStack stack);
    int wandFocusEnlarge(ItemStack stack);
}
