package dev.visfox.forbiddenreforged.test;

import dev.visfox.forbiddenreforged.integration.tc4.Tc4CrystalwellItem;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4PrimewellItem;
import net.minecraft.world.item.Rarity;
import thaumcraft.item.ScribingToolsItem;
import thaumcraft.crafting.TcStack;
import thaumcraft.crafting.ShapelessArcaneRecipe;
import thaumcraft.registry.ModBlocks;
import thaumcraft.registry.ModItems;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4AspectAdapter;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4ScribingAdapter;
import dev.visfox.forbiddenreforged.FmAspects;
import dev.visfox.forbiddenreforged.FmItems;
import dev.visfox.forbiddenreforged.FmConfig;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4ResearchItem;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.Config;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;

final class ScribingResearchGameTests {
    private ScribingResearchGameTests() {
    }

    static void diabolistForkResearchAndRecipe(GameTestHelper helper) {
        ResearchItem fork = ResearchCategories.getResearch("FORK");
        helper.assertTrue(fork != null, "FORK research must be registered");
        helper.assertTrue(fork.isSecondary(), "FORK is secondary");
        helper.assertTrue(fork.isConcealed(), "FORK is concealed");
        helper.assertValueEqual(fork.warp(), 0, "FORK has no warp");
        helper.assertValueEqual(fork.tags().getAmount(Tc4AspectAdapter.resolveAspect(FmAspects.NETHER)), 3, "FORK research NETHER cost");
        helper.assertValueEqual(fork.tags().getAmount(Aspect.MECHANISM), 1, "FORK research MECHANISM cost");
        helper.assertValueEqual(fork.tags().getAmount(Aspect.TOOL), 1, "FORK research TOOL cost");
        helper.assertValueEqual(fork.tags().getAmount(Aspect.FIRE), 0, "FORK research has no FIRE cost");
        helper.assertValueEqual(fork.parents(), List.of("INFUSION", "NETHERSHARDS", "THAUMIUM"), "FORK parents");
        InfusionRecipe recipe = (InfusionRecipe) TcRecipes.get("FORK");
        helper.assertTrue(recipe != null, "FORK infusion recipe exists");
        helper.assertValueEqual(recipe.instability(), 1, "FORK instability");
        helper.assertValueEqual(recipe.aspects().getAmount(Tc4AspectAdapter.resolveAspect(FmAspects.NETHER)), 8, "FORK NETHER cost");
        helper.assertValueEqual(recipe.aspects().getAmount(Aspect.FIRE), 0, "FORK has no FIRE cost");
        helper.assertValueEqual(recipe.aspects().getAmount(Aspect.MECHANISM), 8, "FORK mechanism cost");
        helper.assertValueEqual(recipe.aspects().getAmount(Aspect.ENERGY), 8, "FORK energy cost");
        helper.assertValueEqual(recipe.components().size(), 4, "FORK has 4 components");
        ItemStack forkStack = new ItemStack(FmItems.DIABOLIST_FORK.get());
        helper.assertValueEqual(forkStack.getItem().getDestroySpeed(forkStack, Blocks.COBWEB.defaultBlockState()), 1.0F, "the fork always digs at speed 1");
        helper.succeed();
    }

    static void crystalwellIsAScribingToolWithOriginalDurability(GameTestHelper helper) {
        ItemStack stack = new ItemStack(FmItems.CRYSTALWELL.get());
        helper.assertTrue(stack.getItem() instanceof Tc4CrystalwellItem, "crystalwell uses its behavior item");
        helper.assertTrue(stack.getItem() instanceof ScribingToolsItem, "crystalwell can occupy the research table scribe slot");
        int expectedMaxDamage = thaumcraft.Config.researchDifficulty() == -1 ? 50 : 100;
        helper.assertValueEqual(stack.getMaxDamage(), expectedMaxDamage, "crystalwell durability follows research difficulty");
        helper.assertValueEqual(stack.getCount(), 1, "crystalwell is unstackable");
        helper.succeed();
    }

