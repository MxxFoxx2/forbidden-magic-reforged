package dev.visfox.forbiddenreforged.test;

import dev.visfox.forbiddenreforged.FmItems;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.wand.BlinkFocusCatalog;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4AspectAdapter;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4BlinkFocusItem;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.wand.focus.FocusUpgradeType;

@GameTestHolder
@PrefixGameTestTemplate(false)
public final class BlinkFocusLifecycleGameTests {
    private BlinkFocusLifecycleGameTests() {
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void blinkFocusNativeLifecycleUsesSharedRules(GameTestHelper helper) {
        ItemStack stack = new ItemStack(FmItems.BLINK_FOCUS.get());
        helper.assertTrue(stack.getItem() instanceof Tc4BlinkFocusItem,
            "the registered Blink Focus is materialized as the native TC4 FocusItem implementation");

        Tc4BlinkFocusItem focus = (Tc4BlinkFocusItem) stack.getItem();
        Aspect entropy = Tc4AspectAdapter.resolveAspect(ContentId.parse("forbiddenmagic:concept/entropy"));
        Aspect fire = Tc4AspectAdapter.resolveAspect(ContentId.parse("forbiddenmagic:concept/fire"));
        Aspect order = Tc4AspectAdapter.resolveAspect(ContentId.parse("forbiddenmagic:concept/order"));

        AspectList activationCost = focus.getVisCost(stack);
        helper.assertValueEqual(activationCost.getAmount(entropy), 300,
            "the native FocusItem hook translates the shared base Entropy cost");
        helper.assertValueEqual(activationCost.getAmount(fire), 0,
            "an unupgraded focus does not charge Hellfire activation Vis");
        helper.assertValueEqual(activationCost.getAmount(order), 0,
            "an unupgraded focus does not charge Pandemonium activation Vis");

        FocusUpgradeType[] rankOne = focus.getPossibleUpgradesByRank(stack, 1);
        helper.assertTrue(rankOne != null && rankOne.length == 1 && rankOne[0] == FocusUpgradeType.FRUGAL,
            "the native FocusItem lifecycle exposes the shared rank-one upgrade list");

        FocusUpgradeType[] rankTwo = focus.getPossibleUpgradesByRank(stack, 2);
        helper.assertTrue(rankTwo != null && rankTwo.length == 3,
            "the native FocusItem lifecycle exposes all three shared rank-two choices");
        helper.assertTrue(rankTwo[0] == Tc4BlinkFocusItem.HELLFIRE
                && rankTwo[1] == Tc4BlinkFocusItem.PANDEMONIUM
                && rankTwo[2] == FocusUpgradeType.FRUGAL,
            "the host upgrade array preserves shared semantic ordering");

        helper.assertTrue(focus.getPossibleUpgradesByRank(stack, 0) == null
                && focus.getPossibleUpgradesByRank(stack, 6) == null,
            "unsupported ranks are rejected at the native FocusItem boundary");

        BlinkFocusCatalog.Presentation presentation = BlinkFocusCatalog.presentation();
        helper.assertValueEqual(focus.getFocusColor(stack), presentation.color(),
            "native focus color is read from shared presentation metadata");
        helper.assertValueEqual(focus.getFocusTexture(stack),
            ResourceLocation.fromNamespaceAndPath(
                presentation.focusTexture().namespace(), presentation.focusTexture().path()),
            "native focus texture is materialized from the shared content identifier");
        helper.assertValueEqual(focus.getOrnamentTexture(stack),
            ResourceLocation.fromNamespaceAndPath(
                presentation.ornamentTexture().namespace(), presentation.ornamentTexture().path()),
            "native focus ornament is materialized from the shared content identifier");
        helper.assertTrue(focus.getSortingHelper(stack).startsWith(presentation.sortingPrefix()),
            "native focus sorting uses the shared presentation prefix");
        helper.succeed();
    }
}
