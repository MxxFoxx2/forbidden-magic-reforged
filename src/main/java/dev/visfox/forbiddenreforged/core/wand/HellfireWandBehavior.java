package dev.visfox.forbiddenreforged.core.wand;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public final class HellfireWandBehavior {
    private HellfireWandBehavior() {
    }

    public static boolean ignite(
        Level level,
        ItemStack wand,
        Player player,
        BlockPos position,
        Direction side,
        WandTriggerCatalog.Definition definition,
        WandTriggerAccess access
    ) {
        BlockPos firePosition = position.above();
        if (level.isClientSide()
            || side != Direction.UP
            || !access.isWand(wand)
            || access.hasFocus(wand)
            || !access.researchComplete(player, definition.research())
            || !player.mayUseItemAt(firePosition, Direction.UP, new ItemStack(Items.FIRE_CHARGE))
            || !level.isEmptyBlock(firePosition)
            || !access.consumeVis(wand, player, definition.aspect(), definition.visCost())) {
            return false;
        }
        if (!level.setBlockAndUpdate(firePosition, Blocks.FIRE.defaultBlockState())) {
            return false;
        }
        level.playSound(null, firePosition, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS,
            1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
        player.swing(InteractionHand.MAIN_HAND, true);
        return true;
    }
}
