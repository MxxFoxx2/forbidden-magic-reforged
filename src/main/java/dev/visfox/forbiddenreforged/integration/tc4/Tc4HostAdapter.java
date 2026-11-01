package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.WrathCageBlockEntity;
import dev.visfox.forbiddenreforged.core.aspect.AspectDefinition;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.recipe.RecipeDefinition;
import dev.visfox.forbiddenreforged.core.research.ResearchCategoryDefinition;
import dev.visfox.forbiddenreforged.core.research.ResearchDefinition;
import dev.visfox.forbiddenreforged.integration.api.HostAdapter;
import dev.visfox.forbiddenreforged.integration.api.HostProfile;
import dev.visfox.forbiddenreforged.integration.runtime.HostAspectServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostMorphToolServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostBlockEntityServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostWandServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostTaintServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostScribingServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostItemServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostBootstrapServices;
import dev.visfox.forbiddenreforged.integration.runtime.HostEntityServices;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.registry.EldritchContent;
import java.util.List;
import java.util.function.Predicate;
import java.util.Map;
import java.util.Set;

/**
 * Thaumcraft 4: Reforged implementation of the host-neutral content contract.
 */
public final class Tc4HostAdapter implements HostAdapter, HostAspectServices, HostMorphToolServices, HostBlockEntityServices, HostWandServices, HostTaintServices, HostScribingServices, HostItemServices, HostBootstrapServices, HostEntityServices {
    public static final Tc4HostAdapter INSTANCE = new Tc4HostAdapter();

    private static final HostProfile PROFILE = new HostProfile(
        ContentId.parse("host:tc4-reforged"),
        "1.1.2",
        "26.3",
        Map.ofEntries(
            Map.entry(HostProfile.Feature.ASPECT_REGISTRATION,
                HostProfile.FeatureSupport.adapted("Uses the AddonAspects registration API")),
            Map.entry(HostProfile.Feature.ASPECT_SCAN_TARGETS,
                HostProfile.FeatureSupport.adapted("Registers item, block and entity scan targets through the TC4 AspectSync API")),
            Map.entry(HostProfile.Feature.ASPECT_DIAGNOSTICS,
                HostProfile.FeatureSupport.adapted("Reports native and add-on aspects from the TC4 registry")),
            Map.entry(HostProfile.Feature.ASPECT_PRESENTATION,
                HostProfile.FeatureSupport.adapted("Resolves display components for host aspects")),
            Map.entry(HostProfile.Feature.RUNTIME_BOOTSTRAP,
                HostProfile.FeatureSupport.adapted("Initializes TC4 warp, wand, recipe, research and add-on integrations")),
            Map.entry(HostProfile.Feature.MORPH_TOOL_RUNTIME,
                HostProfile.FeatureSupport.adapted("Provides morph-tool cycle sounds and area-mining behavior")),
            Map.entry(HostProfile.Feature.WAND_RUNTIME,
                HostProfile.FeatureSupport.adapted("Provides wand detection, focus properties, Vis storage and Baubles accessories")),
            Map.entry(HostProfile.Feature.TAINT_RUNTIME,
                HostProfile.FeatureSupport.adapted("Provides taint material rules, purification, special drops and damage semantics")),
            Map.entry(HostProfile.Feature.SCRIBING_RUNTIME,
                HostProfile.FeatureSupport.adapted("Provides research difficulty and player aspect-pool access")),
            Map.entry(HostProfile.Feature.WAND_RODS,
                HostProfile.FeatureSupport.adapted("Registers TC4 wand rods and their update behaviors")),
            Map.entry(HostProfile.Feature.WAND_CAPS,
                HostProfile.FeatureSupport.adapted("Registers TC4 wand caps and material properties")),
            Map.entry(HostProfile.Feature.WAND_TRIGGERS,
                HostProfile.FeatureSupport.adapted("Registers TC4 wand block triggers")),
            Map.entry(HostProfile.Feature.FOCI_OR_SPELLS,
                HostProfile.FeatureSupport.adapted("Integrates the Blink Focus with the TC4 wand/focus API")),
            Map.entry(HostProfile.Feature.OPTIONAL_ENCHANTMENT_INTEGRATION,
                HostProfile.FeatureSupport.partial("Thaumic Tinkerer enchantment costs are registered only when the optional mod is installed")),
            Map.entry(HostProfile.Feature.ITEM_REGISTRATION,
                HostProfile.FeatureSupport.adapted("Creates TC4-backed items and applies TC4 tool material/food property adapters")),
            Map.entry(HostProfile.Feature.BLOCK_ENTITY_IMPLEMENTATION,
                HostProfile.FeatureSupport.adapted("Provides the TC4 EssentiaSource/EssentiaTransport Wrath Cage bridge implementation")),
            Map.entry(HostProfile.Feature.ENTITY_ASPECTS,
                HostProfile.FeatureSupport.adapted("Provides vanilla and Thaumcraft entity aspect mappings plus TC4 boss and taint predicates")),
            Map.entry(HostProfile.Feature.RESEARCH_ENTRIES,
                HostProfile.FeatureSupport.adapted("Translates common research definitions to ResearchItem")),
            Map.entry(HostProfile.Feature.RESEARCH_STAGES,
                HostProfile.FeatureSupport.partial("Only one page group is supported; multi-stage definitions are rejected")),
            Map.entry(HostProfile.Feature.ARCANE_RECIPES,
                HostProfile.FeatureSupport.adapted("Translates common arcane recipe definitions")),
            Map.entry(HostProfile.Feature.CRAFTING_RECIPE_REFERENCES,
                HostProfile.FeatureSupport.adapted("Translates common crafting recipe references")),
            Map.entry(HostProfile.Feature.CRUCIBLE_RECIPES,
                HostProfile.FeatureSupport.adapted("Translates common crucible recipe definitions")),
            Map.entry(HostProfile.Feature.INFUSION_RECIPES,
                HostProfile.FeatureSupport.adapted("Translates common infusion recipe definitions")),
            Map.entry(HostProfile.Feature.INFUSION_ENCHANTMENTS,
                HostProfile.FeatureSupport.adapted("Translates common infusion enchantment definitions"))
        )
    );

