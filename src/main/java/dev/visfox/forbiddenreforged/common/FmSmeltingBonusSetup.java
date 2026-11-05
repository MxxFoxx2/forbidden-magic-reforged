package dev.visfox.forbiddenreforged.common;

import dev.visfox.forbiddenreforged.integration.tc4.Tc4SmeltingBonusAdapter;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.DefaultDataComponentsBoundEvent;

/**
 * Registers Forbidden Magic's smelting bonuses once host data components are
 * bound.
 *
 * <p>Host APIs that accept item stacks read their components, so registration
 * cannot run during common setup. The event fires after the server binds
 * default data components and after a client receives them on join.
 */
@EventBusSubscriber(modid = FmIds.MODID)
public final class FmSmeltingBonusSetup {
    private FmSmeltingBonusSetup() {
    }

    @SubscribeEvent
    static void onComponentsBound(DefaultDataComponentsBoundEvent event) {
        if (!event.shouldUpdateStaticData()) {
            return;
        }
        Tc4SmeltingBonusAdapter.INSTANCE.probe();
        FmSmeltingBonuses.register();
    }
}
