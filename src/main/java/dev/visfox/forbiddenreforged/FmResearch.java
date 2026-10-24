package dev.visfox.forbiddenreforged;

import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcStack;
import thaumcraft.research.ResearchCategories;
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
        new FmResearchItem("NETHERSHARDS", "FORBIDDEN", new AspectList(), -8, -5, 0, TcStack.of(FmItems.id("wrath").toString(), 1))
            .setPages(
                ResearchPage.text("forbiddenmagic.research_page.NETHERSHARDS.1"),
                ResearchPage.text(FmConfig.lustDisabled() ? "forbiddenmagic.research_page.NETHERSHARDS.2b" : "forbiddenmagic.research_page.NETHERSHARDS.2"),
                ResearchPage.text("forbiddenmagic.research_page.NETHERSHARDS.3")
            )
            .setStub()
            .setRound()
            .setAutoUnlock()
            .registerResearchItem();
        new FmResearchItem(
            "HELLFIRE",
            "FORBIDDEN",
            new AspectList().add(Aspect.FIRE, 3).add(FmAspects.NETHER, 4).add(Aspect.TRAVEL, 2),
            -8,
            -8,
            1,
            TcStack.of("minecraft:fire", 1)
        )
            .setPages(ResearchPage.text("forbiddenmagic.research_page.HELLFIRE.1"))
            .setRound()
            .setHidden()
            .setParents("NETHERSHARDS")
            .setAspectTriggers(FmAspects.NETHER)
            .registerResearchItem();
        new FmResearchItem(
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
        new FmResearchItem("FORK", "FORBIDDEN", new AspectList().add(Aspect.FIRE, 3).add(Aspect.MECHANISM, 1).add(Aspect.TOOL, 1), -6, -7, 0, TcStack.of(FmItems.DIABOLIST_FORK.getId().toString(), 1))
            .setPages(
                ResearchPage.text("forbiddenmagic.research_page.FORK.1"),
                ResearchPage.infusion("FORK")
            )
            .setSecondary()
            .setConcealed()
            .setParents("INFUSION", "NETHERSHARDS", "THAUMIUM")
            .registerResearchItem();
        new FmResearchItem("MORPHTOOLS", "FORBIDDEN", new AspectList().add(Aspect.TOOL, 2).add(FmAspects.ENVY, 3).add(Aspect.EXCHANGE, 2), -9, -3, 3, TcStack.of("forbiddenmagic:morph_pickaxe", 1))
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
            new FmResearchItem("CLUSTER", "FORBIDDEN", new AspectList().add(Aspect.METAL, 1).add(Aspect.FIRE, 4).add(FmAspects.ENVY, 3), -10, -1, 3, ForbiddenMagic.id("textures/misc/lucrative.png"))
                .setPages(ResearchPage.text("forbiddenmagic.research_page.CLUSTER.1"), ResearchPage.infusionEnchantment("CLUSTER"))
                .setParents("MORPHTOOLS", "ELEMENTALPICK")
                .setSecondary()
                .setConcealed()
                .registerResearchItem();
            new FmResearchItem("IMPACT", "FORBIDDEN", new AspectList().add(Aspect.ENTROPY, 8).add(Aspect.TOOL, 10).add(Aspect.MINE, 16).add(FmAspects.ENVY, 10), -9, -1, 3, ForbiddenMagic.id("textures/misc/impact.png"))
                .setPages(ResearchPage.text("forbiddenmagic.research_page.IMPACT.1"), ResearchPage.infusionEnchantment("IMPACT"))
                .setParents("MORPHTOOLS", "ELEMENTALSHOVEL")
                .setConcealed()
                .registerResearchItem();
            new FmResearchItem("VOIDTOUCHED", "FORBIDDEN", new AspectList().add(Aspect.ELDRITCH, 16).add(Aspect.TOOL, 10).add(Aspect.CRAFT, 8).add(FmAspects.ENVY, 32), -8, -1, 3, ForbiddenMagic.id("textures/misc/voidtouched.png"))
                .setPages(ResearchPage.text("forbiddenmagic.research_page.VOIDTOUCHED.1"), ResearchPage.infusionEnchantment("VOIDTOUCHED"))
                .setParents("MORPHTOOLS", "VOIDMETAL")
                .setConcealed()
                .setWarp(3)
                .registerResearchItem();
        }
        new FmResearchItem("ARCANECAKE", "FORBIDDEN", new AspectList().add(FmAspects.GLUTTONY, 6).add(Aspect.HUNGER, 3).add(Aspect.CRAFT, 2), -12, -6, 2, TcStack.of("forbiddenmagic:arcane_cake", 1))
            .setPages(ResearchPage.text("forbiddenmagic.research_page.ARCANECAKE.1"), ResearchPage.infusion("ARCANECAKE"))
            .setParents("NETHERSHARDS", "INFUSION")
            .setAspectTriggers(FmAspects.GLUTTONY)
            .setConcealed()
            .registerResearchItem();
        new FmResearchItem("WRATHCAGE", "FORBIDDEN", new AspectList().add(FmAspects.WRATH, 5).add(Aspect.MECHANISM, 3).add(Aspect.BEAST, 2), -9, -9, 3, TcStack.of("forbiddenmagic:wrath_cage", 1))
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
        new FmResearchItem("TAINTTREE", "FORBIDDEN", new AspectList().add(Aspect.TREE, 4).add(Aspect.TAINT, 6).add(Aspect.POISON, 2).add(Aspect.PLANT, 3), -11, 3, 3, TcStack.of("forbiddenmagic:taint_sapling", 1))
            .setPages(ResearchPage.text("forbiddenmagic.research_page.TAINTTREE.1"), ResearchPage.crucible("TAINTTREE"))
            .setParents("THAUMIUM", "INFUSION", "ETHEREALBLOOM")
            .setConcealed()
            .registerResearchItem();
        new FmResearchItem("TAINTSTONE", "FORBIDDEN", new AspectList().add(Aspect.EARTH, 4).add(Aspect.TAINT, 6).add(Aspect.ORDER, 2), -10, 3, 2, TcStack.of("forbiddenmagic:taint_stone_brick", 1))
            .setPages(
                ResearchPage.text("forbiddenmagic.research_page.TAINTSTONE.1"),
                ResearchPage.arcane("TaintStone"),
                ResearchPage.crafting("TaintBrick")
            )
            .setParents("THAUMIUM", "INFUSION", "ETHEREALBLOOM")
            .setConcealed()
            .registerResearchItem();
        new FmResearchItem("BLACKFLOWER", "FORBIDDEN", new AspectList().add(Aspect.PLANT, 3).add(Aspect.SENSES, 2).add(Aspect.DARKNESS, 4), -2, -6, 1, TcStack.of("forbiddenmagic:umbral_rose", 1))
            .setPages(ResearchPage.text("forbiddenmagic.research_page.BLACKFLOWER.1"), ResearchPage.crucible("BLACKFLOWER"))
            .setAspectTriggers(Aspect.SENSES)
            .registerResearchItem();
        if (FmConfig.enchantmentsEnabled()) {
            new FmResearchItem("WRATH", "FORBIDDEN", new AspectList().add(FmAspects.WRATH, 16).add(Aspect.WEAPON, 20).add(Aspect.MAGIC, 10), -6, -3, 3, ForbiddenMagic.id("textures/misc/wrath.png"))
                .setPages(ResearchPage.text("forbiddenmagic.research_page.WRATH.1"), ResearchPage.infusionEnchantment("WRATH"))
                .setParents("NETHERSHARDS", "INFUSIONENCHANTMENT")
                .setConcealed()
                .setWarp(3)
                .registerResearchItem();
            if (FmConfig.greedyEnchantmentEnabled()) {
                new FmResearchItem("GREEDY", "FORBIDDEN", new AspectList().add(Aspect.MAGIC, 2).add(Aspect.WEAPON, 1).add(Aspect.GREED, 3), -6, -5, 2, ForbiddenMagic.id("textures/misc/greedy.png"))
                    .setPages(ResearchPage.text("forbiddenmagic.research_page.GREEDY.1"), ResearchPage.infusionEnchantment("GREEDY"))
                    .setParents("NETHERSHARDS", "INFUSIONENCHANTMENT")
                    .setConcealed()
                    .registerResearchItem();
            }
            new FmResearchItem("CORRUPTING", "FORBIDDEN", new AspectList().add(FmAspects.NETHER, 5).add(Aspect.CRYSTAL, 2).add(Aspect.EXCHANGE, 1), -6, -4, 1, ForbiddenMagic.id("textures/misc/corrupting.png"))
                .setPages(ResearchPage.text("forbiddenmagic.research_page.CORRUPTING.1"), ResearchPage.infusionEnchantment("CORRUPTING"))
                .setParents("NETHERSHARDS", "INFUSIONENCHANTMENT")
                .setConcealed()
                .registerResearchItem();
            new FmResearchItem("CONSUMING", "FORBIDDEN", new AspectList().add(Aspect.VOID, 4).add(Aspect.ENTROPY, 3).add(Aspect.MAGIC, 2), -4, -8, 1, ForbiddenMagic.id("textures/misc/consuming.png"))
                .setPages(ResearchPage.text("forbiddenmagic.research_page.CONSUMING.1"), ResearchPage.infusionEnchantment("CONSUMING"))
                .setParents("INFUSIONENCHANTMENT")
                .setSecondary()
                .setConcealed()
                .registerResearchItem();
            new FmResearchItem("EDUCATIONAL", "FORBIDDEN", new AspectList().add(Aspect.MIND, 5).add(Aspect.WEAPON, 1).add(Aspect.MAGIC, 3), -4, -7, 2, ForbiddenMagic.id("textures/misc/educational.png"))
                .setPages(ResearchPage.text("forbiddenmagic.research_page.EDUCATIONAL.1"), ResearchPage.infusionEnchantment("EDUCATIONAL"))
                .setParents("INFUSIONENCHANTMENT")
                .setSecondary()
                .setConcealed()
                .registerResearchItem();
        }
        new FmResearchItem("RIDINGCROP", "FORBIDDEN", new AspectList(), 0, -8, 0, TcStack.of(FmItems.RIDING_CROP.getId().toString(), 1))
            .setPages(ResearchPage.text("forbiddenmagic.research_page.RIDINGCROP.1"))
            .setStub()
            .setRound()
            .setAutoUnlock()
            .registerResearchItem();
        new FmResearchItem("RINGFOOD", "FORBIDDEN", new AspectList().add(FmAspects.GLUTTONY, 4).add(Aspect.HUNGER, 3).add(Aspect.LIFE, 2), -11, -6, 2, TcStack.of(FmItems.RING_NUTRITION.getId().toString(), 1))
            .setPages(ResearchPage.text("forbiddenmagic.research_page.RINGFOOD.1"), ResearchPage.arcane("RINGFOOD"))
            .setParents("NETHERSHARDS")
            .registerResearchItem();
        new FmResearchItem("ROD_tainted", "FORBIDDEN", new AspectList().add(Aspect.TAINT, 4).add(Aspect.MAGIC, 2), -10, -4, 1, TcStack.of(FmItems.TAINTED_ROD.getId().toString(), 1))
            .setPages(ResearchPage.text("forbiddenmagic.research_page.ROD_tainted.1"))
            .setParents("NETHERSHARDS")
            .setHidden()
            .registerResearchItem();
        new FmResearchItem("ROD_infernal", "FORBIDDEN", new AspectList().add(FmAspects.NETHER, 4).add(Aspect.FIRE, 3).add(Aspect.TOOL, 1), -11, -4, 3, TcStack.of(FmItems.INFERNAL_ROD.getId().toString(), 1))
            .setPages(ResearchPage.text("forbiddenmagic.research_page.ROD_infernal.1"))
            .setParents("ROD_silverwood", "INFUSION", "NETHERSHARDS")
            .setConcealed()
            .registerResearchItem();
        new FmResearchItem("ROD_witchwood", "FORBIDDEN", new AspectList().add(Aspect.TREE, 3).add(Aspect.MAGIC, 2), -10, -5, 2, TcStack.of(FmItems.WITCHWOOD_ROD.getId().toString(), 1))
            .setPages(ResearchPage.text("forbiddenmagic.research_page.ROD_witchwood.1"))
            .setParents("NETHERSHARDS")
            .setHidden()
            .registerResearchItem();
        new FmResearchItem("ROD_profane", "FORBIDDEN", new AspectList(), -9, -8, 0, TcStack.of(FmItems.PROFANE_ROD.getId().toString(), 1))
            .setPages(ResearchPage.text("forbiddenmagic.research_page.ROD_profane.1"))
            .setParents("NETHERSHARDS")
            .setHidden()
            .setStub()
            .registerResearchItem();
        new FmResearchItem("ROD_witchwood_staff", "FORBIDDEN", new AspectList().add(Aspect.TREE, 4).add(Aspect.MAGIC, 3), -10, -6, 3, TcStack.of(FmItems.WITCHWOOD_STAFF_ROD.getId().toString(), 1))
            .setPages(ResearchPage.text("forbiddenmagic.research_page.ROD_witchwood_staff.1"))
            .setParents("ROD_witchwood")
            .setHidden()
            .registerResearchItem();
        if (FmConfig.emeraldTrans()) {
            new FmResearchItem("TRANSEMERALD", "FORBIDDEN", new AspectList().add(Aspect.CRYSTAL, 2).add(Aspect.EXCHANGE, 5).add(Aspect.GREED, 4), 0, -6, 3, TcStack.of(FmItems.EMERALD_FRAGMENT.getId().toString(), 1))
                .setPages(ResearchPage.text("forbiddenmagic.research_page.TRANSEMERALD.1"), ResearchPage.crucible("TRANSEMERALD"))
                .setSecondary()
                .setConcealed()
                .setParents("TRANSGOLD")
                .registerResearchItem();
        }
        new FmResearchItem("CRYSTALWELL", "FORBIDDEN", new AspectList().add(Aspect.MIND, 3).add(Aspect.CRYSTAL, 2).add(Aspect.MAGIC, 1), -2, -8, 1, TcStack.of(FmItems.CRYSTALWELL.getId().toString(), 1))
            .setPages(ResearchPage.text("forbiddenmagic.research_page.CRYSTALWELL.1"), ResearchPage.arcane("CRYSTALWELL"))
            .setParents("RESEARCH")
            .registerResearchItem();
        new FmResearchItem("PRIMEWELL", "FORBIDDEN", new AspectList().add(Aspect.MIND, 3).add(Aspect.ELDRITCH, 6).add(Aspect.CRAFT, 1), 2, -8, 1, TcStack.of(FmItems.PRIMEWELL.getId().toString(), 1))
            .setPages(ResearchPage.text("forbiddenmagic.research_page.PRIMEWELL.1"), ResearchPage.arcane("PRIMEWELL"))
            .setParents("PRIMPEARL")
            .setConcealed()
            .registerResearchItem();
        new FmResearchItem("TAINTSHOVEL", "FORBIDDEN", new AspectList().add(Aspect.TOOL, 2).add(Aspect.TAINT, 4).add(Aspect.HEAL, 3), -10, 2, 2, TcStack.of(FmItems.TAINT_SHOVEL.getId().toString(), 1))
            .setPages(ResearchPage.text("forbiddenmagic.research_page.TAINTSHOVEL.1"), ResearchPage.infusion("TAINTSHOVEL"))
            .setParents("THAUMIUM", "INFUSION")
            .setConcealed()
            .registerResearchItem();
        new FmResearchItem("TAINTPICK", "FORBIDDEN", new AspectList().add(Aspect.TOOL, 2).add(Aspect.TAINT, 4).add(Aspect.ENTROPY, 3), -10, 1, 2, TcStack.of(FmItems.TAINT_PICKAXE.getId().toString(), 1))
            .setPages(ResearchPage.text("forbiddenmagic.research_page.TAINTPICK.1"), ResearchPage.infusion("TAINTPICK"))
            .setParents("THAUMIUM", "INFUSION")
            .setConcealed()
            .registerResearchItem();
        new FmResearchItem("FOCUSBLINK", "FORBIDDEN", new AspectList().add(Aspect.TRAVEL, 3).add(Aspect.ENTROPY, 3).add(FmAspects.NETHER, 3).add(FmAspects.SLOTH, 6), -11, -3, 2, TcStack.of(FmItems.BLINK_FOCUS.getId().toString(), 1))
            .setPages(ResearchPage.text("forbiddenmagic.research_page.FOCUSBLINK.1"), ResearchPage.infusion("FOCUSBLINK"))
            .setParents("NETHERSHARDS", "INFUSION")
            .setConcealed()
            .registerResearchItem();
    }
}
