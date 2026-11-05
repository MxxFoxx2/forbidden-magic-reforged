package dev.visfox.forbiddenreforged.test;

import dev.visfox.forbiddenreforged.common.FmIds;
import dev.visfox.forbiddenreforged.common.FmItems;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import dev.visfox.forbiddenreforged.integration.api.HostProfile;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4SmeltingBonusAdapter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Checks the dormant smelting-bonus contract: the bonus must be active exactly
 * when the host exposes an API to register it, and must stay inactive (with no
 * substitute mechanic) otherwise.
 */
final class SmeltingBonusGameTests {
    /** Stands in for a future host that publishes a smelting-bonus API. */
    public static final class HostWithBonusApi {
        public static final List<String> REGISTERED = new ArrayList<>();

        public static void addSmeltingBonus(ItemStack input, ItemStack bonus) {
            REGISTERED.add(input.getItem() + "->" + bonus.getItem());
        }
    }

    /** Stands in for the current host, whose bonus table stays internal. */
    public static final class HostWithoutBonusApi {
        private HostWithoutBonusApi() {
        }
    }

    private SmeltingBonusGameTests() {
    }

    static void smeltingBonusStaysDormantUntilHostExposesAnApi(GameTestHelper helper) {
        var hostPort = HostAdapterRegistry.smeltingBonuses();
        boolean hostHasApi = hostPort.supportsBonuses();

        helper.assertValueEqual(
            hostHasApi,
            Tc4SmeltingBonusAdapter.resolveRegistration(thaumcraft.device.SmeltingBonuses.class) != null,
            "the port reports bonus support exactly when the host class exposes a registration method"
        );

        if (!hostHasApi) {
            helper.assertFalse(
                hostPort.registerBonus(Items.EMERALD, FmItems.EMERALD_FRAGMENT.get()),
                "registering a bonus on a host without the API is rejected instead of approximated"
            );
        }

        var apiHost = new Tc4SmeltingBonusAdapter(HostWithBonusApi.class);
        helper.assertTrue(
            apiHost.supportsBonuses(),
            "the adapter wakes up as soon as the host exposes a registration method"
        );
        helper.assertTrue(
            apiHost.registerBonus(Items.EMERALD, FmItems.EMERALD_FRAGMENT.get()),
            "the adapter registers the emerald bonus through the host API"
        );
        helper.assertValueEqual(
            HostWithBonusApi.REGISTERED.size(),
            1,
            "the host API received exactly one bonus registration"
        );
        helper.assertValueEqual(
            HostWithBonusApi.REGISTERED.get(0),
            Items.EMERALD + "->" + FmItems.EMERALD_FRAGMENT.get(),
            "the registered bonus maps an emerald to an emerald fragment"
        );

        var internalHost = new Tc4SmeltingBonusAdapter(HostWithoutBonusApi.class);
        helper.assertFalse(
            internalHost.supportsBonuses(),
            "a host without a registration method keeps the bonus dormant"
        );
        helper.assertFalse(
            internalHost.registerBonus(Items.EMERALD, FmItems.EMERALD_FRAGMENT.get()),
            "a dormant adapter does not register anything"
        );

        helper.assertTrue(
            HostProfile.Feature.SMELTING_BONUSES != null,
            "the smelting-bonus capability is declared in the host profile"
        );
        FmIds.LOGGER.info("Smelting bonus support on this host: {}", hostHasApi);
        helper.succeed();
    }
}
