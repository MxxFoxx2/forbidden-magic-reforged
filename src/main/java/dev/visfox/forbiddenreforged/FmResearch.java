package dev.visfox.forbiddenreforged;

import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcStack;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;

public final class FmResearch {
    private FmResearch() {
    }

    public static void register() {
        ResearchCategories.registerCategory(
            "FORBIDDEN",
            ForbiddenMagic.id("textures/misc/forbidden.png"),
            ForbiddenMagic.id("textures/misc/runecircle.png")
        );
        new ResearchItem("NETHERSHARDS", "FORBIDDEN", new AspectList(), -8, -5, 0, TcStack.of(FmItems.id("wrath").toString(), 1))
            .setPages(
                ResearchPage.text("forbiddenmagic.research_page.NETHERSHARDS.1"),
                ResearchPage.text(FmConfig.lustDisabled() ? "forbiddenmagic.research_page.NETHERSHARDS.2b" : "forbiddenmagic.research_page.NETHERSHARDS.2"),
                ResearchPage.text("forbiddenmagic.research_page.NETHERSHARDS.3")
            )
            .setStub()
            .setRound()
            .setAutoUnlock()
            .registerResearchItem();
        new ResearchItem(
            "HELLFIRE",
            "FORBIDDEN",
            new AspectList().add(Aspect.FIRE, 3).add(FmAspects.NETHER, 4).add(Aspect.TRAVEL, 2),
            -8,
            -8,
            1,
            TcStack.of("minecraft:flint_and_steel", 1)
        )
            .setPages(ResearchPage.text("forbiddenmagic.research_page.HELLFIRE.1"))
            .setRound()
            .setHidden()
            .setParents("NETHERSHARDS")
            .setAspectTriggers(FmAspects.NETHER)
            .registerResearchItem();
        new ResearchItem(
            "SKULLAXE",
            "FORBIDDEN",
            new AspectList().add(Aspect.WEAPON, 3).add(FmAspects.WRATH, 4).add(FmAspects.NETHER, 1),
            -11,
            -7,
            2,
            TcStack.of(FmItems.SKULL_AXE.getId().toString(), 1)
        )
            .setPages(
                ResearchPage.text("forbiddenmagic.research_page.SKULLAXE.1"),
                ResearchPage.infusion("SKULLAXE")
            )
            .setParents("NETHERSHARDS", "THAUMIUM", "INFUSION")
            .setConcealed()
            .setWarp(1)
            .registerResearchItem();
        new ResearchItem("MORPHTOOLS", "FORBIDDEN", new AspectList().add(Aspect.TOOL, 2).add(FmAspects.ENVY, 3).add(Aspect.EXCHANGE, 2), -9, -3, 3, TcStack.of("forbiddenmagic:morph_pickaxe", 1))
            .setPages(
                ResearchPage.text("forbiddenmagic.research_page.MORPHTOOLS.1"),
                ResearchPage.infusion("MORPHPICK"),
                ResearchPage.infusion("MORPHSWORD"),
                ResearchPage.infusion("MORPHSHOVEL"),
                ResearchPage.infusion("MORPHAXE")
            )
            .setParents("NETHERSHARDS")
            .setParentsHidden("THAUMIUM", "INFUSIONENCHANTMENT")
            .setConcealed()
            .setWarp(4)
            .registerResearchItem();
        if (FmConfig.enchantmentsEnabled()) {
            new ResearchItem("CLUSTER", "FORBIDDEN", new AspectList().add(Aspect.METAL, 1).add(Aspect.FIRE, 4).add(FmAspects.ENVY, 3), -10, -1, 3, ForbiddenMagic.id("textures/misc/lucrative.png"))
                .setPages(ResearchPage.text("forbiddenmagic.research_page.CLUSTER.1"), ResearchPage.infusionEnchantment("CLUSTER"))
                .setParents("MORPHTOOLS", "ELEMENTALPICK")
                .setSecondary()
                .setConcealed()
                .registerResearchItem();
            new ResearchItem("IMPACT", "FORBIDDEN", new AspectList().add(Aspect.ENTROPY, 8).add(Aspect.TOOL, 10).add(Aspect.MINE, 16).add(FmAspects.ENVY, 10), -9, -1, 3, ForbiddenMagic.id("textures/misc/impact.png"))
                .setPages(ResearchPage.text("forbiddenmagic.research_page.IMPACT.1"), ResearchPage.infusionEnchantment("IMPACT"))
                .setParents("MORPHTOOLS", "ELEMENTALSHOVEL")
                .setConcealed()
                .registerResearchItem();
            new ResearchItem("VOIDTOUCHED", "FORBIDDEN", new AspectList().add(Aspect.ELDRITCH, 16).add(Aspect.TOOL, 10).add(Aspect.CRAFT, 8).add(FmAspects.ENVY, 32), -8, -1, 3, ForbiddenMagic.id("textures/misc/voidtouched.png"))
                .setPages(ResearchPage.text("forbiddenmagic.research_page.VOIDTOUCHED.1"), ResearchPage.infusionEnchantment("VOIDTOUCHED"))
                .setParents("MORPHTOOLS", "VOIDMETAL")
                .setConcealed()
                .setWarp(3)
                .registerResearchItem();
        }
        new ResearchItem("ARCANECAKE", "FORBIDDEN", new AspectList().add(FmAspects.GLUTTONY, 6).add(Aspect.HUNGER, 3).add(Aspect.CRAFT, 2), -12, -6, 2, TcStack.of("forbiddenmagic:arcane_cake", 1))
            .setPages(ResearchPage.text("forbiddenmagic.research_page.ARCANECAKE.1"), ResearchPage.infusion("ARCANECAKE"))
            .setParents("NETHERSHARDS", "INFUSION")
            .setAspectTriggers(FmAspects.GLUTTONY)
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("WRATHCAGE", "FORBIDDEN", new AspectList().add(FmAspects.WRATH, 5).add(Aspect.MECHANISM, 3).add(Aspect.BEAST, 2), -9, -9, 3, TcStack.of("forbiddenmagic:wrath_cage", 1))
            .setPages(
                ResearchPage.text("forbiddenmagic.research_page.WRATHCAGE.1"),
                ResearchPage.infusion("WRATHCAGE"),
                ResearchPage.text("forbiddenmagic.research_page.WRATHCAGE.2"),
                ResearchPage.text("forbiddenmagic.research_page.WRATHCAGE.3"),
                ResearchPage.crucible("MOBCRYSTAL")
            )
            .setParents("NETHERSHARDS")
            .setParentsHidden("INFUSION", "THAUMIUM", "DISTILESSENTIA")
            .setConcealed()
            .setWarp(3)
            .registerResearchItem();
        new ResearchItem("TAINTTREE", "FORBIDDEN", new AspectList().add(Aspect.TREE, 4).add(Aspect.TAINT, 6).add(Aspect.POISON, 2).add(Aspect.PLANT, 3), -11, 3, 3, TcStack.of("forbiddenmagic:taint_sapling", 1))
            .setPages(ResearchPage.text("forbiddenmagic.research_page.TAINTTREE.1"), ResearchPage.crucible("TAINTTREE"))
            .setParents("THAUMIUM", "INFUSION", "ETHEREALBLOOM")
            .setConcealed()
            .registerResearchItem();
        new ResearchItem("BLACKFLOWER", "FORBIDDEN", new AspectList().add(Aspect.PLANT, 3).add(Aspect.SENSES, 2).add(Aspect.DARKNESS, 4), -2, -6, 1, TcStack.of("forbiddenmagic:umbral_rose", 1))
            .setPages(ResearchPage.text("forbiddenmagic.research_page.BLACKFLOWER.1"), ResearchPage.crucible("BLACKFLOWER"))
            .setAspectTriggers(Aspect.SENSES)
            .registerResearchItem();
        if (FmConfig.enchantmentsEnabled()) {
            new ResearchItem("WRATH", "FORBIDDEN", new AspectList().add(FmAspects.WRATH, 16).add(Aspect.WEAPON, 20).add(Aspect.MAGIC, 10), -6, -3, 3, ForbiddenMagic.id("textures/misc/wrath.png"))
                .setPages(ResearchPage.text("forbiddenmagic.research_page.WRATH.1"), ResearchPage.infusionEnchantment("WRATH"))
                .setParents("NETHERSHARDS", "INFUSIONENCHANTMENT")
                .setConcealed()
                .setWarp(3)
                .registerResearchItem();
            new ResearchItem("GREEDY", "FORBIDDEN", new AspectList().add(Aspect.MAGIC, 2).add(Aspect.WEAPON, 1).add(Aspect.GREED, 3), -6, -5, 2, ForbiddenMagic.id("textures/misc/greedy.png"))
                .setPages(ResearchPage.text("forbiddenmagic.research_page.GREEDY.1"), ResearchPage.infusionEnchantment("GREEDY"))
                .setParents("NETHERSHARDS", "INFUSIONENCHANTMENT")
                .setConcealed()
                .registerResearchItem();
            new ResearchItem("CORRUPTING", "FORBIDDEN", new AspectList().add(FmAspects.NETHER, 5).add(Aspect.CRYSTAL, 2).add(Aspect.EXCHANGE, 1), -6, -4, 1, ForbiddenMagic.id("textures/misc/corrupting.png"))
                .setPages(ResearchPage.text("forbiddenmagic.research_page.CORRUPTING.1"), ResearchPage.infusionEnchantment("CORRUPTING"))
                .setParents("NETHERSHARDS", "INFUSIONENCHANTMENT")
                .setConcealed()
                .registerResearchItem();
            new ResearchItem("CONSUMING", "FORBIDDEN", new AspectList().add(Aspect.VOID, 4).add(Aspect.ENTROPY, 3).add(Aspect.MAGIC, 2), -4, -8, 1, ForbiddenMagic.id("textures/misc/consuming.png"))
                .setPages(ResearchPage.text("forbiddenmagic.research_page.CONSUMING.1"), ResearchPage.infusionEnchantment("CONSUMING"))
                .setParents("INFUSIONENCHANTMENT")
                .setSecondary()
                .setConcealed()
                .registerResearchItem();
            new ResearchItem("EDUCATIONAL", "FORBIDDEN", new AspectList().add(Aspect.MIND, 5).add(Aspect.WEAPON, 1).add(Aspect.MAGIC, 3), -4, -7, 2, ForbiddenMagic.id("textures/misc/educational.png"))
                .setPages(ResearchPage.text("forbiddenmagic.research_page.EDUCATIONAL.1"), ResearchPage.infusionEnchantment("EDUCATIONAL"))
                .setParents("INFUSIONENCHANTMENT")
                .setSecondary()
                .setConcealed()
                .registerResearchItem();
        }
    }
}
