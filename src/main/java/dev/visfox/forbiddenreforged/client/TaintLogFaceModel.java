package dev.visfox.forbiddenreforged.client;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.TaintLogTextureVariant;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.SimpleModelState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.Nullable;

public record TaintLogFaceModel(BakedModel[][] models) implements IDynamicBakedModel {
    private static int axisIndex(Direction.Axis axis) {
        return switch (axis) {
            case Y -> 0;
            case X -> 1;
            case Z -> 2;
        };
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        if (state == null || side == null) {
            return models[0][TaintLogTextureVariant.STANDARD].getQuads(state, side, rand, data, renderType);
        }
        Direction.Axis axis = state.getValue(RotatedPillarBlock.AXIS);
        BakedModel[] axisModels = models[axisIndex(axis)];
        if (side.getAxis() == axis) {
            return axisModels[TaintLogTextureVariant.STANDARD].getQuads(state, side, rand, data, renderType);
        }
        int variant = TaintLogTextureVariant.selectSeeded(rand.nextLong(), side.get3DDataValue());
        return axisModels[variant].getQuads(state, side, rand, data, renderType);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return models[0][TaintLogTextureVariant.STANDARD].useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return models[0][TaintLogTextureVariant.STANDARD].isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return models[0][TaintLogTextureVariant.STANDARD].usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return models[0][TaintLogTextureVariant.STANDARD].getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    public record Unbaked() implements IUnbakedGeometry<Unbaked> {
        private static final ResourceLocation[][] MODEL_IDS = {
            {
                ForbiddenMagic.id("block/taint_log"),
                ForbiddenMagic.id("block/taint_log_gross"),
                ForbiddenMagic.id("block/taint_log_gross2")
            },
            {
                ForbiddenMagic.id("block/taint_log_horizontal"),
                ForbiddenMagic.id("block/taint_log_gross_horizontal"),
                ForbiddenMagic.id("block/taint_log_gross2_horizontal")
            },
            {
                ForbiddenMagic.id("block/taint_log_horizontal"),
                ForbiddenMagic.id("block/taint_log_gross_horizontal"),
                ForbiddenMagic.id("block/taint_log_gross2_horizontal")
            }
        };
        private static final ModelState[] AXIS_STATES = {
            BlockModelRotation.X0_Y0,
            BlockModelRotation.X90_Y90,
            BlockModelRotation.X90_Y0
        };

        @Override
        public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
            BakedModel[][] models = new BakedModel[3][3];
            for (int axis = 0; axis < 3; axis++) {
                ModelState state = new SimpleModelState(AXIS_STATES[axis].getRotation());
                for (int variant = 0; variant < 3; variant++) {
                    models[axis][variant] = baker.bake(MODEL_IDS[axis][variant], state, spriteGetter);
                }
            }
            return new TaintLogFaceModel(models);
        }
    }

    public enum Loader implements IGeometryLoader<Unbaked> {
        INSTANCE;

        public static final ResourceLocation ID = ForbiddenMagic.id("taint_log_faces");

        @Override
        public Unbaked read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
            return new Unbaked();
        }
    }
}
