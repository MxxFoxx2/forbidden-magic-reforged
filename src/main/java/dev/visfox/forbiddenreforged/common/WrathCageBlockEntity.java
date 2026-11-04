package dev.visfox.forbiddenreforged.common;

import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public abstract class WrathCageBlockEntity extends BlockEntity {
    private static final int MAX_NEARBY = 6;
    private static final int SPAWN_RANGE = 4;
    private static final int SPAWN_COUNT = 3;

    private final WrathCageEssentiaStore essentia = new WrathCageEssentiaStore();
    private @Nullable String mobId;
    private int fuel;
    private int spawnDelay = 20;
    private boolean slothful;

    protected WrathCageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public @Nullable String getMobId() {
        return mobId;
    }

    public void cycleMode() {
        essentia.cycleMode();
        sync();
    }

    public void setMobId(@Nullable String mobId) {
        ContentId previousAspect = mobEssentiaType();
        this.mobId = mobId;
        if (!previousAspect.equals(mobEssentiaType())) {
            essentia.clearMobEssentia();
        }
        sync();
    }

    public static boolean shouldSpawn(boolean powered, boolean mobSet, int fuel, int cost, int availableEssentia) {
        return !powered && mobSet && (cost == 0 || fuel > 0 || availableEssentia >= cost);
    }

    public static int nextDelay(RandomSource random, boolean slothful) {
        int delay = 200 + random.nextInt(100);
        return slothful ? delay + 200 : delay;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WrathCageBlockEntity cage) {
        if (level.isClientSide()) {
            return;
        }
        int cost = FmConfig.wrathCost();
        if (level instanceof ServerLevel serverLevel
            && FmConfig.wrathCage()
            && !level.hasNeighborSignal(pos)) {
            cage.tickSpawner(serverLevel, pos, cost);
        }
        if (cost > 0 && cage.mobId != null && cage.essentia.hasRoom()) {
            cage.drawEssentia(cost);
        }
    }

    private void tickSpawner(ServerLevel level, BlockPos pos, int cost) {
        if (mobId == null) {
            return;
        }
        if (cost > 0 && fuel <= 0) {
            WrathCageEssentiaStore.FuelChannel channel = essentia.consumeFuel(cost);
            if (channel != null) {
                fuel = FmConfig.wrathEff();
                slothful = channel == WrathCageEssentiaStore.FuelChannel.SLOTH;
                sync();
            }
        }
        if (spawnDelay == -1) {
            spawnDelay = nextDelay(level.getRandom(), slothful);
        }
        if (fuel <= 0 && cost > 0) {
            return;
        }
        if (spawnDelay > 0) {
            spawnDelay--;
            return;
        }
        spawnRound(level, pos);
    }

    private void drawEssentia(int cost) {
        if (essentia.drawOne(cost, mobEssentiaType(), adjacentEssentiaInputs())) {
            sync();
        }
    }

    protected abstract ContentId mobEssentiaType();

    protected abstract List<EssentiaInput> adjacentEssentiaInputs();

    public interface EssentiaInput {
        int take(ContentId aspect, int amount);
    }

    public Map<ContentId, Integer> visibleEssentia() {
        if (FmConfig.wrathCost() <= 0 || mobId == null) {
            return Map.of();
        }
        return essentia.visible(mobEssentiaType());
    }

    public boolean acceptsEssentia(ContentId aspect) {
        return FmConfig.wrathCost() > 0 && essentia.accepts(aspect, mobEssentiaType());
    }

    public int addEssentiaToContainer(ContentId aspect, int amount) {
        return FmConfig.wrathCost() <= 0
            ? amount
            : essentia.addToContainer(aspect, amount, mobEssentiaType());
    }

    public boolean containsEssentia(ContentId aspect, int amount) {
        return essentia.amount(aspect, mobEssentiaType()) >= amount;
    }

    public int essentiaAmount(ContentId aspect) {
        return essentia.amount(aspect, mobEssentiaType());
    }

    public int addTransportEssentia(ContentId aspect, int amount) {
        return essentia.addTransport(aspect, amount, mobEssentiaType());
    }

    public boolean canConnectEssentia() {
        return FmConfig.wrathCost() > 0;
    }

    public ContentId suctionEssentiaType() {
        if (FmConfig.wrathCost() <= 0) {
            return null;
        }
        return essentia.suctionType(mobEssentiaType());
    }

    public int suctionEssentiaAmount() {
        return FmConfig.wrathCost() > 0 ? WrathCageEssentiaStore.SUCTION : 0;
    }

    public int transportEssentiaAmount() {
        return essentia.selectedAmount();
    }

    public static AABB nearbyEntityBounds(BlockPos pos) {
        return new AABB(pos).inflate(SPAWN_RANGE * 2, 4.0, SPAWN_RANGE * 2);
    }

    private void spawnRound(ServerLevel level, BlockPos pos) {
        net.minecraft.resources.Identifier id = mobId == null ? null : net.minecraft.resources.Identifier.tryParse(mobId);
        if (id == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
            return;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(id);
        AABB nearby = nearbyEntityBounds(pos);
        int cost = FmConfig.wrathCost();
        for (int attempt = 0; attempt < SPAWN_COUNT && (fuel > 0 || cost <= 0); attempt++) {
            Entity entity = type.create(level, EntitySpawnReason.SPAWNER);
            if (entity == null) {
                return;
            }
            if (level.getEntitiesOfClass(entity.getClass(), nearby).size() >= MAX_NEARBY) {
                spawnDelay = nextDelay(level.getRandom(), slothful);
                return;
            }
            double x = pos.getX() + (level.getRandom().nextDouble() - level.getRandom().nextDouble()) * SPAWN_RANGE;
            double y = pos.getY() + level.getRandom().nextInt(3) - 1;
            double z = pos.getZ() + (level.getRandom().nextDouble() - level.getRandom().nextDouble()) * SPAWN_RANGE;
            entity.snapTo(x, y, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
            if (level.noCollision(entity) && !level.containsAnyLiquid(entity.getBoundingBox())) {
                level.addFreshEntity(entity);
                level.levelEvent(2004, pos, 0);
                fuel--;
                spawnDelay = nextDelay(level.getRandom(), slothful);
            }
        }
    }

    public static ItemStack crystalForBreak(WrathCageBlockEntity cage) {
        ItemStack stack = new ItemStack(FmItems.MOB_CRYSTAL.get());
        if (cage.mobId != null) {
            MobCrystalItem.setMob(stack, cage.mobId);
        }
        return stack;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (mobId != null && level != null) {
            ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, crystalForBreak(this));
            level.addFreshEntity(drop);
        }
    }

    public void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (mobId != null) {
            output.putString("MobId", mobId);
        }
        output.putInt("Special", essentia.special());
        output.putInt("Wrath", essentia.wrath());
        output.putInt("Sloth", essentia.sloth());
        output.putInt("Fuel", fuel);
        output.putInt("Delay", spawnDelay);
        output.putBoolean("Slothful", slothful);
        output.putInt("Mode", essentia.mode());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        mobId = input.getString("MobId").orElse(null);
        essentia.load(
            input.getIntOr("Special", 0),
            input.getIntOr("Wrath", 0),
            input.getIntOr("Sloth", 0),
            input.getIntOr("Mode", 0)
        );
        fuel = input.getIntOr("Fuel", 0);
        spawnDelay = input.getIntOr("Delay", 20);
        slothful = input.getBooleanOr("Slothful", false);
    }
}
