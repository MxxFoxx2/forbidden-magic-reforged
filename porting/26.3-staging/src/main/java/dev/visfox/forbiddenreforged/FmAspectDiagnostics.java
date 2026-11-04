package dev.visfox.forbiddenreforged;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;

public final class FmAspectDiagnostics {
    private FmAspectDiagnostics() {
    }

    public static List<String> registryLines() {
        List<String> lines = new ArrayList<>();
        for (Aspect aspect : Aspect.values()) {
            lines.add(line(aspect, aspect.ordinal() < Aspect.builtinCount() ? "builtin" : "addon"));
        }
        return List.copyOf(lines);
    }

    public static List<String> addonLines() {
        List<String> lines = new ArrayList<>();
        addAddonLine(lines, FmAspects.LUST);
        addAddonLine(lines, FmAspects.NETHER);
        addAddonLine(lines, FmAspects.PRIDE);
        addAddonLine(lines, FmAspects.GLUTTONY);
        addAddonLine(lines, FmAspects.ENVY);
        addAddonLine(lines, FmAspects.SLOTH);
        addAddonLine(lines, FmAspects.WRATH);
        return List.copyOf(lines);
    }

    private static void addAddonLine(List<String> lines, @Nullable Aspect aspect) {
        if (aspect != null) {
            lines.add(line(aspect, "addon"));
        }
    }

    private static String line(Aspect aspect, String kind) {
        @Nullable Aspect[] components = aspect.getComponents();
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
