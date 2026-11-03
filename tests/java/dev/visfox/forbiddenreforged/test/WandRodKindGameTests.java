package dev.visfox.forbiddenreforged.test;

import dev.visfox.forbiddenreforged.FmToolEvents;
import dev.visfox.forbiddenreforged.core.wand.WandRodKind;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.Wands;

@GameTestHolder
@PrefixGameTestTemplate(false)
public final class WandRodKindGameTests {
    private WandRodKindGameTests() {
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void hostInspectionReturnsSemanticWandRodKinds(GameTestHelper helper) {
        var inspection = HostAdapterRegistry.requireWandInspection();
        ItemStack tainted = Wands.create(WandRegistry.CAP_THAUMIUM, WandRegistry.findRod("tainted"), false);
        ItemStack infernal = Wands.create(WandRegistry.CAP_THAUMIUM, WandRegistry.findRod("infernal"), false);
        ItemStack profane = Wands.create(WandRegistry.CAP_IRON, WandRegistry.findRod("profane"), false);
        ItemStack neutronium = Wands.create(
            WandRegistry.CAP_THAUMIUM, WandRegistry.findRod("neutronium"), false
        );

        helper.assertValueEqual(inspection.wandRodKind(tainted), WandRodKind.TAINTED,
            "TC4 tainted registry tag maps to its semantic rod kind");
        helper.assertValueEqual(inspection.wandRodKind(infernal), WandRodKind.INFERNAL,
            "TC4 infernal registry tag maps to its semantic rod kind");
        helper.assertValueEqual(inspection.wandRodKind(profane), WandRodKind.PROFANE,
            "TC4 profane registry tag maps to its semantic rod kind");
        helper.assertValueEqual(inspection.wandRodKind(neutronium), WandRodKind.NEUTRONIUM,
            "TC4 neutronium registry tag maps to its semantic rod kind");
        helper.assertValueEqual(inspection.wandRodKind(new ItemStack(Items.STICK)), WandRodKind.NONE,
            "non-wand stacks do not leak null or a host registry tag");

        helper.assertValueEqual(FmToolEvents.wandTooltipKind(WandRodKind.NEUTRONIUM), "creativeonly",
            "Neutronium tooltip remains creative-only");
        helper.assertValueEqual(FmToolEvents.wandTooltipKind(WandRodKind.PROFANE), "profane_contract",
            "Profane tooltip remains contract-specific");
        helper.assertValueEqual(FmToolEvents.wandTooltipKind(WandRodKind.TAINTED), "",
            "ordinary Tainted Wand receives no special tooltip");
        helper.assertTrue(FmToolEvents.cancelsInfernalDamage(infernal, true),
            "Infernal Wand still suppresses fire/wither damage");
        helper.assertTrue(!FmToolEvents.cancelsInfernalDamage(tainted, true),
            "Tainted Wand does not suppress fire/wither damage");
        helper.assertTrue(FmToolEvents.restoresTaintedVis(tainted, true),
            "Tainted Wand still enables its taint-damage Vis restoration");
        helper.assertTrue(!FmToolEvents.restoresTaintedVis(infernal, true),
            "Infernal Wand does not enable taint-damage Vis restoration");
        helper.succeed();
    }
}
