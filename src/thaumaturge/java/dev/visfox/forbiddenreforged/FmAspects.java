package dev.visfox.forbiddenreforged;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.resources.ResourceKey;

public final class FmAspects {
    public static final ResourceKey<IAspect> LUXURIA = key("luxuria");
    public static final ResourceKey<IAspect> INFERNUS = key("infernus");
    public static final ResourceKey<IAspect> SUPERBIA = key("superbia");
    public static final ResourceKey<IAspect> GULA = key("gula");
    public static final ResourceKey<IAspect> INVIDIA = key("invidia");
    public static final ResourceKey<IAspect> DESIDIA = key("desidia");
    public static final ResourceKey<IAspect> IRA = key("ira");

    private FmAspects() {
    }

    private static ResourceKey<IAspect> key(String path) {
        return ResourceKey.create(IAspect.REGISTRY_KEY, ForbiddenMagic.id(path));
    }
}
