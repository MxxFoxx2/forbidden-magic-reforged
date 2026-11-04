package dev.visfox.forbiddenreforged.common;

import dev.visfox.forbiddenreforged.core.aspect.ForbiddenMagicAspectCatalog;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;

/**
 * Stable, host-independent identifiers for Forbidden Magic's custom aspects.
 *
 * This class deliberately contains no host Aspect objects. Host integrations
 * resolve these IDs to their native aspect representation at the boundary.
 */
public final class FmAspects {
    public static final ContentId LUST = ForbiddenMagicAspectCatalog.LUXURIA;
    public static final ContentId NETHER = ForbiddenMagicAspectCatalog.INFERNUS;
    public static final ContentId PRIDE = ForbiddenMagicAspectCatalog.SUPERBIA;
    public static final ContentId GLUTTONY = ForbiddenMagicAspectCatalog.GULA;
    public static final ContentId ENVY = ForbiddenMagicAspectCatalog.INVIDIA;
    public static final ContentId SLOTH = ForbiddenMagicAspectCatalog.DESIDIA;
    public static final ContentId WRATH = ForbiddenMagicAspectCatalog.IRA;

    private static volatile boolean initialized;

    private FmAspects() {
    }

    /**
     * Indicates that the selected host successfully registered the aspect catalog.
     */
    public static boolean available() {
        return initialized;
    }

    public static void init() {
        HostAdapterRegistry.require().registerAspects(
            ForbiddenMagicAspectCatalog.definitions(!FmConfig.lustDisabled())
        );
        initialized = true;
    }
}
