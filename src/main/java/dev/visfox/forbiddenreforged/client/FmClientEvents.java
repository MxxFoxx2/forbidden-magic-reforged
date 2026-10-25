package dev.visfox.forbiddenreforged.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.FmBlockEntities;
import dev.visfox.forbiddenreforged.FmComponents;
import dev.visfox.forbiddenreforged.FmConfig;
import dev.visfox.forbiddenreforged.FmItems;
import dev.visfox.forbiddenreforged.MorphState;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = ForbiddenMagic.MODID, value = Dist.CLIENT)
public final class FmClientEvents {
    private FmClientEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(FmBlockEntities.WRATH_CAGE.get(), WrathCageRenderer::new);
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            if (tintIndex != 0) {
                return -1;
            }
            return stack.getOrDefault(FmComponents.MORPH_STATE.get(), MorphState.EMPTY).tintColor() & 0xFFFFFF;
        }, FmItems.MORPH_PICKAXE.get(), FmItems.MORPH_SWORD.get(), FmItems.MORPH_SHOVEL.get(), FmItems.MORPH_AXE.get());
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(FmItems.DIABOLIST_FORK.get(), ForbiddenMagic.id("spork"),
                (stack, level, entity, seed) -> FmConfig.spork() ? 1.0F : 0.0F);
            ItemProperties.register(FmItems.DEADLY_SHARDS.get("lust").get(), ForbiddenMagic.id("no_lust"),
                (stack, level, entity, seed) -> FmConfig.lustDisabled() ? 1.0F : 0.0F);
            ItemProperties.register(FmItems.MOB_CRYSTAL.get(), ForbiddenMagic.id("filled"),
                (stack, level, entity, seed) -> stack.has(FmComponents.MOB_ID.get()) ? 1.0F : 0.0F);
        });
    }
}
