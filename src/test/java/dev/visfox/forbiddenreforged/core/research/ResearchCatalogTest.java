package dev.visfox.forbiddenreforged.core.research;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Structural and gating checks for the host-neutral research catalog. The runtime
 * GameTests still verify that these definitions translate into registered TC4 research;
 * this suite verifies the catalog data itself without a server.
 */
class ResearchCatalogTest {
    private static ForbiddenMagicResearchCatalog.Options allOn() {
        return new ForbiddenMagicResearchCatalog.Options(true, true, false, true, true, true);
    }

    private static Optional<ResearchDefinition> find(List<ResearchDefinition> definitions, String key) {
        return definitions.stream().filter(d -> d.id().path().equals("research/" + key)).findFirst();
    }

    @Test
    void idsAreUnique() {
        List<ResearchDefinition> definitions = ForbiddenMagicResearchCatalog.definitions(allOn());
        Set<String> seen = new HashSet<>();
        for (ResearchDefinition definition : definitions) {
            assertTrue(seen.add(definition.id().toString()), "duplicate research id: " + definition.id());
        }
    }

    @Test
    void witchwoodResearchRequiresArsMagica() {
        assertTrue(find(ForbiddenMagicResearchCatalog.definitions(allOn()), "rod_witchwood").isPresent());
        assertTrue(find(ForbiddenMagicResearchCatalog.definitions(allOn()), "rod_witchwood_staff").isPresent());

        ForbiddenMagicResearchCatalog.Options noArs =
            new ForbiddenMagicResearchCatalog.Options(true, true, false, true, false, true);
        assertFalse(find(ForbiddenMagicResearchCatalog.definitions(noArs), "rod_witchwood").isPresent());
        assertFalse(find(ForbiddenMagicResearchCatalog.definitions(noArs), "rod_witchwood_staff").isPresent());
    }

    @Test
    void subcollarRequiresLustEnabled() {
        assertTrue(find(ForbiddenMagicResearchCatalog.definitions(allOn()), "subcollar").isPresent());

        ForbiddenMagicResearchCatalog.Options noLust =
            new ForbiddenMagicResearchCatalog.Options(true, true, true, true, true, true);
        assertFalse(find(ForbiddenMagicResearchCatalog.definitions(noLust), "subcollar").isPresent());
    }

    @Test
    void emeraldTransmutationIsGated() {
        assertTrue(find(ForbiddenMagicResearchCatalog.definitions(allOn()), "transemerald").isPresent());

        ForbiddenMagicResearchCatalog.Options noEmerald =
            new ForbiddenMagicResearchCatalog.Options(true, true, false, false, true, true);
        assertFalse(find(ForbiddenMagicResearchCatalog.definitions(noEmerald), "transemerald").isPresent());
    }

    @Test
    void witchwoodLayoutMatchesOriginal() {
        List<ResearchDefinition> definitions = ForbiddenMagicResearchCatalog.definitions(allOn());
        ResearchDefinition rod = find(definitions, "rod_witchwood").orElseThrow();
        assertEquals(-3, rod.column());
        assertEquals(1, rod.row());
        assertEquals(3, rod.complexity());

        ResearchDefinition staff = find(definitions, "rod_witchwood_staff").orElseThrow();
        assertEquals(-3, staff.column());
        assertEquals(-1, staff.row());
        assertEquals(2, staff.complexity());
    }

    @Test
    void everyEntryHasStagesAndNonNegativeComplexity() {
        for (ResearchDefinition definition : ForbiddenMagicResearchCatalog.definitions(allOn())) {
            assertFalse(definition.stages().isEmpty(), definition.id() + " must have stages");
            assertTrue(definition.complexity() >= 0, definition.id() + " complexity");
        }
    }
}