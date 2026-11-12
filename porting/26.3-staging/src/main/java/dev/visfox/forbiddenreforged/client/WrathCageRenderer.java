package dev.visfox.forbiddenreforged.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.visfox.forbiddenreforged.WrathCageBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;

public class WrathCageRenderer implements BlockEntityRenderer<WrathCageBlockEntity, WrathCageRenderer.State> {
    public static class State extends BlockEntityRenderState {
        public @Nullable EntityRenderState mob;
        public float spin;
        public float scale = 0.4375F;
    }

    private final EntityRenderDispatcher dispatcher;

    public WrathCageRenderer(BlockEntityRendererProvider.Context context) {
        this.dispatcher = context.entityRenderer();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WrathCageBlockEntity cage, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(cage, state, partialTicks, cameraPosition, breakProgress);
        state.mob = null;
        String mobId = cage.getMobId();
        if (mobId == null || cage.getLevel() == null) {
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(mobId);
        if (id == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
            return;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(id);
        Entity entity = type.create(cage.getLevel(), EntitySpawnReason.TRIGGERED);
        if (entity == null) {
            return;
        }
        entity.setPos(0.0, 0.0, 0.0);
        entity.setId(-1);
        state.mob = dispatcher.extractEntity(entity, partialTicks);
        state.spin = (cage.getLevel().getGameTime() % 360L) * 3.0F;
        state.scale = getScaleForEntity(entity);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.mob == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5, 0.4, 0.5);
        poseStack.rotateDegrees(Axis.YP, state.spin);
        poseStack.translate(0.0, -0.2, 0.0);
        poseStack.rotateDegrees(Axis.XP, -30.0F);
        poseStack.scale(state.scale, state.scale, state.scale);
        dispatcher.submit(state.mob, camera, 0.0, 0.0, 0.0, poseStack, collector);
        poseStack.popPose();
    }

    private float getScaleForEntity(Entity entity) {
        String id = EntityType.getKey(entity.getType()).toString();
        return switch (id) {
            case "minecraft:ghast" -> 0.1F;
            case "minecraft:slime", "minecraft:magma_cube" -> 0.4F;
            case "minecraft:enderman" -> 0.3F;
            default -> 0.4375F;
        };
    }

    @Override
    public AABB getRenderBoundingBox(WrathCageBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(1.0);
    }
}
