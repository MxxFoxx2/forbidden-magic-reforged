package dev.visfox.forbiddenreforged.test;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.Direction;
import thaumcraft.taint.TaintSpread;
import dev.visfox.forbiddenreforged.TaintLogTextureVariant;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import dev.visfox.forbiddenreforged.FmBlocks;
import dev.visfox.forbiddenreforged.FmSpawnerMobs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.InfusionRecipe;
import thaumcraft.research.ResearchItem;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import dev.visfox.forbiddenreforged.integration.api.HostProfile;
import dev.visfox.forbiddenreforged.FmAspects;
import dev.visfox.forbiddenreforged.FmConfig;
import dev.visfox.forbiddenreforged.FmItems;
import dev.visfox.forbiddenreforged.FmCreativeTab;
import dev.visfox.forbiddenreforged.FmToolEvents;
import dev.visfox.forbiddenreforged.FmWands;
import dev.visfox.forbiddenreforged.FmComponents;
import dev.visfox.forbiddenreforged.MobCrystalItem;
import dev.visfox.forbiddenreforged.WrathCageBlock;
import dev.visfox.forbiddenreforged.WrathCageBlockEntity;
import dev.visfox.forbiddenreforged.core.block.WrathCageBehavior;
import dev.visfox.forbiddenreforged.core.block.WrathCageStorage;
import dev.visfox.forbiddenreforged.core.block.WrathCageState;
import dev.visfox.forbiddenreforged.core.block.WrathCageSpawnBehavior;
import net.minecraft.world.phys.AABB;
import dev.visfox.forbiddenreforged.core.wand.WitchwoodManaBehavior;
import dev.visfox.forbiddenreforged.core.wand.BlinkFocusRules;
import dev.visfox.forbiddenreforged.core.wand.WandMaterialCatalog;
import dev.visfox.forbiddenreforged.core.wand.WandTriggerCatalog;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.FmBlockEntities;
import dev.visfox.forbiddenreforged.FmItemEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.fml.ModList;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectRegistry;
import thaumcraft.crafting.TcRecipes;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.research.ResearchCategories;
import thaumcraft.wand.WandRegistry;
import thaumcraft.wand.Wands;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;

