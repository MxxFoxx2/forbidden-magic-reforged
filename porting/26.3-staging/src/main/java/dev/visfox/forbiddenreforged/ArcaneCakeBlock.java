package dev.visfox.forbiddenreforged;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ArcaneCakeBlock extends VegetationBlock {
    public static final int SLICES = 12;
    public static final IntegerProperty SLICE = IntegerProperty.create("slice", 0, SLICES - 1);

    public ArcaneCakeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SLICE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(SLICE);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.isSolid();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int slice = state.getValue(SLICE);
        return box(Math.min(14.0, (1 + slice) / 12.0 * 16.0), 0.0, 1.0, 15.0, 8.0, 15.0);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        eat(level, pos, state, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        eat(level, pos, state, player);
    }

    public static void eat(Level level, BlockPos pos, BlockState state, Player player) {
        if (!player.canEat(false)) {
            return;
        }
        if (!level.isClientSide()) {
            player.getFoodData().eat(2, 1.0F);
            level.playSound(null, pos, SoundEvents.GENERIC_EAT.value(), SoundSource.PLAYERS, 0.5F, 0.8F);
            int slice = state.getValue(SLICE) + 1;
            if (slice >= SLICES) {
                level.removeBlock(pos, false);
            } else {
                level.setBlock(pos, state.setValue(SLICE, slice), 3);
            }
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int slice = state.getValue(SLICE);
        if (slice > 0) {
            level.setBlock(pos, state.setValue(SLICE, slice - 1), 3);
        }
    }
}
