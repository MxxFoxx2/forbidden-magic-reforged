package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.FmResearchItem;
import dev.visfox.forbiddenreforged.core.aspect.AspectDefinition;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.research.ResearchCategoryDefinition;
import dev.visfox.forbiddenreforged.core.research.ResearchDefinition;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import thaumcraft.aspect.Aspect;
import thaumcraft.crafting.TcStack;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;

public final class Tc4ResearchAdapter {
    private static final Map<String, String> LEGACY_RESEARCH_KEYS = Map.of(
        "rod_tainted", "ROD_tainted",
        "rod_infernal", "ROD_infernal",
        "rod_silverwood", "ROD_silverwood",
        "rod_witchwood", "ROD_witchwood",
        "rod_witchwood_staff", "ROD_witchwood_staff",
        "rod_profane", "ROD_profane"
    );

    private Tc4ResearchAdapter() {
    }

    public static void register(
        ResearchCategoryDefinition category,
        List<ResearchDefinition> definitions
    ) {
        String categoryKey = categoryKey(category.id());
        ResearchCategories.registerCategory(
            categoryKey,
            identifier(category.icon()),
            identifier(category.background())
        );
        for (ResearchDefinition definition : definitions) {
            if (!definition.category().equals(category.id())) {
                throw new IllegalArgumentException(
                    "Research category mismatch for " + definition.id()
                );
            }
            ResearchCategories.addResearch(translate(definition));
        }
    }

    public static ResearchItem translate(ResearchDefinition definition) {
        if (definition.stages().size() != 1) {
            throw new IllegalArgumentException(
                "TC4 research supports one page group per entry: " + definition.id()
            );
        }

        ResearchDefinition.Stage stage = definition.stages().get(0);
        if (!stage.prerequisites().isEmpty()) {
            throw new IllegalArgumentException(
                "TC4 adapter cannot represent stage-specific prerequisites: " + definition.id()
            );
        }

        String key = researchKey(definition.id());
        String category = categoryKey(definition.category());
        ResearchItem item;
        if (definition.icon().kind() == ResearchDefinition.IconKind.ITEM) {
            item = new FmResearchItem(
                key,
                category,
                Tc4AspectAdapter.toAspectList(definition.noteCost()),
                definition.column(),
                definition.row(),
                definition.complexity(),
                TcStack.of(definition.icon().value().toString(), 1)
            );
        } else {
            item = new FmResearchItem(
                key,
                category,
                Tc4AspectAdapter.toAspectList(definition.noteCost()),
                definition.column(),
                definition.row(),
                definition.complexity(),
                identifier(definition.icon().value())
            );
        }

        for (ResearchDefinition.Flag flag : definition.flags()) {
            applyFlag(item, flag);
        }

        if (!definition.parents().isEmpty()) {
            item.setParents(definition.parents().stream()
                .map(Tc4ResearchAdapter::researchKey)
                .toArray(String[]::new));
        }
        if (!definition.hiddenParents().isEmpty()) {
            item.setParentsHidden(definition.hiddenParents().stream()
                .map(Tc4ResearchAdapter::researchKey)
                .toArray(String[]::new));
        }
        if (!definition.aspectTriggers().isEmpty()) {
            Aspect[] triggers = definition.aspectTriggers().stream()
                .map(Tc4AspectAdapter::resolveAspect)
                .toArray(Aspect[]::new);
            item.setAspectTriggers(triggers);
        }

        ResearchPage[] pages = stage.pages().stream()
            .map(Tc4ResearchAdapter::translatePage)
            .toArray(ResearchPage[]::new);
        item.setPages(pages);
        item.setWarp(stage.warp());
        return item;
    }

    private static void applyFlag(ResearchItem item, ResearchDefinition.Flag flag) {
        switch (flag) {
            case SPECIAL -> item.setSpecial();
            case SECONDARY -> item.setSecondary();
            case ROUND_ICON -> item.setRound();
            case STUB -> item.setStub();
            case VIRTUAL -> item.setVirtual();
            case CONCEALED -> item.setConcealed();
            case HIDDEN -> item.setHidden();
            case LOST -> item.setLost();
            case AUTO_UNLOCK -> item.setAutoUnlock();
        }
    }

    private static ResearchPage translatePage(ResearchDefinition.Page page) {
        return switch (page.pageKind()) {
            case TEXT -> ResearchPage.text(page.reference());
            case CRAFTING_RECIPE -> ResearchPage.crafting(
                Tc4RecipeAdapter.recipeKey(ContentId.parse(page.reference()))
            );
            case ARCANE_RECIPE -> ResearchPage.arcane(
                Tc4RecipeAdapter.recipeKey(ContentId.parse(page.reference()))
            );
            case CRUCIBLE_RECIPE -> ResearchPage.crucible(
                Tc4RecipeAdapter.recipeKey(ContentId.parse(page.reference()))
            );
            case INFUSION_RECIPE -> ResearchPage.infusion(
                Tc4RecipeAdapter.recipeKey(ContentId.parse(page.reference()))
            );
            case INFUSION_ENCHANTMENT -> ResearchPage.infusionEnchantment(
                Tc4RecipeAdapter.recipeKey(ContentId.parse(page.reference()))
            );
        };
    }

    private static String researchKey(ContentId id) {
        String path = definitionPath(id, "research/");
        return LEGACY_RESEARCH_KEYS.getOrDefault(
            path,
            Tc4RecipeAdapter.researchKey(id)
        );
    }

    private static String categoryKey(ContentId id) {
        return definitionPath(id, "category/").toUpperCase(java.util.Locale.ROOT);
    }

    private static String definitionPath(ContentId id, String prefix) {
        if (!"forbiddenmagic".equals(id.namespace()) || !id.path().startsWith(prefix)) {
            throw new IllegalArgumentException("Unexpected TC4 research identifier: " + id);
        }
        String path = id.path().substring(prefix.length());
        if (path.isBlank()) {
            throw new IllegalArgumentException("Empty TC4 research identifier: " + id);
        }
        return path;
    }

    private static Identifier identifier(ContentId id) {
        return Identifier.fromNamespaceAndPath(id.namespace(), id.path());
    }
}
