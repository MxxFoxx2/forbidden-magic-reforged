package dev.visfox.forbiddenreforged.test;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.wand.BlinkFocusCatalog;
import dev.visfox.forbiddenreforged.core.wand.BlinkFocusCatalog.Presentation;
import dev.visfox.forbiddenreforged.core.wand.BlinkFocusCatalog.Definition;
import dev.visfox.forbiddenreforged.core.wand.BlinkFocusRules.Upgrade;
import dev.visfox.forbiddenreforged.core.wand.BlinkFocusRules.VisCost;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder
@PrefixGameTestTemplate(false)
public final class BlinkFocusCatalogGameTests {
    private BlinkFocusCatalogGameTests() {
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void blinkFocusCatalogPreservesAddonUpgradeMetadata(GameTestHelper helper) {
        List<Definition> expected = List.of(
            new Definition(
                Upgrade.HELLFIRE,
                "hellfire",
                ContentId.parse("forbiddenmagic:textures/misc/hellfire.png"),
                List.of(new VisCost(ContentId.parse("forbiddenmagic:infernus"), 1))
            ),
            new Definition(
                Upgrade.PANDEMONIUM,
                "pandemonium",
                ContentId.parse("forbiddenmagic:textures/misc/pandemonium.png"),
                List.of(new VisCost(ContentId.parse("forbiddenmagic:concept/darkness"), 1))
            )
        );

        helper.assertValueEqual(BlinkFocusCatalog.definitions(), expected,
            "Blink Focus catalog preserves upgrade identity, texture and Vis aspect costs");
        helper.assertValueEqual(BlinkFocusCatalog.presentation(),
            new Presentation(
                "BL",
                0x909090,
                ContentId.parse("forbiddenmagic:item/blink_focus"),
                ContentId.parse("forbiddenmagic:item/blink_focus_ornament")
            ),
            "Blink Focus presentation metadata stays independent of the TC4 item subclass");
        helper.succeed();
    }
}