@GameTestHolder
@PrefixGameTestTemplate(false)
public final class FmGameTests {
    private FmGameTests() {
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void wrathCageStorageOwnsBoundedChannelsAndMode(GameTestHelper helper) {
        WrathCageStorage storage = new WrathCageStorage();
        helper.assertTrue(storage.hasRoom(), "empty cage storage has room");
        helper.assertValueEqual(storage.add(WrathCageStorage.Channel.MOB, 70), 64,
            "mob channel accepts only its capacity");
        helper.assertValueEqual(storage.add(WrathCageStorage.Channel.MOB, 1), 0,
            "full mob channel rejects more essentia");
        helper.assertValueEqual(storage.add(WrathCageStorage.Channel.WRATH, 64), 64,
            "Wrath channel has independent capacity");
        helper.assertValueEqual(storage.add(WrathCageStorage.Channel.SLOTH, 64), 64,
            "Sloth channel has independent capacity");
        helper.assertValueEqual(storage.total(), 192, "total is the sum of all channels");
        helper.assertTrue(!storage.hasRoom(), "three full channels leave no room");
        helper.assertValueEqual(storage.remove(WrathCageStorage.Channel.MOB, 12), 12,
            "removal takes the requested available amount");
        helper.assertValueEqual(storage.amount(WrathCageStorage.Channel.MOB), 52,
            "removal preserves the channel remainder");
        helper.assertTrue(storage.hasRoom(), "removal restores channel capacity");
        helper.assertValueEqual(storage.remove(WrathCageStorage.Channel.WRATH, 100), 64,
            "removal cannot exceed stored amount");
        helper.assertValueEqual(storage.remove(WrathCageStorage.Channel.SLOTH, -1), 0,
            "negative removal is ignored");
        helper.assertValueEqual(storage.selectedChannel(), WrathCageStorage.Channel.MOB,
            "default display selects mob aspect");
        helper.assertValueEqual(storage.cycleMode(), 1, "first cycle selects Wrath");
        helper.assertValueEqual(storage.selectedChannel(), WrathCageStorage.Channel.WRATH,
            "second display mode selects Wrath");
        helper.assertValueEqual(storage.selectedAmount(), 0,
            "selected amount follows the display mode");
        helper.assertValueEqual(storage.cycleMode(), 2, "second cycle selects Sloth");
        helper.assertValueEqual(storage.selectedChannel(), WrathCageStorage.Channel.SLOTH,
            "third display mode selects Sloth");
        helper.assertValueEqual(storage.cycleMode(), 0, "third cycle returns to mob");

        ContentId mobAspect = ContentId.parse("thaumcraft:greed");
        ContentId wrathAspect = ContentId.parse("forbiddenmagic:ira");
        ContentId slothAspect = ContentId.parse("forbiddenmagic:desidia");
        helper.assertValueEqual(storage.channelFor(mobAspect, mobAspect, wrathAspect, slothAspect),
            WrathCageStorage.Channel.MOB, "mob aspect maps to the mob storage channel");
        helper.assertValueEqual(storage.channelFor(wrathAspect, mobAspect, wrathAspect, slothAspect),
            WrathCageStorage.Channel.WRATH, "Wrath aspect maps to the Wrath storage channel");
        helper.assertValueEqual(storage.channelFor(slothAspect, mobAspect, wrathAspect, slothAspect),
            WrathCageStorage.Channel.SLOTH, "Sloth aspect maps to the Sloth storage channel");
        helper.assertTrue(storage.channelFor(ContentId.parse("minecraft:stone"), mobAspect, wrathAspect, slothAspect) == null,
            "unrelated semantic aspects do not map to cage storage");
        helper.assertTrue(storage.channelFor(wrathAspect, mobAspect, null, null) == null,
            "unavailable optional aspects are not accepted");

        WrathCageStorage extractionStorage = new WrathCageStorage();
        helper.assertValueEqual(extractionStorage.extractionOrder(12),
            List.of(WrathCageStorage.Channel.MOB, WrathCageStorage.Channel.WRATH,
                WrathCageStorage.Channel.SLOTH),
            "empty channels request essentia in mob, Wrath, Sloth order");
        extractionStorage.setAmount(WrathCageStorage.Channel.MOB, 12);
        helper.assertValueEqual(extractionStorage.extractionOrder(12),
            List.of(WrathCageStorage.Channel.MOB),
            "Wrath and Sloth extraction pause when mob essentia reaches the fuel threshold");
        extractionStorage.setAmount(WrathCageStorage.Channel.MOB, 0);
        extractionStorage.setAmount(WrathCageStorage.Channel.WRATH, 12);
        helper.assertValueEqual(extractionStorage.extractionOrder(12),
            List.of(WrathCageStorage.Channel.MOB, WrathCageStorage.Channel.WRATH),
            "Sloth extraction pauses when Wrath reaches the fuel threshold");
        helper.assertTrue(extractionStorage.extractionOrder(0).isEmpty(),
            "disabled essentia cost requests no transport");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void wrathCageStorageSnapshotsPreserveLegacyState(GameTestHelper helper) {
        WrathCageStorage storage = new WrathCageStorage();
        helper.assertValueEqual(storage.snapshot(), new WrathCageStorage.State(0, 0, 0, 0),
            "new storage starts from the legacy empty state");

        // Preserve values as read from older saves; do not clamp or reinterpret them here.
        storage.setAmount(WrathCageStorage.Channel.MOB, 65);
        storage.setAmount(WrathCageStorage.Channel.WRATH, -3);
        storage.setAmount(WrathCageStorage.Channel.SLOTH, 41);
        storage.setMode(7);
        WrathCageStorage.State saved = storage.snapshot();
        helper.assertValueEqual(saved, new WrathCageStorage.State(65, -3, 41, 7),
            "snapshot records every channel and the raw display mode");

        storage.setAmount(WrathCageStorage.Channel.MOB, 0);
        storage.setAmount(WrathCageStorage.Channel.WRATH, 0);
        storage.setAmount(WrathCageStorage.Channel.SLOTH, 0);
        storage.setMode(1);
        storage.restore(saved);
        helper.assertValueEqual(storage.snapshot(), saved,
            "restore round-trips all persisted storage values exactly");
        helper.assertValueEqual(storage.selectedChannel(), WrathCageStorage.Channel.SLOTH,
            "legacy unknown display modes retain the Sloth fallback");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void wrathCageStateOwnsAttunementAndPersistentRuntimeFields(GameTestHelper helper) {
        WrathCageState state = new WrathCageState();
        helper.assertValueEqual(state.snapshot(),
            new WrathCageState.Snapshot(null, new WrathCageStorage.State(0, 0, 0, 0), 0, 20, false),
            "new cage state has legacy defaults");

        state.storage().setAmount(WrathCageStorage.Channel.MOB, 12);
        state.setMobId("minecraft:zombie");
        helper.assertValueEqual(state.storage().amount(WrathCageStorage.Channel.MOB), 0,
            "changing the attuned mob clears mob-specific essentia");
        state.storage().setAmount(WrathCageStorage.Channel.MOB, 8);
        state.setMobId("minecraft:zombie");
        helper.assertValueEqual(state.storage().amount(WrathCageStorage.Channel.MOB), 8,
            "setting the same mob keeps its accumulated essentia");

        state.setFuel(6);
        state.consumeFuel();
        state.setSpawnDelay(99);
        state.setSlothful(true);
        WrathCageState.Snapshot saved = state.snapshot();
        helper.assertValueEqual(saved,
            new WrathCageState.Snapshot("minecraft:zombie",
                new WrathCageStorage.State(8, 0, 0, 0), 5, 99, true),
            "snapshot includes attunement, storage, fuel, delay, and Sloth mode");

        WrathCageState restored = new WrathCageState();
        restored.restore(saved);
        helper.assertValueEqual(restored.snapshot(), saved,
            "complete cage state restores without running attunement side effects");

        restored.setMobId(null);
        helper.assertValueEqual(restored.storage().amount(WrathCageStorage.Channel.MOB), 0,
            "clearing attunement also clears mob-specific essentia");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void wrathCageStateOwnsSpawnEligibilityAndFuelPriority(GameTestHelper helper) {
        WrathCageState empty = new WrathCageState();
        helper.assertTrue(!empty.canSpawn(false, 0),
            "an unattuned cage cannot spawn even when essentia cost is disabled");
        empty.setMobId("minecraft:zombie");
        helper.assertTrue(empty.canSpawn(false, 0),
            "an unattuned-free cage can spawn when essentia cost is disabled");
        helper.assertTrue(!empty.canSpawn(true, 0),
            "redstone power blocks spawn attempts");

        WrathCageState mobPriority = new WrathCageState();
        mobPriority.setMobId("minecraft:zombie");
        mobPriority.storage().setAmount(WrathCageStorage.Channel.MOB, 5);
        mobPriority.storage().setAmount(WrathCageStorage.Channel.WRATH, 5);
        mobPriority.storage().setAmount(WrathCageStorage.Channel.SLOTH, 5);
        helper.assertTrue(mobPriority.canSpawn(false, 5),
            "the spawn gate sees the combined stored amount");
        helper.assertTrue(mobPriority.loadFuel(5, 120),
            "mob essentia can be converted into fuel");
        helper.assertValueEqual(mobPriority.storage().amount(WrathCageStorage.Channel.MOB), 0,
            "mob essentia has first fuel priority");
        helper.assertValueEqual(mobPriority.storage().amount(WrathCageStorage.Channel.WRATH), 5,
            "mob fuel selection leaves Wrath untouched");
        helper.assertValueEqual(mobPriority.fuel(), 120,
            "fuel duration is applied after channel consumption");
        helper.assertTrue(!mobPriority.slothful(),
            "mob fuel does not enable Sloth delay");

        WrathCageState wrathPriority = new WrathCageState();
        wrathPriority.setMobId("minecraft:zombie");
        wrathPriority.storage().setAmount(WrathCageStorage.Channel.WRATH, 5);
        wrathPriority.storage().setAmount(WrathCageStorage.Channel.SLOTH, 5);
        helper.assertTrue(wrathPriority.loadFuel(5, 90),
            "Wrath essentia can be converted when mob essentia is absent");
        helper.assertValueEqual(wrathPriority.storage().amount(WrathCageStorage.Channel.WRATH), 0,
            "Wrath has priority over Sloth");
        helper.assertTrue(!wrathPriority.slothful(),
            "Wrath fuel does not enable Sloth delay");

        WrathCageState slothPriority = new WrathCageState();
        slothPriority.setMobId("minecraft:zombie");
        slothPriority.storage().setAmount(WrathCageStorage.Channel.SLOTH, 5);
        helper.assertTrue(slothPriority.loadFuel(5, 60),
            "Sloth essentia can be converted when higher-priority channels are absent");
        helper.assertValueEqual(slothPriority.storage().amount(WrathCageStorage.Channel.SLOTH), 0,
            "Sloth fuel is consumed from the Sloth channel");
        helper.assertTrue(slothPriority.slothful(),
            "Sloth fuel enables the longer delay");

        WrathCageState splitChannels = new WrathCageState();
        splitChannels.setMobId("minecraft:zombie");
        splitChannels.storage().setAmount(WrathCageStorage.Channel.MOB, 2);
        splitChannels.storage().setAmount(WrathCageStorage.Channel.WRATH, 3);
        helper.assertTrue(splitChannels.canSpawn(false, 5),
            "the initial gate still uses the total across channels");
        helper.assertTrue(!splitChannels.loadFuel(5, 60),
            "fuel cannot be assembled by combining incomplete channels");
        helper.assertValueEqual(splitChannels.fuel(), 0,
            "failed fuel selection does not create fuel");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void wrathCageStateAdvancesSpawnDelayInCore(GameTestHelper helper) {
        WrathCageState state = new WrathCageState();
        helper.assertValueEqual(state.spawnDelay(), 20,
            "new cage state retains the legacy initial delay");
        helper.assertTrue(state.advanceSpawnDelay(),
            "a positive delay consumes one wait tick");
        helper.assertValueEqual(state.spawnDelay(), 19,
            "advancing a wait tick decrements the delay once");

        state.setSpawnDelay(1);
        helper.assertTrue(state.advanceSpawnDelay(),
            "the final positive delay tick still defers spawning");
        helper.assertValueEqual(state.spawnDelay(), 0,
            "the final wait tick reaches zero");
        helper.assertTrue(!state.advanceSpawnDelay(),
            "spawning may proceed on the tick after delay reaches zero");
        helper.assertValueEqual(state.spawnDelay(), 0,
            "an elapsed delay stays at zero");

        state.setSpawnDelay(-4);
        helper.assertTrue(!state.advanceSpawnDelay(),
            "legacy negative delay values do not create an additional wait");
        helper.assertValueEqual(state.spawnDelay(), -4,
            "negative delay values are preserved without normalization");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void wrathCageSpawnBehaviorPreservesRuntimeLimitsAndBounds(GameTestHelper helper) {
        helper.assertTrue(WrathCageSpawnBehavior.shouldAttemptSpawn(0, 0, 0),
            "free-spawn mode permits the first attempt without fuel");
        helper.assertTrue(WrathCageSpawnBehavior.shouldAttemptSpawn(2, 0, 0),
            "free-spawn mode permits all three configured attempts");
        helper.assertTrue(!WrathCageSpawnBehavior.shouldAttemptSpawn(3, 0, 0),
            "a spawn round is limited to three attempts");
        helper.assertTrue(!WrathCageSpawnBehavior.shouldAttemptSpawn(0, 0, 12),
            "paid spawning requires loaded fuel");
        helper.assertTrue(WrathCageSpawnBehavior.shouldAttemptSpawn(0, 1, 12),
            "loaded fuel permits a paid spawn attempt");
        helper.assertTrue(!WrathCageSpawnBehavior.shouldAttemptSpawn(3, 1, 12),
            "fuel does not bypass the per-round attempt limit");

        helper.assertTrue(!WrathCageSpawnBehavior.hasReachedEntityLimit(5),
            "five matching nearby entities remain below the cap");
        helper.assertTrue(WrathCageSpawnBehavior.hasReachedEntityLimit(6),
            "six matching nearby entities reach the cap");
        helper.assertTrue(WrathCageSpawnBehavior.hasReachedEntityLimit(7),
            "an already exceeded entity cap still blocks further spawns");

        AABB bounds = WrathCageSpawnBehavior.nearbyBounds(new BlockPos(10, 20, 30));
        helper.assertValueEqual(bounds.minX, 6.0, "nearby bounds extend four blocks west");
        helper.assertValueEqual(bounds.minY, 19.0, "nearby bounds extend one block down");
        helper.assertValueEqual(bounds.minZ, 26.0, "nearby bounds extend four blocks north");
        helper.assertValueEqual(bounds.maxX, 15.0, "nearby bounds extend five blocks east");
        helper.assertValueEqual(bounds.maxY, 22.0, "nearby bounds extend two blocks up");
        helper.assertValueEqual(bounds.maxZ, 35.0, "nearby bounds extend five blocks south");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void wandTriggerCatalogPreservesHellfireDefinition(GameTestHelper helper) {
        var trigger = WandTriggerCatalog.hellfire();
        helper.assertValueEqual(trigger.id(), ContentId.parse("forbiddenmagic:wand_trigger/hellfire"),
            "Hellfire trigger has a stable semantic identifier");
        helper.assertValueEqual(trigger.blocks(),
            List.of(ContentId.parse("minecraft:obsidian"), ContentId.parse("minecraft:netherrack")),
            "Hellfire trigger remains bound to obsidian and netherrack");
        helper.assertValueEqual(trigger.research(), ContentId.parse("forbiddenmagic:research/hellfire"),
            "Hellfire trigger retains its research gate");
        helper.assertValueEqual(trigger.aspect(), ContentId.parse("forbiddenmagic:concept/fire"),
            "Hellfire trigger consumes the semantic Fire aspect");
        helper.assertValueEqual(trigger.visCost(), 2,
            "Hellfire trigger retains its two-Vis cost");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void wandMaterialCatalogPreservesHostRegistrationData(GameTestHelper helper) {
        var withoutArsMagica = WandMaterialCatalog.rods(false);
        var withArsMagica = WandMaterialCatalog.rods(true);
        helper.assertValueEqual(withoutArsMagica.size(), 7,
            "base wand catalog contains the seven unconditional rods");
        helper.assertValueEqual(withArsMagica.size(), 9,
            "optional Ars Magica integration contributes wand and staff rods");
        helper.assertTrue(withoutArsMagica.stream().noneMatch(rod ->
                rod.update() == WandMaterialCatalog.RodUpdateKind.WITCHWOOD_WAND
                    || rod.update() == WandMaterialCatalog.RodUpdateKind.WITCHWOOD_STAFF),
            "Witchwood rods are omitted when Ars Magica is unavailable");
        helper.assertTrue(withArsMagica.stream().anyMatch(rod ->
                rod.update() == WandMaterialCatalog.RodUpdateKind.WITCHWOOD_STAFF
                    && rod.staff() && rod.capacity() == 50 && rod.cost() == 24),
            "Witchwood staff retains its staff flag, capacity and crafting cost");
        helper.assertTrue(withoutArsMagica.stream().anyMatch(rod ->
                rod.tag().equals("profane") && rod.capacity() == 50
                    && rod.update() == WandMaterialCatalog.RodUpdateKind.PROFANE),
            "Profane rod retains its finite-warp update behavior");
        helper.assertTrue(withoutArsMagica.stream().anyMatch(rod ->
                rod.tag().equals("neutronium") && rod.capacity() == 9002 && rod.staff()
                    && rod.update() == WandMaterialCatalog.RodUpdateKind.CREATIVE),
            "Neutronium staff retains its creative update profile");
        var caps = WandMaterialCatalog.caps();
        helper.assertValueEqual(caps.size(), 2, "both custom wand caps are catalogued");
        helper.assertTrue(caps.stream().anyMatch(cap ->
                cap.tag().equals("alchemical") && cap.multiplier() == 0.9F
                    && cap.discount() == 0.8F && cap.cost() == 7
                    && cap.aspects().equals(List.of(ContentId.parse("thaumcraft:aqua")))),
            "Alchemical cap retains its water aspect and original tuning");
        helper.assertTrue(caps.stream().anyMatch(cap ->
                cap.tag().equals("orichalcum") && cap.multiplier() == 0.0F
                    && cap.discount() == 0.0F && cap.aspects().isEmpty()),
            "Orichalcum cap retains its zero-cost creative profile");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void blinkFocusUpgradeProgressionRemainsStable(GameTestHelper helper) {
        helper.assertValueEqual(BlinkFocusRules.possibleUpgradesByRank(1, false, false),
            List.of(BlinkFocusRules.Upgrade.FRUGAL), "rank one only offers Frugal");
        helper.assertValueEqual(BlinkFocusRules.possibleUpgradesByRank(2, false, false),
            List.of(BlinkFocusRules.Upgrade.HELLFIRE, BlinkFocusRules.Upgrade.PANDEMONIUM,
                BlinkFocusRules.Upgrade.FRUGAL), "rank two offers both special paths and Frugal");
        helper.assertValueEqual(BlinkFocusRules.possibleUpgradesByRank(4, true, false),
            List.of(BlinkFocusRules.Upgrade.POTENCY, BlinkFocusRules.Upgrade.FRUGAL,
                BlinkFocusRules.Upgrade.ENLARGE), "Hellfire path unlocks Potency and Enlarge");
        helper.assertValueEqual(BlinkFocusRules.possibleUpgradesByRank(4, false, true),
            List.of(BlinkFocusRules.Upgrade.FRUGAL, BlinkFocusRules.Upgrade.ENLARGE),
            "Pandemonium path excludes Potency");
        helper.assertValueEqual(BlinkFocusRules.possibleUpgradesByRank(4, false, false),
            List.of(BlinkFocusRules.Upgrade.FRUGAL), "base path only retains Frugal");
        helper.assertTrue(BlinkFocusRules.possibleUpgradesByRank(6, false, false).isEmpty(),
            "unsupported focus ranks expose no upgrades");
        helper.assertValueEqual(BlinkFocusRules.costProfile(true, true),
            BlinkFocusRules.CostProfile.HELLFIRE, "Hellfire cost takes precedence when both flags are set");
        helper.assertValueEqual(BlinkFocusRules.costProfile(false, true),
            BlinkFocusRules.CostProfile.PANDEMONIUM, "Pandemonium selects its own cost profile");
        helper.assertValueEqual(BlinkFocusRules.costProfile(false, false),
            BlinkFocusRules.CostProfile.BASE, "unupgraded focus uses base cost");
        helper.assertValueEqual(BlinkFocusRules.visCosts(false, false),
            List.of(new BlinkFocusRules.VisCost(ContentId.parse("forbiddenmagic:concept/entropy"), 300)),
            "base focus costs 300 Entropy");
        helper.assertValueEqual(BlinkFocusRules.visCosts(true, false),
            List.of(new BlinkFocusRules.VisCost(ContentId.parse("forbiddenmagic:concept/entropy"), 300),
                new BlinkFocusRules.VisCost(ContentId.parse("forbiddenmagic:concept/fire"), 100)),
            "Hellfire focus adds 100 Fire");
        helper.assertValueEqual(BlinkFocusRules.visCosts(false, true),
            List.of(new BlinkFocusRules.VisCost(ContentId.parse("forbiddenmagic:concept/entropy"), 300),
                new BlinkFocusRules.VisCost(ContentId.parse("forbiddenmagic:concept/order"), 100)),
            "Pandemonium focus adds 100 Order");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void hostAdapterDeclaresThe1_21_1CapabilityProfile(GameTestHelper helper) {
        HostProfile profile = HostAdapterRegistry.require().profile();
        helper.assertValueEqual(profile.minecraftVersion(), "1.21.1",
            "the selected TC4 adapter must target Minecraft 1.21.1");
        helper.assertValueEqual(profile.hostVersion(), "1.1.2",
            "the selected adapter must identify the pinned Thaumcraft host version");
        helper.assertValueEqual(profile.features().size(), HostProfile.Feature.values().length,
            "the host profile must declare every capability");

        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.ASPECT_REGISTRATION).level(),
            HostProfile.SupportLevel.ADAPTED, "aspect registration uses the shared catalog adapter");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.ITEM_PROPERTIES).level(),
            HostProfile.SupportLevel.ADAPTED, "item properties are supplied through host capabilities");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.ITEM_FACTORIES).level(),
            HostProfile.SupportLevel.ADAPTED, "host-backed item factories are routed through capabilities");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.MORPH_TOOL_RUNTIME).level(),
            HostProfile.SupportLevel.ADAPTED, "morph tool behavior uses host services");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.TAINT_BLOCK_SERVICES).level(),
            HostProfile.SupportLevel.ADAPTED, "taint block services use the host adapter");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.TAINT_ORIGINS).level(),
            HostProfile.SupportLevel.UNSUPPORTED, "taint origins are explicitly declared unsupported");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.WAND_CAPS).level(),
            HostProfile.SupportLevel.PARTIAL, "wand cap definitions are shared but registration remains TC4-specific");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.WAND_RODS).level(),
            HostProfile.SupportLevel.PARTIAL, "wand rod definitions are shared but callbacks remain TC4-specific");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.WAND_PARTS).level(),
            HostProfile.SupportLevel.PARTIAL, "Profane Wand part mutation still uses TC4 WandParts and WandKeys");
        helper.assertTrue(HostAdapterRegistry.requireWandParts() != null,
            "wand part mutation is exposed through the selected host capability");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.WAND_TRIGGERS).level(),
            HostProfile.SupportLevel.PARTIAL, "Hellfire trigger rules are shared but registration remains TC4-specific");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.FOCI_OR_SPELLS).level(),
            HostProfile.SupportLevel.PARTIAL, "Blink Focus lifecycle and rendering remain TC4-bound");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.ESSENTIA_STORAGE).level(),
            HostProfile.SupportLevel.PARTIAL, "shared storage still relies on TC4 persistence callbacks");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.ESSENTIA_TRANSPORT).level(),
            HostProfile.SupportLevel.PARTIAL, "transport policy is shared but EssentiaTransport remains host-bound");
        helper.assertTrue(HostAdapterRegistry.requireEssentiaTransport() != null,
            "physical essentia pulls use a typed host capability");
        helper.assertTrue(HostAdapterRegistry.requireFocus() != null,
            "focus upgrade inspection and Vis payment are routed through a typed host capability");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.VILLAGER_TRADES).level(),
            HostProfile.SupportLevel.ADAPTED, "Thaumcraft-specific heretic trades are supplied by the host adapter");
        helper.assertValueEqual(profile.supportOf(HostProfile.Feature.RUNTIME_BOOTSTRAP).level(),
            HostProfile.SupportLevel.ADAPTED, "host bootstrap capability is implemented");
        int optionalEnchantments = HostAdapterRegistry.requireBootstrap().registeredOptionalEnchantments();
        int expectedOptionalEnchantments = FmConfig.enchantmentsEnabled()
            && ModList.get().isLoaded("thaumictinkerer") ? 8 : 0;
        helper.assertValueEqual(optionalEnchantments, expectedOptionalEnchantments,
            "the host bootstrap registers the complete optional Thaumic Tinkerer cost set or none");
        boolean hasTaintedCreativeWand = FmCreativeTab.stacks().stream()
            .anyMatch(stack -> Wands.isWand(stack) && "tainted".equals(Wands.getRod(stack).tag()));
        helper.assertTrue(hasTaintedCreativeWand,
            "the host bootstrap contributes a Tainted Wand to the shared creative tab");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void taintCoalHasFuelValue(GameTestHelper helper) {
        FurnaceFuelBurnTimeEvent event = new FurnaceFuelBurnTimeEvent(
            new ItemStack(FmItems.TAINT_COAL.get()), 0, RecipeType.SMELTING
        );
        FmItemEvents.onFuel(event);
        helper.assertTrue(event.getBurnTime() >= 1 && event.getBurnTime() <= 2400,
            "taint coal burn time must remain in the original 1..2400 range");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void itemRegistryContainsPortedBaseItems(GameTestHelper helper) {
        helper.assertTrue(BuiltInRegistries.ITEM.get(ForbiddenMagic.id("taint_fruit")) != net.minecraft.world.item.Items.AIR,
            "taint fruit must be registered");
        helper.assertTrue(BuiltInRegistries.ITEM.get(ForbiddenMagic.id("black_dye")) != net.minecraft.world.item.Items.AIR,
            "black dye must be registered");
        helper.assertTrue(BuiltInRegistries.ITEM.get(ForbiddenMagic.id("lust_shard")) != net.minecraft.world.item.Items.AIR,
            "lust shard must be registered");
        helper.succeed();
    }


    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void taintAndUmbralBlocksAreRegistered(GameTestHelper helper) {
        helper.assertTrue(BuiltInRegistries.BLOCK.get(ForbiddenMagic.id("taint_log")) != Blocks.AIR,
            "taint log must be registered");
        helper.assertTrue(BuiltInRegistries.BLOCK.get(ForbiddenMagic.id("taint_sapling")) != Blocks.AIR,
            "taint sapling must be registered");
        helper.assertTrue(BuiltInRegistries.BLOCK.get(ForbiddenMagic.id("umbral_rose")) != Blocks.AIR,
            "umbral rose must be registered");
        helper.assertTrue(BuiltInRegistries.BLOCK.get(ForbiddenMagic.id("arcane_cake")) != Blocks.AIR,
            "arcane cake must be registered");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void mobCrystalAttunesAndSwapsCageTarget(GameTestHelper helper) {
        var cage = new WrathCageBlockEntity(new net.minecraft.core.BlockPos(0, 0, 0),
            dev.visfox.forbiddenreforged.FmBlocks.WRATH_CAGE.get().defaultBlockState());
        ItemStack crystal = new ItemStack(FmItems.MOB_CRYSTAL.get());
        MobCrystalItem.setMob(crystal, "minecraft:zombie");
        helper.assertTrue(WrathCageBlock.attune(cage, crystal), "valid crystal should attune cage");
        helper.assertValueEqual(cage.getMobId(), "minecraft:zombie", "cage should remember the captured entity");
        helper.assertTrue(MobCrystalItem.mob(crystal) == null, "crystal should be consumed into an empty cage");
        ItemStack second = new ItemStack(FmItems.MOB_CRYSTAL.get());
        MobCrystalItem.setMob(second, "minecraft:skeleton");
        helper.assertTrue(WrathCageBlock.attune(cage, second), "second crystal should swap cage target");
        helper.assertValueEqual(cage.getMobId(), "minecraft:skeleton", "cage should change its target");
        helper.assertValueEqual(MobCrystalItem.mob(second), "minecraft:zombie", "old target should be returned in the crystal");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void forbiddenWandRodsAreRegistered(GameTestHelper helper) {
        boolean am2 = ModList.get().isLoaded("arsmagica2");
        if (am2) {
            helper.assertTrue(WandRegistry.findRod("witchwood").onUpdate() != null,
                "Witchwood wand core must have its mana recharge update");
            helper.assertTrue(WandRegistry.findRod("witchwood_staff").onUpdate() != null,
                "Witchwood staff core must have its faster mana recharge update");
        }

        helper.assertTrue(WandRegistry.findRod("tainted") != null, "tainted rod must be registered");
        helper.assertTrue(WandRegistry.findRod("infernal") != null, "infernal rod must be registered");
        helper.assertTrue(WandRegistry.findRod("profane") != null, "profane rod must be registered");
        helper.assertValueEqual(WandRegistry.findRod("witchwood") != null, am2, "witchwood rod registration follows Ars Magica 2");
        helper.assertValueEqual(WandRegistry.findRod("witchwood_staff") != null, am2, "witchwood staff rod registration follows Ars Magica 2");
        helper.assertTrue(BuiltInRegistries.BLOCK_ENTITY_TYPE.get(ForbiddenMagic.id("wrath_cage")) == FmBlockEntities.WRATH_CAGE.get(),
            "Wrath Cage block entity type must be registered");
        helper.assertTrue(WrathCageBlockEntity.shouldSpawn(false, true, 0, 12, 12), "cage should spawn when fuel is available");
        helper.assertTrue(!WrathCageBlockEntity.shouldSpawn(true, true, 0, 12, 12), "redstone must disable spawning");
        helper.assertTrue(!WrathCageBlockEntity.shouldSpawn(false, false, 0, 12, 12), "unattuned cage must not spawn");
        helper.assertValueEqual(WrathCageBehavior.acceptedAmount(60, 10, 64), 4,
            "essentia insertion must respect the remaining per-channel capacity");
        helper.assertValueEqual(WrathCageBehavior.acceptedAmount(64, 10, 64), 0,
            "a full essentia channel must reject additional storage");
        helper.assertValueEqual(WrathCageBehavior.acceptedAmount(0, -5, 64), 0,
            "negative essentia requests must not change storage");
        helper.assertTrue(WrathCageBehavior.canDrawMobEssentia(63, 64),
            "mob essentia can be drawn while its channel has room");
        helper.assertTrue(!WrathCageBehavior.canDrawMobEssentia(64, 64),
            "mob essentia must stop at channel capacity");
        helper.assertTrue(WrathCageBehavior.canDrawWrathEssentia(11, 63, 64, 12),
            "Wrath can be drawn until its own channel fills or mob fuel reaches cost");
        helper.assertTrue(!WrathCageBehavior.canDrawWrathEssentia(12, 0, 64, 12),
            "Wrath extraction pauses once mob essentia alone reaches the fuel cost");
        helper.assertTrue(WrathCageBehavior.canDrawSlothEssentia(11, 11, 63, 64, 12),
            "Sloth can be drawn while both higher-priority channels are below cost");
        helper.assertTrue(!WrathCageBehavior.canDrawSlothEssentia(11, 12, 0, 64, 12),
            "Sloth extraction pauses once the Wrath channel reaches cost");
        helper.assertValueEqual(WrathCageBehavior.selectFuelChannel(12, 12, 12, 12),
            WrathCageBehavior.FuelChannel.SPECIAL, "mob aspect has first fuel priority");
        helper.assertValueEqual(WrathCageBehavior.selectFuelChannel(0, 12, 12, 12),
            WrathCageBehavior.FuelChannel.WRATH, "Wrath has second fuel priority");
        helper.assertValueEqual(WrathCageBehavior.selectFuelChannel(0, 0, 12, 12),
            WrathCageBehavior.FuelChannel.SLOTH, "Sloth is the final fuel fallback");
        helper.assertValueEqual(WrathCageBehavior.selectFuelChannel(0, 0, 11, 12),
            WrathCageBehavior.FuelChannel.NONE, "partial channels cannot fuel a spawn cycle");
        helper.assertValueEqual(WrathCageBehavior.selectFuelChannel(64, 64, 64, 0),
            WrathCageBehavior.FuelChannel.NONE, "disabled essentia cost consumes no fuel channel");
        helper.assertTrue(WrathCageBehavior.nextDelay(0, false) >= 200
            && WrathCageBehavior.nextDelay(99, false) < 300,
            "normal spawn delay must remain in the original 200..299 tick range");
        helper.assertTrue(WrathCageBehavior.nextDelay(99, true) >= 400
            && WrathCageBehavior.nextDelay(0, true) < 500,
            "slothful spawn delay must remain in the original 400..499 tick range");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void witchwoodManaCostsPreserveOriginalRates(GameTestHelper helper) {
        helper.assertTrue(WitchwoodManaBehavior.supportsArsMagicaModId("arsmagicalegacy"),
            "modern Ars Magica: Legacy mod id is recognized by shared core");
        helper.assertTrue(WitchwoodManaBehavior.supportsArsMagicaModId("arsmagica2"),
            "legacy Ars Magica 2 mod id remains supported by shared core");
        helper.assertTrue(!WitchwoodManaBehavior.supportsArsMagicaModId("unrelatedmod"),
            "unrelated mods do not activate Witchwood mana integration");
        helper.assertValueEqual(WitchwoodManaBehavior.rechargeInterval(false), 100,
            "shared Witchwood wand interval remains 100 ticks");
        helper.assertValueEqual(WitchwoodManaBehavior.rechargeInterval(true), 40,
            "shared Witchwood staff interval remains 40 ticks");
        helper.assertTrue(Math.abs(WitchwoodManaBehavior.manaCost(false, "iron", 20) - 28.0F) < 0.01F,
            "shared core scales partial wand deficits correctly");
        helper.assertTrue(Math.abs(WitchwoodManaBehavior.manaCost(true, "vinteum", 20) - 10.0F) < 0.01F,
            "shared core scales partial Vinteum staff deficits correctly");
        helper.assertValueEqual(FmWands.witchwoodRechargeInterval(false), 100,
            "Witchwood wand recharges on the original 100-tick interval");
        helper.assertValueEqual(FmWands.witchwoodRechargeInterval(true), 40,
            "Witchwood staff recharges faster on the original 40-tick interval");
        helper.assertTrue(Math.abs(FmWands.witchwoodManaCost(false, "iron") - 140.0F) < 0.01F,
            "ordinary wand cap costs 140 mana per vis point");
        helper.assertTrue(Math.abs(FmWands.witchwoodManaCost(true, "iron") - 100.0F) < 0.01F,
            "ordinary staff cap costs 100 mana per vis point");
        helper.assertTrue(Math.abs(FmWands.witchwoodManaCost(false, "vinteum") - 80.0F) < 0.01F,
            "Vinteum reduces wand recharge cost");
        helper.assertTrue(Math.abs(FmWands.witchwoodManaCost(true, "vinteum") - 50.0F) < 0.01F,
            "Vinteum reduces staff recharge cost");
        helper.assertTrue(Math.abs(FmWands.witchwoodManaCost(false, "iron", 20) - 28.0F) < 0.01F,
            "partially depleted wand aspect only charges for missing vis units");
        helper.assertTrue(Math.abs(FmWands.witchwoodManaCost(true, "vinteum", 20) - 10.0F) < 0.01F,
            "partial staff recharge applies the cap modifier to missing vis units");
        helper.assertTrue(Math.abs(FmWands.witchwoodManaCost(false, "iron", 150) - 140.0F) < 0.01F,
            "one update never recharges more than one vis point per aspect");
        helper.assertTrue(FmWands.witchwoodManaCost(false, "iron", 0) == 0.0F,
            "a full aspect costs no mana to recharge");
        helper.assertTrue(FmWands.supportsArsMagicaModId("arsmagicalegacy"),
            "modern Ars Magica: Legacy mod id is recognized");
        helper.assertTrue(FmWands.supportsArsMagicaModId("arsmagica2"),
            "legacy Ars Magica 2 mod id remains supported");
        helper.assertTrue(!FmWands.supportsArsMagicaModId("unrelatedmod"),
            "unrelated mods do not activate Witchwood mana integration");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void nutritionRingOnlyCountsRingSlotStacks(GameTestHelper helper) {
        ItemStack ring = new ItemStack(FmItems.RING_NUTRITION.get());
        helper.assertTrue(FmToolEvents.hasNutritionRingInRingSlots(ring, ItemStack.EMPTY),
            "nutrition ring in first ring slot grants its food bonus");
        helper.assertTrue(FmToolEvents.hasNutritionRingInRingSlots(ItemStack.EMPTY, ring),
            "nutrition ring in second ring slot grants its food bonus");
        helper.assertTrue(!FmToolEvents.hasNutritionRingInRingSlots(ItemStack.EMPTY, ItemStack.EMPTY),
            "empty ring slots grant no food bonus");
        helper.succeed();
    }


    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void forbiddenMagicResearchIsRegistered(GameTestHelper helper) {
        helper.assertTrue(ResearchCategories.get("FORBIDDEN") != null, "Forbidden research category must exist");
        helper.assertTrue(ResearchCategories.getResearch("NETHERSHARDS") != null, "Nether shards entry must exist");
        helper.assertTrue(ResearchCategories.getResearch("TAINTTREE") != null, "taint tree research must exist");
        helper.assertTrue(ResearchCategories.getResearch("BLACKFLOWER") != null, "black flower research must exist");
        helper.assertTrue(ResearchCategories.getResearch("TAINTSTONE") != null, "tainted stone research must exist");
        helper.assertTrue(ResearchCategories.getResearch("ROD_tainted") != null, "tainted rod research must exist");
        helper.assertTrue(ResearchCategories.getResearch("ROD_infernal") != null, "infernal rod research must exist");
        helper.assertTrue(ResearchCategories.getResearch("ARCANECAKE") != null, "arcane cake research must exist");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void forbiddenMagicThaumcraftRecipesAreRegistered(GameTestHelper helper) {
        helper.assertTrue(TcRecipes.get("TAINTTREE") != null, "taint sapling crucible recipe must exist");
        helper.assertTrue(TcRecipes.get("BLACKFLOWER") != null, "umbral rose crucible recipe must exist");
        helper.assertTrue(TcRecipes.get("TaintStone") != null, "tainted stone arcane recipe must exist");
        helper.assertTrue(TcRecipes.get("ARCANECAKE") != null, "Thaumic Cake infusion recipe must exist");
        helper.assertTrue(TcRecipes.get("ROD_TAINTED") != null, "tainted rod infusion recipe must exist");
        helper.assertTrue(TcRecipes.get("ROD_INFERNAL") != null, "infernal rod infusion recipe must exist");
        helper.assertTrue(TcRecipes.get("WRATHCAGE") != null, "Wrath Cage infusion recipe must exist");
        helper.assertTrue(TcRecipes.get("MOBCRYSTAL") != null, "Mob Crystal crucible recipe must exist");
        helper.assertTrue(TcIngredient.item("thaumcraft:tainted_goo").exists(), "tainted goo central ingredient must exist");
        helper.assertTrue(TcIngredient.item("thaumcraft:salis_mundus").exists(), "Salis Mundus must exist");
        helper.assertTrue(TcIngredient.item("forbiddenmagic:tainted_rod").exists(), "tainted rod result must exist");
        helper.assertTrue(TcIngredient.item("forbiddenmagic:infernal_rod").exists(), "infernal rod result must exist");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void itemAspectTagsExist(GameTestHelper helper) {
        AspectList fruit = AspectRegistry.active().getObjectTags(FmItems.TAINT_FRUIT.get());
        AspectList shard = AspectRegistry.active().getObjectTags(FmItems.DEADLY_SHARDS.get("wrath").get());
        helper.assertTrue(fruit != null && fruit.getAmount(Aspect.TAINT) >= 2,
            "taint fruit must carry taint aspects");
        helper.assertTrue(shard != null && shard.getAmount(FmAspects.WRATH) >= 2,
            "wrath shard must carry wrath aspects");
        AspectList taintLog = AspectRegistry.active().getObjectTags(
            BuiltInRegistries.ITEM.get(ForbiddenMagic.id("taint_log")));
        helper.assertTrue(taintLog != null && taintLog.getAmount(Aspect.TAINT) >= 1,
            "taint log block item must carry taint aspects");
        helper.succeed();
    }


    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void addonAspectsSurviveSerialization(GameTestHelper helper) {
        Aspect nether = FmAspects.NETHER;
        helper.assertTrue(nether != null && nether.ordinal() >= FmAspects.BUILTIN_ASPECT_COUNT, "infernus must be an add-on aspect");
        helper.assertValueEqual(Aspect.getAspect("infernus"), nether, "by-tag lookup must find infernus");
        helper.assertTrue(FmAspects.PRIDE != null && FmAspects.GLUTTONY != null && FmAspects.ENVY != null
            && FmAspects.SLOTH != null && FmAspects.WRATH != null, "all enabled sin aspects must be registered");
        if (!FmConfig.lustDisabled()) {
            helper.assertTrue(FmAspects.LUST != null, "luxuria must register when no_lust is disabled");
        } else {
            helper.assertTrue(Aspect.getAspect("luxuria") == null, "luxuria must not register when no_lust is enabled");
        }
        AspectList list = new AspectList().add(nether, 3);
        io.netty.buffer.ByteBuf buffer = io.netty.buffer.Unpooled.buffer();
        AspectList.STREAM_CODEC.encode(buffer, list);
        buffer.readerIndex(0);
        AspectList decoded = AspectList.STREAM_CODEC.decode(buffer);
        helper.assertValueEqual(decoded.getAmount(nether), 3, "aspect amount must survive a network round trip");
        buffer.release();
        helper.succeed();
    }
}
