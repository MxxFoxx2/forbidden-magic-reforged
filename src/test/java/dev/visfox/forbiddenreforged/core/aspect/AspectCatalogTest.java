package dev.visfox.forbiddenreforged.core.aspect;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure-logic port of the {@code coreAspectCatalogIsHostNeutralAndComplete} GameTest:
 * the aspect catalog is host-neutral data and needs no Minecraft runtime to verify.
 */
class AspectCatalogTest {
    private static final String MODID = "forbiddenmagic";

    @Test
    void fullCatalogDefinesSevenAspectsAndNoLustDefinesSix() {
        assertEquals(7, ForbiddenMagicAspectCatalog.definitions(true).size());
        assertEquals(6, ForbiddenMagicAspectCatalog.definitions(false).size());
    }

    @Test
    void componentReferencesUseSemanticIds() {
        for (AspectDefinition definition : ForbiddenMagicAspectCatalog.definitions(true)) {
            assertEquals(MODID, definition.id().namespace(), "aspect id namespace");
            for (ContentId component : definition.components()) {
                assertEquals(MODID, component.namespace(), "component namespace");
                assertTrue(component.path().startsWith("concept/"),
                    "components must use semantic concept IDs, not host aspect IDs: " + component);
            }
        }
    }

    @Test
    void luxuriaIsFirstAndKeepsOriginalComponents() {
        List<AspectDefinition> enabled = ForbiddenMagicAspectCatalog.definitions(true);
        AspectDefinition luxuria = enabled.get(0);
        assertEquals(ForbiddenMagicAspectCatalog.LUXURIA, luxuria.id());
        assertEquals(List.of(
            ContentId.parse("forbiddenmagic:concept/flesh"),
            ContentId.parse("forbiddenmagic:concept/hunger")
        ), luxuria.components());
    }

    @Test
    void infernusRetainsOriginalBlendMode() {
        List<AspectDefinition> enabled = ForbiddenMagicAspectCatalog.definitions(true);
        assertEquals(AspectDefinition.BlendMode.ONE_MINUS_SOURCE_COLOR, enabled.get(1).blendMode());
    }

    @Test
    void noLustCatalogOmitsLuxuria() {
        assertFalse(ForbiddenMagicAspectCatalog.definitions(false).stream()
            .anyMatch(definition -> definition.id().equals(ForbiddenMagicAspectCatalog.LUXURIA)));
    }

    @Test
    void duplicateAspectCostsMerge() {
        ContentId fire = ContentId.parse("forbiddenmagic:concept/fire");
        AspectDefinition.Cost merged = new AspectDefinition.Cost(List.of(
            new AspectDefinition.Amount(fire, 2),
            new AspectDefinition.Amount(fire, 3)
        ));
        assertEquals(5, merged.amountOf(fire));
        assertEquals(5, merged.totalAmount());
    }
}