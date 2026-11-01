package dev.visfox.forbiddenreforged.core.aspect;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.ArrayList;
import java.util.List;

/**
 * Source-of-truth definitions for Forbidden Magic's own thaumic aspects.
 *
 * <p>The component identifiers under {@code forbiddenmagic:concept/} are semantic concepts, not
 * host registry identifiers. Each adapter maps those concepts to its host's native aspects.
 */
public final class ForbiddenMagicAspectCatalog {
    public static final ContentId LUXURIA = id("luxuria");
    public static final ContentId INFERNUS = id("infernus");
    public static final ContentId SUPERBIA = id("superbia");
    public static final ContentId GULA = id("gula");
    public static final ContentId INVIDIA = id("invidia");
    public static final ContentId DESIDIA = id("desidia");
    public static final ContentId IRA = id("ira");

    private static final ContentId FLESH = concept("flesh");
    private static final ContentId HUNGER = concept("hunger");
    private static final ContentId FIRE = concept("fire");
    private static final ContentId MAGIC = concept("magic");
    private static final ContentId FLIGHT = concept("flight");
    private static final ContentId VOID = concept("void");
    private static final ContentId SENSES = concept("senses");
    private static final ContentId TRAP = concept("trap");
    private static final ContentId SOUL = concept("soul");
    private static final ContentId WEAPON = concept("weapon");

    private ForbiddenMagicAspectCatalog() {
    }

    public static List<AspectDefinition> definitions(boolean includeLuxuria) {
        List<AspectDefinition> definitions = new ArrayList<>();
        if (includeLuxuria) {
            definitions.add(aspect(LUXURIA, 0xFFC1CE, 1, FLESH, HUNGER));
        }
        definitions.add(aspect(INFERNUS, 0xFF0000, 771, FIRE, MAGIC));
        definitions.add(aspect(SUPERBIA, 0x9639FF, 1, FLIGHT, VOID));
        definitions.add(aspect(GULA, 0xD59C46, 1, HUNGER, VOID));
        definitions.add(aspect(INVIDIA, 0x00BA00, 1, SENSES, HUNGER));
        definitions.add(aspect(DESIDIA, 0x6E6E6E, 771, TRAP, SOUL));
        definitions.add(aspect(IRA, 0x870404, 771, WEAPON, FIRE));
        return List.copyOf(definitions);
    }

    private static AspectDefinition aspect(ContentId id, int color, int sortOrder, ContentId first, ContentId second) {
        return new AspectDefinition(id, color, sortOrder, List.of(first, second),
            new ContentId(id.namespace(), "textures/aspects/" + id.path() + ".png"));
    }

    private static ContentId concept(String name) {
        return new ContentId("forbiddenmagic", "concept/" + name);
    }

    private static ContentId id(String path) {
        return new ContentId("forbiddenmagic", path);
    }
}
