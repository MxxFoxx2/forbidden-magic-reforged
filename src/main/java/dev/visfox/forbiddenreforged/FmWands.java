package dev.visfox.forbiddenreforged;

import java.lang.reflect.Method;
import java.util.ArrayList;
import net.minecraft.world.entity.LivingEntity;
import java.util.List;
import net.neoforged.fml.ModList;
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
        WandRegistry.registerRod("witchwood", 100, ForbiddenMagic.id("witchwood_rod"), 12,
            new ManaWandUpdate(), false, false, false,
            ForbiddenMagic.id("textures/model/wand_rod_witchwood.png"));
        WandRegistry.registerRod("witchwood", 50, ForbiddenMagic.id("witchwood_staff_rod"), 24,
            new ManaStaffUpdate(), false, true, false,
            ForbiddenMagic.id("textures/model/wand_rod_witchwood_staff.png"));
        WandRegistry.registerRod("profaned", 50, ResourceLocation.withDefaultNamespace("bedrock"), 1000,
            null, false, false, false,
            ForbiddenMagic.id("textures/model/wand_rod_profaned.png"));
        WandRegistry.registerCap("orichalcum", 0.0F, List.of(), 0.0F,
            ResourceLocation.withDefaultNamespace("command_block"), 1000, null,
            ForbiddenMagic.id("textures/model/wand_cap_orichalcum.png"));
        WandRegistry.registerCap("alchemical", 0.9F, List.of(Aspect.WATER), 0.8F,
            ForbiddenMagic.id("alchemical_cap"), 7, null,
            ForbiddenMagic.id("textures/model/wand_cap_alchemical.png"));
        WandRegistry.registerRod("equivalent", 100, ForbiddenMagic.id("equivalent_rod"), 12,
            null, false, false, false,
            ForbiddenMagic.id("textures/model/wand_rod_equivalent.png"));
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
                player.serverLevel().sendParticles(ParticleTypes.EXPLOSION,
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

    /** Optional Ars Magica mana bridge for Witchwood wand and staff cores. */
    private abstract static class ManaConversionUpdate implements RodUpdate {
        private static boolean warnedAboutApi;
        private final boolean staff;
        private ManaConversionUpdate(boolean staff) { this.staff = staff; }

        @Override
        public void onUpdate(ItemStack stack, Player player) {
            if (player.tickCount % witchwoodRechargeInterval(staff) != 0 || !isArsMagicaLoaded()) return;
            try {
                ManaAccess access = manaAccess(player);
                if (access == null) return;
                double current = access.getMana();
                if (current <= 0.0D) return;
                double remaining = current;
                String cap = Wands.getCap(stack).tag();
                List<Aspect> toRecharge = new ArrayList<>();
                for (Aspect aspect : Aspect.getPrimalAspects()) {
                    int deficit = Wands.getMaxVis(stack) - Wands.getVis(stack, aspect);
                    if (deficit <= 0) continue;
                    double cost = witchwoodManaCost(staff, cap, deficit);
                    if (remaining > cost) {
                        remaining -= cost;
                        toRecharge.add(aspect);
                    }
                }
                // Debit mana first, then add vis. If the optional API rejects the
                // debit, the wand must not receive free vis.
                if (!toRecharge.isEmpty() && access.setMana(remaining)) {
                    for (Aspect aspect : toRecharge) {
                        Wands.addVis(stack, aspect, 1, true);
                    }
                }
            } catch (ReflectiveOperationException | LinkageError | RuntimeException error) {
                if (!warnedAboutApi) {
                    warnedAboutApi = true;
                    ForbiddenMagic.LOGGER.warn("Witchwood recharge disabled: Ars Magica mana API unavailable", error);
                }
            }
        }

        private static ManaAccess manaAccess(Player player) throws ReflectiveOperationException {
            if (ModList.get().isLoaded("arsmagicalegacy")) {
                Class<?> apiType = Class.forName("at.minecraftschurli.mods.arsmagicalegacy.api.ArsMagicaApi");
                Object helper = apiType.getMethod("manaHelper").invoke(null);
                Class<?> helperType = Class.forName("at.minecraftschurli.mods.arsmagicalegacy.api.magic.ManaHelper");
                Method getMana = helperType.getMethod("getMana", LivingEntity.class);
                Method setMana = helperType.getMethod("setMana", LivingEntity.class, double.class);
                return new ManaAccess(helper, getMana, setMana, player, true);
            }
            if (ModList.get().isLoaded("arsmagica2")) {
                Class<?> apiType = Class.forName("am2.api.ArsMagicaApi");
                Object instance = apiType.getField("instance").get(null);
                Object properties = null;
                for (Method method : apiType.getMethods()) {
                    if (method.getName().equals("getExtendedProperties") && method.getParameterCount() == 1
                        && method.getParameterTypes()[0].isInstance(player)) {
                        properties = method.invoke(instance, player);
                        break;
                    }
                }
                if (properties == null) return null;
                Class<?> propertiesType = Class.forName("am2.api.IExtendedProperties");
                return new ManaAccess(properties, propertiesType.getMethod("getCurrentMana"),
                    propertiesType.getMethod("setCurrentMana", float.class), player, false);
            }
            return null;
        }
    }

    private record ManaAccess(Object receiver, Method getter, Method setter, Player player, boolean modernApi) {
        private double getMana() throws ReflectiveOperationException {
            Object value = modernApi ? getter.invoke(receiver, player) : getter.invoke(receiver);
            return ((Number)value).doubleValue();
        }
        private boolean setMana(double amount) throws ReflectiveOperationException {
            if (modernApi) {
                return Boolean.TRUE.equals(setter.invoke(receiver, player, amount));
            }
            setter.invoke(receiver, (float)amount);
            return true;
        }
    }
    private static final class ManaWandUpdate extends ManaConversionUpdate {
        private ManaWandUpdate() { super(false); }
    }
    private static final class ManaStaffUpdate extends ManaConversionUpdate {
        private ManaStaffUpdate() { super(true); }
    }
    public static boolean supportsArsMagicaModId(String modId) {
        return "arsmagicalegacy".equals(modId) || "arsmagica2".equals(modId);
    }

    public static boolean isArsMagicaLoaded() {
        return (ModList.get().isLoaded("arsmagicalegacy") || ModList.get().isLoaded("arsmagica2"));
    }

    public static int witchwoodRechargeInterval(boolean staff) { return staff ? 40 : 100; }
    /** Cost for a full vis point, before partial-deficit scaling. */
    public static float witchwoodManaCost(boolean staff, String capTag) {
        if (staff) return "vinteum".equals(capTag) ? 50.0F : 100.0F;
        return "vinteum".equals(capTag) ? 80.0F : 140.0F;
    }

    /**
     * Costs scale with stored vis units, where 100 internal units equal one vis
     * point. A nearly full aspect therefore costs less to top up than an empty one.
     */
    public static float witchwoodManaCost(boolean staff, String capTag, int deficit) {
        if (deficit <= 0) return 0.0F;
        return witchwoodManaCost(staff, capTag) * Math.min(deficit, 100) / 100.0F;
    }

}
