package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.common.FmIds;
import dev.visfox.forbiddenreforged.common.FmWandContractAccess;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import dev.visfox.forbiddenreforged.core.wand.WandBehaviors;
import dev.visfox.forbiddenreforged.core.wand.WandPartsAccess;
import dev.visfox.forbiddenreforged.core.wand.WandVisAccess;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.aspect.Aspect;
import thaumcraft.taint.TaintBiomes;
import thaumcraft.wand.RodUpdate;
import thaumcraft.wand.WandCap;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.WandRod;
import thaumcraft.wand.Wands;

public final class Tc4WandAdapter {
    private static final WandVisAccess VIS = new Tc4WandVisAdapter();
    private static final WandPartsAccess PARTS = new Tc4WandPartsAdapter();

    private Tc4WandAdapter() {
    }


    public static void registerWands() {
        WandRegistry.registerRod("tainted", 150, FmIds.id("tainted_rod"), 12,
            new TaintedWandUpdate(), false, false, false,
            FmIds.id("textures/model/wand_rod_tainted.png"));
        WandRegistry.registerRod("infernal", 150, FmIds.id("infernal_rod"), 12,
            new InfernalWandUpdate(), false, false, false,
            FmIds.id("textures/model/wand_rod_infernal.png"));
        WandRegistry.registerRod("profane", 50, FmIds.id("profane_rod"), 12,
            new ProfaneWandUpdate(), false, false, false,
            FmIds.id("textures/model/wand_rod_profane.png"));
        WandRegistry.registerRod("profaned", 50, Identifier.withDefaultNamespace("bedrock"), 1000,
            null, false, false, false,
            FmIds.id("textures/model/wand_rod_profaned.png"));
        WandRegistry.registerCap("orichalcum", 0.0F, List.of(), 0.0F,
            Identifier.withDefaultNamespace("command_block"), 1000, null,
            FmIds.id("textures/model/wand_cap_orichalcum.png"));
        WandRegistry.registerCap("alchemical", 0.9F, List.of(Aspect.WATER), 0.8F,
            FmIds.id("alchemical_cap"), 7, null,
            FmIds.id("textures/model/wand_cap_alchemical.png"));
        WandRegistry.registerCap("vinteum", 0.9F, List.of(), 0.0F,
            FmIds.id("vinteum_cap"), 6, null,
            FmIds.id("textures/model/wand_cap_vinteum.png"));
        WandRegistry.registerCap("manasteel", 0.9F, List.of(), 0.0F,
            FmIds.id("manasteel_cap"), 6, null,
            FmIds.id("textures/model/wand_cap_manasteel.png"));
        WandRegistry.registerCap("elementium", 0.8F, List.of(), 0.0F,
            FmIds.id("elementium_cap"), 9, null,
            FmIds.id("textures/model/wand_cap_elementium.png"));
        WandRegistry.registerCap("terrasteel", 1.8F, List.of(), 0.0F,
            FmIds.id("terrasteel_cap"), 1, null,
            FmIds.id("textures/model/wand_cap_terrasteel.png"));
        WandRegistry.registerRod("equivalent", 100, FmIds.id("equivalent_rod"), 12,
            null, false, false, false,
            FmIds.id("textures/model/wand_rod_equivalent.png"));
        WandRegistry.registerRod("neutronium", 9001, Identifier.withDefaultNamespace("bedrock"), 1000,
            new CreativeWandUpdate(), true, false, false,
            FmIds.id("textures/model/wand_rod_neutronium.png"));
        WandRegistry.registerRod("neutronium", 9002, Identifier.withDefaultNamespace("bedrock"), 1000,
            new CreativeWandUpdate(), true, true, false,
            FmIds.id("textures/model/wand_rod_neutronium.png"));
    }

    public static void addCreativeWands(List<ItemStack> stacks) {
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
            for (var aspect : VIS.primalAspects()) {
                VIS.addVis(filled, aspect, 5000, true);
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

    private static final class CreativeWandUpdate implements RodUpdate {
        @Override
        public void onUpdate(ItemStack stack, Player player) {
            WandBehaviors.creative(stack, player, VIS);
        }
    }

    private static final class TaintedWandUpdate implements RodUpdate {
        @Override
        public void onUpdate(ItemStack stack, Player player) {
            boolean tainted = TaintBiomes.isTainted(player.level(), player.blockPosition());
            WandBehaviors.tainted(stack, player, VIS, tainted);
        }
    }

    private static final class InfernalWandUpdate implements RodUpdate {
        @Override
        public void onUpdate(ItemStack stack, Player player) {
            WandBehaviors.infernal(stack, player, VIS);
        }
    }

    private static final class ProfaneWandUpdate implements RodUpdate {
        @Override
        public void onUpdate(ItemStack stack, Player player) {
            WandBehaviors.profane(stack, player, VIS, PARTS, FmWandContractAccess.INSTANCE, HostAdapterRegistry.warp());
        }
    }
}
