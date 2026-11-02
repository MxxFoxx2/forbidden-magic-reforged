package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.wand.WandRodKind;
import net.minecraft.world.item.ItemStack;
import thaumcraft.wand.Wands;

public final class Tc4WandRuntimeAdapter {
    private Tc4WandRuntimeAdapter() {
    }

    public static boolean isWand(ItemStack wand) {
        return Wands.isWand(wand);
    }

    public static WandRodKind rodKind(ItemStack wand) {
        if (!Wands.isWand(wand)) {
            return WandRodKind.NONE;
        }
        String tag = Wands.getRod(wand).tag();
        if (tag.startsWith("neutronium")) {
            return WandRodKind.NEUTRONIUM;
        }
        if (tag.startsWith("profane")) {
            return WandRodKind.PROFANE;
        }
        return switch (tag) {
            case "tainted" -> WandRodKind.TAINTED;
            case "infernal" -> WandRodKind.INFERNAL;
            default -> WandRodKind.OTHER;
        };
    }

    public static int focusTreasure(ItemStack wand) {
        return Wands.getFocusTreasure(wand);
    }

    public static int focusPotency(ItemStack wand) {
        return Wands.getFocusPotency(wand);
    }

    public static ItemStack focusItem(ItemStack wand) {
        return Wands.getFocusItem(wand);
    }

    public static int focusEnlarge(ItemStack wand) {
        return Wands.getFocusEnlarge(wand);
    }

}
