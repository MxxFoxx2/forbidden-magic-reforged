package dev.visfox.forbiddenreforged.client;

import dev.visfox.forbiddenreforged.FmBlockEntities;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;

@EventBusSubscriber(modid = ForbiddenMagic.MODID, value = Dist.CLIENT)
public final class FmClientEvents {
    private FmClientEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(FmBlockEntities.WRATH_CAGE.get(), WrathCageRenderer::new);
    }

    @SubscribeEvent
    public static void registerItemModelProperties(RegisterConditionalItemModelPropertyEvent event) {
        event.register(ForbiddenMagic.id("spork"), SporkCondition.MAP_CODEC);
        event.register(ForbiddenMagic.id("no_lust"), NoLustCondition.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(ForbiddenMagic.id("morph_phase"), MorphPhaseTintSource.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerBlockStateModels(RegisterBlockStateModels event) {
        event.registerModel(TaintLogFaceModel.Unbaked.ID, TaintLogFaceModel.Unbaked.CODEC);
    }
}
