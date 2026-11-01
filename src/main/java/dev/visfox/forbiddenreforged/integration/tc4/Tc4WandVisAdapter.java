package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.wand.WandVisAccess;
import java.util.List;
import thaumcraft.aspect.Aspect;
import net.minecraft.world.item.ItemStack;
import thaumcraft.wand.Wands;

public final class Tc4WandVisAdapter implements WandVisAccess {
    @Override
    public List<ContentId> primalAspects() {
        return Aspect.getPrimalAspects().stream()
            .map(Tc4AspectAdapter::toContentId)
            .map(Tc4WandVisAdapter::semanticAspectId)
            .toList();
    }

    private static ContentId semanticAspectId(ContentId aspect) {
        if (!"thaumcraft".equals(aspect.namespace())) {
            return aspect;
        }
        return switch (aspect.path()) {
            case "aer" -> concept("air");
            case "terra" -> concept("earth");
            case "ignis" -> concept("fire");
            case "aqua" -> concept("water");
            case "ordo" -> concept("order");
            case "perditio" -> concept("entropy");
            default -> aspect;
        };
    }

    private static ContentId concept(String name) {
        return new ContentId("forbiddenmagic", "concept/" + name);
    }

    @Override
    public int maximumVis(ItemStack wand) {
        return Wands.getMaxVis(wand);
    }

    @Override
    public int vis(ItemStack wand, ContentId aspect) {
        return Wands.getVis(wand, Tc4AspectAdapter.resolveAspect(aspect));
    }

    @Override
    public void addVis(ItemStack wand, ContentId aspect, int amount, boolean apply) {
        Wands.addVis(wand, Tc4AspectAdapter.resolveAspect(aspect), amount, apply);
    }

    @Override
    public void addRealVis(ItemStack wand, ContentId aspect, int amount, boolean apply) {
        Wands.addRealVis(wand, Tc4AspectAdapter.resolveAspect(aspect), amount, apply);
    }

    @Override
    public void fill(ItemStack wand) {
        Wands.fill(wand);
    }
}
