package dev.visfox.forbiddenreforged.integration.runtime;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Read-only wand and focus queries exposed by the selected magic host. */
public interface HostWandInspectionServices {
    boolean isWand(ItemStack stack);

    @Nullable String wandRodTag(ItemStack stack);

    int wandFocusTreasure(ItemStack stack);

    int wandFocusPotency(ItemStack stack);
}
