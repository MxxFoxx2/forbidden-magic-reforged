package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.core.aspect.AspectDefinition;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.core.recipe.RecipeDefinition;
import dev.visfox.forbiddenreforged.core.research.ResearchCategoryDefinition;
import dev.visfox.forbiddenreforged.core.research.ResearchDefinition;
import dev.visfox.forbiddenreforged.integration.api.HostAdapter;
import dev.visfox.forbiddenreforged.integration.api.HostProfile;
import dev.visfox.forbiddenreforged.integration.runtime.HostRuntimeServices;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
import java.util.Map;

/**
 * Thaumcraft 4: Reforged implementation of the host-neutral content contract.
 */
public final class Tc4HostAdapter implements HostAdapter, HostRuntimeServices {
    public static final Tc4HostAdapter INSTANCE = new Tc4HostAdapter();

    private static final HostProfile PROFILE = new HostProfile(
        ContentId.parse("host:tc4-reforged"),
        "1.1.2",
        "26.3",
        Map.ofEntries(
            Map.entry(HostProfile.Feature.ASPECT_REGISTRATION,
                HostProfile.FeatureSupport.adapted("Uses the AddonAspects registration API")),
            Map.entry(HostProfile.Feature.ASPECT_DIAGNOSTICS,
                HostProfile.FeatureSupport.adapted("Reports native and add-on aspects from the TC4 registry")),
            Map.entry(HostProfile.Feature.RUNTIME_BOOTSTRAP,
                HostProfile.FeatureSupport.adapted("Initializes TC4 warp, wand, recipe, research and add-on integrations")),
            Map.entry(HostProfile.Feature.RUNTIME_SERVICES,
                HostProfile.FeatureSupport.adapted("Provides entity, wand, accessory, taint, damage and special-mining runtime services")),
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
    public ItemStack specialMiningDrop(BlockState state, ItemStack drop, float chance, RandomSource random) {
        return Tc4EnchantmentAdapter.specialMiningDrop(state, drop, chance, random);
    }

    @Override
    public boolean isTaintDamage(DamageSource source) {
        return Tc4EnchantmentAdapter.isTaintDamage(source);
    }
}
