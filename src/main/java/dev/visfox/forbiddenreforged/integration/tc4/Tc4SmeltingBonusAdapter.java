package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.common.FmIds;
import dev.visfox.forbiddenreforged.integration.runtime.HostSmeltingBonusServices;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Function;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Registers Forbidden Magic's smelting bonuses through the host's public
 * smelting-bonus API, if the host exposes one.
 *
 * <p>TC4 1.1.2 and 1.1.3 keep the bonus table internal
 * ({@code thaumcraft.device.SmeltingBonuses} has no public registration
 * method), so the bonus stays dormant on those hosts. The probe binds to a
 * registration method as soon as the host publishes one, without the add-on
 * needing a host patch or a recompile.
 *
 * <p>No approximation is used when the API is missing: the ported crafting
 * conversions remain the only emerald/fragment conversion.
 */
public final class Tc4SmeltingBonusAdapter implements HostSmeltingBonusServices {
    /** Host class that owns the smelting-bonus table. */
    public static final String HOST_CLASS_NAME = "thaumcraft.device.SmeltingBonuses";

    public static final Tc4SmeltingBonusAdapter INSTANCE = new Tc4SmeltingBonusAdapter(null);

    private static final List<Candidate> CANDIDATES = List.of(
        new Candidate("addSmeltingBonus", new Class<?>[] {ItemStack.class, ItemStack.class},
            item -> new ItemStack(item), item -> new ItemStack(item)),
        new Candidate("addSmeltingBonus", new Class<?>[] {Item.class, Item.class},
            item -> item, item -> item),
        new Candidate("register", new Class<?>[] {ItemStack.class, Item.class},
            item -> new ItemStack(item), item -> item),
        new Candidate("register", new Class<?>[] {Item.class, Item.class},
            item -> item, item -> item)
    );

    private final Class<?> explicitHostClass;
    private volatile Registration registration;
    private volatile boolean probed;
    private volatile boolean available;

    /** Probes the host class named by {@link #HOST_CLASS_NAME}. */
    public Tc4SmeltingBonusAdapter() {
        this(null);
    }

    /**
     * Probes the given host class instead of {@link #HOST_CLASS_NAME}. Used to
     * bind to a specific host implementation and to exercise the wake-up path.
     */
    public Tc4SmeltingBonusAdapter(Class<?> explicitHostClass) {
        this.explicitHostClass = explicitHostClass;
    }

    /**
     * Resolves the host API once. Must run after host data components are
     * bound, because a host API that accepts item stacks reads their
     * components.
     */
    public void probe() {
        if (probed) {
            return;
        }
        synchronized (this) {
            if (probed) {
                return;
            }
            registration = resolveRegistration(hostClass());
            available = registration != null;
            probed = true;
        }
        if (available) {
            FmIds.LOGGER.info("Host exposes a smelting-bonus API: {}", registration.method().getName());
        } else {
            FmIds.LOGGER.info("Host exposes no smelting-bonus API; the emerald bonus stays inactive");
        }
    }

    @Override
    public boolean supportsBonuses() {
        probe();
        return available;
    }

    @Override
    public boolean registerBonus(Item input, Item bonus) {
        probe();
        Registration target = registration;
        if (target == null || input == null || bonus == null) {
            return false;
        }
        try {
            target.method().invoke(null, target.input().apply(input), target.bonus().apply(bonus));
            return true;
        } catch (ReflectiveOperationException | LinkageError error) {
            FmIds.LOGGER.warn("Host smelting-bonus API rejected the emerald bonus", error);
            return false;
        }
    }

    private Class<?> hostClass() {
        if (explicitHostClass != null) {
            return explicitHostClass;
        }
        try {
            return Class.forName(HOST_CLASS_NAME);
        } catch (ClassNotFoundException | LinkageError error) {
            return null;
        }
    }

    public static Registration resolveRegistration(Class<?> hostClass) {
        if (hostClass == null) {
            return null;
        }
        for (Candidate candidate : CANDIDATES) {
            Method method;
            try {
                method = hostClass.getMethod(candidate.name(), candidate.parameters());
            } catch (NoSuchMethodException error) {
                continue;
            }
            if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            return new Registration(method, candidate.input(), candidate.bonus());
        }
        return null;
    }

    public record Registration(Method method, Function<Item, Object> input, Function<Item, Object> bonus) {
    }

    private record Candidate(String name, Class<?>[] parameters,
                             Function<Item, Object> input, Function<Item, Object> bonus) {
    }
}
