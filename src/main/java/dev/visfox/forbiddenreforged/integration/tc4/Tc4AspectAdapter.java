package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.aspect.AspectDefinition;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import thaumcraft.aspect.AddonAspects;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;

public final class Tc4AspectAdapter {
    private static volatile Map<ContentId, Aspect> registeredAspects = Map.of();

    private Tc4AspectAdapter() {
    }

    public static void registerAspects(List<AspectDefinition> definitions) {
        Map<ContentId, Aspect> registered = new LinkedHashMap<>();
        AddonAspects.addRegistrar(api -> {
            for (AspectDefinition definition : definitions) {
                Aspect[] components = definition.components().stream()
                    .map(Tc4AspectAdapter::resolveAspect)
                    .toArray(Aspect[]::new);
                Aspect aspect = api.register(
                    definition.id().path(),
                    definition.color(),
                    components,
                    blendConstant(definition.blendMode()),
                    ResourceLocation.fromNamespaceAndPath(
                        definition.texture().namespace(),
                        definition.texture().path()
                    )
                );
                registered.put(definition.id(), aspect);
            }
        });
        AddonAspects.init();
        registeredAspects = Map.copyOf(registered);
    }


    public static boolean isRegistered(ContentId id) {
        return registeredAspects.containsKey(id);
    }

    public static ContentId toContentId(Aspect aspect) {
        for (Map.Entry<ContentId, Aspect> entry : registeredAspects.entrySet()) {
            if (entry.getValue().equals(aspect)) {
                return entry.getKey();
            }
        }
        return new ContentId("thaumcraft", aspect.getTag());
    }

    public static net.minecraft.network.chat.Component description(ContentId id) {
        return resolveAspect(id).getDescription();
    }

    public static Aspect resolveAspect(ContentId id) {
        Aspect addonAspect = registeredAspects.get(id);
        if (addonAspect == null && "thaumcraft".equals(id.namespace())) {
            Aspect hostAspect = Aspect.getAspect(id.path());
            if (hostAspect != null) {
                return hostAspect;
            }
        }
        if (addonAspect != null) {
            return addonAspect;
        }
        if (!"forbiddenmagic".equals(id.namespace()) || !id.path().startsWith("concept/")) {
            throw new IllegalArgumentException("Aspect is not registered by the TC4 adapter: " + id);
        }
        return switch (id.path()) {
            case "concept/air" -> Aspect.AIR;
            case "concept/earth" -> Aspect.EARTH;
            case "concept/fire" -> Aspect.FIRE;
            case "concept/water" -> Aspect.WATER;
            case "concept/order" -> Aspect.ORDER;
            case "concept/entropy" -> Aspect.ENTROPY;
            case "concept/void" -> Aspect.VOID;
            case "concept/light" -> Aspect.LIGHT;
            case "concept/weather" -> Aspect.WEATHER;
            case "concept/motion" -> Aspect.MOTION;
            case "concept/cold" -> Aspect.COLD;
            case "concept/crystal" -> Aspect.CRYSTAL;
            case "concept/life" -> Aspect.LIFE;
            case "concept/poison" -> Aspect.POISON;
            case "concept/energy" -> Aspect.ENERGY;
            case "concept/exchange" -> Aspect.EXCHANGE;
            case "concept/metal" -> Aspect.METAL;
            case "concept/death" -> Aspect.DEATH;
            case "concept/flight" -> Aspect.FLIGHT;
            case "concept/darkness" -> Aspect.DARKNESS;
            case "concept/soul" -> Aspect.SOUL;
            case "concept/heal" -> Aspect.HEAL;
            case "concept/travel" -> Aspect.TRAVEL;
            case "concept/eldritch" -> Aspect.ELDRITCH;
            case "concept/magic" -> Aspect.MAGIC;
            case "concept/aura" -> Aspect.AURA;
            case "concept/taint" -> Aspect.TAINT;
            case "concept/slime" -> Aspect.SLIME;
            case "concept/plant" -> Aspect.PLANT;
            case "concept/tree" -> Aspect.TREE;
            case "concept/beast" -> Aspect.BEAST;
            case "concept/flesh" -> Aspect.FLESH;
            case "concept/undead" -> Aspect.UNDEAD;
            case "concept/mind" -> Aspect.MIND;
            case "concept/senses" -> Aspect.SENSES;
            case "concept/man" -> Aspect.MAN;
            case "concept/crop" -> Aspect.CROP;
            case "concept/mine" -> Aspect.MINE;
            case "concept/tool" -> Aspect.TOOL;
            case "concept/harvest" -> Aspect.HARVEST;
            case "concept/weapon" -> Aspect.WEAPON;
            case "concept/armor" -> Aspect.ARMOR;
            case "concept/hunger" -> Aspect.HUNGER;
            case "concept/greed" -> Aspect.GREED;
            case "concept/craft" -> Aspect.CRAFT;
            case "concept/cloth" -> Aspect.CLOTH;
            case "concept/mechanism" -> Aspect.MECHANISM;
            case "concept/trap" -> Aspect.TRAP;
            default -> throw new IllegalArgumentException("Unsupported TC4 aspect concept: " + id);
        };
    }

    public static AspectList toAspectList(AspectDefinition.Cost cost) {
        AspectList result = new AspectList();
        for (AspectDefinition.Amount amount : cost.amounts()) {
            result.add(resolveAspect(amount.aspect()), amount.amount());
        }
        return result;
    }

    private static int blendConstant(AspectDefinition.BlendMode blendMode) {
        return switch (blendMode) {
            case ADDITIVE -> 1;
            case ONE_MINUS_SOURCE_COLOR -> 771;
        };
    }
}
