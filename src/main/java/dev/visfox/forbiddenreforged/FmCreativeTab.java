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
import thaumcraft.aspect.Aspect;
import thaumcraft.wand.WandCap;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.WandRod;
import thaumcraft.wand.Wands;

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
        for (String mobId : FmSpawnerMobs.ids()) {
            ItemStack crystal = new ItemStack(FmItems.MOB_CRYSTAL.get());
            MobCrystalItem.setMob(crystal, mobId);
            stacks.add(crystal);
        }
        addCreativeWands(stacks);
        return stacks;
    }

    private static void addCreativeWands(List<ItemStack> stacks) {
        addWand(stacks, WandRegistry.CAP_THAUMIUM, "tainted");
        addWand(stacks, WandRegistry.CAP_THAUMIUM, "infernal");
        addWand(stacks, WandRegistry.CAP_VOID, "equivalent");

        WandCap orichalcum = WandRegistry.findCap("orichalcum");
        WandRod neutronium = WandRegistry.findRod("neutronium");
        WandRod neutroniumStaff = WandRegistry.findRod("neutronium_staff");
        if (orichalcum != null && neutronium != null) {
            stacks.add(Wands.create(orichalcum, neutronium, false));
        }
        if (orichalcum != null && neutroniumStaff != null) {
            stacks.add(Wands.create(orichalcum, neutroniumStaff, false));
        }

        WandRod profane = WandRegistry.findRod("profane");
        if (profane != null) {
            ItemStack filled = Wands.create(WandRegistry.CAP_IRON, profane, false);
            for (Aspect aspect : Aspect.getPrimalAspects()) {
                Wands.addVis(filled, aspect, 5000, true);
            }
            stacks.add(filled);
        }
    }

    private static void addWand(List<ItemStack> stacks, WandCap cap, String rodTag) {
        WandRod rod = WandRegistry.findRod(rodTag);
        if (rod != null) {
            stacks.add(Wands.create(cap, rod, false));
        }
    }
}
