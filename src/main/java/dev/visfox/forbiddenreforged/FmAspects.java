package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.core.aspect.ForbiddenMagicAspectCatalog;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;

/**
 * Compatibility facade for legacy gameplay classes that still consume TC4 Aspect instances.
 * Aspect definitions and registration are owned by the shared core catalog and selected host adapter.
 */
public final class FmAspects {
    public static @Nullable Aspect LUST;
    public static @Nullable Aspect NETHER;
    public static @Nullable Aspect PRIDE;
    public static @Nullable Aspect GLUTTONY;
    public static @Nullable Aspect ENVY;
    public static @Nullable Aspect SLOTH;
    public static @Nullable Aspect WRATH;

    private FmAspects() {
    }

    public static boolean available() {
        return NETHER != null;
    }

    public static void init() {
        HostAdapterRegistry.require().registerAspects(
            ForbiddenMagicAspectCatalog.definitions(!FmConfig.lustDisabled())
        );
        // Older TC4 gameplay code still consumes Aspect singletons directly. Keep this narrow
        // compatibility facade while catalogs and new consumers use semantic ContentId values.
        LUST = FmConfig.lustDisabled() ? null : Aspect.getAspect("luxuria");
        NETHER = Aspect.getAspect("infernus");
        PRIDE = Aspect.getAspect("superbia");
        GLUTTONY = Aspect.getAspect("gula");
        ENVY = Aspect.getAspect("invidia");
        SLOTH = Aspect.getAspect("desidia");
        WRATH = Aspect.getAspect("ira");
        if (NETHER == null || PRIDE == null || GLUTTONY == null || ENVY == null || SLOTH == null || WRATH == null
            || (!FmConfig.lustDisabled() && LUST == null)) {
            throw new IllegalStateException("Thaumcraft did not register the complete Forbidden Magic aspect catalog");
        }
    }

    public static final int BUILTIN_ASPECT_COUNT = 48;

    public static int count() {
        return Aspect.values().length;
    }
}