    static void primewellIsAPermanentEpicScribingTool(GameTestHelper helper) {
        ItemStack stack = new ItemStack(FmItems.PRIMEWELL.get());
        helper.assertTrue(stack.getItem() instanceof Tc4PrimewellItem, "primewell uses its behavior item");
        helper.assertTrue(stack.getItem() instanceof ScribingToolsItem, "primewell can occupy the research table scribe slot");
        helper.assertValueEqual(stack.getRarity(), Rarity.EPIC, "primewell retains epic rarity");
        helper.assertTrue(stack.getMaxDamage() > 1_000_000_000, "primewell does not wear out during research");
        stack.setDamageValue(1000);
        helper.assertFalse(stack.getItem().isBarVisible(stack), "primewell does not display a misleading durability bar");
        helper.assertTrue(stack.getDamageValue() < stack.getMaxDamage(), "primewell remains usable after repeated scribe operations");
        helper.succeed();
    }

    static void scribeWellsKeepOriginalRecipesAndResearch(GameTestHelper helper) {
        ShapelessArcaneRecipe crystalwell = (ShapelessArcaneRecipe) TcRecipes.get("CRYSTALWELL");
        helper.assertTrue(crystalwell != null, "crystalwell arcane recipe is registered");
        helper.assertTrue(TcRecipes.get("PRIMEWELL") != null, "primewell arcane recipe is registered");
        helper.assertTrue(ResearchCategories.getResearch("CRYSTALWELL") != null, "crystalwell research is registered");
        helper.assertTrue(ResearchCategories.getResearch("PRIMEWELL") != null, "primewell research is registered");
        helper.assertTrue(crystalwell.ingredients().get(0).test(new ItemStack(ModItems.SCRIBING_TOOLS.get())),
            "crystalwell recipe uses the registered scribing tools item");
        if (thaumcraft.Config.researchDifficulty() == -1) {
            helper.assertValueEqual(crystalwell.ingredients().size(), 4, "easy research uses two arbitrary Thaumcraft shards");
            helper.assertTrue(crystalwell.ingredients().get(2).test(new ItemStack(ModItems.BALANCED_SHARD.get())),
                "the easy recipe accepts any shard through the shard tag");
        } else {
            helper.assertValueEqual(crystalwell.ingredients().size(), 6, "normal research uses a mixed crystal and three balanced shards");
            helper.assertTrue(crystalwell.ingredients().get(2).test(new ItemStack(ModBlocks.MIXED_CRYSTAL_CLUSTER.get().asItem())),
                "the hard recipe uses the mixed crystal cluster");
            helper.assertTrue(crystalwell.ingredients().get(3).test(new ItemStack(ModItems.BALANCED_SHARD.get())),
                "the hard recipe uses balanced shards");
        }
        helper.succeed();
    }

    static void itemTranslationKeysAreNotNestedUnderResearchPages(GameTestHelper helper) {
            JsonObject english = GameTestResources.readJson(helper, "/assets/forbiddenmagic/lang/en_us.json");
            JsonObject russian = GameTestResources.readJson(helper, "/assets/forbiddenmagic/lang/ru_ru.json");
            List<String> expected = List.of(
                "item.forbiddenmagic.taint_coal",
                "item.forbiddenmagic.riding_crop",
                "item.forbiddenmagic.black_dye",
                "item.forbiddenmagic.taint_fruit",
                "item.forbiddenmagic.ring_nutrition",
                "item.forbiddenmagic.tainted_rod",
                "item.forbiddenmagic.infernal_rod",
                "item.forbiddenmagic.witchwood_rod",
                "item.forbiddenmagic.profane_rod",
                "item.forbiddenmagic.witchwood_staff_rod",
                "item.Wand.tainted.rod",
                "item.Wand.infernal.rod",
                "item.Wand.profane.rod",
                "item.Wand.profaned.rod",
                "item.Wand.orichalcum.cap",
                "item.Wand.neutronium.rod",
                "item.forbiddenmagic.taint_shovel",
                "item.forbiddenmagic.taint_pickaxe",
                "item.forbiddenmagic.crystalwell",
                "item.forbiddenmagic.primewell",
                "item.forbiddenmagic.blink_focus",
                "entity.forbiddenmagic.villager.heretic",
                "tooltip.creativeonly",
                "tooltip.profane_contract"
            );
            List<String> pages = List.of(
                "RIDINGCROP", "RINGFOOD", "ROD_tainted", "ROD_infernal", "ROD_witchwood",
                "ROD_profane", "ROD_witchwood_staff", "TRANSEMERALD", "CRYSTALWELL",
                "PRIMEWELL", "TAINTSHOVEL", "TAINTPICK", "FOCUSBLINK"
            );
            for (JsonObject translations : List.of(english, russian)) {
                for (String key : expected) {
                    helper.assertTrue(translations.has(key), "missing translation key " + key);
                }
                for (String page : pages) {
                    helper.assertTrue(translations.has("forbiddenmagic.research_page." + page + ".1"),
                        "missing research page key " + page);
                }
                for (String key : translations.keySet()) {
                    helper.assertFalse(key.contains(".research_page.ARCANECAKE.item.")
                            || key.contains(".research_page.ARCANECAKE.entity.")
                            || key.contains(".research_page.ARCANECAKE.forbiddenmagic.")
                            || key.startsWith("itemGroup.tooltip."),
                        "translation key has an invalid nested prefix " + key);
                }
            }
            helper.succeed();
        }

