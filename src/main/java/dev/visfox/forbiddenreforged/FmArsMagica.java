package dev.visfox.forbiddenreforged;

import net.neoforged.fml.ModList;

/**
 * Detection of an Ars Magica install. The original cross-wand integration targeted AM2
 * (`arsmagica2`); the modern port of that mod is Ars Magica: Legacy
 * (`arsmagicalegacy`), and either satisfies the original gate, so items and research
 * register for both without duplicating.
 */
public final class FmArsMagica {
    public static final String LEGACY_MODID = "arsmagicalegacy";
    public static final String CLASSIC_MODID = "arsmagica2";

    private FmArsMagica() {
    }

    public static boolean loaded() {
        return ModList.get().isLoaded(CLASSIC_MODID) || ModList.get().isLoaded(LEGACY_MODID);
    }

    public static boolean modernApiAvailable() {
        return ModList.get().isLoaded(LEGACY_MODID);
    }
}
