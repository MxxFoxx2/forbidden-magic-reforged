package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.FmAspects;

import java.util.ArrayList;
import java.util.List;
import thaumcraft.aspect.Aspect;

public final class Tc4AspectDiagnostics {
    private Tc4AspectDiagnostics() {
    }

    public static List<String> registryLines() {
        List<String> lines = new ArrayList<>();
        for (Aspect aspect : Aspect.values()) {
            lines.add(line(aspect, aspect.ordinal() < FmAspects.BUILTIN_ASPECT_COUNT ? "builtin" : "addon"));
        }
        return List.copyOf(lines);
    }

    public static List<String> addonLines() {
        return registryLines().stream().filter(line -> line.contains("kind=addon")).toList();
    }

    private static String line(Aspect aspect, String kind) {
        Aspect[] components = aspect.getComponents();
        String componentTags = "-";
        if (components != null && components.length > 0) {
            List<String> tags = new ArrayList<>();
            for (Aspect component : components) {
                tags.add(component.getTag());
            }
            componentTags = String.join(",", tags);
        }
        return "ordinal=" + aspect.ordinal()
            + " tag=" + aspect.getTag()
            + " name=" + aspect.getName()
            + " kind=" + kind
            + " components=" + componentTags;
    }
}
