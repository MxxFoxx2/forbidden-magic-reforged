package dev.visfox.forbiddenreforged;

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
        BlockPos firePos = pos.above();
        if (level.isClientSide()
            || side != Direction.UP
            || !Wands.isWand(wand)
            || Wands.getFocus(wand) != null
            || !ResearchManager.isComplete(player, "HELLFIRE")
            || !player.mayUseItemAt(firePos, Direction.UP, new ItemStack(Items.FIRE_CHARGE))
            || !level.isEmptyBlock(firePos)
            || !(wand.getItem() instanceof WandItem wandItem)
            || !wandItem.consumeVis(wand, player, Aspect.FIRE, 2, false)) {
            return false;
        }
        if (!level.setBlockAndUpdate(firePos, Blocks.FIRE.defaultBlockState())) {
            return false;
        }
        level.playSound(null, firePos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS,
            1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
        player.swing(InteractionHand.MAIN_HAND);
        return true;
    }
}
