package dev.visfox.forbiddenreforged.common;

import dev.visfox.forbiddenreforged.core.wand.WandContractAccess;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/** Maps the common Profane Contract data component to the core wand contract port. */
public final class FmWandContractAccess implements WandContractAccess {
    public static final FmWandContractAccess INSTANCE = new FmWandContractAccess();

    private FmWandContractAccess() {
    }

    @Override
    public @Nullable Integer remaining(ItemStack wand) {
        return wand.get(FmComponents.PROFANE_CONTRACT.get());
    }

    @Override
    public void setRemaining(ItemStack wand, int amount) {
        wand.set(FmComponents.PROFANE_CONTRACT.get(), amount);
    }

    @Override
    public void clear(ItemStack wand) {
        wand.remove(FmComponents.PROFANE_CONTRACT.get());
    }
}
