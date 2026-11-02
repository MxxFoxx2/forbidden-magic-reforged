package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.core.aspect.ForbiddenMagicAspectCatalog;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4AspectAdapter;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;

/**
 * Semantic identifiers for Forbidden Magic aspects.
 *
 * The Aspect fields are a temporary compatibility bridge for legacy 26.1.2
 * consumers. New shared code must use the ContentId constants instead.
 */
public final class FmAspects {
    public static final ContentId LUST_ID = ForbiddenMagicAspectCatalog.LUXURIA;
    public static final ContentId NETHER_ID = ForbiddenMagicAspectCatalog.INFERNUS;
    public static final ContentId PRIDE_ID = ForbiddenMagicAspectCatalog.SUPERBIA;
    public static final ContentId GLUTTONY_ID = ForbiddenMagicAspectCatalog.GULA;
    public static final ContentId ENVY_ID = ForbiddenMagicAspectCatalog.INVIDIA;
    public static final ContentId SLOTH_ID = ForbiddenMagicAspectCatalog.DESIDIA;
    public static final ContentId WRATH_ID = ForbiddenMagicAspectCatalog.IRA;

    /** @deprecated Use the corresponding semantic ID in new code. */
    @Deprecated public static @Nullable Aspect LUST;
    /** @deprecated Use the corresponding semantic ID in new code. */
    @Deprecated public static @Nullable Aspect NETHER;
    /** @deprecated Use the corresponding semantic ID in new code. */
    @Deprecated public static @Nullable Aspect PRIDE;
    /** @deprecated Use the corresponding semantic ID in new code. */
    @Deprecated public static @Nullable Aspect GLUTTONY;
    /** @deprecated Use the corresponding semantic ID in new code. */
    @Deprecated public static @Nullable Aspect ENVY;
    /** @deprecated Use the corresponding semantic ID in new code. */
    @Deprecated public static @Nullable Aspect SLOTH;
    /** @deprecated Use the corresponding semantic ID in new code. */
    @Deprecated public static @Nullable Aspect WRATH;

    public static final int BUILTIN_ASPECT_COUNT = 48;

    private FmAspects() {
    }

    public static boolean available() {
        return NETHER != null;
    }

    public static void init() {
        HostAdapterRegistry.require().registerAspects(
            ForbiddenMagicAspectCatalog.definitions(!FmConfig.lustDisabled())
        );
        LUST = FmConfig.lustDisabled() ? null : Tc4AspectAdapter.resolveAspect(LUST_ID);
        NETHER = Tc4AspectAdapter.resolveAspect(NETHER_ID);
        PRIDE = Tc4AspectAdapter.resolveAspect(PRIDE_ID);
        GLUTTONY = Tc4AspectAdapter.resolveAspect(GLUTTONY_ID);
        ENVY = Tc4AspectAdapter.resolveAspect(ENVY_ID);
        SLOTH = Tc4AspectAdapter.resolveAspect(SLOTH_ID);
        WRATH = Tc4AspectAdapter.resolveAspect(WRATH_ID);
    }

    public static int count() {
        return Aspect.values().length;
    }
}