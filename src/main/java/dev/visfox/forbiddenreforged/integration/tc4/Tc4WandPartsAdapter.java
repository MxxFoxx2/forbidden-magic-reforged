package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.wand.WandPartsAccess;
import dev.visfox.forbiddenreforged.core.wand.WandMaterialCatalog;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import net.minecraft.world.item.ItemStack;
import thaumcraft.wand.WandKeys;
import thaumcraft.wand.WandParts;
import thaumcraft.wand.Wands;

/** Writes canonical semantic rod identifiers into the native TC4 WandParts value. */
public final class Tc4WandPartsAdapter implements WandPartsAccess {
    public Tc4WandPartsAdapter() {
    }

    @Override
    public void setRod(ItemStack wand, ContentId rod) {
        String rodTag = WandMaterialCatalog.rodTag(rod);
        WandParts parts = Wands.getParts(wand);
        WandKeys.PARTS.set(wand, new WandParts(parts.cap(), rodTag, parts.sceptre()));
    }
}
