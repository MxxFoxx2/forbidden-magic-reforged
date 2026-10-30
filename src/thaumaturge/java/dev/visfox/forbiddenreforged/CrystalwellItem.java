package dev.visfox.forbiddenreforged;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.research.pool.AspectPoolAccess;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class CrystalwellItem extends Item {
    private static final int MAX_DAMAGE = 100;

    public CrystalwellItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return MAX_DAMAGE;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() < stack.getMaxDamage()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        var lookup = serverPlayer.level().registryAccess().lookup(IAspect.REGISTRY_KEY).orElse(null);
        if (lookup != null) {
            for (var key : TTAspects.PRIMALS) {
                Holder<IAspect> aspect = lookup.get(key).orElse(null);
                if (aspect != null) {
                    AspectPoolAccess.grant(serverPlayer, aspect, 4 + level.getRandom().nextInt(4));
                }
            }
        }
        Item scribingTools = net.minecraft.core.registries.BuiltInRegistries.ITEM
            .getValue(Identifier.fromNamespaceAndPath("thaumaturge", "scribing_tools"));
        ItemStack spent = new ItemStack(scribingTools);
        spent.setDamageValue(spent.getMaxDamage());
        player.setItemInHand(hand, spent);
        return InteractionResult.SUCCESS;
    }
}
