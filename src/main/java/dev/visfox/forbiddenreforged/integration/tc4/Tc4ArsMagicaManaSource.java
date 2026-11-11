package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.wand.ManaSource;
import java.lang.reflect.Method;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Reads and spends Ars Magica: Legacy mana for the conversion wands. The API is reached
 * reflectively so the add-on neither compiles nor loads against a mana mod it does not
 * ship; any failure reports as an unavailable source instead of throwing.
 */
public final class Tc4ArsMagicaManaSource implements ManaSource {
    private static final String API_CLASS = "at.minecraftschurli.mods.arsmagicalegacy.api.ArsMagicaApi";
    private static final String HELPER_CLASS = "at.minecraftschurli.mods.arsmagicalegacy.api.magic.ManaHelper";

    private boolean resolved;
    private Object helper;
    private Method getMana;
    private Method decreaseMana;

    public static final Tc4ArsMagicaManaSource INSTANCE = new Tc4ArsMagicaManaSource();

    private Tc4ArsMagicaManaSource() {
    }

    @Override
    public boolean available() {
        return resolve() != null;
    }

    @Override
    public double mana(Player player) {
        Object manaHelper = resolve();
        if (manaHelper == null) {
            return 0;
        }
        try {
            return ((Number) getMana.invoke(manaHelper, player)).doubleValue();
        } catch (ReflectiveOperationException | ClassCastException failure) {
            return 0;
        }
    }

    @Override
    public boolean spend(Player player, double amount) {
        Object manaHelper = resolve();
        if (manaHelper == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(decreaseMana.invoke(manaHelper, player, amount));
        } catch (ReflectiveOperationException | ClassCastException failure) {
            return false;
        }
    }

    private Object resolve() {
        if (resolved) {
            return helper;
        }
        resolved = true;
        try {
            // Resolve inside the mod's own classloader: ArsMagicaApi is loaded from the
            // Ars Magica: Legacy jar, not from the add-on's loader.
            ClassLoader loader = Class.forName("at.minecraftschurli.mods.arsmagicalegacy.api.ArsMagicaApi").getClassLoader();
            Class<?> api = Class.forName(API_CLASS, true, loader);
            Object instance = api.getMethod("manaHelper").invoke(null);
            Class<?> helperType = Class.forName(HELPER_CLASS, true, loader);
            getMana = helperType.getMethod("getMana", LivingEntity.class);
            decreaseMana = helperType.getMethod("decreaseMana", LivingEntity.class, double.class);
            helper = instance;
        } catch (ReflectiveOperationException | LinkageError failure) {
            helper = null;
        }
        return helper;
    }
}
