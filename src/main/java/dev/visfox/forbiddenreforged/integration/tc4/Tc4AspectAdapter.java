package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.aspect.AspectDefinition;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import thaumcraft.aspect.AddonAspects;
import thaumcraft.aspect.Aspect;

/**
 * Thaumcraft 4: Reforged translation for the common Forbidden Magic aspect catalog.
 *
 * <p>All host registry objects and aspect constants stay inside this adapter. The catalog only
 * exposes semantic identifiers and values.
 */
public final class Tc4AspectAdapter {
    private Tc4AspectAdapter() {
    }

    public static Map<ContentId, Aspect> registerAspects(List<AspectDefinition> definitions) {
        Map<ContentId, Aspect> registered = new LinkedHashMap<>();
        AddonAspects.addRegistrar(api -> {
            for (AspectDefinition definition : definitions) {
                Aspect[] components = definition.components().stream()
                    .map(Tc4AspectAdapter::resolveConcept)
                    .toArray(Aspect[]::new);
                Aspect aspect = api.register(
                    definition.id().path(),
                    definition.color(),
                    components,
                    blendConstant(definition.blendMode()),
                    Identifier.fromNamespaceAndPath(
                        definition.texture().namespace(),
                        definition.texture().path()
                    )
                );
                registered.put(definition.id(), aspect);
            }
        });
        AddonAspects.init();
        return Map.copyOf(registered);
    }

    private static int blendConstant(AspectDefinition.BlendMode blendMode) {
        return switch (blendMode) {
            case ADDITIVE -> 1;
            case ONE_MINUS_SOURCE_COLOR -> 771;
        };
    }

    private static Aspect resolveConcept(ContentId concept) {
        if (!"forbiddenmagic".equals(concept.namespace())) {
            throw new IllegalArgumentException("Unknown semantic aspect namespace: " + concept);
        }
        return switch (concept.path()) {
            case "concept/flesh" -> Aspect.FLESH;
            case "concept/hunger" -> Aspect.HUNGER;
            case "concept/fire" -> Aspect.FIRE;
            case "concept/magic" -> Aspect.MAGIC;
            case "concept/flight" -> Aspect.FLIGHT;
            case "concept/void" -> Aspect.VOID;
            case "concept/senses" -> Aspect.SENSES;
            case "concept/trap" -> Aspect.TRAP;
            case "concept/soul" -> Aspect.SOUL;
            case "concept/weapon" -> Aspect.WEAPON;
            default -> throw new IllegalArgumentException("Unsupported semantic aspect concept: " + concept);
        };
    }
}
