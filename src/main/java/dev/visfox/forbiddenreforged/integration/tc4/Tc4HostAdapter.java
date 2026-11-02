package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.aspect.AspectDefinition;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.wand.BlinkFocusRules;
import dev.visfox.forbiddenreforged.core.recipe.RecipeDefinition;
import dev.visfox.forbiddenreforged.core.research.ResearchCategoryDefinition;
import dev.visfox.forbiddenreforged.core.research.ResearchDefinition;
import dev.visfox.forbiddenreforged.core.warp.WarpKind;
import dev.visfox.forbiddenreforged.integration.api.HostAdapter;
import dev.visfox.forbiddenreforged.integration.api.HostProfile;
import dev.visfox.forbiddenreforged.integration.runtime.HostAspectServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostBootstrapServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostAccessoryServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostBlockEntityServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostEnchantmentEffectServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostEntityAspectServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostEntityClassificationServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostMorphToolServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostItemFactoryServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostItemPropertyServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostScribingServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostTaintBlockServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostWandInspectionServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostWandVisServices;
import dev.visfox.forbiddenreforged.core.wand.WandVisAccess;
import dev.visfox.forbiddenreforged.integration.runtime.HostWandPartsServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostFocusServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostEssentiaTransportServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostWarpServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostVillagerTradeServices;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.npc.VillagerTrades;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Set;
import java.util.function.Predicate;
import thaumcraft.item.gear.TcMaterials;
import thaumcraft.platform.Registration;
import net.neoforged.fml.ModList;
import thaumcraft.wand.focus.FocusItem;
import net.minecraft.server.level.ServerPlayer;

/**
 * Thaumcraft 4: Reforged adapter for the Minecraft 1.21.1 release track.
 *
 * Shared semantic catalogs are registered through this boundary. Existing
 * 1.21.1 gameplay integrations remain intact while their extraction into
 * focused host capabilities proceeds incrementally.
 */
public final class Tc4HostAdapter implements HostAdapter, HostAspectServices, HostWarpServices, HostMorphToolServices, HostBlockEntityServices, HostWandInspectionServices, HostWandVisServices, HostWandPartsServices, HostFocusServices, HostEssentiaTransportServices, HostAccessoryServices, HostTaintBlockServices, HostEnchantmentEffectServices, HostScribingServices, HostEntityAspectServices, HostEntityClassificationServices, HostItemFactoryServices, HostItemPropertyServices, HostBootstrapServices, HostVillagerTradeServices {
    private static final WandVisAccess WAND_VIS = new Tc4WandVisAdapter();

    public static final Tc4HostAdapter INSTANCE = new Tc4HostAdapter();

    private static final String LEGACY_RUNTIME_NOTE =
        "Still implemented by the 1.21.1 legacy gameplay path; extraction behind this host capability is pending";

    private static final HostProfile PROFILE = createProfile();

    private Tc4HostAdapter() {
    }

