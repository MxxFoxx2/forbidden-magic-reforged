package dev.visfox.forbiddenreforged.core.wand;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.warp.WarpAccess;
import dev.visfox.forbiddenreforged.core.warp.WarpKind;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class WandBehaviors {
    private static final ContentId AIR = aspect("air");
    private static final ContentId EARTH = aspect("earth");
    private static final ContentId FIRE = aspect("fire");
    private static final ContentId WATER = aspect("water");
    private static final ContentId ORDER = aspect("order");
    private static final ContentId ENTROPY = aspect("entropy");
    private static final ContentId PROFANED_ROD = WandMaterialCatalog.rodId("profaned");
    private static final List<ContentId> NON_FIRE = List.of(ORDER, ENTROPY, AIR, EARTH, WATER);

    private WandBehaviors() {
    }

    public static void creative(ItemStack wand, Player player, WandVisAccess vis) {
        vis.fill(wand);
    }

    public static void tainted(ItemStack wand, Player player, WandVisAccess vis, boolean inTaintedArea) {
        if (player.tickCount % 100 != 0 || !inTaintedArea) {
            return;
        }
        int maximum = vis.maximumVis(wand);
        for (ContentId aspect : vis.primalAspects()) {
            if (vis.vis(wand, aspect) < maximum / 10) {
                vis.addVis(wand, aspect, 1, true);
            }
        }
    }

    public static void infernal(ItemStack wand, Player player, WandVisAccess vis) {
        if (player.tickCount % 100 == 0) {
            int maximum = vis.maximumVis(wand);
            if (player.level().dimension() == Level.NETHER) {
                for (ContentId aspect : NON_FIRE) {
                    if (vis.vis(wand, aspect) < maximum / 10) {
                        vis.addVis(wand, aspect, 1, true);
                    }
                }
            }
            if (vis.vis(wand, FIRE) < maximum / 5) {
                vis.addVis(wand, FIRE, 1, true);
            }
        }
        if (player.isOnFire()) {
            player.clearFire();
        }
        if (player.hasEffect(MobEffects.WITHER)) {
            player.removeEffect(MobEffects.WITHER);
        }
    }

    public static void profane(
        ItemStack wand,
        Player entity,
        WandVisAccess vis,
        WandPartsAccess parts,
        WandContractAccess contractAccess,
        WarpAccess warpAccess
    ) {
        if (!(entity instanceof ServerPlayer player) || player.tickCount % 20 != 0) {
            return;
        }
        Integer stored = contractAccess.remaining(wand);
        int remainingContract = stored == null ? beginContract(wand, contractAccess) : stored;
        if (remainingContract <= 0) {
            return;
        }
        int maximum = vis.maximumVis(wand);
        for (ContentId aspect : vis.primalAspects()) {
            if (remainingContract <= 0) {
                break;
            }
            int deficit = maximum - vis.vis(wand, aspect);
            if (deficit > 0) {
                int restore = Math.min(remainingContract, deficit);
                vis.addRealVis(wand, aspect, restore, true);
                remainingContract -= restore;
                if (player.getRandom().nextInt(2501) < restore) {
                    warpAccess.changeWarp(player, 1, WarpKind.STICKY);
                }
            }
        }
        if (remainingContract <= 0) {
            parts.setRod(wand, PROFANED_ROD);
            contractAccess.clear(wand);
            warpAccess.changeWarp(player, 1, WarpKind.STICKY);
            player.serverLevel().sendParticles(
                ParticleTypes.EXPLOSION,
                player.getX(),
                player.getY() + player.getBbHeight() / 2.0,
                player.getZ(),
                1,
                0.0,
                0.0,
                0.0,
                0.0
            );
            return;
        }
        contractAccess.setRemaining(wand, remainingContract);
    }

    private static int beginContract(ItemStack wand, WandContractAccess contractAccess) {
        contractAccess.setRemaining(wand, 25000);
        return 25000;
    }

    private static ContentId aspect(String name) {
        return new ContentId("forbiddenmagic", "concept/" + name);
    }
}
