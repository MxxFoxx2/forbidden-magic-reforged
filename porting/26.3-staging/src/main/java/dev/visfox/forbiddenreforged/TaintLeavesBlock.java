package dev.visfox.forbiddenreforged;

import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer;

public class TaintLeavesBlock extends LeavesBlock {
    public TaintLeavesBlock(Properties properties) {
        super(AmbientLeavesBlockSoundPlayer.noAmbientSound(), properties);
    }
}
