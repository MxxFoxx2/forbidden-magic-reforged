package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.FmWandContractAccess;
import dev.visfox.forbiddenreforged.core.wand.WandBehaviors;
import dev.visfox.forbiddenreforged.core.wand.WandMaterialCatalog.RodUpdateKind;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import thaumcraft.taint.TaintBiomes;
import thaumcraft.wand.RodUpdate;

/** Maps semantic rod update kinds to TC4 callbacks; gameplay rules remain in core. */
public final class Tc4WandUpdateAdapter {
    private Tc4WandUpdateAdapter() {
    }

    public static RodUpdate create(RodUpdateKind kind) {
        return switch (kind) {
            case TAINTED -> new TaintedWandUpdate();
            case INFERNAL -> new InfernalWandUpdate();
            case PROFANE -> new ProfaneWandUpdate();
            case WITCHWOOD_WAND -> new Tc4ArsMagicaManaUpdate(false);
            case WITCHWOOD_STAFF -> new Tc4ArsMagicaManaUpdate(true);
            case CREATIVE -> new CreativeWandUpdate();
            case NONE -> null;
        };
    }

    private static final class CreativeWandUpdate implements RodUpdate {
        @Override
        public void onUpdate(ItemStack stack, Player player) {
            WandBehaviors.creative(stack, player, HostAdapterRegistry.requireWandVis());
        }
    }

    /** Refills depleted primals while the holder stands in a tainted area. */
    private static final class TaintedWandUpdate implements RodUpdate {
        @Override
        public void onUpdate(ItemStack stack, Player player) {
            boolean tainted = TaintBiomes.isTainted(player.level(), player.blockPosition());
            WandBehaviors.tainted(stack, player, HostAdapterRegistry.requireWandVis(), tainted);
        }
    }

    /** Refills primals in the Nether and fire anywhere; extinguishes fire and wither. */
    private static final class InfernalWandUpdate implements RodUpdate {
        @Override
        public void onUpdate(ItemStack stack, Player player) {
            WandBehaviors.infernal(stack, player, HostAdapterRegistry.requireWandVis());
        }
    }

    /** Runs the finite Profane rod contract through typed wand/warp capabilities. */
    private static final class ProfaneWandUpdate implements RodUpdate {
        @Override
        public void onUpdate(ItemStack stack, Player player) {
            WandBehaviors.profane(
                stack,
                player,
                HostAdapterRegistry.requireWandVis(),
                HostAdapterRegistry.requireWandParts(),
                FmWandContractAccess.INSTANCE,
                HostAdapterRegistry.requireWarp()
            );
        }
    }
}
