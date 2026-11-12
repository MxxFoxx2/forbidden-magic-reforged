package dev.visfox.forbiddenreforged.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContentIdTest {
    @Test
    void parseSplitsNamespaceAndPath() {
        ContentId id = ContentId.parse("forbiddenmagic:concept/fire");
        assertEquals("forbiddenmagic", id.namespace());
        assertEquals("concept/fire", id.path());
        assertEquals("forbiddenmagic:concept/fire", id.toString());
    }

    @Test
    void parseRejectsMalformedIdentifiers() {
        assertThrows(IllegalArgumentException.class, () -> ContentId.parse("no-separator"));
        assertThrows(IllegalArgumentException.class, () -> ContentId.parse(":missing/namespace"));
        assertThrows(IllegalArgumentException.class, () -> ContentId.parse("missing:path:"));
        assertThrows(IllegalArgumentException.class, () -> ContentId.parse("two:colons:here"));
    }

    @Test
    void constructorRejectsInvalidCharactersAndEdgeSlashes() {
        assertThrows(IllegalArgumentException.class, () -> new ContentId("UPPER", "path"));
        assertThrows(IllegalArgumentException.class, () -> new ContentId("ns", "/leading"));
        assertThrows(IllegalArgumentException.class, () -> new ContentId("ns", "trailing/"));
    }

    @Test
    void equalityIsValueBased() {
        assertEquals(ContentId.parse("forbiddenmagic:wrath_shard"),
            new ContentId("forbiddenmagic", "wrath_shard"));
    }
}