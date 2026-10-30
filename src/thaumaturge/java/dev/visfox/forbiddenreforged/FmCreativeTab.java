package dev.visfox.forbiddenreforged;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FmCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ForbiddenMagic.MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FORBIDDEN = TABS.register("forbidden",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.forbiddenmagic"))
            .icon(() -> new ItemStack(FmItems.DEADLY_SHARDS.get("lust").get()))
            .displayItems((parameters, output) -> FmItems.ITEMS.getEntries().forEach(item -> output.accept(item.get())))
            .build());

    private FmCreativeTab() {
    }
}
