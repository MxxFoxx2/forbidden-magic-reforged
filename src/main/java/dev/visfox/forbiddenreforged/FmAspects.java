package dev.visfox.forbiddenreforged;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.aspect.AddonAspects;
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
            AddonAspects.addRegistrar(api -> {
                if (!FmConfig.lustDisabled()) {
                    LUST = api.register("luxuria", 0xFFC1CE, new Aspect[]{Aspect.FLESH, Aspect.HUNGER}, 1, texture("luxuria"));
                }
                NETHER = api.register("infernus", 0xFF0000, new Aspect[]{Aspect.FIRE, Aspect.MAGIC}, 771, texture("infernus"));
                PRIDE = api.register("superbia", 0x9639FF, new Aspect[]{Aspect.FLIGHT, Aspect.VOID}, 1, texture("superbia"));
                GLUTTONY = api.register("gula", 0xD59C46, new Aspect[]{Aspect.HUNGER, Aspect.VOID}, 1, texture("gula"));
                ENVY = api.register("invidia", 0x00BA00, new Aspect[]{Aspect.SENSES, Aspect.HUNGER}, 1, texture("invidia"));
                SLOTH = api.register("desidia", 0x6E6E6E, new Aspect[]{Aspect.TRAP, Aspect.SOUL}, 771, texture("desidia"));
                WRATH = api.register("ira", 0x870404, new Aspect[]{Aspect.WEAPON, Aspect.FIRE}, 771, texture("ira"));
            });
        } catch (NoSuchMethodError | NoClassDefFoundError absent) {
            throw new IllegalStateException("Forbidden Magic requires Thaumcraft 4: Reforged 1.1.2 or newer with the add-on aspect API", absent);
        }
    }

    public static final int BUILTIN_ASPECT_COUNT = 48;

    public static int count() {
        return Aspect.values().length;
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ForbiddenMagic.MODID, "textures/aspects/" + name + ".png");
    }
}