    private static HostProfile createProfile() {
        Map<HostProfile.Feature, HostProfile.FeatureSupport> support =
            new EnumMap<>(HostProfile.Feature.class);
        for (HostProfile.Feature feature : HostProfile.Feature.values()) {
            support.put(feature, HostProfile.FeatureSupport.partial(LEGACY_RUNTIME_NOTE));
        }

        adapted(support, HostProfile.Feature.ASPECT_REGISTRATION,
            "Registers shared semantic aspect definitions through AddonAspects");
        adapted(support, HostProfile.Feature.ASPECT_DIAGNOSTICS,
            "Reads the TC4 aspect registry for startup diagnostics");
        adapted(support, HostProfile.Feature.ASPECT_PRESENTATION,
            "Resolves semantic aspect identifiers to TC4 display components");
        adapted(support, HostProfile.Feature.RUNTIME_BOOTSTRAP,
            "Registers TC4 wand rods, caps, triggers, optional enchantment costs, creative host wands, and shared catalogs");
        adapted(support, HostProfile.Feature.ASPECT_RECIPE_DERIVATION,
            "Uses semantic aspect definitions and component identifiers");
        adapted(support, HostProfile.Feature.ASPECT_SCAN_TARGETS,
            "Registers scan targets through the existing 1.21.1 AspectSync implementation");
        adapted(support, HostProfile.Feature.RESEARCH_ENTRIES,
            "Translates shared research definitions, including sibling links and item/entity/aspect unlock triggers, to TC4 ResearchItem entries");
        support.put(HostProfile.Feature.RESEARCH_STAGES,
            HostProfile.FeatureSupport.partial("The TC4 adapter accepts one page group; multi-stage definitions are rejected"));
        adapted(support, HostProfile.Feature.ARCANE_RECIPES, "Translates shared arcane recipe definitions");
        adapted(support, HostProfile.Feature.CRAFTING_RECIPE_REFERENCES,
            "Translates shared crafting recipe references");
        adapted(support, HostProfile.Feature.CRUCIBLE_RECIPES, "Translates shared crucible recipe definitions");
        adapted(support, HostProfile.Feature.INFUSION_RECIPES, "Translates shared infusion recipe definitions");
        adapted(support, HostProfile.Feature.INFUSION_ENCHANTMENTS,
            "Translates shared infusion enchantment definitions");
        adapted(support, HostProfile.Feature.WARP_POOLS,
            "Maps semantic warp kinds to the three TC4 warp pools");
        adapted(support, HostProfile.Feature.STICKY_WARP, "Preserves sticky warp as WarpType.STICKY");
        adapted(support, HostProfile.Feature.VILLAGER_TRADES,
            "Contributes TC4-backed Profane Wand and Fire Focus novice trade offers");
        adapted(support, HostProfile.Feature.MORPH_TOOL_RUNTIME,
            "Uses the TC4 ToolHarvest API for morph-tool area harvesting and cycle sounds");
        adapted(support, HostProfile.Feature.WAND_INSPECTION,
            "Reads wand, rod and focus properties through the TC4 wand API");
        support.put(HostProfile.Feature.WAND_RODS, HostProfile.FeatureSupport.partial(
            "Rod definitions are shared; TC4 registry registration and RodUpdate callbacks remain host-specific"));
        support.put(HostProfile.Feature.WAND_CAPS, HostProfile.FeatureSupport.partial(
            "Cap definitions are shared; TC4 registry registration and host material resolution remain host-specific"));
        support.put(HostProfile.Feature.WAND_PARTS, HostProfile.FeatureSupport.partial(
            "Core exposes a typed parts access contract; mutation still uses TC4 WandParts and WandKeys"));
        support.put(HostProfile.Feature.WAND_TRIGGERS, HostProfile.FeatureSupport.partial(
            "Hellfire trigger rules are shared; TC4 trigger registration and runtime callbacks remain host-specific"));
        support.put(HostProfile.Feature.FOCI_OR_SPELLS, HostProfile.FeatureSupport.partial(
            "Blink Focus upgrade rules and Vis costs are shared; FocusItem registration and runtime remain TC4-bound"));
        adapted(support, HostProfile.Feature.ACCESSORY_INTEGRATION,
            "Reads accessory slots through the TC4 Baubles bridge");
        adapted(support, HostProfile.Feature.TAINT_BLOCK_SERVICES,
            "Uses TC4 taint material and flux purification APIs");
        adapted(support, HostProfile.Feature.ENCHANTMENT_EFFECTS,
            "Delegates special mining drops and taint damage classification to TC4");
        adapted(support, HostProfile.Feature.SCRIBING_RUNTIME,
            "Reads TC4 research difficulty, primal aspects and player aspect pools");
        adapted(support, HostProfile.Feature.BLOCK_ENTITY_IMPLEMENTATION,
            "Creates the 1.21.1 TC4 EssentiaSource/EssentiaTransport Wrath Cage");
        support.put(HostProfile.Feature.ESSENTIA_STORAGE, HostProfile.FeatureSupport.partial(
            "Three-channel storage policy is shared; the TC4 block entity still owns persistence and EssentiaSource callbacks"));
        support.put(HostProfile.Feature.ESSENTIA_TRANSPORT, HostProfile.FeatureSupport.partial(
            "Extraction priority is shared and physical neighbor pulls use a typed host capability; TC4 EssentiaTransport remains the runtime implementation"));
        adapted(support, HostProfile.Feature.WAND_VIS_STORAGE,
            "Exposes semantic wand Vis reads/writes and separate item-backed real-Vis storage writes");
        adapted(support, HostProfile.Feature.ENTITY_ASPECTS,
            "Provides entity-to-aspect mappings using semantic identifiers");
        adapted(support, HostProfile.Feature.ENTITY_CLASSIFICATION,
            "Delegates tainted-mob, TC4 boss and golem classification to TC4");
        adapted(support, HostProfile.Feature.ITEM_PROPERTIES,
            "Provides TC4 tool stats, rarity, food and focus properties");
        adapted(support, HostProfile.Feature.ITEM_FACTORIES,
            "Creates TC4-bound morph, ring, scribing and accessory items behind the host factory");
        support.put(HostProfile.Feature.TAINT_ORIGINS,
            HostProfile.FeatureSupport.unsupported("No TC4 taint-origin registration or propagation adapter exists"));

        return new HostProfile(
            ContentId.parse("host:tc4-reforged"),
            "1.1.2",
            "1.21.1",
            support
        );
    }

