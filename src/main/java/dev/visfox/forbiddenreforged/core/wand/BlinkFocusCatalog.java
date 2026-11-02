package dev.visfox.forbiddenreforged.core.wand;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.wand.BlinkFocusRules.Upgrade;
import dev.visfox.forbiddenreforged.core.wand.BlinkFocusRules.VisCost;
import java.util.List;

/** Semantic definitions for add-on-specific Blink Focus upgrades. */
public final class BlinkFocusCatalog {
    public record Definition(
        Upgrade upgrade,
        String name,
        ContentId texture,
        List<VisCost> visCosts
    ) {
        public Definition {
            if (upgrade == null || name == null || name.isBlank() || texture == null) {
                throw new IllegalArgumentException("Blink Focus upgrade metadata is incomplete");
            }
            visCosts = List.copyOf(visCosts);
        }
    }

    private static final List<Definition> DEFINITIONS = List.of(
        new Definition(
            Upgrade.HELLFIRE,
            "hellfire",
            ContentId.parse("forbiddenmagic:textures/misc/hellfire.png"),
            List.of(new VisCost(ContentId.parse("forbiddenmagic:concept/infernus"), 1))
        ),
        new Definition(
            Upgrade.PANDEMONIUM,
            "pandemonium",
            ContentId.parse("forbiddenmagic:textures/misc/pandemonium.png"),
            List.of(new VisCost(ContentId.parse("thaumcraft:darkness"), 1))
        )
    );

    private BlinkFocusCatalog() {
    }

    public static List<Definition> definitions() {
        return DEFINITIONS;
    }

    public static Definition hellfire() {
        return DEFINITIONS.get(0);
    }

    public static Definition pandemonium() {
        return DEFINITIONS.get(1);
    }
}
