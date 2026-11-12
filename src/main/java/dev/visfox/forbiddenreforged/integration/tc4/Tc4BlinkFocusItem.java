package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.wand.BlinkFocusCatalog;
import dev.visfox.forbiddenreforged.core.wand.BlinkFocusBehavior;
import dev.visfox.forbiddenreforged.core.wand.BlinkFocusRules;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import javax.annotation.Nullable;
import thaumcraft.aspect.AspectList;
import thaumcraft.wand.focus.FocusItem;
import thaumcraft.wand.focus.FocusUpgradeType;

public final class Tc4BlinkFocusItem extends FocusItem {
    private static final BlinkFocusCatalog.Presentation PRESENTATION = BlinkFocusCatalog.presentation();

    public static final FocusUpgradeType HELLFIRE = Tc4FocusUpgradeTypes.HELLFIRE;
    public static final FocusUpgradeType PANDEMONIUM = Tc4FocusUpgradeTypes.PANDEMONIUM;

    public Tc4BlinkFocusItem(Properties properties) {
        super(properties);
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return PRESENTATION.sortingPrefix() + super.getSortingHelper(focus);
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return PRESENTATION.color();
    }

    @Override
    public @Nullable ResourceLocation getFocusTexture(ItemStack focus) {
        return hostLocation(PRESENTATION.focusTexture());
    }

    @Override
    public @Nullable ResourceLocation getOrnamentTexture(ItemStack focus) {
        return hostLocation(PRESENTATION.ornamentTexture());
    }

    private static ResourceLocation hostLocation(ContentId id) {
        return ResourceLocation.fromNamespaceAndPath(id.namespace(), id.path());
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        var focusServices = HostAdapterRegistry.requireFocus();
        boolean hellfire = focusServices.hasFocusUpgrade(focus, BlinkFocusRules.Upgrade.HELLFIRE);
        boolean pandemonium = focusServices.hasFocusUpgrade(focus, BlinkFocusRules.Upgrade.PANDEMONIUM);
        return Tc4FocusAdapter.toAspectList(BlinkFocusRules.visCosts(hellfire, pandemonium));
    }

    @Override
    public @Nullable FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        if (rank < 1 || rank > 5) {
            return null;
        }
        var focusServices = HostAdapterRegistry.requireFocus();
        boolean hellfire = focusServices.hasFocusUpgrade(focus, BlinkFocusRules.Upgrade.HELLFIRE);
        boolean pandemonium = focusServices.hasFocusUpgrade(focus, BlinkFocusRules.Upgrade.PANDEMONIUM);
        return Tc4FocusAdapter.toHostUpgrades(
            BlinkFocusRules.possibleUpgradesByRank(rank, hellfire, pandemonium)
        );
    }

    @Override
    public InteractionResult onFocusRightClick(ItemStack wand, Level level,
        net.minecraft.world.entity.player.Player player, @Nullable HitResult ignored) {
        var inspection = HostAdapterRegistry.requireWandInspection();
        var focusServices = HostAdapterRegistry.requireFocus();
        ItemStack focus = inspection.wandFocusItem(wand);
        boolean hellfire = focusServices.hasFocusUpgrade(focus, BlinkFocusRules.Upgrade.HELLFIRE);
        boolean pandemonium = focusServices.hasFocusUpgrade(focus, BlinkFocusRules.Upgrade.PANDEMONIUM);
        int potency = inspection.wandFocusPotency(wand);
        int enlarge = inspection.wandFocusEnlarge(wand);
        var costs = BlinkFocusRules.visCosts(hellfire, pandemonium);
        return BlinkFocusBehavior.onRightClick(
            wand, level, player, hellfire, pandemonium, potency, enlarge,
            (stack, holder, apply) -> focusServices.consumeFocusVis(stack, holder, costs, apply)
        );
    }
}
