package dev.visfox.forbiddenreforged;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import thaumcraft.aspect.Aspect;
import thaumcraft.research.ResearchManager;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.WandTriggers;
import thaumcraft.wand.Wands;

public final class FmWandTriggers {
    private FmWandTriggers() {
    }

    public static void register() {
        WandTriggers.register(
            state -> state.is(Blocks.OBSIDIAN) || state.is(Blocks.NETHERRACK),
            FmWandTriggers::hellfire
        );
    }

    private static boolean hellfire(Level level, ItemStack wand, Player player, BlockPos pos, Direction side) {
        if (level.isClientSide()
            || side != Direction.UP
            || !Wands.isWand(wand)
            || !Wands.getFocusItem(wand).isEmpty()
            || !ResearchManager.isComplete(player, "HELLFIRE")
            || !(wand.getItem() instanceof WandItem wandItem)) {
            return false;
        }

        BlockPos firePos = pos.above();
        if (!level.isEmptyBlock(firePos) || !player.mayUseItemAt(pos, Direction.UP, wand)
            || !wandItem.consumeVis(wand, player, Aspect.FIRE, 2, false)) {
            return false;
        }

        level.setBlockAndUpdate(firePos, Blocks.FIRE.defaultBlockState());
        player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
        return true;
    }
}
