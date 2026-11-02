package dev.visfox.forbiddenreforged.test;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.core.aspect.AspectDefinition;
import dev.visfox.forbiddenreforged.core.aspect.ForbiddenMagicAspectCatalog;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4AspectDiagnostics;
import dev.visfox.forbiddenreforged.FmAspects;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4AspectAdapter;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4AspectTagAdapter;
import dev.visfox.forbiddenreforged.FmConfig;
import dev.visfox.forbiddenreforged.FmCreativeTab;
import dev.visfox.forbiddenreforged.FmDrops;
import dev.visfox.forbiddenreforged.FmBlocks;
import dev.visfox.forbiddenreforged.FmItems;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectRegistry;
import thaumcraft.research.ResearchCategories;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ScanManager;

@EventBusSubscriber(modid = ForbiddenMagic.MODID)
public final class AspectGameTests {
    public static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
        DeferredRegister.create(Registries.TEST_FUNCTION, ForbiddenMagic.MODID);

    private static final String[] TESTS = {
        "mod_loads_and_host_is_present",
        "aspect_registry_keeps_builtin_order_and_combines",
        "addon_aspect_survives_client_sync",
        "sin_aspects_registered_with_own_textures",
        "sin_aspect_tags_merge_with_existing",
        "entity_aspect_tags_merge_with_existing",
        "every_tag_target_resolved",
        "shards_are_registered_with_seven_ids",
        "no_lust_removes_luxuria_items",
        "mob_killed_by_player_drops_wrath_shard",
        "boss_drops_pride_shards",
        "unattended_death_drops_sloth_shard",
        "forbidden_category_and_shard_entries_resolve",
        "nether_shard_drop_rules_match_original_boundaries",
        "aspect_diagnostics_expose_stable_registry_table",
        "forbiddenmagic_aspects_command_is_registered",
        "addon_item_aspects_match_original_assignments",
        "core_aspect_catalog_is_host_neutral_and_complete"
    };

    static {
        FUNCTIONS.register(TESTS[0], () -> AspectGameTests::modLoadsAndHostIsPresent);
        FUNCTIONS.register(TESTS[1], () -> AspectGameTests::aspectRegistryKeepsBuiltinOrderAndCombines);
        FUNCTIONS.register(TESTS[2], () -> AspectGameTests::addonAspectSurvivesClientSync);
        FUNCTIONS.register(TESTS[3], () -> AspectGameTests::sinAspectsRegisteredWithOwnTextures);
        FUNCTIONS.register(TESTS[4], () -> AspectGameTests::sinAspectTagsMergeWithExisting);
        FUNCTIONS.register(TESTS[5], () -> AspectGameTests::entityAspectTagsMergeWithExisting);
        FUNCTIONS.register(TESTS[6], () -> AspectGameTests::everyTagTargetResolved);
        FUNCTIONS.register(TESTS[7], () -> AspectGameTests::shardsAreRegisteredWithSevenIds);
        FUNCTIONS.register(TESTS[8], () -> AspectGameTests::noLustRemovesLuxuriaItems);
        FUNCTIONS.register(TESTS[9], () -> AspectGameTests::mobKilledByPlayerDropsWrathShard);
        FUNCTIONS.register(TESTS[10], () -> AspectGameTests::bossDropsPrideShards);
        FUNCTIONS.register(TESTS[11], () -> AspectGameTests::unattendedDeathDropsSlothShard);
        FUNCTIONS.register(TESTS[12], () -> AspectGameTests::forbiddenCategoryAndShardEntriesResolve);
        FUNCTIONS.register(TESTS[13], () -> AspectGameTests::netherShardDropRulesMatchOriginalBoundaries);
        FUNCTIONS.register(TESTS[14], () -> AspectGameTests::aspectDiagnosticsExposeStableRegistryTable);
        FUNCTIONS.register(TESTS[15], () -> AspectGameTests::forbiddenmagicAspectsCommandIsRegistered);
        FUNCTIONS.register(TESTS[16], () -> AspectGameTests::addonItemAspectsMatchOriginalAssignments);
        FUNCTIONS.register(TESTS[17], () -> AspectGameTests::coreAspectCatalogIsHostNeutralAndComplete);
    }

