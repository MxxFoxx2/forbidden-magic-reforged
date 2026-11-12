package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.wand.BlinkFocusCatalog;
import dev.visfox.forbiddenreforged.core.wand.BlinkFocusCatalog.Definition;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.aspect.AspectList;
import thaumcraft.wand.focus.FocusUpgradeType;

/** Materializes semantic Blink Focus upgrades as native TC4 upgrade types. */
public final class Tc4FocusUpgradeTypes {
    public static final FocusUpgradeType HELLFIRE = create(100, BlinkFocusCatalog.hellfire());
    public static final FocusUpgradeType PANDEMONIUM = create(101, BlinkFocusCatalog.pandemonium());

    private Tc4FocusUpgradeTypes() {
    }

    private static FocusUpgradeType create(int hostId, Definition definition) {
        AspectList costs = new AspectList();
        for (var cost : definition.visCosts()) {
            costs.add(Tc4AspectAdapter.resolveAspect(cost.aspect()), cost.amount());
        }
        ContentId texture = definition.texture();
        ResourceLocation hostTexture =
            ResourceLocation.fromNamespaceAndPath(texture.namespace(), texture.path());
        return new FocusUpgradeType(hostId, definition.name(), hostTexture, costs);
    }
}
