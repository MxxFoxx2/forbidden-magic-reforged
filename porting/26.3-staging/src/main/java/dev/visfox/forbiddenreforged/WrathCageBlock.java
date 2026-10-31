package dev.visfox.forbiddenreforged;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nullable;

public class WrathCageBlock extends Block implements EntityBlock {
    public WrathCageBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WrathCageBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != FmBlockEntities.WRATH_CAGE.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> WrathCageBlockEntity.tick(tickLevel, pos, tickState, (WrathCageBlockEntity) entity);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof WrathCageBlockEntity cage) || level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (attune(cage, player.getMainHandItem())) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof WrathCageBlockEntity cage)) {
            return InteractionResult.PASS;
        }
        if (attune(cage, stack)) {
            return InteractionResult.SUCCESS;
        }
        if (!stack.is(FmItems.DIABOLIST_FORK.get()) || FmConfig.wrathCost() <= 0) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            cage.cycleMode();
        }
        player.swing(hand, SwingAnimation.DEFAULT, true);
        return InteractionResult.SUCCESS;
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
