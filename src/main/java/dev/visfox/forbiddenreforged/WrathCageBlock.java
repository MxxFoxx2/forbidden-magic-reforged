package dev.visfox.forbiddenreforged;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import thaumcraft.blockentity.TcBlockEntity;

public final class WrathCageBlock extends Block implements EntityBlock {
    public WrathCageBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WrathCageBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
        Level level, BlockState state, BlockEntityType<T> type
    ) {
        if (type != FmBlockEntities.WRATH_CAGE.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) ->
            WrathCageBlockEntity.tick(tickLevel, pos, tickState, (WrathCageBlockEntity) entity);
    }

    @Override
    protected InteractionResult useWithoutItem(
        BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit
    ) {
        if (!(level.getBlockEntity(pos) instanceof WrathCageBlockEntity cage)) {
            return InteractionResult.PASS;
        }
        if (attune(cage, player.getMainHandItem())) {
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    protected ItemInteractionResult useItemOn(
        ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
        InteractionHand hand, BlockHitResult hit
    ) {
        if (!(level.getBlockEntity(pos) instanceof WrathCageBlockEntity cage)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (attune(cage, stack)) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        if (!stack.is(FmItems.DIABOLIST_FORK.get()) || FmConfig.wrathCost() <= 0) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide()) {
            cage.cycleMode();
        }
        player.swing(hand);
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        TcBlockEntity.beforeRemove(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public static boolean attune(WrathCageBlockEntity cage, ItemStack held) {
        String incoming = MobCrystalItem.mob(held);
        if (incoming == null || !FmSpawnerMobs.ids().contains(incoming)) {
            return false;
        }
        String previous = cage.getMobId();
        cage.setMobId(incoming);
        if (previous == null) {
            held.remove(FmComponents.MOB_ID.get());
        } else {
            MobCrystalItem.setMob(held, previous);
        }
        return true;
    }
}