    private static void adapted(
        Map<HostProfile.Feature, HostProfile.FeatureSupport> support,
        HostProfile.Feature feature,
        String notes
    ) {
        support.put(feature, HostProfile.FeatureSupport.adapted(notes));
    }

    @Override
    public HostProfile profile() {
        return PROFILE;
    }

    @Override
    public void registerAspectTags() {
        Tc4AspectTagAdapter.register();
    }

    @Override
    public boolean crystalwellUsesShardTag() {
        return Tc4RecipeAdapter.crystalwellUsesShardTag();
    }

    @Override
    public void initializeRuntime() {
        Tc4BootstrapAdapter.initialize();
    }

    @Override
    public List<String> aspectDiagnostics() {
        return Tc4AspectDiagnostics.registryLines();
    }

    @Override
    public void registerAspects(List<AspectDefinition> aspects) {
        Tc4AspectAdapter.registerAspects(aspects);
    }

    @Override
    public void registerRecipes(List<RecipeDefinition> recipes) {
        Tc4RecipeAdapter.registerRecipes(recipes);
    }

    @Override
    public void registerResearch(
        List<ResearchCategoryDefinition> categories,
        List<ResearchDefinition> research
    ) {
        Tc4ResearchAdapter.register(categories, research);
    }

    @Override
    public Component aspectDescription(ContentId aspect) {
        return Tc4AspectAdapter.description(aspect);
    }

    @Override
    public void changeWarp(ServerPlayer player, int amount, WarpKind kind) {
        Tc4WarpAdapter.changeWarp(player, amount, kind);
    }

    @Override
    public void playMorphCycleSound(Level level, BlockPos pos, RandomSource random) {
        Tc4MorphToolAdapter.playCycleSound(level, pos, random);
    }

    @Override
    public boolean isAreaHarvesting() {
        return Tc4MorphToolAdapter.isAreaHarvesting();
    }

    @Override
    public void breakArea(ItemStack tool, ServerLevel level, ServerPlayer player, BlockPos origin,
                          Predicate<BlockState> correctTool) {
        Tc4MorphToolAdapter.breakArea(tool, level, player, origin, correctTool);
    }

    @Override
    public BlockEntity createWrathCageBlockEntity(BlockPos pos, BlockState state) {
        return new Tc4WrathCageBlockEntity(pos, state);
    }

    @Override
    public String wrathCageMobId(BlockEntity cage) {
        return requireWrathCage(cage).getMobId();
    }

    @Override
    public void setWrathCageMobId(BlockEntity cage, String mobId) {
        requireWrathCage(cage).setMobId(mobId);
    }

    @Override
    public void cycleWrathCageMode(BlockEntity cage) {
        requireWrathCage(cage).cycleMode();
    }

    @Override
    public void tickWrathCage(Level level, BlockPos pos, BlockState state, BlockEntity cage) {
        Tc4WrathCageBlockEntity.tick(level, pos, state, requireWrathCage(cage));
    }

    private static Tc4WrathCageBlockEntity requireWrathCage(BlockEntity cage) {
        if (cage instanceof Tc4WrathCageBlockEntity tc4Cage) {
            return tc4Cage;
        }
        throw new IllegalArgumentException(
            "Expected a Thaumcraft-backed Wrath Cage block entity, got " + cage.getClass().getName()
        );
    }

    @Override
    public void beforeRemoveWrathCage(BlockState state, Level level, BlockPos pos, BlockState newState) {
        Tc4BlockEntityAdapter.beforeRemoveWrathCage(state, level, pos, newState);
    }

    @Override
    public boolean takeOneFromOutput(net.minecraft.world.level.block.entity.BlockEntity source,
                                     net.minecraft.core.Direction outputSide, ContentId aspect) {
        return Tc4EssentiaTransportAdapter.takeOneFromOutput(source, outputSide, aspect);
    }

