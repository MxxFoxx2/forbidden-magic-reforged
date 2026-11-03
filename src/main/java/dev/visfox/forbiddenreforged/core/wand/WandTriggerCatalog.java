package dev.visfox.forbiddenreforged.core.wand;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.List;

/** Semantic trigger definitions consumed by host-specific wand trigger adapters. */
public final class WandTriggerCatalog {
    public record Definition(
        ContentId id,
        List<ContentId> blocks,
        ContentId research,
        ContentId aspect,
        int visCost
    ) {
        public Definition {
            blocks = List.copyOf(blocks);
        }
    }

    private WandTriggerCatalog() {
    }

    public static Definition hellfire() {
        return new Definition(
            ContentId.parse("forbiddenmagic:wand_trigger/hellfire"),
            List.of(ContentId.parse("minecraft:obsidian"), ContentId.parse("minecraft:netherrack")),
            ContentId.parse("forbiddenmagic:research/hellfire"),
            ContentId.parse("forbiddenmagic:concept/fire"),
            2
        );
    }
}