    private Tc4HostAdapter() {
    }

    @Override
    public HostProfile profile() {
        return PROFILE;
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
    public void registerAspectTags() {
        Tc4AspectTagAdapter.register();
    }

    @Override
    public void registerAspects(List<AspectDefinition> aspects) {
        try {
            Tc4AspectAdapter.registerAspects(aspects);
        } catch (NoSuchMethodError | NoClassDefFoundError absent) {
            throw new IllegalStateException(
                "Forbidden Magic requires Thaumcraft 4: Reforged 1.1.2 or newer with the add-on aspect API",
                absent
            );
        }
    }

    @Override
    public boolean crystalwellUsesShardTag() {
        return Tc4RecipeAdapter.crystalwellUsesShardTag();
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
    public Map<ContentId, ContentId> aspectCatalog(boolean silverfishEmeralds) {
        return Tc4EntityAdapter.aspectCatalog(silverfishEmeralds);
    }

    @Override
    public Set<ContentId> bossEntityIds() {
        return Tc4EntityAdapter.bossEntityIds();
    }

    @Override
    public Component aspectDescription(ContentId aspect) {
        return Tc4AspectAdapter.description(aspect);
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
    public void breakArea(
        ItemStack tool,
        ServerLevel level,
        ServerPlayer player,
        BlockPos origin,
        Predicate<BlockState> correctTool
    ) {
        Tc4MorphToolAdapter.breakArea(tool, level, player, origin, correctTool);
    }

    @Override
    public WrathCageBlockEntity createWrathCageBlockEntity(BlockPos pos, BlockState state) {
        return new Tc4WrathCageBlockEntity(pos, state);
    }

    @Override
    public boolean isWand(ItemStack stack) {
        return Tc4WandRuntimeAdapter.isWand(stack);
    }

    @Override
    public String wandRodTag(ItemStack stack) {
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
    public void addWandVis(ItemStack stack, ContentId aspect, int amount, boolean apply) {
        Tc4WandRuntimeAdapter.addVis(stack, aspect, amount, apply);
    }

    @Override
    public void addRealVis(ItemStack stack, ContentId aspect, int amount, boolean apply) {
        Tc4VisAdapter.addRealVis(stack, aspect, amount, apply);
    }

    @Override
    public ItemStack accessory(Player player, int slot) {
        return Tc4AccessoryAdapter.getBauble(player, slot);
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
    public Item.Properties gluttonyFoodProperties(Item.Properties properties) {
        return Tc4ItemRegistrationAdapter.gluttonyFood(properties);
    }

    @Override
    public Item.Properties skullAxeProperties(Item.Properties properties) {
        return Tc4ItemRegistrationAdapter.skullAxe(properties);
    }

    @Override
    public Item.Properties morphPickaxeProperties(Item.Properties properties) {
        return Tc4ItemRegistrationAdapter.morphPickaxe(properties);
    }

    @Override
    public Item.Properties morphSwordProperties(Item.Properties properties) {
        return Tc4ItemRegistrationAdapter.morphSword(properties);
    }

    @Override
    public Item.Properties morphShovelProperties(Item.Properties properties) {
        return Tc4ItemRegistrationAdapter.morphShovel(properties);
    }

    @Override
    public Item.Properties morphAxeProperties(Item.Properties properties) {
        return Tc4ItemRegistrationAdapter.morphAxe(properties);
    }

    @Override
    public Item.Properties diabolistForkProperties(Item.Properties properties) {
        return Tc4ItemRegistrationAdapter.diabolistFork(properties);
    }

    @Override
    public Item.Properties taintShovelProperties(Item.Properties properties) {
        return Tc4ItemRegistrationAdapter.taintShovel(properties);
    }

    @Override
    public Item.Properties taintPickaxeProperties(Item.Properties properties) {
        return Tc4ItemRegistrationAdapter.taintPickaxe(properties);
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
    public Item.Properties blinkFocusProperties(Item.Properties properties) {
        return Tc4BlinkFocusItem.properties(properties);
    }

    @Override
    public Item createSubCollar(Item.Properties properties) {
        return new Tc4SubCollarItem(properties);
    }

    @Override
    public Item primordialPearlItem() {
        return EldritchContent.PRIMORDIAL_PEARL.get();
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
    public void addCreativeWands(List<ItemStack> stacks) {
        Tc4WandAdapter.addCreativeWands(stacks);
    }
}
