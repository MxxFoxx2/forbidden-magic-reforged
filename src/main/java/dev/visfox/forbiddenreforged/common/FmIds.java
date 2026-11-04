package dev.visfox.forbiddenreforged.common;

import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Mod identity constants, usable from any layer without touching the composition root. */
public final class FmIds {
    public static final String MODID = "forbiddenmagic";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    private FmIds() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
