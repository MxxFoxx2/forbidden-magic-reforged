package dev.visfox.forbiddenreforged.core.wand;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.wand.BlinkFocusRules.Upgrade;
import dev.visfox.forbiddenreforged.core.wand.BlinkFocusRules.VisCost;
import java.util.List;

/** Semantic definitions for add-on-specific Blink Focus upgrades. */
public final class BlinkFocusCatalog {
    public record Presentation(
        String sortingPrefix,
        int color,
        ContentId focusTexture,
        ContentId ornamentTexture
    ) {
        public Presentation {
            if (sortingPrefix == null || focusTexture == null || ornamentTexture == null) {
                throw new IllegalArgumentException("Blink Focus presentation metadata is incomplete");
            }
        }
    }

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

    private static final Presentation PRESENTATION = new Presentation(
        "BL",
        0x909090,
        ContentId.parse("forbiddenmagic:item/blink_focus"),
        ContentId.parse("forbiddenmagic:item/blink_focus_ornament")
    );

    private static final List<Definition> DEFINITIONS = List.of(
        new Definition(
            Upgrade.HELLFIRE,
            "hellfire",
            ContentId.parse("forbiddenmagic:textures/misc/hellfire.png"),
            List.of(new VisCost(ContentId.parse("forbiddenmagic:infernus"), 1))
        ),
        new Definition(
            Upgrade.PANDEMONIUM,
            "pandemonium",
            ContentId.parse("forbiddenmagic:textures/misc/pandemonium.png"),
            List.of(new VisCost(ContentId.parse("forbiddenmagic:concept/darkness"), 1))
        )
    );

    private BlinkFocusCatalog() {
    }

    public static Presentation presentation() {
        return PRESENTATION;
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
