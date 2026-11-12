package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.aspect.ForbiddenMagicAspectCatalog;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.core.wand.BlinkFocusBehavior;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.wand.WandItem;
import thaumcraft.wand.Wands;
import thaumcraft.wand.focus.FocusItem;
import thaumcraft.wand.focus.FocusUpgradeType;

public class Tc4BlinkFocusItem extends FocusItem {
    private static final AspectList COST_BASE = new AspectList().add(Aspect.ENTROPY, 300);
    private static final AspectList COST_HELLFIRE = new AspectList().add(Aspect.ENTROPY, 300).add(Aspect.FIRE, 100);
    private static final AspectList COST_PANDEMONIUM = new AspectList().add(Aspect.ENTROPY, 300).add(Aspect.ORDER, 100);

    public static final FocusUpgradeType HELLFIRE = new FocusUpgradeType(100, "hellfire",
        ForbiddenMagic.id("textures/misc/hellfire.png"), new AspectList().add(Tc4AspectAdapter.resolveAspect(ForbiddenMagicAspectCatalog.INFERNUS), 1));
    public static final FocusUpgradeType PANDEMONIUM = new FocusUpgradeType(101, "pandemonium",
        ForbiddenMagic.id("textures/misc/pandemonium.png"), new AspectList().add(Aspect.DARKNESS, 1));

    public Tc4BlinkFocusItem(Properties properties) {
        super(properties);
    }

    public static Properties properties(Properties properties) {
        return FocusItem.properties(properties);
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
    public @Nullable Identifier getOrnamentTexture(ItemStack focus) {
        return ForbiddenMagic.id("item/blink_focus_ornament");
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return this.isUpgradedWith(focus, HELLFIRE)
            ? COST_HELLFIRE
            : this.isUpgradedWith(focus, PANDEMONIUM) ? COST_PANDEMONIUM : COST_BASE;
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
    public InteractionResult onFocusRightClick(
        ItemStack wand,
        Level level,
        Player player,
        @Nullable HitResult ignored
    ) {
        if (!(wand.getItem() instanceof WandItem wandItem)) {
            return InteractionResult.PASS;
        }
        ItemStack focus = Wands.getFocusItem(wand);
        boolean hellfire = this.isUpgradedWith(focus, HELLFIRE);
        boolean pandemonium = this.isUpgradedWith(focus, PANDEMONIUM);
        return BlinkFocusBehavior.onRightClick(
            wand,
            level,
            player,
            hellfire,
            pandemonium,
            Wands.getFocusPotency(wand),
            Wands.getFocusEnlarge(wand),
            (stack, caster, apply) -> wandItem.consumeAllVis(stack, caster, getVisCost(stack), apply, false)
        );
    }
}
