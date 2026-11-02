package dev.visfox.forbiddenreforged.core.research;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.Objects;

public record ResearchCategoryDefinition(
    ContentId id,
    ContentId icon,
    ContentId background
) {
    public ResearchCategoryDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(icon, "icon");
        Objects.requireNonNull(background, "background");
    }
}
