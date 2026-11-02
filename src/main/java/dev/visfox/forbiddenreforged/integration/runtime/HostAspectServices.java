package dev.visfox.forbiddenreforged.integration.runtime;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import net.minecraft.network.chat.Component;

/** Host-specific labels for semantic aspects displayed by shared gameplay. */
public interface HostAspectServices {
    Component aspectDescription(ContentId aspect);
}
