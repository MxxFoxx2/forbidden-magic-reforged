package dev.visfox.forbiddenreforged.core.wand;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.List;

/**
 * Semantic definitions for the wands exposed by the add-on's creative tab.
 *
 * Hosts resolve the cap and rod identifiers to their native registry objects.
 * A positive visPerPrimal value is applied through the host's semantic wand
 * Vis API after the native wand has been created.
 */
public final class CreativeWandCatalog {
    public record Definition(ContentId cap, ContentId rod, int visPerPrimal) {
        public Definition {
            if (cap == null || rod == null) {
                throw new IllegalArgumentException("Creative wand cap and rod identifiers are required");
            }
            if (visPerPrimal < 0) {
                throw new IllegalArgumentException("Creative wand Vis amount cannot be negative");
            }
        }
    }

    private static final Definition PROFANE = definition("iron", "profane", 5000);

    private static final List<Definition> DEFINITIONS = List.of(
        definition("thaumium", "tainted", 0),
        definition("thaumium", "infernal", 0),
        definition("void", "equivalent", 0),
        definition("orichalcum", "neutronium", 0),
        definition("orichalcum", "neutronium_staff", 0),
        PROFANE
    );

    private CreativeWandCatalog() {
    }

    public static List<Definition> definitions() {
        return DEFINITIONS;
    }

    public static Definition profane() {
        return PROFANE;
    }

    private static Definition definition(String cap, String rod, int visPerPrimal) {
        return new Definition(
            WandMaterialCatalog.capId(cap),
            WandMaterialCatalog.rodId(rod),
            visPerPrimal
        );
    }
}