    private AspectGameTests() {}

    @SubscribeEvent
    static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
            Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, "fm_aspects"), new TestEnvironmentDefinition.AllOf());
        for (String name : TESTS) {
            ResourceKey<Consumer<GameTestHelper>> function =
                ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, name));
            event.registerTest(
                Identifier.fromNamespaceAndPath(ForbiddenMagic.MODID, name),
                new FunctionGameTestInstance(function, new TestData<>(environment, Identifier.parse("thaumcraft:test/platform"), 100, 0, true))
            );
        }
    }

    private static void coreAspectCatalogIsHostNeutralAndComplete(GameTestHelper helper) {
        List<AspectDefinition> enabled = ForbiddenMagicAspectCatalog.definitions(true);
        List<AspectDefinition> disabled = ForbiddenMagicAspectCatalog.definitions(false);
        helper.assertValueEqual(enabled.size(), 7, "full catalog must define seven Forbidden Magic aspects");
        helper.assertValueEqual(disabled.size(), 6, "no-lust catalog must define six aspects");
        helper.assertTrue(enabled.stream().allMatch(definition ->
            definition.id().namespace().equals(ForbiddenMagic.MODID)
                && definition.components().stream().allMatch(component ->
                    component.namespace().equals(ForbiddenMagic.MODID)
                        && component.path().startsWith("concept/"))),
            "catalog component references must use semantic IDs, not host aspect IDs");

        AspectDefinition luxuria = enabled.get(0);
        helper.assertValueEqual(luxuria.id(), ForbiddenMagicAspectCatalog.LUXURIA, "Luxuria must be the first enabled definition");
        helper.assertValueEqual(luxuria.components(), List.of(
            ContentId.parse("forbiddenmagic:concept/flesh"),
            ContentId.parse("forbiddenmagic:concept/hunger")
        ), "Luxuria must preserve its original semantic components");
        helper.assertValueEqual(enabled.get(1).blendMode(), AspectDefinition.BlendMode.ONE_MINUS_SOURCE_COLOR,
            "Infernus must retain its original blend mode");
        helper.assertTrue(disabled.stream().noneMatch(definition ->
            definition.id().equals(ForbiddenMagicAspectCatalog.LUXURIA)),
            "no-lust catalog must omit Luxuria");

        ContentId fire = ContentId.parse("forbiddenmagic:concept/fire");
        AspectDefinition.Cost merged = new AspectDefinition.Cost(List.of(
            new AspectDefinition.Amount(fire, 2),
            new AspectDefinition.Amount(fire, 3)
        ));
        helper.assertValueEqual(merged.amountOf(fire), 5, "duplicate aspect costs must merge");
        helper.assertValueEqual(merged.totalAmount(), 5, "merged cost total must remain stable");
        helper.succeed();
    }

    private static void modLoadsAndHostIsPresent(GameTestHelper helper) {
        helper.assertTrue(ModList.get().isLoaded("thaumcraft"), "thaumcraft must be loaded");
        helper.succeed();
    }

    private static void aspectRegistryKeepsBuiltinOrderAndCombines(GameTestHelper helper) {
        Aspect[] all = Aspect.values();
        helper.assertValueEqual(Tc4AspectDiagnostics.BUILTIN_ASPECT_COUNT, 48, "exactly 48 builtin aspects");
        helper.assertTrue(all.length >= 48, "builtins must remain registered");
        helper.assertValueEqual(all[0], Aspect.AIR, "first builtin must stay index 0");
        helper.assertValueEqual(all[47], Aspect.TRAP, "last builtin must stay index 47");
        Aspect fire = Aspect.combine(Aspect.AIR, Aspect.FIRE);
        helper.assertTrue(fire == Aspect.LIGHT, "combine(AIR, FIRE) must be LIGHT");
        helper.assertTrue(Aspect.getAspect("aer") == Aspect.AIR, "by-tag lookup must work");
        helper.succeed();
    }

    private static void addonAspectSurvivesClientSync(GameTestHelper helper) {
        Aspect nether = Tc4AspectAdapter.resolveAspect(FmAspects.NETHER);
        helper.assertTrue(nether != null && nether.ordinal() >= Tc4AspectDiagnostics.BUILTIN_ASPECT_COUNT, "infernus must be an add-on aspect");
        helper.assertValueEqual(Aspect.getAspect("infernus"), nether, "by-tag lookup must find the add-on aspect");
        AspectList list = new AspectList().add(nether, 3);
        ByteBuf buffer = io.netty.buffer.Unpooled.buffer();
        AspectList.STREAM_CODEC.encode(buffer, list);
        buffer.readerIndex(0);
        AspectList decoded = AspectList.STREAM_CODEC.decode(buffer);
        helper.assertValueEqual(decoded.getAmount(nether), 3, "aspect amount must survive a network round trip");
        helper.succeed();
    }

    private static void sinAspectsRegisteredWithOwnTextures(GameTestHelper helper) {
        helper.assertTrue(FmAspects.available(), "add-on aspect API missing: host too old");
        Aspect[] sins = {Tc4AspectAdapter.resolveAspect(FmAspects.NETHER), Tc4AspectAdapter.resolveAspect(FmAspects.WRATH), Tc4AspectAdapter.resolveAspect(FmAspects.ENVY), Tc4AspectAdapter.resolveAspect(FmAspects.GLUTTONY), Tc4AspectAdapter.resolveAspect(FmAspects.PRIDE), Tc4AspectAdapter.resolveAspect(FmAspects.SLOTH)};
        for (Aspect aspect : sins) {
            helper.assertTrue(aspect != null, "sin aspect must be registered");
            helper.assertTrue(aspect.ordinal() >= Tc4AspectDiagnostics.BUILTIN_ASPECT_COUNT, aspect.getSerializedName() + " must be an add-on aspect");
            helper.assertTrue(aspect.getImage().getNamespace().equals(ForbiddenMagic.MODID), aspect.getSerializedName() + " texture must live in the add-on namespace");
        }
        Aspect ira = Aspect.combine(Aspect.WEAPON, Aspect.FIRE);
        helper.assertTrue(ira == Tc4AspectAdapter.resolveAspect(FmAspects.WRATH), "combine(WEAPON, FIRE) must resolve to the add-on ira");
        helper.assertTrue(Aspect.combine(Aspect.AIR, Aspect.FIRE) == Aspect.LIGHT, "adding ira must not disturb builtin combine");
        int expected = FmConfig.lustDisabled() ? 6 : 7;
        helper.assertValueEqual(Tc4AspectDiagnostics.totalCount(), Tc4AspectDiagnostics.BUILTIN_ASPECT_COUNT + expected, FmConfig.lustDisabled() ? "six add-on aspects: luxuria is switched off" : "exactly seven add-on aspects");
        helper.succeed();
    }

    private static void sinAspectTagsMergeWithExisting(GameTestHelper helper) {
        AspectRegistry active = AspectRegistry.active();
        AspectList netherrack = active.getObjectTags(Items.NETHERRACK);
        helper.assertTrue(netherrack != null && netherrack.getAmount(Tc4AspectAdapter.resolveAspect(FmAspects.NETHER)) >= 1, "netherrack must carry infernus");
        helper.assertTrue(netherrack.getAmount(Aspect.FIRE) >= 1, "netherrack must keep its built-in fire aspect");
        AspectList star = active.getObjectTags(Items.NETHER_STAR);
        helper.assertTrue(star != null && star.getAmount(Tc4AspectAdapter.resolveAspect(FmAspects.NETHER)) >= 8 && star.getAmount(Tc4AspectAdapter.resolveAspect(FmAspects.PRIDE)) >= 8, "nether star must carry infernus 8 and superbia 8");
        helper.succeed();
    }

    private static void entityAspectTagsMergeWithExisting(GameTestHelper helper) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.parse("thaumcraft:brainy_zombie")).orElse(null);
        helper.assertTrue(type != null, "brainy zombie type must exist");
        Entity entity = type.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
        helper.assertTrue(entity != null, "brainy zombie must be creatable");
        AspectList tags = AspectRegistry.active().getEntityTags(entity);
        helper.assertTrue(tags != null && tags.getAmount(Aspect.UNDEAD) >= 3, "brainy zombie keeps its base undead tag");
        helper.assertTrue(tags.getAmount(Tc4AspectAdapter.resolveAspect(FmAspects.WRATH)) >= 3, "brainy zombie gains wrath from the FM table");
        helper.succeed();
    }

    private static void everyTagTargetResolved(GameTestHelper helper) {
        List<String> unresolved = new ArrayList<>();
        for (String target : Tc4AspectTagAdapter.declaredTargets()) {
            if (target.startsWith("entity:")) {
                if (BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.parse(target.substring("entity:".length()))).isEmpty()) {
                    unresolved.add(target);
                }
            } else if (target.startsWith("block:")) {
                if (BuiltInRegistries.BLOCK.getOptional(Identifier.parse(target.substring("block:".length()))).isEmpty()) {
                    unresolved.add(target);
                }
            } else if (target.startsWith("#")) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(target.substring(1)));
                boolean found = false;
                for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
                    found = true;
                    break;
                }
                if (!found) {
                    unresolved.add(target);
                }
            } else if (BuiltInRegistries.ITEM.getOptional(Identifier.parse(target)).isEmpty()) {
                unresolved.add(target);
            }
        }
        helper.assertTrue(unresolved.isEmpty(), "unresolved aspect tag targets: " + unresolved);
        helper.succeed();
    }

    private static void shardsAreRegisteredWithSevenIds(GameTestHelper helper) {
        helper.assertValueEqual(FmItems.DEADLY_SHARDS.size(), 7, "the original registers seven vices");
        for (var entry : FmItems.DEADLY_SHARDS.entrySet()) {
            Item item = entry.getValue().get();
            helper.assertTrue(BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(ForbiddenMagic.MODID), entry.getKey() + " must be namespaced to the port");
            AspectList tags = AspectRegistry.active().getObjectTags(item);
            helper.assertTrue(tags != null && tags.size() > 0, entry.getKey() + " must carry an aspect tag");
        }
        helper.succeed();
    }

    private static void noLustRemovesLuxuriaItems(GameTestHelper helper) {
        ForbiddenMagic.LOGGER.info("no_lust gate: disabled={}, luxuria={}", FmConfig.lustDisabled(), Aspect.getAspect("luxuria"));
        if (!FmConfig.lustDisabled()) {
            helper.succeed();
            return;
        }
        helper.assertTrue(Aspect.getAspect("luxuria") == null, "luxuria must be absent from the registry");
        ItemStack shard = new ItemStack(FmItems.DEADLY_SHARDS.get("lust").get());
        helper.assertValueEqual(shard.getHoverName(), Component.translatable("item.forbiddenmagic.despair_shard"), "no_lust renames the fourth shard to despair");
        AspectList tags = AspectRegistry.active().getObjectTags(shard.getItem());
        helper.assertTrue(tags != null, "the despair shard must carry an aspect tag");
        helper.assertValueEqual(tags.getAmount(Aspect.VOID), 1, "the despair shard carries one void");
        helper.assertValueEqual(tags.size(), 1, "the despair shard carries only void");
        helper.assertTrue(FmCreativeTab.stacks().stream().anyMatch(stack -> stack.getItem() == shard.getItem()), "the despair shard stays in the creative tab");
        helper.succeed();
    }

    private static void mobKilledByPlayerDropsWrathShard(GameTestHelper helper) {
        helper.assertTrue(FmDrops.rollWrath(2, 2), "chance 2 must drop at roll 2, the original uses nextInt(61) <= wrath");
        helper.assertFalse(FmDrops.rollWrath(2, 3), "chance 2 must not drop at roll 3");
        helper.assertValueEqual(FmDrops.wrathShardStack().getItem(), FmItems.DEADLY_SHARDS.get("wrath").get(), "wrath drop must be the wrath shard");
        helper.succeed();
    }

    private static void addonItemAspectsMatchOriginalAssignments(GameTestHelper helper) {
        AspectRegistry registry = AspectRegistry.active();

        AspectList emerald = registry.getObjectTags(FmItems.EMERALD_FRAGMENT.get());
        helper.assertTrue(emerald != null && emerald.getAmount(Aspect.CRYSTAL) == 1,
            "emerald fragment keeps the original crystal aspect");
        AspectList dye = registry.getObjectTags(FmItems.BLACK_DYE.get());
        helper.assertTrue(dye != null && dye.getAmount(Aspect.DARKNESS) == 1,
            "black dye keeps the original darkness aspect");

        AspectList fruit = registry.getObjectTags(FmItems.TAINT_FRUIT.get());
        helper.assertTrue(fruit != null && fruit.getAmount(Aspect.TAINT) == 2 && fruit.getAmount(Aspect.POISON) == 1,
            "taint fruit keeps taint and poison");
        AspectList coal = registry.getObjectTags(FmItems.TAINT_COAL.get());
        helper.assertTrue(coal != null && coal.getAmount(Aspect.TAINT) == 2 && coal.getAmount(Aspect.ENTROPY) == 2,
            "taint coal keeps taint and entropy");

        AspectList leaves = registry.getObjectTags(FmBlocks.TAINT_LEAVES.get().asItem());
        helper.assertTrue(leaves != null && leaves.getAmount(Aspect.TAINT) == 1 && leaves.getAmount(Aspect.PLANT) == 1,
            "tainted leaves keep taint and plant");
        AspectList log = registry.getObjectTags(FmBlocks.TAINT_LOG.get().asItem());
        helper.assertTrue(log != null && log.getAmount(Aspect.TAINT) == 1 && log.getAmount(Aspect.TREE) == 3,
            "tainted log keeps taint and tree");
        AspectList planks = registry.getObjectTags(FmBlocks.TAINT_PLANKS.get().asItem());
        helper.assertTrue(planks != null && planks.getAmount(Aspect.TREE) == 1,
            "tainted planks keep tree");
        AspectList brick = registry.getObjectTags(FmBlocks.TAINT_STONE_BRICK.get().asItem());
        helper.assertTrue(brick != null && brick.getAmount(Aspect.EARTH) == 1,
            "tainted stone bricks keep earth");

        AspectList flower = registry.getObjectTags(FmBlocks.UMBRAL_ROSE.get().asItem());
        helper.assertTrue(flower != null
                && flower.getAmount(Aspect.PLANT) == 1
                && flower.getAmount(Aspect.LIFE) == 1
                && flower.getAmount(Aspect.SENSES) == 1
                && flower.getAmount(Aspect.DARKNESS) == 1,
            "umbral rose keeps the original black flower aspects");
        if (!FmConfig.lustDisabled()) {
            AspectList crop = registry.getObjectTags(FmItems.RIDING_CROP.get());
            helper.assertTrue(crop != null
                    && crop.getAmount(Aspect.TREE) == 2
                    && crop.getAmount(Aspect.CLOTH) == 2
                    && crop.getAmount(Aspect.BEAST) == 1
                    && crop.getAmount(Tc4AspectAdapter.resolveAspect(FmAspects.LUST)) == 1
                    && crop.getAmount(Aspect.WEAPON) == 1,
                "riding crop keeps the original aspect list when lust is enabled");
        }
        BlockPos portalPos = helper.absolutePos(new BlockPos(3, 3, 3));
        helper.getLevel().setBlockAndUpdate(portalPos, Blocks.NETHER_PORTAL.defaultBlockState());
        ScanManager.Target portal = ScanManager.resolveBlock(helper.getLevel(), portalPos);
        if (!Tc4AspectTagAdapter.blockTargetsAvailable()) {
            helper.assertTrue(portal == null || !portal.key().startsWith("block:"),
                "without block aspect targets the portal cannot resolve through its block identity");
            helper.succeed();
            return;
        }
        helper.assertTrue(portal != null, "the Nether Portal must be a scan target");
        if (portal != null) {
            helper.assertValueEqual(portal.key(), "block:minecraft:nether_portal", "the portal scan target must use its block identity");
            helper.assertTrue(portal.aspects() != null, "the portal must have registered aspects");
            if (portal.aspects() != null) {
                helper.assertValueEqual(portal.aspects().getAmount(Tc4AspectAdapter.resolveAspect(FmAspects.NETHER)), 4,
                    "the original NETHER 4 aspect assignment belongs to the portal block, not obsidian");
            }
        }

        helper.succeed();
    }

    private static void netherShardDropRulesMatchOriginalBoundaries(GameTestHelper helper) {
        helper.assertTrue(FmDrops.rollGluttony(0), "gluttony shard drops at roll 0");
        helper.assertTrue(FmDrops.rollGluttony(1), "gluttony shard drops at roll 1");
        helper.assertFalse(FmDrops.rollGluttony(2), "gluttony shard does not drop at roll 2");

        helper.assertFalse(FmDrops.rollQuartzGreed(0, 0), "quartz ore requires positive Fortune");
        helper.assertTrue(FmDrops.rollQuartzGreed(1, 1), "Fortune 1 includes roll 1");
        helper.assertFalse(FmDrops.rollQuartzGreed(1, 2), "Fortune 1 excludes roll 2");
        helper.assertTrue(FmDrops.rollQuartzGreed(3, 3), "Fortune 3 includes roll 3");

        helper.assertFalse(FmDrops.rollEnvy(0), "envy shard is not assigned at roll 0");
        helper.assertTrue(FmDrops.rollEnvy(1), "envy shard is assigned at roll 1");
        helper.assertFalse(FmDrops.rollEnvy(174), "envy shard is not assigned at roll 174");
        helper.assertTrue(FmItems.GLUTTONY_SHARD.get() != null, "gluttony shard item exists");
        helper.assertTrue(FmItems.DEADLY_SHARDS.get("greed").get() != null, "greed shard item exists");
        helper.assertTrue(FmItems.DEADLY_SHARDS.get("envy").get() != null, "envy shard item exists");
        helper.succeed();
    }

    private static void bossDropsPrideShards(GameTestHelper helper) {
        helper.assertValueEqual(FmDrops.prideCount(0, 0), 2, "boss without looting drops two pride shards");
        helper.assertValueEqual(FmDrops.prideCount(2, 1), 3, "looting 2 with roll 1 gives three");
        helper.succeed();
    }

    private static void unattendedDeathDropsSlothShard(GameTestHelper helper) {
        helper.assertTrue(FmDrops.rollSloth(3), "sloth drops on the last allowed roll");
        helper.assertFalse(FmDrops.rollSloth(4), "sloth must not drop on roll 4");
        helper.assertValueEqual(FmDrops.slothCount(0), 1, "minimum sloth drop is one");
        helper.assertValueEqual(FmDrops.slothCount(2), 3, "maximum sloth drop is three");
        helper.succeed();
    }

    private static void forbiddenmagicAspectsCommandIsRegistered(GameTestHelper helper) {
            var root = helper.getLevel().getServer().getCommands().getDispatcher().getRoot();
            helper.assertTrue(root.getChild("forbiddenmagic") != null, "forbiddenmagic command must be registered");
            helper.assertTrue(root.getChild("forbiddenmagic").getChild("aspects") != null, "forbiddenmagic aspects command must be registered");
            var source = helper.getLevel().getServer().createCommandSourceStack();
            helper.assertTrue(root.getChild("forbiddenmagic").getRequirement().test(source.withPermission(PermissionSet.ALL_PERMISSIONS)), "game masters must be allowed");
            helper.assertFalse(root.getChild("forbiddenmagic").getRequirement().test(source.withPermission(PermissionSet.NO_PERMISSIONS)), "ordinary players must be denied");
            helper.succeed();
        }

    private static void aspectDiagnosticsExposeStableRegistryTable(GameTestHelper helper) {
            List<String> registryLines = Tc4AspectDiagnostics.registryLines();
            helper.assertValueEqual(registryLines.size(), Tc4AspectDiagnostics.totalCount(), "diagnostics must contain every registered aspect");
            for (int index = 0; index < Aspect.values().length; index++) {
                Aspect aspect = Aspect.values()[index];
                helper.assertTrue(registryLines.get(index).contains("ordinal=" + index + " "), "registry ordinals must be strict and ordered: " + aspect.getTag());
            }
            helper.assertValueEqual(registryLines.get(0), "ordinal=0 tag=aer name=Aer kind=builtin components=-", "first registry row must be exact");
            helper.assertTrue(registryLines.stream().anyMatch(line -> line.contains("ordinal=47 ") && line.contains("tag=vinculum ") && line.contains("name=Vinculum ") && line.contains("kind=builtin")), "ordinal 47 vinculum must be present");
            for (Aspect aspect : Aspect.values()) {
                if (aspect.ordinal() < Tc4AspectDiagnostics.BUILTIN_ASPECT_COUNT) {
                    helper.assertTrue(registryLines.stream().anyMatch(line -> line.contains("ordinal=" + aspect.ordinal() + " ") && line.contains("tag=" + aspect.getTag() + " ") && line.contains("kind=builtin")), "builtin aspect must be classified: " + aspect.getTag());
                }
            }
    
            List<String> addonLines = Tc4AspectDiagnostics.addonLines();
            int expected = FmConfig.lustDisabled() ? 6 : 7;
            helper.assertValueEqual(addonLines.size(), expected, "diagnostics must list every registered Forbidden Magic aspect");
            helper.assertValueEqual(
                registryLines.stream().filter(line -> line.contains("kind=addon")).toList(),
                addonLines,
                "Forbidden Magic rows must match the add-on section of the canonical registry"
            );
            int expectedOrdinal = Tc4AspectDiagnostics.BUILTIN_ASPECT_COUNT;
            for (String line : addonLines) {
                helper.assertTrue(line.contains("ordinal=" + expectedOrdinal + " "), "add-on ordinals must be consecutive: " + line);
                helper.assertTrue(line.contains("kind=addon"), "add-on aspect must be classified: " + line);
                helper.assertTrue(line.contains("tag="), "add-on aspect must include its tag: " + line);
                helper.assertTrue(line.contains("components="), "add-on aspect must include its components: " + line);
                expectedOrdinal++;
            }
            helper.assertValueEqual(expectedOrdinal, Tc4AspectDiagnostics.BUILTIN_ASPECT_COUNT + expected, "all add-on ordinals must be present");
            helper.assertTrue(addonLines.stream().anyMatch(line -> line.contains("tag=infernus ") && line.contains("name=Infernus ") && line.contains("components=ignis,praecantatio")), "infernus row must include exact name and component tags");
            helper.succeed();
        }

    private static void forbiddenCategoryAndShardEntriesResolve(GameTestHelper helper) {
        ResearchItem shards = ResearchCategories.getResearch("NETHERSHARDS");
        helper.assertTrue(shards != null, "NETHERSHARDS must be registered");
        helper.assertValueEqual(shards.category(), "FORBIDDEN", "category");
        helper.assertValueEqual(shards.column(), -8, "column");
        helper.assertValueEqual(shards.row(), -5, "row");
        helper.assertTrue(shards.isStub() && shards.isRound() && shards.isAutoUnlock(), "stub, round and auto-unlock as in the original");
        ResearchItem axe = ResearchCategories.getResearch("SKULLAXE");
        helper.assertTrue(axe != null, "SKULLAXE must be registered");
        helper.assertTrue(axe.isConcealed(), "SKULLAXE must stay concealed");
        helper.assertValueEqual(axe.warp(), 1, "SKULLAXE carries one warp point as in the original");
        helper.assertValueEqual(axe.parents(), List.of("NETHERSHARDS", "THAUMIUM", "INFUSION"), "SKULLAXE parents, in order");
        helper.succeed();
    }
}
