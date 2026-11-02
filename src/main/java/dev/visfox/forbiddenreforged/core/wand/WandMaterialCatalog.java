package dev.visfox.forbiddenreforged.core.wand;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.List;

/**
 * Semantic definitions for Forbidden Magic wand materials.
 *
 * The selected host remains responsible for translating these definitions to
 * its native rod/cap registry and update callback types.
 */
public final class WandMaterialCatalog {
    public enum RodUpdateKind {
        TAINTED,
        INFERNAL,
        PROFANE,
        WITCHWOOD_WAND,
        WITCHWOOD_STAFF,
        CREATIVE,
        NONE
    }

    public record RodDefinition(
        String tag,
        int capacity,
        ContentId item,
        int cost,
        RodUpdateKind update,
        boolean creative,
        boolean staff,
        boolean sceptre,
        ContentId texture
    ) {
    }

    public record CapDefinition(
        String tag,
        float multiplier,
        List<ContentId> aspects,
        float discount,
        ContentId item,
        int cost,
        ContentId texture
    ) {
        public CapDefinition {
            aspects = List.copyOf(aspects);
        }
    }

    private static final String MOD = "forbiddenmagic";

    private WandMaterialCatalog() {
    }

    public static List<RodDefinition> rods(boolean arsMagicaLoaded) {
        var rods = new java.util.ArrayList<RodDefinition>();
        rods.add(rod("tainted", 150, "tainted_rod", 12, RodUpdateKind.TAINTED,
            false, false, false, "textures/model/wand_rod_tainted.png"));
        rods.add(rod("infernal", 150, "infernal_rod", 12, RodUpdateKind.INFERNAL,
            false, false, false, "textures/model/wand_rod_infernal.png"));
        rods.add(rod("profane", 50, "profane_rod", 12, RodUpdateKind.PROFANE,
            false, false, false, "textures/model/wand_rod_profane.png"));
        if (arsMagicaLoaded) {
            rods.add(rod("witchwood", 100, "witchwood_rod", 12, RodUpdateKind.WITCHWOOD_WAND,
                false, false, false, "textures/model/wand_rod_witchwood.png"));
            rods.add(rod("witchwood", 50, "witchwood_staff_rod", 24, RodUpdateKind.WITCHWOOD_STAFF,
                false, true, false, "textures/model/wand_rod_witchwood_staff.png"));
        }
        rods.add(new RodDefinition("profaned", 50, id("minecraft", "bedrock"), 1000,
            RodUpdateKind.NONE, false, false, false, id(MOD, "textures/model/wand_rod_profaned.png")));
        rods.add(rod("equivalent", 100, "equivalent_rod", 12, RodUpdateKind.NONE,
            false, false, false, "textures/model/wand_rod_equivalent.png"));
        rods.add(new RodDefinition("neutronium", 9001, id("minecraft", "bedrock"), 1000,
            RodUpdateKind.CREATIVE, true, false, false, id(MOD, "textures/model/wand_rod_neutronium.png")));
        rods.add(new RodDefinition("neutronium", 9002, id("minecraft", "bedrock"), 1000,
            RodUpdateKind.CREATIVE, true, true, false, id(MOD, "textures/model/wand_rod_neutronium.png")));
        return List.copyOf(rods);
    }

    public static List<CapDefinition> caps() {
        return List.of(
            new CapDefinition("orichalcum", 0.0F, List.of(), 0.0F,
                id("minecraft", "command_block"), 1000,
                id(MOD, "textures/model/wand_cap_orichalcum.png")),
            new CapDefinition("alchemical", 0.9F, List.of(id("thaumcraft", "aqua")), 0.8F,
                id(MOD, "alchemical_cap"), 7,
                id(MOD, "textures/model/wand_cap_alchemical.png"))
        );
    }

    private static RodDefinition rod(
        String tag, int capacity, String item, int cost, RodUpdateKind update,
        boolean creative, boolean staff, boolean sceptre, String texture
    ) {
        return new RodDefinition(tag, capacity, id(MOD, item), cost, update,
            creative, staff, sceptre, id(MOD, texture));
    }

    private static ContentId id(String namespace, String path) {
        return new ContentId(namespace, path);
    }
}
