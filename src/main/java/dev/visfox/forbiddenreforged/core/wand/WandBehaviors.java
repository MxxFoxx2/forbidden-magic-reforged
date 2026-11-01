package dev.visfox.forbiddenreforged.core.wand;

import dev.visfox.forbiddenreforged.FmComponents;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.warp.WarpChangeRequested;
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
    private static final ContentId PROFANED_ROD = new ContentId("forbiddenmagic", "profaned");
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
        WandPartsAccess parts
    ) {
        if (!(entity instanceof ServerPlayer player) || player.tickCount % 20 != 0) {
            return;
        }
        Integer stored = wand.get(FmComponents.PROFANE_CONTRACT.get());
        int contract = stored == null ? beginContract(wand) : stored;
        if (contract <= 0) {
            return;
        }
        int maximum = vis.maximumVis(wand);
        for (ContentId aspect : vis.primalAspects()) {
            if (contract <= 0) {
                break;
            }
            int deficit = maximum - vis.vis(wand, aspect);
            if (deficit > 0) {
                int restore = Math.min(contract, deficit);
                vis.addRealVis(wand, aspect, restore, true);
                contract -= restore;
                if (player.getRandom().nextInt(2501) < restore) {
                    WarpChangeRequested.request(player, 1, WarpKind.STICKY);
                }
            }
        }
        if (contract <= 0) {
            parts.setRod(wand, PROFANED_ROD);
            wand.remove(FmComponents.PROFANE_CONTRACT.get());
            WarpChangeRequested.request(player, 1, WarpKind.STICKY);
            player.level().sendParticles(
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
        wand.set(FmComponents.PROFANE_CONTRACT.get(), contract);
    }

    private static int beginContract(ItemStack wand) {
        wand.set(FmComponents.PROFANE_CONTRACT.get(), 25000);
        return 25000;
    }

    private static ContentId aspect(String name) {
        return new ContentId("forbiddenmagic", "concept/" + name);
    }
}