    static void researchTranslationKeysAreComplete(GameTestHelper helper) {
        JsonObject english = GameTestResources.readJson(helper, "/assets/forbiddenmagic/lang/en_us.json");
        JsonObject russian = GameTestResources.readJson(helper, "/assets/forbiddenmagic/lang/ru_ru.json");
        List<String> researchIds = ResearchCategories.allResearch().stream()
            .filter(item -> item.category().equals("FORBIDDEN"))
            .map(ResearchItem::key)
            .sorted()
            .toList();
        helper.assertTrue(!researchIds.isEmpty(), "Forbidden research is registered");
        List<String> required = List.of(
            "tc.research_category.FORBIDDEN",
            "focus.upgrade.hellfire.name",
            "focus.upgrade.hellfire.text",
            "focus.upgrade.pandemonium.name",
            "focus.upgrade.pandemonium.text",
            "forbiddenmagic.research_page.FORK.wc",
            "forbiddenmagic.research_page.ROD_infernal.2",
            "forbiddenmagic.research_page.TAINTSHOVEL.2",
            "forbiddenmagic.research_page.CRYSTALWELL.1",
            "forbiddenmagic.research_page.CRYSTALWELL.1b"
        );
        for (JsonObject translations : List.of(english, russian)) {
            for (String key : required) {
                helper.assertTrue(translations.has(key), "missing translation key " + key);
            }
            for (String researchId : researchIds) {
                helper.assertTrue(translations.has("tc.research_name." + researchId),
                    "missing research name " + researchId);
                helper.assertTrue(translations.has("tc.research_text." + researchId),
                    "missing research text " + researchId);
                helper.assertTrue(translations.has("forbiddenmagic.research_text." + researchId),
                    "missing tagged research text " + researchId);
            }
        }
        ResearchItem infernal = ResearchCategories.getResearch("ROD_infernal");
        helper.assertTrue(infernal != null, "infernal wand research is registered");
        List<String> infernalTextPages = infernal.pages().stream()
            .filter(ResearchPage.Text.class::isInstance)
            .map(ResearchPage.Text.class::cast)
            .map(ResearchPage.Text::text)
            .toList();
        helper.assertTrue(infernalTextPages.contains("forbiddenmagic.research_page.ROD_infernal.2"),
            "infernal wand includes the original protection and extinguishing page");

        helper.assertValueEqual(
            english.get("forbiddenmagic.research_page.ROD_tainted.1").getAsString(),
            "Tainted Lands are places of great and terrible power for those Thaumaturges who know how to tap into them.<BR><BR>The Tainted Wand Core can store up to 150 vis of each type and slowly replenishes itself up to 10% in every aspect as long as it is being held inside of a Tainted Land.  While the wand is held in your hand, it can also feed off of taint coursing through your veins, randomly restoring vis every time you take damage from flux taint.",
            "tainted wand page matches the original");
        helper.assertValueEqual(
            russian.get("forbiddenmagic.research_page.ROD_tainted.1").getAsString(),
            "Заражённые земли - это место великой и ужасной энергии для тех тауматургов, которые знают как её использовать.<BR><BR>Заражённый стержень может хранить до 150 Вис каждого типа и медленно пополнять каждый аспект до 10%, пока жезл находятся на заражённой земле.  Когда Вы держите жезл в руке, он также может питаться от заражения, бегущего по Вашим венам, случайно восстанавливая Вис, во время получении урона от порчи.",
            "tainted wand page matches the original Russian");
        helper.assertTrue(english.keySet().equals(russian.keySet()),
            "English and Russian translation key sets must match");
        helper.succeed();
    }

