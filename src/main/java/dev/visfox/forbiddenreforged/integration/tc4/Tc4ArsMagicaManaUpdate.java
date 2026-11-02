package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.core.wand.WitchwoodManaBehavior;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import thaumcraft.aspect.Aspect;
import thaumcraft.wand.RodUpdate;
import thaumcraft.wand.Wands;

/**
 * Optional Ars Magica mana bridge for Witchwood rods. Third-party classes are
 * resolved reflectively so the addon does not link against either optional API.
 */
public final class Tc4ArsMagicaManaUpdate implements RodUpdate {
    private static boolean warnedAboutApi;
    private final boolean staff;

    public Tc4ArsMagicaManaUpdate(boolean staff) {
        this.staff = staff;
    }

    @Override
    public void onUpdate(ItemStack stack, Player player) {
        if (player.tickCount % WitchwoodManaBehavior.rechargeInterval(staff) != 0 || !isArsMagicaLoaded()) {
            return;
        }
        try {
            ManaAccess access = manaAccess(player);
            if (access == null) {
                return;
            }
            double current = access.getMana();
            if (current <= 0.0D) {
                return;
            }
            double remaining = current;
            String cap = Wands.getCap(stack).tag();
            List<Aspect> toRecharge = new ArrayList<>();
            for (Aspect aspect : Aspect.getPrimalAspects()) {
                int deficit = Wands.getMaxVis(stack) - Wands.getVis(stack, aspect);
                if (deficit <= 0) {
                    continue;
                }
                double cost = WitchwoodManaBehavior.manaCost(staff, cap, deficit);
                if (remaining > cost) {
                    remaining -= cost;
                    toRecharge.add(aspect);
                }
            }
            // Debit mana first. If the optional API rejects it, the wand gets no free Vis.
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

    private static boolean isArsMagicaLoaded() {
        return ModList.get().isLoaded("arsmagicalegacy") || ModList.get().isLoaded("arsmagica2");
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
            if (properties == null) {
                return null;
            }
            Class<?> propertiesType = Class.forName("am2.api.IExtendedProperties");
            return new ManaAccess(
                properties,
                propertiesType.getMethod("getCurrentMana"),
                propertiesType.getMethod("setCurrentMana", float.class),
                player,
                false
            );
        }
        return null;
    }

    private record ManaAccess(
        Object receiver,
        Method getter,
        Method setter,
        Player player,
        boolean modernApi
    ) {
        private double getMana() throws ReflectiveOperationException {
            Object value = modernApi ? getter.invoke(receiver, player) : getter.invoke(receiver);
            return ((Number) value).doubleValue();
        }

        private boolean setMana(double amount) throws ReflectiveOperationException {
            if (modernApi) {
                return Boolean.TRUE.equals(setter.invoke(receiver, player, amount));
            }
            setter.invoke(receiver, (float) amount);
            return true;
        }
    }
}
