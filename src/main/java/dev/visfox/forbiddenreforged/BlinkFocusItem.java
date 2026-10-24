package dev.visfox.forbiddenreforged;

import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.Wands;
import thaumcraft.wand.focus.FocusItem;
import thaumcraft.wand.focus.FocusUpgradeType;

public class BlinkFocusItem extends FocusItem {
    private static final AspectList COST_BASE = new AspectList().add(Aspect.ENTROPY, 300);
    private static final AspectList COST_HELLFIRE = new AspectList().add(Aspect.ENTROPY, 300).add(Aspect.FIRE, 100);
    private static final AspectList COST_PANDEMONIUM = new AspectList().add(Aspect.ENTROPY, 300).add(Aspect.ORDER, 100);

    public static final FocusUpgradeType HELLFIRE = new FocusUpgradeType(100, "hellfire", new AspectList().add(FmAspects.NETHER, 1));
    public static final FocusUpgradeType PANDEMONIUM = new FocusUpgradeType(101, "pandemonium", new AspectList().add(Aspect.DARKNESS, 1));

    public BlinkFocusItem(Properties properties) {
        super(properties);
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "BL" + super.getSortingHelper(focus);
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0x909090;
    }

    @Override
    public @Nullable Identifier getFocusTexture(ItemStack focus) {
        return ForbiddenMagic.id("item/blink_focus");
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return this.isUpgradedWith(focus, HELLFIRE) ? COST_HELLFIRE : this.isUpgradedWith(focus, PANDEMONIUM) ? COST_PANDEMONIUM : COST_BASE;
    }

    @Override
    public int getActivationCooldown(ItemStack focus) {
        return 500;
    }

    @Override
    public FocusUpgradeType @Nullable [] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        switch (rank) {
            case 1:
                return new FocusUpgradeType[]{FocusUpgradeType.FRUGAL};
            case 2:
                return new FocusUpgradeType[]{HELLFIRE, PANDEMONIUM, FocusUpgradeType.FRUGAL};
            case 3:
            case 4:
            case 5:
                if (this.isUpgradedWith(focus, HELLFIRE)) {
                    return new FocusUpgradeType[]{FocusUpgradeType.POTENCY, FocusUpgradeType.FRUGAL, FocusUpgradeType.ENLARGE};
                } else if (this.isUpgradedWith(focus, PANDEMONIUM)) {
                    return new FocusUpgradeType[]{FocusUpgradeType.FRUGAL, FocusUpgradeType.ENLARGE};
                } else {
                    return new FocusUpgradeType[]{FocusUpgradeType.FRUGAL};
                }
            default:
                return null;
        }
    }

    @Override
    public InteractionResult onFocusRightClick(ItemStack wand, Level level, Player player, @Nullable HitResult hit) {
        if (!(hit instanceof BlockHitResult blockHit) || blockHit.getType() == HitResult.Type.MISS) {
            return InteractionResult.PASS;
        }
        WandItem item = (WandItem) wand.getItem();
        if (!item.consumeAllVis(wand, player, this.getVisCost(wand), !level.isClientSide(), false)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Vec3 targetPos = Vec3.atBottomCenterOf(blockHit.getBlockPos().relative(blockHit.getDirection()));
        double range = 3.0 + 1.5 * Wands.getFocusEnlarge(wand);
        ItemStack focus = Wands.getFocusItem(wand);
        if (this.isUpgradedWith(focus, HELLFIRE)) {
            AABB aabb = new AABB(targetPos.x - range, targetPos.y - range, targetPos.z - range,
                targetPos.x + range, targetPos.y + range, targetPos.z + range);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, aabb);
            int potency = Wands.getFocusPotency(wand);
            float damage = 3.0F + 3.0F * potency;
            for (LivingEntity entity : entities) {
                if (entity == player) {
                    continue;
                }
                entity.hurt(level.damageSources().inFire(), damage);
                entity.setRemainingFireTicks((3 + 3 * potency) * 20);
            }
        } else if (this.isUpgradedWith(focus, PANDEMONIUM)) {
            AABB aabb = new AABB(targetPos.x - range, targetPos.y - range, targetPos.z - range,
                targetPos.x + range, targetPos.y + range, targetPos.z + range);
            List<LivingEntity> enemies = level.getEntitiesOfClass(
                LivingEntity.class, aabb, entity -> entity instanceof Enemy);
            for (LivingEntity enemy : enemies) {
                enemy.teleportTo(player.getX(), player.getY(), player.getZ());
            }
        }
        player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }
}
