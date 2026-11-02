package dev.visfox.forbiddenreforged.test;

import dev.visfox.forbiddenreforged.core.aspect.AspectDefinition;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.research.ResearchDefinition;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4ResearchAdapter;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.research.ResearchItem;

@GameTestHolder
@PrefixGameTestTemplate(false)
public final class ResearchGameTests {
    private ResearchGameTests() {
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void researchAdapterPreservesItemAndEntityTriggers(GameTestHelper helper) {
        ResearchDefinition definition = new ResearchDefinition(
            ContentId.parse("forbiddenmagic:research/adapter_trigger_test"),
            ContentId.parse("forbiddenmagic:category/forbidden"),
            ResearchDefinition.Icon.item(ContentId.parse("minecraft:diamond")),
            0,
            0,
            1,
            AspectDefinition.Cost.EMPTY,
            List.of(),
            List.of(),
            Set.of(),
            List.of(ContentId.parse("minecraft:diamond")),
            List.of(ContentId.parse("minecraft:zombie")),
            List.of(),
            List.of(new ResearchDefinition.Stage(
                List.of(ResearchDefinition.Page.text("forbiddenmagic.research_page.ADAPTER_TRIGGER_TEST.1")),
                List.of(),
                0
            ))
        );

        ResearchItem translated = Tc4ResearchAdapter.translate(definition);
        helper.assertValueEqual(
            translated.itemTriggers(),
            List.of(ResourceLocation.parse("minecraft:diamond")),
            "item unlock triggers are translated to TC4 resource locations"
        );
        helper.assertValueEqual(
            translated.entityTriggers(),
            List.of(ResourceLocation.parse("minecraft:zombie")),
            "entity unlock triggers are translated to TC4 resource locations"
        );
        helper.succeed();
    }
}
