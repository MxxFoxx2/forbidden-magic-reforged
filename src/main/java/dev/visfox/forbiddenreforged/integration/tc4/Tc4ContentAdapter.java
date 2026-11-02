package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.Objects;
import net.minecraft.resources.Identifier;

/**
 * Resolves the semantic host-content keys used by core catalogs to TC4 registry IDs.
 */
public final class Tc4ContentAdapter {
    private static final String HOST_NAMESPACE = "host";
    private static final String TC4_NAMESPACE = "thaumcraft";

    private Tc4ContentAdapter() {
    }

    public static Identifier resolveItem(ContentId id) {
        return resolve(id, "item/");
    }

    public static Identifier resolveTag(ContentId id) {
        return resolve(id, "tag/");
    }

    private static Identifier resolve(ContentId id, String expectedKind) {
        Objects.requireNonNull(id, "id");
        if (!HOST_NAMESPACE.equals(id.namespace())) {
            return Identifier.fromNamespaceAndPath(id.namespace(), id.path());
        }
        if (!id.path().startsWith(expectedKind)) {
            throw new IllegalArgumentException(
                "Expected " + expectedKind + " host content reference, got " + id
            );
        }

        String hostPath = id.path().substring(expectedKind.length());
        if (hostPath.isBlank()) {
            throw new IllegalArgumentException("Empty host content reference: " + id);
        }
        return Identifier.fromNamespaceAndPath(TC4_NAMESPACE, hostPath);
    }
}
