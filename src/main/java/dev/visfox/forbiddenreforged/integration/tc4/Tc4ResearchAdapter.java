package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.aspect.AspectDefinition;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.research.ResearchCategoryDefinition;
import dev.visfox.forbiddenreforged.core.research.ResearchDefinition;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.crafting.TcStack;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchPage;
import thaumcraft.research.ResearchStageRegistry;

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
        register(List.of(category), definitions);
    }

    public static void register(
        List<ResearchCategoryDefinition> categories,
        List<ResearchDefinition> definitions
    ) {
        Objects.requireNonNull(categories, "categories");
        Objects.requireNonNull(definitions, "definitions");

        Map<ContentId, ResearchCategoryDefinition> categoriesById = new LinkedHashMap<>();
        for (ResearchCategoryDefinition category : categories) {
            Objects.requireNonNull(category, "category");
            if (categoriesById.putIfAbsent(category.id(), category) != null) {
                throw new IllegalArgumentException("Duplicate research category: " + category.id());
            }
            ResearchCategories.registerCategory(
                categoryKey(category.id()),
                identifier(category.icon()),
                identifier(category.background())
            );
        }

        Set<ContentId> definitionIds = new HashSet<>();
        for (ResearchDefinition definition : definitions) {
            Objects.requireNonNull(definition, "definition");
            if (!definitionIds.add(definition.id())) {
                throw new IllegalArgumentException("Duplicate research definition: " + definition.id());
            }
            if (!categoriesById.containsKey(definition.category())) {
                throw new IllegalArgumentException(
                    "Unknown research category " + definition.category() + " for " + definition.id()
                );
            }
        }

        Map<ContentId, List<Position>> positions = planPositions(definitions);
        List<ResearchItem> translated = new ArrayList<>();
        List<String> automaticStageKeys = new ArrayList<>();
        for (ResearchDefinition definition : definitions) {
            if (definition.stages().size() > 1
                && definition.noteCost().isEmpty()
                && (definition.flags().contains(ResearchDefinition.Flag.HIDDEN)
                    || definition.flags().contains(ResearchDefinition.Flag.LOST))
                && !definition.flags().contains(ResearchDefinition.Flag.AUTO_UNLOCK)) {
                throw new IllegalArgumentException(
                    "Multi-stage hidden research with no note cost must be AUTO_UNLOCK: " + definition.id()
                );
            }

            List<ResearchItem> entries = translateStages(definition, positions.get(definition.id()));
            translated.addAll(entries);
            if (entries.size() > 1) {
                for (int index = 1; index < entries.size(); index++) {
                    automaticStageKeys.add(entries.get(index).key());
                }
                if (definition.noteCost().isEmpty()
                    && !definition.flags().contains(ResearchDefinition.Flag.HIDDEN)
                    && !definition.flags().contains(ResearchDefinition.Flag.LOST)) {
                    automaticStageKeys.add(entries.get(0).key());
                }
            }
        }

        for (ResearchItem item : translated) {
            ResearchCategories.addResearch(item);
        }
        ResearchStageRegistry.registerAutomaticStages(automaticStageKeys);
    }

    /**
     * Compatibility view for callers that expect one ResearchItem. For a
     * multi-stage definition this is the final entry, whose key is the
     * definition's canonical research key.
     */
    public static ResearchItem translate(ResearchDefinition definition) {
        List<ResearchItem> stages = translateStages(definition);
        return stages.get(stages.size() - 1);
    }

    /**
     * Lowers a semantic research definition into native TC4 entries.
     *
     * TC4 has no stage primitive. Each stage is therefore represented by a
     * linked ResearchItem: intermediate entries use adapter-owned keys and
     * the final entry keeps the original key so existing recipe and research
     * references continue to point at the completed definition.
     */
    public static List<ResearchItem> translateStages(ResearchDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        return translateStages(definition, preferredPositions(definition));
    }

    private static List<ResearchItem> translateStages(
        ResearchDefinition definition,
        List<Position> positions
    ) {
        if (positions.size() != definition.stages().size()) {
            throw new IllegalArgumentException(
                "Expected " + definition.stages().size() + " positions for " + definition.id()
            );
        }

        String baseKey = researchKey(definition.id());
        String category = categoryKey(definition.category());
        int finalStage = definition.stages().size() - 1;
        List<ResearchItem> translated = new ArrayList<>(definition.stages().size());

        for (int index = 0; index <= finalStage; index++) {
            ResearchDefinition.Stage stage = definition.stages().get(index);
            boolean first = index == 0;
            boolean last = index == finalStage;
            String key = last ? baseKey : stageKey(baseKey, index);
            Position position = positions.get(index);

            AspectList noteCost = first
                ? Tc4AspectAdapter.toAspectList(definition.noteCost())
                : new AspectList();
            ResearchItem item;
            if (definition.icon().kind() == ResearchDefinition.IconKind.ITEM) {
                item = new Tc4ResearchItem(
                    key,
                    category,
                    noteCost,
                    position.column(),
                    position.row(),
                    definition.complexity(),
                    TcStack.of(definition.icon().value().toString(), 1),
                    baseKey
                );
            } else {
                item = new Tc4ResearchItem(
                    key,
                    category,
                    noteCost,
                    position.column(),
                    position.row(),
                    definition.complexity(),
                    identifier(definition.icon().value()),
                    baseKey
                );
            }

            for (ResearchDefinition.Flag flag : definition.flags()) {
                if ((flag == ResearchDefinition.Flag.HIDDEN || flag == ResearchDefinition.Flag.LOST) && !first) {
                    continue;
                }
                applyFlag(item, flag);
            }

            List<String> parentKeys = new ArrayList<>();
            if (first) {
                definition.parents().stream().map(Tc4ResearchAdapter::researchKey).forEach(parentKeys::add);
            } else {
                parentKeys.add(keyForStage(baseKey, index - 1, finalStage));
            }
            stage.prerequisites().stream().map(Tc4ResearchAdapter::researchKey).forEach(parentKeys::add);
            setParentKeys(item, parentKeys);

            if (first) {
                setParentsHidden(item, definition.hiddenParents());
                setTriggers(item, definition);
            }
            if (last) {
                setSiblings(item, definition.siblings());
            }

            ResearchPage[] pages = stage.pages().stream()
                .map(Tc4ResearchAdapter::translatePage)
                .toArray(ResearchPage[]::new);
            item.setPages(pages);
            item.setWarp(stage.warp());
            translated.add(item);
        }

        return List.copyOf(translated);
    }

    private static void setParentKeys(ResearchItem item, List<String> parents) {
        LinkedHashSet<String> keys = new LinkedHashSet<>(parents);
        if (!keys.isEmpty()) {
            item.setParents(keys.toArray(String[]::new));
        }
    }

    private static void setParentsHidden(ResearchItem item, List<ContentId> parents) {
        if (!parents.isEmpty()) {
            item.setParentsHidden(parents.stream()
                .map(Tc4ResearchAdapter::researchKey)
                .toArray(String[]::new));
        }
    }

    private static void setSiblings(ResearchItem item, List<ContentId> siblings) {
        if (!siblings.isEmpty()) {
            item.setSiblings(siblings.stream()
                .map(Tc4ResearchAdapter::researchKey)
                .toArray(String[]::new));
        }
    }

    private static void setTriggers(ResearchItem item, ResearchDefinition definition) {
        if (!definition.itemTriggers().isEmpty()) {
            item.setItemTriggers(definition.itemTriggers().stream()
                .map(ContentId::toString)
                .toArray(String[]::new));
        }
        if (!definition.entityTriggers().isEmpty()) {
            item.setEntityTriggers(definition.entityTriggers().stream()
                .map(ContentId::toString)
                .toArray(String[]::new));
        }
        if (!definition.aspectTriggers().isEmpty()) {
            Aspect[] triggers = definition.aspectTriggers().stream()
                .map(Tc4AspectAdapter::resolveAspect)
                .toArray(Aspect[]::new);
            item.setAspectTriggers(triggers);
        }
    }

    private static Map<ContentId, List<Position>> planPositions(List<ResearchDefinition> definitions) {
        Map<String, Set<Position>> occupied = new HashMap<>();
        Set<String> knownKeys = new HashSet<>();
        for (ResearchItem item : ResearchCategories.allResearch()) {
            knownKeys.add(item.key());
            if (!item.isVirtual()) {
                occupied.computeIfAbsent(item.category(), ignored -> new HashSet<>())
                    .add(new Position(item.column(), item.row()));
            }
        }

        for (ResearchDefinition definition : definitions) {
            String key = researchKey(definition.id());
            if (!knownKeys.add(key)) {
                throw new IllegalArgumentException("Research key is already registered: " + key);
            }
            for (int index = 0; index < definition.stages().size() - 1; index++) {
                String intermediate = stageKey(key, index);
                if (!knownKeys.add(intermediate)) {
                    throw new IllegalArgumentException("Generated research stage key is already registered: " + intermediate);
                }
            }

            if (!definition.flags().contains(ResearchDefinition.Flag.VIRTUAL)) {
                Position canonical = new Position(definition.column(), definition.row());
                Set<Position> categoryPositions = occupied.computeIfAbsent(
                    categoryKey(definition.category()),
                    ignored -> new HashSet<>()
                );
                if (!categoryPositions.add(canonical)) {
                    throw new IllegalArgumentException(
                        "Research layout collision at " + canonical + " for " + definition.id()
                    );
                }
            }
        }

        Map<ContentId, List<Position>> result = new LinkedHashMap<>();
        List<ResearchDefinition> ordered = definitions.stream()
            .sorted(Comparator.comparing(ResearchDefinition::id))
            .toList();
        for (ResearchDefinition definition : ordered) {
            List<Position> stagePositions = new ArrayList<>(preferredPositions(definition));
            if (!definition.flags().contains(ResearchDefinition.Flag.VIRTUAL)) {
                Set<Position> categoryPositions = occupied.get(categoryKey(definition.category()));
                int finalStage = definition.stages().size() - 1;
                for (int index = 0; index < finalStage; index++) {
                    Position preferred = stagePositions.get(index);
                    Position available = findAvailable(preferred, categoryPositions);
                    categoryPositions.add(available);
                    stagePositions.set(index, available);
                }
            }
            result.put(definition.id(), List.copyOf(stagePositions));
        }
        return Map.copyOf(result);
    }

    private static List<Position> preferredPositions(ResearchDefinition definition) {
        int finalStage = definition.stages().size() - 1;
        List<Position> positions = new ArrayList<>(definition.stages().size());
        for (int index = 0; index <= finalStage; index++) {
            int column = index == finalStage
                ? definition.column()
                : Math.subtractExact(definition.column(), finalStage - index);
            positions.add(new Position(column, definition.row()));
        }
        return List.copyOf(positions);
    }

    private static Position findAvailable(Position preferred, Set<Position> occupied) {
        for (int distance = 0; distance < 1024; distance++) {
            for (int rowOffset = -distance; rowOffset <= distance; rowOffset++) {
                int columnOffset = distance - Math.abs(rowOffset);
                int row = Math.addExact(preferred.row(), rowOffset);
                Position left = new Position(
                    Math.subtractExact(preferred.column(), columnOffset),
                    row
                );
                if (!occupied.contains(left)) {
                    return left;
                }
                if (columnOffset != 0) {
                    Position right = new Position(
                        Math.addExact(preferred.column(), columnOffset),
                        row
                    );
                    if (!occupied.contains(right)) {
                        return right;
                    }
                }
            }
        }
        throw new IllegalStateException("Could not allocate a free TC4 research position near " + preferred);
    }

    private static String stageKey(String baseKey, int zeroBasedIndex) {
        return baseKey + "_FM_STAGE_" + (zeroBasedIndex + 1);
    }

    private static String keyForStage(String baseKey, int index, int finalStage) {
        return index == finalStage ? baseKey : stageKey(baseKey, index);
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

    private static ResourceLocation identifier(ContentId id) {
        return ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path());
    }

    private record Position(int column, int row) {
    }
}
