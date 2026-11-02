package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.FmComponents;
import dev.visfox.forbiddenreforged.FmWands;
import dev.visfox.forbiddenreforged.FmWandContractAccess;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.core.wand.WandBehaviors;
import dev.visfox.forbiddenreforged.core.wand.WandMaterialCatalog;
import dev.visfox.forbiddenreforged.core.wand.WandMaterialCatalog.RodDefinition;
import dev.visfox.forbiddenreforged.core.wand.WandMaterialCatalog.RodUpdateKind;
import dev.visfox.forbiddenreforged.core.wand.WitchwoodManaBehavior;
import dev.visfox.forbiddenreforged.core.wand.WandVisAccess;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.neoforged.fml.ModList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import thaumcraft.aspect.Aspect;
import thaumcraft.taint.TaintBiomes;
import thaumcraft.wand.RodUpdate;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.WandCap;
import thaumcraft.wand.WandRod;
import thaumcraft.wand.Wands;

public final class Tc4WandAdapter {
    private static final WandVisAccess VIS = new Tc4WandVisAdapter();

    private Tc4WandAdapter() {
    }

    public static void registerWands() {
        for (RodDefinition rod : WandMaterialCatalog.rods(isArsMagicaLoaded())) {
            WandRegistry.registerRod(
                rod.tag(),
                rod.capacity(),
                hostId(rod.item()),
                rod.cost(),
                rodUpdate(rod.update()),
                rod.creative(),
                rod.staff(),
                rod.sceptre(),
                hostId(rod.texture())
            );
        }
        for (var cap : WandMaterialCatalog.caps()) {
            List<Aspect> aspects = cap.aspects().stream()
                .map(Tc4AspectAdapter::resolveAspect)
                .toList();
            WandRegistry.registerCap(
                cap.tag(),
                cap.multiplier(),
                aspects,
                cap.discount(),
                hostId(cap.item()),
                cap.cost(),
                null,
                hostId(cap.texture())
            );
        }
    }

    private static ResourceLocation hostId(dev.visfox.forbiddenreforged.core.model.ContentId id) {
        return ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path());
    }

    private static RodUpdate rodUpdate(RodUpdateKind kind) {
        return switch (kind) {
            case TAINTED -> new TaintedWandUpdate();
            case INFERNAL -> new InfernalWandUpdate();
            case PROFANE -> new ProfaneWandUpdate();
            case WITCHWOOD_WAND -> new ManaWandUpdate();
            case WITCHWOOD_STAFF -> new ManaStaffUpdate();
            case CREATIVE -> new CreativeWandUpdate();
            case NONE -> null;
        };
    }

    public static void addCreativeWands(List<ItemStack> stacks) {
        addWand(stacks, WandRegistry.CAP_THAUMIUM, "tainted");
        addWand(stacks, WandRegistry.CAP_THAUMIUM, "infernal");
        addWand(stacks, WandRegistry.CAP_VOID, "equivalent");

        WandCap orichalcum = WandRegistry.findCap("orichalcum");
        WandRod neutronium = WandRegistry.findRod("neutronium");
        WandRod neutroniumStaff = WandRegistry.findRod("neutronium_staff");
        if (orichalcum != null && neutronium != null) {
            stacks.add(Wands.create(orichalcum, neutronium, false));
        }
        if (orichalcum != null && neutroniumStaff != null) {
            stacks.add(Wands.create(orichalcum, neutroniumStaff, false));
        }

        WandRod profane = WandRegistry.findRod("profane");
        if (profane != null) {
            ItemStack filled = Wands.create(WandRegistry.CAP_IRON, profane, false);
            for (var aspect : VIS.primalAspects()) {
                VIS.addVis(filled, aspect, 5000, true);
            }
            stacks.add(filled);
        }
    }

    private static void addWand(List<ItemStack> stacks, WandCap cap, String rodTag) {
        WandRod rod = WandRegistry.findRod(rodTag);
        if (rod != null) {
            stacks.add(Wands.create(cap, rod, false));
        }
    }

    private static final class CreativeWandUpdate implements RodUpdate {
        @Override
        public void onUpdate(ItemStack stack, Player player) {
            WandBehaviors.creative(stack, player, VIS);
        }
    }

    // Refills any primal below a tenth of capacity while standing in a tainted area (original TaintedWandUpdate).
    private static final class TaintedWandUpdate implements RodUpdate {
        @Override
        public void onUpdate(ItemStack stack, Player player) {
            boolean tainted = TaintBiomes.isTainted(player.level(), player.blockPosition());
            WandBehaviors.tainted(stack, player, VIS, tainted);
        }
    }

    // Refills primals in the nether and fire anywhere; puts out the holder and clears wither (original InfernalWandUpdate).
    private static final class InfernalWandUpdate implements RodUpdate {
        private static final List<Aspect> NON_FIRE = List.of(Aspect.ORDER, Aspect.ENTROPY, Aspect.AIR, Aspect.EARTH, Aspect.WATER);

        @Override
        public void onUpdate(ItemStack stack, Player player) {
            WandBehaviors.infernal(stack, player, VIS);
        }
    }

    // Drains a finite 25000 vis "contract" into the wand, gaining sticky warp, then burns out into the profaned rod (original ProfaneWandUpdate).
    private static final class ProfaneWandUpdate implements RodUpdate {
        @Override
        public void onUpdate(ItemStack stack, Player entity) {
            WandBehaviors.profane(stack, entity, VIS, HostAdapterRegistry.requireWandParts(), FmWandContractAccess.INSTANCE,
                HostAdapterRegistry.requireWarp());
        }


    }

    /** Optional Ars Magica mana bridge for Witchwood wand and staff cores. */
    private abstract static class ManaConversionUpdate implements RodUpdate {
        private static boolean warnedAboutApi;
        private final boolean staff;
        private ManaConversionUpdate(boolean staff) { this.staff = staff; }

        @Override
        public void onUpdate(ItemStack stack, Player player) {
            if (player.tickCount % WitchwoodManaBehavior.rechargeInterval(staff) != 0 || !isArsMagicaLoaded()) return;
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
                    double cost = WitchwoodManaBehavior.manaCost(staff, cap, deficit);
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
        return WitchwoodManaBehavior.supportsArsMagicaModId(modId);
    }

    public static boolean isArsMagicaLoaded() {
        return (ModList.get().isLoaded("arsmagicalegacy") || ModList.get().isLoaded("arsmagica2"));
    }

    public static int witchwoodRechargeInterval(boolean staff) { return WitchwoodManaBehavior.rechargeInterval(staff); }
    /** Cost for a full vis point, before partial-deficit scaling. */
    public static float witchwoodManaCost(boolean staff, String capTag) {
        return WitchwoodManaBehavior.manaCost(staff, capTag);
    }

    /**
     * Costs scale with stored vis units, where 100 internal units equal one vis
     * point. A nearly full aspect therefore costs less to top up than an empty one.
     */
    public static float witchwoodManaCost(boolean staff, String capTag, int deficit) {
        return WitchwoodManaBehavior.manaCost(staff, capTag, deficit);
    }

}