    @Override
    public boolean isWand(ItemStack stack) {
        return Tc4WandRuntimeAdapter.isWand(stack);
    }

    @Override
    public @org.jetbrains.annotations.Nullable String wandRodTag(ItemStack stack) {
        return Tc4WandRuntimeAdapter.rodTag(stack);
    }

    @Override
    public int wandFocusTreasure(ItemStack stack) {
        return Tc4WandRuntimeAdapter.focusTreasure(stack);
    }

    @Override
    public int wandFocusPotency(ItemStack stack) {
        return Tc4WandRuntimeAdapter.focusPotency(stack);
    }

    @Override
    public ItemStack wandFocusItem(ItemStack stack) {
        return Tc4WandRuntimeAdapter.focusItem(stack);
    }

    @Override
    public int wandFocusEnlarge(ItemStack stack) {
        return Tc4WandRuntimeAdapter.focusEnlarge(stack);
    }

    @Override
    public boolean hasFocusUpgrade(ItemStack focus, BlinkFocusRules.Upgrade upgrade) {
        return Tc4FocusAdapter.hasUpgrade(focus, upgrade);
    }

    @Override
    public boolean consumeFocusVis(ItemStack wand, Player player, List<BlinkFocusRules.VisCost> costs, boolean apply) {
        return Tc4FocusAdapter.consumeVis(wand, player, costs, apply);
    }

    @Override
    public List<ContentId> primalAspects() {
        return WAND_VIS.primalAspects();
    }

    @Override
    public int maximumVis(ItemStack wand) {
        return WAND_VIS.maximumVis(wand);
    }

    @Override
    public int vis(ItemStack wand, ContentId aspect) {
        return WAND_VIS.vis(wand, aspect);
    }

    @Override
    public void addVis(ItemStack wand, ContentId aspect, int amount, boolean apply) {
        WAND_VIS.addVis(wand, aspect, amount, apply);
    }

    @Override
    public void addRealVis(ItemStack wand, ContentId aspect, int amount, boolean apply) {
        WAND_VIS.addRealVis(wand, aspect, amount, apply);
    }

    @Override
    public void fill(ItemStack wand) {
        WAND_VIS.fill(wand);
    }

    @Override
    public void addItemRealVis(ItemStack stack, ContentId aspect, int amount, boolean apply) {
        Tc4VisAdapter.addRealVis(stack, aspect, amount, apply);
    }

    @Override
    public void setRod(ItemStack wand, ContentId rod) {
        new Tc4WandPartsAdapter().setRod(wand, rod);
    }

    @Override
    public ItemStack accessory(Player player, int slot) {
        return Tc4AccessoryAdapter.getBauble(player, slot);
    }

    @Override
    public void setAccessory(Player player, int slot, ItemStack stack) {
        Tc4AccessoryAdapter.setBauble(player, slot, stack);
    }

    @Override
    public boolean isTaintMaterial(BlockState state) {
        return Tc4TaintAdapter.isHostTaintMaterial(state);
    }

    @Override
    public boolean isTaintShovelPurificationTarget(BlockState state) {
        return Tc4TaintAdapter.isTaintShovelPurificationTarget(state);
    }

    @Override
    public float taintShovelMiningSpeed() {
        return Tc4TaintAdapter.taintShovelMiningSpeed();
    }

    @Override
    public void playTaintShovelSound(Level level, BlockPos pos) {
        Tc4TaintAdapter.playTaintShovelSound(level, pos);
    }

    @Override
    public ItemStack specialMiningDrop(BlockState state, ItemStack drop, float chance, RandomSource random) {
        return Tc4EnchantmentAdapter.specialMiningDrop(state, drop, chance, random);
    }

    @Override
    public boolean isTaintDamage(DamageSource source) {
        return Tc4EnchantmentAdapter.isTaintDamage(source);
    }

    @Override
    public boolean isNoResearchDifficulty() {
        return Tc4ScribingAdapter.isNoResearchDifficulty();
    }

    @Override
    public List<ContentId> primalAspectIds() {
        return Tc4ScribingAdapter.primalAspectIds();
    }

    @Override
    public boolean addAspectPool(ServerPlayer player, ContentId aspect, int amount) {
        return Tc4ScribingAdapter.addAspectPool(player, aspect, amount);
    }

    @Override
    public ItemStack createSpentScribingToolsStack() {
        return Tc4ScribingAdapter.spentScribingToolsStack();
    }

