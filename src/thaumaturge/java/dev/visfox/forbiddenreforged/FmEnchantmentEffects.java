package dev.visfox.forbiddenreforged;

import java.util.List;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

public final class FmEnchantmentEffects {
    private static final Set<Item> GARBAGE = Set.of(Items.DIRT, Items.SAND, Items.GRAVEL, Items.COBBLESTONE, Items.NETHERRACK);
    private static final TagKey<Block> INFUSED_STONES = TagKey.create(Registries.BLOCK, ForbiddenMagic.id("infused_stones"));
    private static final List<String> SIN_POOL = List.of("wrath", "envy", "gluttony", "pride", "lust", "sloth", "greed");

    private FmEnchantmentEffects() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(FmEnchantmentEffects::onBlockDrops);
        NeoForge.EVENT_BUS.addListener(FmEnchantmentEffects::onLivingDrops);
        NeoForge.EVENT_BUS.addListener(FmEnchantmentEffects::onExperienceDrop);
    }

    public static boolean isGarbage(ItemStack stack) {
        return GARBAGE.contains(stack.getItem());
    }

    public static ItemStack sinShard(RandomSource random) {
        while (true) {
            String vice = SIN_POOL.get(random.nextInt(SIN_POOL.size()));
            if (vice.equals("lust") && FmConfig.lustDisabled()) {
                continue;
            }
            if (vice.equals("gluttony")) {
                return new ItemStack(FmItems.GLUTTONY_SHARD.get());
            }
            return new ItemStack(FmItems.DEADLY_SHARDS.get(vice).get());
        }
    }

    public static boolean corruptRoll(RandomSource random) {
        return random.nextInt(3) == 1;
    }

    public static ItemStack greedyDrop(boolean villager, int roll) {
        if (villager) {
            return new ItemStack(Items.EMERALD);
        }
        return roll < 3 ? new ItemStack(FmItems.EMERALD_FRAGMENT.get()) : ItemStack.EMPTY;
    }

    public static int educationalExperience(int original, int level) {
        return original + 3 * original * level;
    }

    public static float wrathDamage(int level) {
        return 1.25F * level;
    }

    public static float clusterChance(int level) {
        return 0.2F + 0.075F * level;
    }

    public static boolean silverfishEmeraldRoll(boolean windsweptHills, int roll, int looting) {
        return roll <= (windsweptHills ? 2 + looting * 2 : 1 + looting * 3);
    }

    public static boolean skullAxeRoll(int roll, int looting, int baseChance, int lootingMultiplier) {
        return roll <= baseChance + looting * lootingMultiplier;
    }

    private static void onBlockDrops(BlockDropsEvent event) {
        if (!FmEnchantments.enabled()) {
            return;
        }
        Entity breaker = event.getBreaker();
        if (!(breaker instanceof ServerPlayer player) || breaker instanceof FakePlayer) {
            return;
        }
        ItemStack tool = event.getTool();
        int consuming = FmEnchantments.level(player.level().registryAccess(), tool, FmEnchantments.CONSUMING);
        if (consuming > 0) {
            event.getDrops().removeIf(drop -> isGarbage(drop.getItem()));
        }
        int cluster = FmEnchantments.level(player.level().registryAccess(), tool, FmEnchantments.CLUSTER);
        if (cluster > 0 && FmEnchantments.level(player.level().registryAccess(), tool, Enchantments.FORTUNE) == 0) {
            RandomSource random = event.getLevel().getRandom();
            float chance = clusterChance(cluster);
            for (ItemEntity drop : List.copyOf(event.getDrops())) {
                if (random.nextFloat() < chance) {
                    ItemStack stack = drop.getItem();
                    event.getDrops().add(new ItemEntity(event.getLevel(),
                        drop.getX(), drop.getY(), drop.getZ(), stack.copyWithCount(stack.getCount())));
                }
            }
        }
        int corrupting = FmEnchantments.level(player.level().registryAccess(), tool, FmEnchantments.CORRUPTING);
        if (corrupting > 0 && event.getState().is(INFUSED_STONES)) {
            RandomSource random = event.getLevel().getRandom();
            for (ItemEntity drop : event.getDrops()) {
                ItemStack stack = drop.getItem();
                if (corruptRoll(random) && "thaumaturge".equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace())) {
                    drop.setItem(sinShard(random).copyWithCount(stack.getCount()));
                }
            }
        }
    }

    private static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity victim = event.getEntity();
        Identifier victimType = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType());
        if (FmConfig.silverfishEmeralds()
            && event.isRecentlyHit()
            && victimType.equals(Identifier.withDefaultNamespace("silverfish"))) {
            Entity killer = event.getSource().getEntity();
            if (killer instanceof ServerPlayer player) {
                boolean windsweptHills = victim.level().getBiome(victim.blockPosition()).is(
                    ResourceKey.create(Registries.BIOME, Identifier.withDefaultNamespace("windswept_hills")));
                if (windsweptHills || !(killer instanceof FakePlayer)) {
                    int looting = FmEnchantments.level(
                        player.level().registryAccess(), player.getMainHandItem(), Enchantments.LOOTING);
                    int bound = windsweptHills ? 30 : 70;
                    int roll = victim.getRandom().nextInt(bound);
                    if (silverfishEmeraldRoll(windsweptHills, roll, looting)) {
                        addDrop(event, new ItemStack(FmItems.EMERALD_FRAGMENT.get()));
                    }
                }
            }
        }

        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof ServerPlayer player) || attacker instanceof FakePlayer || !event.isRecentlyHit()) {
            return;
        }
        if (FmEnchantments.enabled()) {
            greedyDrops(event, player);
        }
        ItemStack weapon = player.getMainHandItem();
        if (weapon.is(FmItems.DIABOLIST_FORK.get()) && FmSpawnerMobs.canImprint(event.getEntity().getType())) {
            String id = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).toString();
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack candidate = player.getInventory().getItem(slot);
                if (candidate.is(FmItems.MOB_CRYSTAL.get()) && MobCrystalItem.mob(candidate) == null) {
                    MobCrystalItem.setMob(candidate, id);
                    break;
                }
            }
        }
    }

    private static void greedyDrops(LivingDropsEvent event, ServerPlayer player) {
        if (!FmConfig.greedyEnchantmentEnabled()) {
            return;
        }
        ItemStack weapon = player.getMainHandItem();
        if (FmEnchantments.level(player.level().registryAccess(), weapon, FmEnchantments.GREEDY) <= 0) {
            return;
        }
        if (FmEnchantments.level(player.level().registryAccess(), weapon, Enchantments.LOOTING) > 0) {
            return;
        }
        boolean villager = event.getEntity() instanceof Villager;
        boolean hostile = event.getEntity() instanceof Enemy;
        if (!villager && !hostile) {
            return;
        }
        ItemStack drop = greedyDrop(villager, player.getRandom().nextInt(35));
        if (!drop.isEmpty()) {
            addDrop(event, drop);
        }
    }

    private static void onExperienceDrop(LivingExperienceDropEvent event) {
        if (!FmEnchantments.enabled() || !(event.getAttackingPlayer() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack weapon = player.getMainHandItem();
        int level = FmEnchantments.level(player.level().registryAccess(), weapon, FmEnchantments.EDUCATIONAL);
        if (level <= 0) {
            return;
        }
        if (FmEnchantments.level(player.level().registryAccess(), weapon, Enchantments.LOOTING) > 0) {
            return;
        }
        event.setDroppedExperience(educationalExperience(event.getOriginalExperience(), level));
    }

    private static void addDrop(LivingDropsEvent event, ItemStack stack) {
        LivingEntity entity = event.getEntity();
        ItemEntity drop = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), stack);
        drop.setPickUpDelay(10);
        event.getDrops().add(drop);
    }
}
