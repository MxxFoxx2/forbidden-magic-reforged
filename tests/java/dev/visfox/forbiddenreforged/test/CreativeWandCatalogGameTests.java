package dev.visfox.forbiddenreforged.test;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.wand.CreativeWandCatalog;
import dev.visfox.forbiddenreforged.FmCreativeTab;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.minecraft.world.item.ItemStack;
import thaumcraft.wand.Wands;

@GameTestHolder
@PrefixGameTestTemplate(false)
public final class CreativeWandCatalogGameTests {
    private CreativeWandCatalogGameTests() {
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void creativeWandCatalogPreservesHostIndependentDefinitions(GameTestHelper helper) {
        List<CreativeWandCatalog.Definition> expected = List.of(
            new CreativeWandCatalog.Definition(
                ContentId.parse("forbiddenmagic:wand_cap/thaumium"),
                ContentId.parse("forbiddenmagic:wand_rod/tainted"),
                0
            ),
            new CreativeWandCatalog.Definition(
                ContentId.parse("forbiddenmagic:wand_cap/thaumium"),
                ContentId.parse("forbiddenmagic:wand_rod/infernal"),
                0
            ),
            new CreativeWandCatalog.Definition(
                ContentId.parse("forbiddenmagic:wand_cap/void"),
                ContentId.parse("forbiddenmagic:wand_rod/equivalent"),
                0
            ),
            new CreativeWandCatalog.Definition(
                ContentId.parse("forbiddenmagic:wand_cap/orichalcum"),
                ContentId.parse("forbiddenmagic:wand_rod/neutronium"),
                0
            ),
            new CreativeWandCatalog.Definition(
                ContentId.parse("forbiddenmagic:wand_cap/orichalcum"),
                ContentId.parse("forbiddenmagic:wand_rod/neutronium_staff"),
                0
            ),
            new CreativeWandCatalog.Definition(
                ContentId.parse("forbiddenmagic:wand_cap/iron"),
                ContentId.parse("forbiddenmagic:wand_rod/profane"),
                5000
            )
        );

        helper.assertValueEqual(CreativeWandCatalog.definitions(), expected,
            "creative wand catalog retains all six entries and profane wand Vis fill");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void creativeWandCatalogBuildsNativeWandsWithExpectedVis(GameTestHelper helper) {
        List<ItemStack> wands = FmCreativeTab.stacks().stream()
            .filter(Wands::isWand)
            .toList();
        helper.assertValueEqual(wands.size(), 6,
            "all six semantic creative wand definitions resolve to native TC4 wands");
        helper.assertTrue(wands.stream().anyMatch(wand -> Wands.getRod(wand).tag().equals("tainted")),
            "the Tainted Wand is included in the creative tab");
        helper.assertTrue(wands.stream().anyMatch(wand -> Wands.getRod(wand).tag().equals("infernal")),
            "the Infernal Wand is included in the creative tab");
        helper.assertTrue(wands.stream().anyMatch(wand -> Wands.getRod(wand).tag().equals("equivalent")),
            "the Equivalent Wand is included in the creative tab");
        helper.assertTrue(wands.stream().anyMatch(wand -> Wands.getRod(wand).tag().equals("profane")),
            "the Profane Wand is included in the creative tab");

        ItemStack profane = wands.stream()
            .filter(wand -> Wands.getRod(wand).tag().equals("profane"))
            .findFirst()
            .orElse(ItemStack.EMPTY);
        helper.assertTrue(!profane.isEmpty(), "the Profane Wand can be resolved for its Vis check");

        var vis = HostAdapterRegistry.requireWandVis();
        for (ContentId aspect : vis.primalAspects()) {
            helper.assertValueEqual(vis.vis(profane, aspect), vis.maximumVis(profane),
                "the Profane Wand starts fully charged for " + aspect);
        }
        helper.succeed();
    }
}
