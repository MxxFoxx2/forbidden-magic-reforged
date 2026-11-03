package dev.visfox.forbiddenreforged.core.wand;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Storage boundary for the Forbidden Magic Profane Wand's remaining contract. */
public interface WandContractAccess {
    @Nullable Integer remaining(ItemStack wand);

    void setRemaining(ItemStack wand, int amount);

    void clear(ItemStack wand);
}
