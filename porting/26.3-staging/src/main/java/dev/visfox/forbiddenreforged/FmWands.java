package dev.visfox.forbiddenreforged;

import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.aspect.Aspect;
import thaumcraft.research.Warp;
import thaumcraft.research.WarpType;
import thaumcraft.taint.TaintBiomes;
import thaumcraft.wand.RodUpdate;
import thaumcraft.wand.WandKeys;
import thaumcraft.wand.WandParts;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.Wands;

public final class FmWands {
    private FmWands() {
    }

    public static void register() {
        WandRegistry.registerRod("tainted", 150, ForbiddenMagic.id("tainted_rod"), 12,
            new TaintedWandUpdate(), false, false, false,
            ForbiddenMagic.id("textures/model/wand_rod_tainted.png"));
        WandRegistry.registerRod("infernal", 150, ForbiddenMagic.id("infernal_rod"), 12,
            new InfernalWandUpdate(), false, false, false,
            ForbiddenMagic.id("textures/model/wand_rod_infernal.png"));
        WandRegistry.registerRod("profane", 50, ForbiddenMagic.id("profane_rod"), 12,
            new ProfaneWandUpdate(), false, false, false,
            ForbiddenMagic.id("textures/model/wand_rod_profane.png"));
        WandRegistry.registerRod("profaned", 50, ResourceLocation.withDefaultNamespace("bedrock"), 1000,
            null, false, false, false,
            ForbiddenMagic.id("textures/model/wand_rod_profaned.png"));
        WandRegistry.registerCap("orichalcum", 0.0F, List.of(), 0.0F,
            ResourceLocation.withDefaultNamespace("command_block"), 1000, null,
            ForbiddenMagic.id("textures/model/wand_cap_orichalcum.png"));
        WandRegistry.registerRod("neutronium", 9001, ResourceLocation.withDefaultNamespace("bedrock"), 1000,
            new CreativeWandUpdate(), true, false, false,
            ForbiddenMagic.id("textures/model/wand_rod_neutronium.png"));
        WandRegistry.registerRod("neutronium", 9002, ResourceLocation.withDefaultNamespace("bedrock"), 1000,
            new CreativeWandUpdate(), true, true, false,
            ForbiddenMagic.id("textures/model/wand_rod_neutronium.png"));
    }

    private static final class CreativeWandUpdate implements RodUpdate {
        @Override
        public void onUpdate(ItemStack stack, Player player) {
            Wands.fill(stack);
        }
    }

    // Refills any primal below a tenth of capacity while standing in a tainted area (original TaintedWandUpdate).
    private static final class TaintedWandUpdate implements RodUpdate {
        @Override
        public void onUpdate(ItemStack stack, Player player) {
            if (player.tickCount % 100 != 0 || !TaintBiomes.isTainted(player.level(), player.blockPosition())) {
                return;
            }
            int max = Wands.getMaxVis(stack);
            for (Aspect aspect : Aspect.getPrimalAspects()) {
                if (Wands.getVis(stack, aspect) < max / 10) {
                    Wands.addVis(stack, aspect, 1, true);
                }
            }
        }
    }

    // Refills primals in the nether and fire anywhere; puts out the holder and clears wither (original InfernalWandUpdate).
    private static final class InfernalWandUpdate implements RodUpdate {
        private static final List<Aspect> NON_FIRE = List.of(Aspect.ORDER, Aspect.ENTROPY, Aspect.AIR, Aspect.EARTH, Aspect.WATER);

        @Override
        public void onUpdate(ItemStack stack, Player player) {
            if (player.tickCount % 100 == 0) {
                int max = Wands.getMaxVis(stack);
                if (player.level().dimension() == Level.NETHER) {
                    for (Aspect aspect : NON_FIRE) {
                        if (Wands.getVis(stack, aspect) < max / 10) {
                            Wands.addVis(stack, aspect, 1, true);
                        }
                    }
                }
                if (Wands.getVis(stack, Aspect.FIRE) < max / 5) {
                    Wands.addVis(stack, Aspect.FIRE, 1, true);
                }
            }
            if (player.isOnFire()) {
                player.clearFire();
            }
            if (player.hasEffect(MobEffects.WITHER)) {
                player.removeEffect(MobEffects.WITHER);
            }
        }
    }

    // Drains a finite 25000 vis "contract" into the wand, gaining sticky warp, then burns out into the profaned rod (original ProfaneWandUpdate).
    private static final class ProfaneWandUpdate implements RodUpdate {
        @Override
        public void onUpdate(ItemStack stack, Player entity) {
            if (!(entity instanceof ServerPlayer player) || player.tickCount % 20 != 0) {
                return;
            }
            Integer stored = stack.get(FmComponents.PROFANE_CONTRACT.get());
            int contract = stored == null ? beginContract(stack, player) : stored;
            if (contract <= 0) {
                return;
            }
            int max = Wands.getMaxVis(stack);
            for (Aspect aspect : Aspect.getPrimalAspects()) {
                if (contract <= 0) {
                    break;
                }
                int deficit = max - Wands.getVis(stack, aspect);
                if (deficit > 0) {
                    int restore = Math.min(contract, deficit);
                    Wands.addRealVis(stack, aspect, restore, true);
                    contract -= restore;
                    if (player.getRandom().nextInt(2501) < restore) {
                        Warp.add(player, 1, WarpType.STICKY);
                    }
                }
            }
            if (contract <= 0) {
                WandParts parts = Wands.getParts(stack);
                WandKeys.PARTS.set(stack, new WandParts(parts.cap(), "profaned", parts.sceptre()));
                stack.remove(FmComponents.PROFANE_CONTRACT.get());
                Warp.add(player, 1, WarpType.STICKY);
                player.level().sendParticles(ParticleTypes.EXPLOSION,
                    player.getX(), player.getY() + player.getBbHeight() / 2.0, player.getZ(),
                    1, 0.0, 0.0, 0.0, 0.0);
                return;
            }
            stack.set(FmComponents.PROFANE_CONTRACT.get(), contract);
        }

        private int beginContract(ItemStack stack, ServerPlayer player) {
            stack.set(FmComponents.PROFANE_CONTRACT.get(), 25000);
            return 25000;
        }
    }
}
