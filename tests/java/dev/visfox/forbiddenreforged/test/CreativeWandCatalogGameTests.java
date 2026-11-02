package dev.visfox.forbiddenreforged.test;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.wand.CreativeWandCatalog;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

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
}
