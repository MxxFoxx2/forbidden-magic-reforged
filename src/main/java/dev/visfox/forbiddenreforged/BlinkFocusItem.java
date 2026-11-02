package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.core.wand.BlinkFocusBehavior;
import dev.visfox.forbiddenreforged.core.wand.BlinkFocusRules;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
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

    public static final FocusUpgradeType HELLFIRE = new FocusUpgradeType(100, "hellfire", ForbiddenMagic.id("textures/misc/hellfire.png"), new AspectList().add(FmAspects.NETHER, 1));
    public static final FocusUpgradeType PANDEMONIUM = new FocusUpgradeType(101, "pandemonium", ForbiddenMagic.id("textures/misc/pandemonium.png"), new AspectList().add(Aspect.DARKNESS, 1));

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
    public @Nullable ResourceLocation getFocusTexture(ItemStack focus) {
        return ForbiddenMagic.id("item/blink_focus");
    }

    @Override
    public @Nullable ResourceLocation getOrnamentTexture(ItemStack focus) {
        return ForbiddenMagic.id("item/blink_focus_ornament");
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return switch (BlinkFocusRules.costProfile(
            this.isUpgradedWith(focus, HELLFIRE), this.isUpgradedWith(focus, PANDEMONIUM))) {
            case HELLFIRE -> COST_HELLFIRE;
            case PANDEMONIUM -> COST_PANDEMONIUM;
            case BASE -> COST_BASE;
        };
    }

    @Override
    public @Nullable FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        if (rank < 1 || rank > 5) {
            return null;
        }
        boolean hellfire = this.isUpgradedWith(focus, HELLFIRE);
        boolean pandemonium = this.isUpgradedWith(focus, PANDEMONIUM);
        return BlinkFocusRules.possibleUpgradesByRank(rank, hellfire, pandemonium).stream()
            .map(BlinkFocusItem::hostUpgrade)
            .toArray(FocusUpgradeType[]::new);
    }

    private static FocusUpgradeType hostUpgrade(BlinkFocusRules.Upgrade upgrade) {
        return switch (upgrade) {
            case FRUGAL -> FocusUpgradeType.FRUGAL;
            case HELLFIRE -> HELLFIRE;
            case PANDEMONIUM -> PANDEMONIUM;
            case POTENCY -> FocusUpgradeType.POTENCY;
            case ENLARGE -> FocusUpgradeType.ENLARGE;
        };
    }

    public static HitResult target(net.minecraft.world.entity.player.Player player) {
        return BlinkFocusBehavior.target(player);
    }

    public static Vec3 destination(BlockHitResult hit) {
        return BlinkFocusBehavior.destination(hit);
    }

    @Override
    public InteractionResult onFocusRightClick(ItemStack wand, Level level,
        net.minecraft.world.entity.player.Player player, @Nullable HitResult ignored) {
        ItemStack focus = Wands.getFocusItem(wand);
        boolean hellfire = this.isUpgradedWith(focus, HELLFIRE);
        boolean pandemonium = this.isUpgradedWith(focus, PANDEMONIUM);
        int potency = Wands.getFocusPotency(wand);
        int enlarge = Wands.getFocusEnlarge(wand);
        return BlinkFocusBehavior.onRightClick(
            wand,
            level,
            player,
            hellfire,
            pandemonium,
            potency,
            enlarge,
            (stack, holder, apply) -> ((WandItem) stack.getItem())
                .consumeAllVis(stack, holder, this.getVisCost(stack), apply, false)
        );
    }
}
