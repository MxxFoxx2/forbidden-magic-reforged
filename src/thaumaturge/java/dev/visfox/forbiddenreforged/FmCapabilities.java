package dev.visfox.forbiddenreforged;

import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosCapability;

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class FmCapabilities {
    private FmCapabilities() {
    }

    @SubscribeEvent
    static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, FmBlockEntities.WRATH_CAGE.get(), (entity, side) -> entity);
        event.registerItem(CuriosCapability.ITEM, (stack, context) -> RingNutritionItem.curio(), FmItems.RING_NUTRITION.get());
    }
}
