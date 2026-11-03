package dev.visfox.forbiddenreforged.test;

import dev.visfox.forbiddenreforged.core.aspect.AspectDefinition;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.research.ForbiddenMagicResearchCatalog;
import dev.visfox.forbiddenreforged.core.research.ResearchDefinition;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import dev.visfox.forbiddenreforged.integration.api.HostProfile;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4ResearchAdapter;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.aspect.Aspect;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchManager;
import thaumcraft.test.TestSupport;
import thaumcraft.taint.TaintBiomes;
import thaumcraft.taint.TaintSpread;

@GameTestHolder
@PrefixGameTestTemplate(false)
public final class ArchitectureCapabilityGameTests {
    private ArchitectureCapabilityGameTests() {
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void hostProfileDeclaresAllThreeCapabilitiesAdapted(GameTestHelper helper) {
        HostProfile profile = HostAdapterRegistry.require().profile();
        helper.assertValueEqual(
            profile.supportOf(HostProfile.Feature.RESEARCH_STAGES).level(),
            HostProfile.SupportLevel.ADAPTED,
            "TC4 research stages are lowered into a sequential chain of native entries"
        );
        helper.assertValueEqual(
            profile.supportOf(HostProfile.Feature.OPTIONAL_ENCHANTMENT_INTEGRATION).level(),
            HostProfile.SupportLevel.ADAPTED,
            "the pinned optional Thaumic Tinkerer bridge is ABI-validated"
        );
        helper.assertValueEqual(
            profile.supportOf(HostProfile.Feature.TAINT_ORIGINS).level(),
            HostProfile.SupportLevel.ADAPTED,
            "registered origin blocks can participate in TC4 taint spread"
        );
        helper.assertTrue(HostAdapterRegistry.requireTaintOrigins() != null,
            "taint origin services are available through the neutral capability registry");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void multiStageResearchLowersToSequentialTc4Entries(GameTestHelper helper) {
        ContentId id = ContentId.parse("forbiddenmagic:research/stage_adapter_test");
        ContentId category = ForbiddenMagicResearchCatalog.category().id();
        ResearchDefinition definition = new ResearchDefinition(
            id,
            category,
            ResearchDefinition.Icon.item(ContentId.parse("minecraft:diamond")),
            -100,
            -100,
            2,
            AspectDefinition.Cost.of(new AspectDefinition.Amount(ContentId.parse("thaumcraft:fire"), 3)),
            List.of(ContentId.parse("forbiddenmagic:research/nethershards")),
            List.of(ContentId.parse("forbiddenmagic:research/thaumium")),
            List.of(ContentId.parse("forbiddenmagic:research/fork")),
            Set.of(ResearchDefinition.Flag.CONCEALED, ResearchDefinition.Flag.HIDDEN),
            List.of(ContentId.parse("minecraft:diamond")),
            List.of(ContentId.parse("minecraft:zombie")),
            List.of(),
            List.of(
                new ResearchDefinition.Stage(
                    List.of(ResearchDefinition.Page.text("forbiddenmagic.research_page.STAGE_TEST.1")),
                    List.of(ContentId.parse("forbiddenmagic:research/hellfire")),
                    1
                ),
                new ResearchDefinition.Stage(
                    List.of(ResearchDefinition.Page.text("forbiddenmagic.research_page.STAGE_TEST.2")),
                    List.of(ContentId.parse("forbiddenmagic:research/skullaxe")),
                    2
                )
            )
        );

        List<ResearchItem> stages = Tc4ResearchAdapter.translateStages(definition);
        helper.assertValueEqual(stages.size(), 2, "one native TC4 entry is created per semantic stage");
        ResearchItem first = stages.get(0);
        ResearchItem last = stages.get(1);

        helper.assertValueEqual(first.key(), "STAGE_ADAPTER_TEST_FM_STAGE_1",
            "the intermediate key is stable and adapter-owned");
        helper.assertValueEqual(last.key(), "STAGE_ADAPTER_TEST",
            "the final entry preserves the canonical external research key");
        helper.assertValueEqual(first.parents(), List.of("NETHERSHARDS", "HELLFIRE"),
            "the first stage combines definition and stage prerequisites");
        helper.assertValueEqual(first.parentsHidden(), List.of("THAUMIUM"),
            "definition-wide hidden prerequisites remain on the first stage");
        helper.assertValueEqual(last.parents(), List.of("STAGE_ADAPTER_TEST_FM_STAGE_1", "SKULLAXE"),
            "later stages require the prior stage and their own prerequisites");
        helper.assertValueEqual(first.pages().size(), 1, "first stage receives only its page group");
        helper.assertValueEqual(last.pages().size(), 1, "last stage receives only its page group");
        helper.assertValueEqual(first.warp(), 1, "first stage keeps its own warp value");
        helper.assertValueEqual(last.warp(), 2, "last stage keeps its own warp value");
        helper.assertValueEqual(first.tags().getAmount(Aspect.FIRE), 3,
            "definition note cost is charged on the first stage");
        helper.assertValueEqual(last.tags().getAmount(Aspect.FIRE), 0,
            "later stages do not charge the definition note cost again");
        helper.assertTrue(first.isHidden(), "hidden discovery state stays on the first stage");
        helper.assertTrue(first.isConcealed() && last.isConcealed(),
            "presentation flags are preserved through the stage chain");
        helper.assertTrue(first.itemTriggers().contains(net.minecraft.resources.ResourceLocation.parse("minecraft:diamond")),
            "item discovery triggers stay on the first stage");
        helper.assertTrue(first.entityTriggers().contains(net.minecraft.resources.ResourceLocation.parse("minecraft:zombie")),
            "entity discovery triggers stay on the first stage");
        helper.assertTrue(last.siblings().contains("FORK"),
            "sibling completion is applied only to the canonical final stage");
        helper.assertTrue(first.column() < last.column(),
            "intermediate stages are laid out before the canonical final entry");
        helper.assertValueEqual(first.nameKey(), last.nameKey(),
            "all native stage entries reuse the semantic research name key");
        helper.assertValueEqual(first.textKey(), last.textKey(),
            "all native stage entries reuse the semantic research text key");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void laterResearchStagesAdvanceOnlyAfterTheirPrerequisites(GameTestHelper helper) {
        ResearchDefinition definition = new ResearchDefinition(
            ContentId.parse("forbiddenmagic:research/stage_progression_test"),
            ForbiddenMagicResearchCatalog.category().id(),
            ResearchDefinition.Icon.item(ContentId.parse("minecraft:diamond")),
            120,
            120,
            2,
            AspectDefinition.Cost.of(new AspectDefinition.Amount(ContentId.parse("thaumcraft:fire"), 3)),
            List.of(ContentId.parse("forbiddenmagic:research/nethershards")),
            List.of(),
            List.of(),
            Set.of(ResearchDefinition.Flag.CONCEALED),
            List.of(),
            List.of(),
            List.of(),
            List.of(
                new ResearchDefinition.Stage(
                    List.of(ResearchDefinition.Page.text("forbiddenmagic.research_page.STAGE_PROGRESSION_TEST.1")),
                    List.of(),
                    0
                ),
                new ResearchDefinition.Stage(
                    List.of(ResearchDefinition.Page.text("forbiddenmagic.research_page.STAGE_PROGRESSION_TEST.2")),
                    List.of(ContentId.parse("forbiddenmagic:research/skullaxe")),
                    1
                )
            )
        );
        Tc4ResearchAdapter.register(ForbiddenMagicResearchCatalog.category(), List.of(definition));

        ResearchItem first = ResearchCategories.getResearch("STAGE_PROGRESSION_TEST_FM_STAGE_1");
        ResearchItem last = ResearchCategories.getResearch("STAGE_PROGRESSION_TEST");
        helper.assertTrue(first != null && last != null, "both stage entries are registered in TC4");
        helper.assertValueEqual(last.tags().size(), 0,
            "continuation entries carry no duplicate note cost");
        var player = TestSupport.mockServerPlayer(helper);

        ResearchManager.complete(player, "NETHERSHARDS", false);
        ResearchManager.complete(player, first.key());
        helper.assertFalse(ResearchManager.isComplete(player, last.key()),
            "the final stage remains locked while its stage-specific prerequisite is missing");

        ResearchManager.complete(player, "SKULLAXE", false);
        helper.assertTrue(ResearchManager.isComplete(player, last.key()),
            "the final canonical entry advances automatically after all stage prerequisites are complete");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void registeredTaintOriginCanSpreadAdjacentFibres(GameTestHelper helper) {
        var origins = HostAdapterRegistry.requireTaintOrigins();
        BlockPos origin = helper.absolutePos(new BlockPos(2, 3, 2));
        BlockPos target = origin.east();
        origins.registerOrigin(Blocks.EMERALD_BLOCK);
        try {
            helper.getLevel().setBlock(origin, Blocks.EMERALD_BLOCK.defaultBlockState(), 3);
            helper.getLevel().setBlock(target.below(), Blocks.STONE.defaultBlockState(), 3);

            helper.assertTrue(origins.isOrigin(helper.getLevel().getBlockState(origin)),
                "registered block state is recognised as a taint origin");
            helper.assertTrue(TaintSpread.isTaintBlock(helper.getLevel().getBlockState(origin)),
                "origin blocks participate in host taint adjacency checks");
            helper.assertTrue(origins.spreadFromOrigin(helper.getLevel(), origin, target),
                "origin invokes the host fibre-spread implementation for an adjacent target");
            helper.assertTrue(TaintSpread.isTaintBlock(helper.getLevel().getBlockState(target)),
                "the target becomes a native taint block");
            helper.assertTrue(TaintBiomes.isTainted(helper.getLevel(), target),
                "successful origin spread marks the target column as tainted land");
        } finally {
            origins.unregisterOrigin(Blocks.EMERALD_BLOCK);
        }
        helper.assertFalse(origins.isOrigin(helper.getLevel().getBlockState(origin)),
            "unregistered block types stop acting as origins");
        helper.succeed();
    }
}