    static void scribeWellAspectIdsStaySemantic(GameTestHelper helper) {
        Set<ContentId> expected = Set.of(
            ContentId.parse("thaumcraft:aer"),
            ContentId.parse("thaumcraft:terra"),
            ContentId.parse("thaumcraft:ignis"),
            ContentId.parse("thaumcraft:aqua"),
            ContentId.parse("thaumcraft:ordo"),
            ContentId.parse("thaumcraft:perditio")
        );
        List<ContentId> primalIds = Tc4ScribingAdapter.primalAspectIds();
        helper.assertValueEqual(primalIds.size(), expected.size(), "host primal aspect count is six");
        helper.assertTrue(new HashSet<>(primalIds).equals(expected),
            "scribe well exposes the exact primal aspect ids without leaking Aspect objects");
        for (ContentId id : primalIds) {
            helper.assertTrue(Tc4AspectAdapter.resolveAspect(id).isPrimal(),
                "every semantic scribe aspect id resolves to a host primal");
        }
        helper.assertValueEqual(Tc4ScribingAdapter.isNoResearchDifficulty(), Config.researchDifficulty() == -1,
            "no-research mode remains sourced from the host config");
        helper.succeed();
    }

    static void crystalwellResearchPageMatchesDifficulty(GameTestHelper helper) {
        ResearchItem item = ResearchCategories.getResearch("CRYSTALWELL");
        helper.assertTrue(item != null, "crystalwell research is registered");
        List<String> textPages = item.pages().stream()
            .filter(ResearchPage.Text.class::isInstance)
            .map(ResearchPage.Text.class::cast)
            .map(ResearchPage.Text::text)
            .toList();
        String expected = Config.researchDifficulty() == -1
            ? "forbiddenmagic.research_page.CRYSTALWELL.1b"
            : "forbiddenmagic.research_page.CRYSTALWELL.1";
        helper.assertValueEqual(textPages.size(), 1, "crystalwell exposes one behavior page");
        helper.assertValueEqual(textPages.get(0), expected, "crystalwell page follows research difficulty");
        helper.succeed();
    }

    static void researchPrefixRespectsTagResearch(GameTestHelper helper) {
        ResearchItem item = new Tc4ResearchItem("TEST", "FORBIDDEN", new AspectList(), 0, 0, 1,
            ForbiddenMagic.id("textures/misc/forbidden.png"));
        String expected = FmConfig.tagResearchEnabled()
            ? "forbiddenmagic.research_text.TEST"
            : "tc.research_text.TEST";
        helper.assertValueEqual(item.textKey(), expected, "research text key follows tagResearch config");
        helper.succeed();
    }

    static void researchIconsResolveToRealContent(GameTestHelper helper) {
        List<String> broken = new ArrayList<>();
        for (ResearchItem item : ResearchCategories.allResearch()) {
            if (!"FORBIDDEN".equals(item.category())) {
                continue;
            }
            if (item.iconTexture() != null) {
                continue;
            }
            TcStack icon = item.iconItem();
            if (icon == null || !icon.exists()) {
                broken.add(item.key() + " -> " + (icon == null ? "no icon" : icon.item().toString()));
            }
        }
        helper.assertTrue(broken.isEmpty(), "research icons must resolve to a real item or texture: " + broken);
        helper.succeed();
    }
}
