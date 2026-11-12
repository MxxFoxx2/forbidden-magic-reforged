package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FmCreativeTab {
    private static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ForbiddenMagic.MODID);

    public static final Supplier<CreativeModeTab> FORBIDDEN = TABS.register("forbidden", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.forbiddenmagic"))
        .icon(() -> new ItemStack(FmItems.DIABOLIST_FORK.get()))
        .displayItems((parameters, output) -> stacks().forEach(output::accept))
        .build());

    private FmCreativeTab() {
    }

    public static void register(IEventBus modEventBus) {
        if (FmConfig.wrathCage()) {
            TABS.register("mobcrystal", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.mobcrystal"))
                .icon(() -> new ItemStack(FmItems.MOB_CRYSTAL.get()))
                .displayItems((parameters, output) -> mobCrystalStacks().forEach(output::accept))
                .build());
        }
        TABS.register(modEventBus);
    }

    public static List<ItemStack> stacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (DeferredHolder<Item, ? extends Item> holder : FmItems.ITEMS.getEntries()) {
            if (holder.get() == FmItems.MOB_CRYSTAL.get()) {
                continue;
            }
            stacks.add(new ItemStack(holder.get()));
        }
        HostAdapterRegistry.requireCreativeContent().addCreativeContent(stacks);
        return stacks;
    }

    /** Returns the empty and imprinted crystals shown in the legacy Wrath Cage tab. */
    public static List<ItemStack> mobCrystalStacks() {
        if (!FmConfig.wrathCage()) {
            return List.of();
        }
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(FmItems.MOB_CRYSTAL.get()));
        for (String mobId : FmSpawnerMobs.ids()) {
            ItemStack crystal = new ItemStack(FmItems.MOB_CRYSTAL.get());
            MobCrystalItem.setMob(crystal, mobId);
            stacks.add(crystal);
        }
        return stacks;
    }
}
