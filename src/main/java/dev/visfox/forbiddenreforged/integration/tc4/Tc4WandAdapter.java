package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.wand.CreativeWandCatalog;
import dev.visfox.forbiddenreforged.core.wand.WandVisAccess;
import dev.visfox.forbiddenreforged.core.wand.WandMaterialCatalog;
import dev.visfox.forbiddenreforged.core.wand.WandMaterialCatalog.RodDefinition;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import thaumcraft.aspect.Aspect;
import thaumcraft.wand.WandCap;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.WandRod;
import thaumcraft.wand.Wands;

/** Registers semantic wand materials in the pinned TC4 registry. */
public final class Tc4WandAdapter {
    private Tc4WandAdapter() {
    }

    public static void registerWands() {
        for (RodDefinition rod : WandMaterialCatalog.rods(isArsMagicaLoaded())) {
            WandRegistry.registerRod(
                rod.tag(),
                rod.capacity(),
                hostId(rod.item()),
                rod.cost(),
                Tc4WandUpdateAdapter.create(rod.update()),
                rod.creative(),
                rod.staff(),
                rod.sceptre(),
                hostId(rod.texture())
            );
        }
        for (var cap : WandMaterialCatalog.caps()) {
            List<Aspect> aspects = cap.aspects().stream()
                .map(Tc4AspectAdapter::resolveAspect)
                .toList();
            WandRegistry.registerCap(
                cap.tag(),
                cap.multiplier(),
                aspects,
                cap.discount(),
                hostId(cap.item()),
                cap.cost(),
                null,
                hostId(cap.texture())
            );
        }
    }

    public static void addCreativeWands(List<ItemStack> stacks) {
        WandVisAccess wandVis = null;
        for (CreativeWandCatalog.Definition definition : CreativeWandCatalog.definitions()) {
            WandCap cap = resolveCap(definition.cap());
            WandRod rod = resolveRod(definition.rod());
            if (cap == null || rod == null) {
                continue;
            }

            ItemStack wand = Wands.create(cap, rod, false);
            if (definition.visPerPrimal() > 0) {
                if (wandVis == null) {
                    wandVis = HostAdapterRegistry.requireWandVis();
                }
                for (ContentId aspect : wandVis.primalAspects()) {
                    wandVis.addVis(wand, aspect, definition.visPerPrimal(), true);
                }
            }
            stacks.add(wand);
        }
    }

    private static WandCap resolveCap(ContentId capId) {
        String tag = registryKey(capId, "wand_cap/");
        return switch (tag) {
            case "thaumium" -> WandRegistry.CAP_THAUMIUM;
            case "void" -> WandRegistry.CAP_VOID;
            case "iron" -> WandRegistry.CAP_IRON;
            default -> WandRegistry.findCap(tag);
        };
    }

    private static WandRod resolveRod(ContentId rodId) {
        return WandRegistry.findRod(registryKey(rodId, "wand_rod/"));
    }

    private static String registryKey(ContentId id, String prefix) {
        if (!"forbiddenmagic".equals(id.namespace()) || !id.path().startsWith(prefix)
            || id.path().length() == prefix.length()) {
            throw new IllegalArgumentException("Unsupported creative wand identifier: " + id);
        }
        return id.path().substring(prefix.length());
    }

    private static ResourceLocation hostId(ContentId id) {
        return ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path());
    }

    private static boolean isArsMagicaLoaded() {
        return ModList.get().isLoaded("arsmagicalegacy") || ModList.get().isLoaded("arsmagica2");
    }
}
