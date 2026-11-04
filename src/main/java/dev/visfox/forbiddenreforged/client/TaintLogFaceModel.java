package dev.visfox.forbiddenreforged.client;

import com.mojang.math.Quadrant;
import com.mojang.serialization.MapCodec;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.TaintLogTextureVariant;
import java.util.List;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.jspecify.annotations.Nullable;

public record TaintLogFaceModel(BlockStateModelPart[][] models) implements DynamicBlockStateModel {
    private static final Identifier[][] MODEL_IDS = {
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

    private static int axisIndex(Direction.Axis axis) {
        return switch (axis) {
            case Y -> 0;
            case X -> 1;
            case Z -> 2;
        };
    }

    @Override
    public Material.Baked particleMaterial() {
        return models[0][TaintLogTextureVariant.STANDARD].particleMaterial();
    }

    @Override
    public int materialFlags() {
        return models[0][TaintLogTextureVariant.STANDARD].materialFlags();
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
        parts.add(models[0][TaintLogTextureVariant.STANDARD]);
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return new GeometryKey(pos.asLong(), state.getValue(RotatedPillarBlock.AXIS));
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        Direction.Axis axis = state.getValue(RotatedPillarBlock.AXIS);
        parts.add(new FacePart(models, axis, pos.getX(), pos.getY(), pos.getZ()));
    }

    private record GeometryKey(long position, Direction.Axis axis) {
    }

    private record FacePart(BlockStateModelPart[][] models, Direction.Axis axis, int x, int y, int z)
        implements BlockStateModelPart {
        @Override
        public List<BakedQuad> getQuads(@Nullable Direction direction) {
            BlockStateModelPart[] axisModels = models[axisIndex(axis)];
            if (direction != null) {
                int variant = TaintLogTextureVariant.variantForDirection(axis, direction, x, y, z);
                return axisModels[variant].getQuads(direction);
            }
            List<BakedQuad> quads = new java.util.ArrayList<>();
            for (Direction face : Direction.values()) {
                int variant = TaintLogTextureVariant.variantForDirection(axis, face, x, y, z);
                quads.addAll(axisModels[variant].getQuads(face));
            }
            return quads;
        }

        @Override
        public int materialFlags() {
            return models[axisIndex(axis)][TaintLogTextureVariant.STANDARD].materialFlags();
        }

        @Override
        public Material.Baked particleMaterial() {
            return models[axisIndex(axis)][TaintLogTextureVariant.STANDARD].particleMaterial();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return models[axisIndex(axis)][TaintLogTextureVariant.STANDARD].useAmbientOcclusion();
        }
    }

    public record Unbaked() implements CustomUnbakedBlockStateModel {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new Unbaked());
        public static final Identifier ID = ForbiddenMagic.id("taint_log_faces");

        private static final ModelState VERTICAL = BlockModelRotation.IDENTITY;
        private static final ModelState HORIZONTAL_X = BlockModelRotation.get(Quadrant.fromXYAngles(Quadrant.R90, Quadrant.R90));
        private static final ModelState HORIZONTAL_Z = BlockModelRotation.get(Quadrant.fromXYAngles(Quadrant.R90, Quadrant.R0));

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            for (Identifier[] row : MODEL_IDS) {
                for (Identifier id : row) {
                    resolver.markDependency(id);
                }
            }
        }

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            BlockStateModelPart[][] baked = new BlockStateModelPart[3][3];
            for (int variant = 0; variant < 3; variant++) {
                baked[0][variant] = SimpleModelWrapper.bake(baker, MODEL_IDS[0][variant], VERTICAL);
                baked[1][variant] = SimpleModelWrapper.bake(baker, MODEL_IDS[1][variant], HORIZONTAL_X);
                baked[2][variant] = SimpleModelWrapper.bake(baker, MODEL_IDS[1][variant], HORIZONTAL_Z);
            }
            return new TaintLogFaceModel(baked);
        }
    }
}
