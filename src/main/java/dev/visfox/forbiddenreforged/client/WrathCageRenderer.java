package dev.visfox.forbiddenreforged.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.visfox.forbiddenreforged.WrathCageBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

public final class WrathCageRenderer implements BlockEntityRenderer<WrathCageBlockEntity> {
    private final EntityRenderDispatcher dispatcher;

    public WrathCageRenderer(BlockEntityRendererProvider.Context context) {
        this.dispatcher = context.getEntityRenderer();
    }

    @Override
    public void render(WrathCageBlockEntity cage, float partialTick, PoseStack poseStack,
        MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Level level = cage.getLevel();
        String mobId = cage.getMobId();
        if (level == null || mobId == null || mobId.isBlank()) {
            return;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(mobId));
        Entity entity = type.create(level);
        if (entity == null) {
            return;
        }
        entity.setNoGravity(true);
        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
        }
        entity.setYRot(0.0F);
        entity.yRotO = 0.0F;
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.22D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees((cage.getBlockPos().asLong() % 360L) + partialTick));
        poseStack.scale(0.38F, 0.38F, 0.38F);
        dispatcher.render(entity, 0.0D, 0.0D, 0.0D, entity.getYRot(), partialTick, poseStack, buffers, packedLight);
        poseStack.popPose();
    }
}
