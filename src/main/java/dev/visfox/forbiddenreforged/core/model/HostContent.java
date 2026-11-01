package dev.visfox.forbiddenreforged.core.model;

import java.util.Objects;

/**
 * Creates semantic references to content supplied by the selected magic host.
 *
 * These identifiers are internal core keys, not Minecraft registry identifiers.
 * A host adapter must resolve them before passing them to the game or host API.
 */
public final class HostContent {
    private HostContent() {
    }

    public static ContentId item(String key) {
        return reference("item", key);
    }

    public static ContentId tag(String key) {
        return reference("tag", key);
    }

    private static ContentId reference(String kind, String key) {
        Objects.requireNonNull(key, "key");
        if (key.isBlank()) {
            throw new IllegalArgumentException("Host content key cannot be blank");
        }
        return new ContentId("host", kind + "/" + key);
    }
}
