package dev.visfox.forbiddenreforged.integration.runtime;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.Map;

/** Host-owned entity-to-aspect mappings exposed using semantic identifiers. */
public interface HostEntityAspectServices {
    Map<ContentId, ContentId> aspectCatalog(boolean silverfishEmeralds);
}
