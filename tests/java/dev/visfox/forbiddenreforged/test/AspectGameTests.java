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
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameType;
import net.neoforged.fml.ModList;
import dev.visfox.forbiddenreforged.ForbiddenMagic;
import dev.visfox.forbiddenreforged.FmAspectDiagnostics;
import dev.visfox.forbiddenreforged.FmAspects;
import dev.visfox.forbiddenreforged.FmAspectTags;
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

@GameTestHolder
@PrefixGameTestTemplate(false)
public final class AspectGameTests {



    private AspectGameTests() {}


    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void modLoadsAndHostIsPresent(GameTestHelper helper) {
        helper.assertTrue(ModList.get().isLoaded("thaumcraft"), "thaumcraft must be loaded");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void aspectRegistryKeepsBuiltinOrderAndCombines(GameTestHelper helper) {
        Aspect[] all = Aspect.values();
        helper.assertValueEqual(FmAspects.BUILTIN_ASPECT_COUNT, 48, "exactly 48 builtin aspects");
        helper.assertTrue(all.length >= 48, "builtins must remain registered");
        helper.assertValueEqual(all[0], Aspect.AIR, "first builtin must stay index 0");
        helper.assertValueEqual(all[47], Aspect.TRAP, "last builtin must stay index 47");
        Aspect fire = Aspect.combine(Aspect.AIR, Aspect.FIRE);
        helper.assertTrue(fire == Aspect.LIGHT, "combine(AIR, FIRE) must be LIGHT");
        helper.assertTrue(Aspect.getAspect("aer") == Aspect.AIR, "by-tag lookup must work");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void addonAspectSurvivesClientSync(GameTestHelper helper) {
        Aspect nether = FmAspects.NETHER;
        helper.assertTrue(nether != null && nether.ordinal() >= FmAspects.BUILTIN_ASPECT_COUNT, "infernus must be an add-on aspect");
        helper.assertValueEqual(Aspect.getAspect("infernus"), nether, "by-tag lookup must find the add-on aspect");
        AspectList list = new AspectList().add(nether, 3);
        ByteBuf buffer = io.netty.buffer.Unpooled.buffer();
        AspectList.STREAM_CODEC.encode(buffer, list);
        buffer.readerIndex(0);
        AspectList decoded = AspectList.STREAM_CODEC.decode(buffer);
        helper.assertValueEqual(decoded.getAmount(nether), 3, "aspect amount must survive a network round trip");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void sinAspectsRegisteredWithOwnTextures(GameTestHelper helper) {
        helper.assertTrue(FmAspects.available(), "add-on aspect API missing: host too old");
        Aspect[] sins = {FmAspects.NETHER, FmAspects.WRATH, FmAspects.ENVY, FmAspects.GLUTTONY, FmAspects.PRIDE, FmAspects.SLOTH};
        for (Aspect aspect : sins) {
            helper.assertTrue(aspect != null, "sin aspect must be registered");
            helper.assertTrue(aspect.ordinal() >= FmAspects.BUILTIN_ASPECT_COUNT, aspect.getSerializedName() + " must be an add-on aspect");
            helper.assertTrue(aspect.getImage().getNamespace().equals(ForbiddenMagic.MODID), aspect.getSerializedName() + " texture must live in the add-on namespace");
        }
        Aspect ira = Aspect.combine(Aspect.WEAPON, Aspect.FIRE);
        helper.assertTrue(ira == FmAspects.WRATH, "combine(WEAPON, FIRE) must resolve to the add-on ira");
        helper.assertTrue(Aspect.combine(Aspect.AIR, Aspect.FIRE) == Aspect.LIGHT, "adding ira must not disturb builtin combine");
        int expected = FmConfig.lustDisabled() ? 6 : 7;
        helper.assertValueEqual(FmAspects.count(), FmAspects.BUILTIN_ASPECT_COUNT + expected, FmConfig.lustDisabled() ? "six add-on aspects: luxuria is switched off" : "exactly seven add-on aspects");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void sinAspectTagsMergeWithExisting(GameTestHelper helper) {
        AspectRegistry active = AspectRegistry.active();
        AspectList netherrack = active.getObjectTags(Items.NETHERRACK);
        helper.assertTrue(netherrack != null && netherrack.getAmount(FmAspects.NETHER) >= 1, "netherrack must carry infernus");
        helper.assertTrue(netherrack.getAmount(Aspect.FIRE) >= 1, "netherrack must keep its built-in fire aspect");
        AspectList star = active.getObjectTags(Items.NETHER_STAR);
        helper.assertTrue(star != null && star.getAmount(FmAspects.NETHER) >= 8 && star.getAmount(FmAspects.PRIDE) >= 8, "nether star must carry infernus 8 and superbia 8");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void entityAspectTagsMergeWithExisting(GameTestHelper helper) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse("thaumcraft:brainy_zombie")).orElse(null);
        helper.assertTrue(type != null, "brainy zombie type must exist");
        Entity entity = type.create(helper.getLevel());
        helper.assertTrue(entity != null, "brainy zombie must be creatable");
        AspectList tags = AspectRegistry.active().getEntityTags(entity);
        helper.assertTrue(tags != null && tags.getAmount(Aspect.UNDEAD) >= 3, "brainy zombie keeps its base undead tag");
        helper.assertTrue(tags.getAmount(FmAspects.WRATH) >= 3, "brainy zombie gains wrath from the FM table");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void everyTagTargetResolved(GameTestHelper helper) {
        List<String> unresolved = new ArrayList<>();
        for (String target : FmAspectTags.declaredTargets()) {
            if (target.startsWith("entity:")) {
                if (BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse(target.substring("entity:".length()))).isEmpty()) {
                    unresolved.add(target);
                }
            } else if (target.startsWith("block:")) {
                if (BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(target.substring("block:".length()))).isEmpty()) {
                    unresolved.add(target);
                }
            } else if (target.startsWith("#")) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(target.substring(1)));
                boolean found = false;
                for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
                    found = true;
                    break;
                }
                if (!found) {
                    unresolved.add(target);
                }
            } else if (BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(target)).isEmpty()) {
                unresolved.add(target);
            }
        }
        helper.assertTrue(unresolved.isEmpty(), "unresolved aspect tag targets: " + unresolved);
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void shardsAreRegisteredWithSevenIds(GameTestHelper helper) {
        helper.assertValueEqual(FmItems.DEADLY_SHARDS.size(), 7, "the original registers seven vices");
        for (var entry : FmItems.DEADLY_SHARDS.entrySet()) {
            Item item = entry.getValue().get();
            helper.assertTrue(BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(ForbiddenMagic.MODID), entry.getKey() + " must be namespaced to the port");
            AspectList tags = AspectRegistry.active().getObjectTags(item);
            helper.assertTrue(tags != null && tags.size() > 0, entry.getKey() + " must carry an aspect tag");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void noLustRemovesLuxuriaItems(GameTestHelper helper) {
        ForbiddenMagic.LOGGER.info("no_lust gate: disabled={}, luxuria={}", FmConfig.lustDisabled(), Aspect.getAspect("luxuria"));
        if (!FmConfig.lustDisabled()) {
            helper.succeed();
            return;
        }
        helper.assertTrue(FmAspects.LUST == null && Aspect.getAspect("luxuria") == null, "luxuria must be absent from the registry");
        ItemStack shard = new ItemStack(FmItems.DEADLY_SHARDS.get("lust").get());
        helper.assertValueEqual(shard.getHoverName(), Component.translatable("item.forbiddenmagic.despair_shard"), "no_lust renames the fourth shard to despair");
        AspectList tags = AspectRegistry.active().getObjectTags(shard.getItem());
        helper.assertTrue(tags != null, "the despair shard must carry an aspect tag");
        helper.assertValueEqual(tags.getAmount(Aspect.VOID), 1, "the despair shard carries one void");
        helper.assertValueEqual(tags.size(), 1, "the despair shard carries only void");
        helper.assertTrue(FmCreativeTab.stacks().stream().anyMatch(stack -> stack.getItem() == shard.getItem()), "the despair shard stays in the creative tab");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void mobKilledByPlayerDropsWrathShard(GameTestHelper helper) {
        helper.assertTrue(FmDrops.rollWrath(2, 2), "chance 2 must drop at roll 2, the original uses nextInt(61) <= wrath");
        helper.assertFalse(FmDrops.rollWrath(2, 3), "chance 2 must not drop at roll 3");
        helper.assertValueEqual(FmDrops.wrathShardStack().getItem(), FmItems.DEADLY_SHARDS.get("wrath").get(), "wrath drop must be the wrath shard");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void addonItemAspectsMatchOriginalAssignments(GameTestHelper helper) {
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
                    && crop.getAmount(FmAspects.LUST) == 1
                    && crop.getAmount(Aspect.WEAPON) == 1,
                "riding crop keeps the original aspect list when lust is enabled");
        }
        BlockPos portalPos = helper.absolutePos(new BlockPos(3, 3, 3));
        helper.getLevel().setBlockAndUpdate(portalPos, Blocks.NETHER_PORTAL.defaultBlockState());
        ScanManager.Target portal = ScanManager.resolveBlock(helper.getLevel(), portalPos);
        if (!FmAspectTags.blockTargetsAvailable()) {
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
                helper.assertValueEqual(portal.aspects().getAmount(FmAspects.NETHER), 4,
                    "the original NETHER 4 aspect assignment belongs to the portal block, not obsidian");
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void netherShardDropRulesMatchOriginalBoundaries(GameTestHelper helper) {
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

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void bossDropsPrideShards(GameTestHelper helper) {
        helper.assertValueEqual(FmDrops.prideCount(0, 0), 2, "boss without looting drops two pride shards");
        helper.assertValueEqual(FmDrops.prideCount(2, 1), 3, "looting 2 with roll 1 gives three");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void unattendedDeathDropsSlothShard(GameTestHelper helper) {
        helper.assertTrue(FmDrops.rollSloth(3), "sloth drops on the last allowed roll");
        helper.assertFalse(FmDrops.rollSloth(4), "sloth must not drop on roll 4");
        helper.assertValueEqual(FmDrops.slothCount(0), 1, "minimum sloth drop is one");
        helper.assertValueEqual(FmDrops.slothCount(2), 3, "maximum sloth drop is three");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void forbiddenmagicAspectsCommandIsRegistered(GameTestHelper helper) {
            var root = helper.getLevel().getServer().getCommands().getDispatcher().getRoot();
            helper.assertTrue(root.getChild("forbiddenmagic") != null, "forbiddenmagic command must be registered");
            helper.assertTrue(root.getChild("forbiddenmagic").getChild("aspects") != null, "forbiddenmagic aspects command must be registered");
            var source = helper.getLevel().getServer().createCommandSourceStack();
            helper.assertTrue(root.getChild("forbiddenmagic").getRequirement().test(source.withPermission(4)), "game masters must be allowed");
            helper.assertFalse(root.getChild("forbiddenmagic").getRequirement().test(source.withPermission(0)), "ordinary players must be denied");
            helper.succeed();
        }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void aspectDiagnosticsExposeStableRegistryTable(GameTestHelper helper) {
            List<String> registryLines = FmAspectDiagnostics.registryLines();
            helper.assertValueEqual(registryLines.size(), FmAspects.count(), "diagnostics must contain every registered aspect");
            for (int index = 0; index < Aspect.values().length; index++) {
                Aspect aspect = Aspect.values()[index];
                helper.assertTrue(registryLines.get(index).contains("ordinal=" + index + " "), "registry ordinals must be strict and ordered: " + aspect.getTag());
            }
            helper.assertValueEqual(registryLines.get(0), "ordinal=0 tag=aer name=Aer kind=builtin components=-", "first registry row must be exact");
            helper.assertTrue(registryLines.stream().anyMatch(line -> line.contains("ordinal=47 ") && line.contains("tag=vinculum ") && line.contains("name=Vinculum ") && line.contains("kind=builtin")), "ordinal 47 vinculum must be present");
            for (Aspect aspect : Aspect.values()) {
                if (aspect.ordinal() < FmAspects.BUILTIN_ASPECT_COUNT) {
                    helper.assertTrue(registryLines.stream().anyMatch(line -> line.contains("ordinal=" + aspect.ordinal() + " ") && line.contains("tag=" + aspect.getTag() + " ") && line.contains("kind=builtin")), "builtin aspect must be classified: " + aspect.getTag());
                }
            }
    
            List<String> addonLines = FmAspectDiagnostics.addonLines();
            int expected = FmConfig.lustDisabled() ? 6 : 7;
            helper.assertValueEqual(addonLines.size(), expected, "diagnostics must list every registered Forbidden Magic aspect");
            helper.assertValueEqual(
                registryLines.stream().filter(line -> line.contains("kind=addon")).toList(),
                addonLines,
                "Forbidden Magic rows must match the add-on section of the canonical registry"
            );
            int expectedOrdinal = FmAspects.BUILTIN_ASPECT_COUNT;
            for (String line : addonLines) {
                helper.assertTrue(line.contains("ordinal=" + expectedOrdinal + " "), "add-on ordinals must be consecutive: " + line);
                helper.assertTrue(line.contains("kind=addon"), "add-on aspect must be classified: " + line);
                helper.assertTrue(line.contains("tag="), "add-on aspect must include its tag: " + line);
                helper.assertTrue(line.contains("components="), "add-on aspect must include its components: " + line);
                expectedOrdinal++;
            }
            helper.assertValueEqual(expectedOrdinal, FmAspects.BUILTIN_ASPECT_COUNT + expected, "all add-on ordinals must be present");
            helper.assertTrue(addonLines.stream().anyMatch(line -> line.contains("tag=infernus ") && line.contains("name=Infernus ") && line.contains("components=ignis,praecantatio")), "infernus row must include exact name and component tags");
            helper.succeed();
        }

    @GameTest(template = "empty", templateNamespace = "forbiddenmagic")
    public static void forbiddenCategoryAndShardEntriesResolve(GameTestHelper helper) {
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
