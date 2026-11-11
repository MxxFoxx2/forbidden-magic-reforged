package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.wand.ManaSource;
import net.minecraft.world.entity.player.Player;

/** Used when no mana mod is installed; conversion rods stay registered but inert. */
public final class Tc4NoManaSource implements ManaSource {
    public static final Tc4NoManaSource INSTANCE = new Tc4NoManaSource();

    private Tc4NoManaSource() {
    }

    @Override
    public boolean available() {
        return false;
    }

    @Override
    public double mana(Player player) {
        return 0;
    }

    @Override
    public boolean spend(Player player, double amount) {
        return false;
    }
}
