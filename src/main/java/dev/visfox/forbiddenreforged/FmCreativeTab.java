package dev.visfox.forbiddenreforged;

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
import dev.visfox.forbiddenreforged.integration.tc4.Tc4WandAdapter;

public final class FmCreativeTab {
    private static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ForbiddenMagic.MODID);

    public static final Supplier<CreativeModeTab> FORBIDDEN = TABS.register("forbidden", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.forbiddenmagic"))
        .icon(() -> new ItemStack(FmItems.DIABOLIST_FORK.get()))
        .displayItems((parameters, output) -> stacks().forEach(output::accept))
        .build());

    public static final Supplier<CreativeModeTab> MOB_CRYSTAL = TABS.register("mobcrystal", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.forbiddenmagic_mobcrystal"))
        .icon(() -> new ItemStack(FmItems.MOB_CRYSTAL.get()))
        .displayItems((parameters, output) -> mobCrystalStacks().forEach(output::accept))
        .build());

    private FmCreativeTab() {
    }

    public static void register(IEventBus modEventBus) {
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
        addCreativeWands(stacks);
        return stacks;
    }

    public static List<ItemStack> mobCrystalStacks() {
        if (!FmConfig.wrathCage()) {
            return List.of();
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (String mobId : FmSpawnerMobs.ids()) {
            ItemStack crystal = new ItemStack(FmItems.MOB_CRYSTAL.get());
            MobCrystalItem.setMob(crystal, mobId);
            stacks.add(crystal);
        }
        return stacks;
    }

    private static void addCreativeWands(List<ItemStack> stacks) {
        Tc4WandAdapter.addCreativeWands(stacks);
    }
}
