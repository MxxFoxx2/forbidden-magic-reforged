package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.core.aspect.ForbiddenMagicAspectCatalog;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4AspectAdapter;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;

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
        try {
            HostAdapterRegistry.require().registerAspects(
                ForbiddenMagicAspectCatalog.definitions(!FmConfig.lustDisabled())
            );
            Map<ContentId, Aspect> registered = Tc4AspectAdapter.registeredAspects();
            LUST = registered.get(ForbiddenMagicAspectCatalog.LUXURIA);
            NETHER = registered.get(ForbiddenMagicAspectCatalog.INFERNUS);
            PRIDE = registered.get(ForbiddenMagicAspectCatalog.SUPERBIA);
            GLUTTONY = registered.get(ForbiddenMagicAspectCatalog.GULA);
            ENVY = registered.get(ForbiddenMagicAspectCatalog.INVIDIA);
            SLOTH = registered.get(ForbiddenMagicAspectCatalog.DESIDIA);
            WRATH = registered.get(ForbiddenMagicAspectCatalog.IRA);
        } catch (NoSuchMethodError | NoClassDefFoundError absent) {
            throw new IllegalStateException("Forbidden Magic requires Thaumcraft 4: Reforged 1.1.2 or newer with the add-on aspect API", absent);
        }
    }

    public static final int BUILTIN_ASPECT_COUNT = 48;

    public static int count() {
        return Aspect.values().length;
    }
}