    @Override
    public Map<ContentId, ContentId> aspectCatalog(boolean silverfishEmeralds) {
        return Tc4EntityAdapter.aspectCatalog(silverfishEmeralds);
    }

    @Override
    public boolean isTaintedMob(LivingEntity entity) {
        return Tc4EntityAdapter.isTaintedMob(entity);
    }

    @Override
    public boolean isHostBoss(LivingEntity entity) {
        return Tc4EntityAdapter.isHostBoss(entity);
    }

    @Override
    public boolean isHostGolem(LivingEntity entity) {
        return Tc4EntityAdapter.isHostGolem(entity);
    }

    @Override
    public Set<ContentId> bossEntityIds() {
        return Tc4EntityAdapter.bossEntityIds();
    }

    @Override
    public Item.Properties gluttonyFoodProperties(Item.Properties properties) {
        return Registration.food(properties, 2, 0.1F);
    }

    @Override
    public Item.Properties skullAxeProperties(Item.Properties properties) {
        return Registration.sword(properties.rarity(Rarity.UNCOMMON), TcMaterials.ELEMENTAL_TOOL, 3.0F, -2.4F);
    }

    @Override
    public Item.Properties morphPickaxeProperties(Item.Properties properties) {
        return Registration.pickaxe(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 2.0F, -2.8F);
    }

    @Override
    public Item.Properties morphSwordProperties(Item.Properties properties) {
        return Registration.sword(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 3.0F, -2.4F);
    }

    @Override
    public Item.Properties morphShovelProperties(Item.Properties properties) {
        return Registration.shovel(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 1.0F, -3.0F);
    }

    @Override
    public Item.Properties morphAxeProperties(Item.Properties properties) {
        return Registration.axe(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 3.0F, -3.0F);
    }

    @Override
    public Item.Properties diabolistForkProperties(Item.Properties properties) {
        return Registration.sword(properties.rarity(Rarity.UNCOMMON), TcMaterials.THAUMIUM_TOOL, 3.0F, -2.4F);
    }

    @Override
    public Item.Properties taintShovelProperties(Item.Properties properties) {
        return Registration.shovel(properties.rarity(Rarity.UNCOMMON), TcMaterials.ELEMENTAL_TOOL, 1.0F, -3.0F);
    }

    @Override
    public Item.Properties taintPickaxeProperties(Item.Properties properties) {
        return Registration.pickaxe(properties.rarity(Rarity.UNCOMMON), TcMaterials.ELEMENTAL_TOOL, 1.0F, -2.8F);
    }

    @Override
    public Item.Properties blinkFocusProperties(Item.Properties properties) {
        return FocusItem.properties(properties);
    }

    @Override
    public Item createMorphTool(Item.Properties properties) {
        return new Tc4MorphToolItem(properties);
    }

    @Override
    public Item createRingNutrition(Item.Properties properties) {
        return new Tc4RingNutritionItem(properties);
    }

    @Override
    public Item createCrystalwell(Item.Properties properties) {
        return new Tc4CrystalwellItem(properties);
    }

    @Override
    public Item createPrimewell(Item.Properties properties) {
        return new Tc4PrimewellItem(properties);
    }

    @Override
    public Item createBlinkFocus(Item.Properties properties) {
        return new Tc4BlinkFocusItem(properties);
    }

    @Override
    public Item primordialPearlItem() {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse("thaumcraft:primordial_pearl"));
    }

    @Override
    public Item createSubCollar(Item.Properties properties) {
        return new Tc4SubCollarItem(properties);
    }

    @Override
    public void registerWands() {
        Tc4WandAdapter.registerWands();
    }

    @Override
    public void registerWandTriggers() {
        Tc4WandTriggerAdapter.register();
    }

    @Override
    public void registerOptionalEnchantments() {
        Tc4TinkererAdapter.register();
    }

    @Override
    public int registeredOptionalEnchantments() {
        return Tc4TinkererAdapter.registeredCount();
    }

    @Override
    public boolean isArsMagicaLoaded() {
        return ModList.get().isLoaded("arsmagicalegacy") || ModList.get().isLoaded("arsmagica2");
    }

    @Override
    public void addCreativeWands(List<ItemStack> stacks) {
        Tc4WandAdapter.addCreativeWands(stacks);
    }

    @Override
    public void addNoviceTrades(List<VillagerTrades.ItemListing> trades) {
        Tc4VillagerTradeAdapter.addNoviceTrades(trades);
    }
}
