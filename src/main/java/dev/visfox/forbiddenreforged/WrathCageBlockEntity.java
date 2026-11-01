package dev.visfox.forbiddenreforged;

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
    private static final int MAX_STORED = 64;
    private static final int MAX_NEARBY = 6;
    private static final int SPAWN_RANGE = 4;
    private static final int SPAWN_COUNT = 3;
    private static final int PLAYER_RANGE = 16;
    private static final int SUCTION = 128;
    private static final ContentId WRATH_ASPECT = ContentId.parse("forbiddenmagic:ira");
    private static final ContentId SLOTH_ASPECT = ContentId.parse("forbiddenmagic:desidia");

    private @Nullable String mobId;
    private int special;
    private int wrath;
    private int sloth;
    private int fuel;
    private int spawnDelay = 20;
    private boolean slothful;
    private int mode;

    protected WrathCageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public @Nullable String getMobId() {
        return mobId;
    }

    public void cycleMode() {
        mode = mode >= 2 ? 0 : mode + 1;
        sync();
    }

    public void setMobId(@Nullable String mobId) {
        if (!Objects.equals(this.mobId, mobId)) {
            special = 0;
        }
        this.mobId = mobId;
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
        if (level.isClientSide() || !FmConfig.wrathCage()) {
            return;
        }
        int cost = FmConfig.wrathCost();
        if (cost > 0 && cage.mobId != null && cage.hasRoom()) {
            cage.drawEssentia(cost);
        }
        boolean powered = level.hasNeighborSignal(pos);
        if (!shouldSpawn(powered, cage.mobId != null, cage.fuel, cost, cage.special + cage.wrath + cage.sloth)) {
            return;
        }
        if (cost > 0 && cage.fuel <= 0) {
            if (cage.special >= cost) {
                cage.special -= cost;
                cage.fuel = FmConfig.wrathEff();
                cage.slothful = false;
                cage.sync();
            } else if (cage.wrath >= cost) {
                cage.wrath -= cost;
                cage.fuel = FmConfig.wrathEff();
                cage.slothful = false;
                cage.sync();
            } else if (cage.sloth >= cost) {
                cage.sloth -= cost;
                cage.fuel = FmConfig.wrathEff();
                cage.slothful = true;
                cage.sync();
            }
        }
        if (cage.fuel <= 0 && cost > 0) {
            return;
        }
        if (cage.spawnDelay > 0) {
            cage.spawnDelay--;
            return;
        }
        if (level instanceof ServerLevel serverLevel) {
            cage.spawnRound(serverLevel, pos);
        }
    }

    private boolean hasRoom() {
        return special < MAX_STORED || wrath < MAX_STORED || sloth < MAX_STORED;
    }

    private void drawEssentia(int cost) {
        ContentId mobAspect = mobEssentiaType();
        for (EssentiaInput input : adjacentEssentiaInputs()) {
            if (special < MAX_STORED && input.take(mobAspect, 1) == 1) {
                special++;
                sync();
                return;
            }
            if (wrath < MAX_STORED && special < cost && input.take(WRATH_ASPECT, 1) == 1) {
                wrath++;
                sync();
                return;
            }
            if (sloth < MAX_STORED && special < cost && wrath < cost
                && input.take(SLOTH_ASPECT, 1) == 1) {
                sloth++;
                sync();
                return;
            }
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
        return switch (mode) {
            case 0 -> Map.of(mobEssentiaType(), special);
            case 1 -> Map.of(WRATH_ASPECT, wrath);
            default -> Map.of(SLOTH_ASPECT, sloth);
        };
    }

    public boolean acceptsEssentia(ContentId aspect) {
        return FmConfig.wrathCost() > 0
            && (WRATH_ASPECT.equals(aspect) || SLOTH_ASPECT.equals(aspect) || mobEssentiaType().equals(aspect));
    }

    public int addEssentiaToContainer(ContentId aspect, int amount) {
        if (FmConfig.wrathCost() <= 0) {
            return amount;
        }
        if (WRATH_ASPECT.equals(aspect)) {
            wrath += amount;
            return 0;
        }
        if (SLOTH_ASPECT.equals(aspect)) {
            sloth += amount;
            return 0;
        }
        if (mobEssentiaType().equals(aspect)) {
            special += amount;
            return 0;
        }
        return amount;
    }

    public boolean containsEssentia(ContentId aspect, int amount) {
        return essentiaAmount(aspect) >= amount;
    }

    public int essentiaAmount(ContentId aspect) {
        if (WRATH_ASPECT.equals(aspect)) {
            return wrath;
        }
        if (SLOTH_ASPECT.equals(aspect)) {
            return sloth;
        }
        if (mobEssentiaType().equals(aspect)) {
            return special;
        }
        return 0;
    }

    public int addTransportEssentia(ContentId aspect, int amount) {
        int accepted = Math.min(amount, MAX_STORED);
        if (mobEssentiaType().equals(aspect)) {
            special += accepted;
            return accepted;
        }
        if (WRATH_ASPECT.equals(aspect)) {
            wrath += accepted;
            return accepted;
        }
        if (SLOTH_ASPECT.equals(aspect)) {
            sloth += accepted;
            return accepted;
        }
        return amount;
    }

    public boolean canConnectEssentia() {
        return FmConfig.wrathCost() > 0;
    }

    public ContentId suctionEssentiaType() {
        if (FmConfig.wrathCost() <= 0) {
            return null;
        }
        return switch (mode) {
            case 0 -> mobEssentiaType();
            case 1 -> WRATH_ASPECT;
            default -> SLOTH_ASPECT;
        };
    }

    public int suctionEssentiaAmount() {
        return FmConfig.wrathCost() > 0 ? SUCTION : 0;
    }

    public int transportEssentiaAmount() {
        return switch (mode) {
            case 0 -> special;
            case 1 -> wrath;
            default -> sloth;
        };
    }

    private void spawnRound(ServerLevel level, BlockPos pos) {
        if (level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, PLAYER_RANGE, false) == null) {
            return;
        }
        net.minecraft.resources.Identifier id = mobId == null ? null : net.minecraft.resources.Identifier.tryParse(mobId);
        if (id == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
            spawnDelay = nextDelay(level.getRandom(), slothful);
            return;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(id);
        AABB nearby = new AABB(pos.getX() - 4, pos.getY() - 1, pos.getZ() - 4, pos.getX() + 5, pos.getY() + 2, pos.getZ() + 5);
        int cost = FmConfig.wrathCost();
        for (int attempt = 0; attempt < SPAWN_COUNT && (fuel > 0 || cost <= 0); attempt++) {
            Entity entity = type.create(level, EntitySpawnReason.SPAWNER);
            if (entity == null) {
                break;
            }
            if (level.getEntitiesOfClass(Entity.class, nearby, other -> other.getType() == type).size() >= MAX_NEARBY) {
                break;
            }
            double x = pos.getX() + 0.5 + (level.getRandom().nextDouble() - level.getRandom().nextDouble()) * SPAWN_RANGE;
            double y = pos.getY() + level.getRandom().nextInt(3) - 1;
            double z = pos.getZ() + 0.5 + (level.getRandom().nextDouble() - level.getRandom().nextDouble()) * SPAWN_RANGE;
            entity.snapTo(x, y, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
            if (level.noCollision(entity) && !level.containsAnyLiquid(entity.getBoundingBox())) {
                level.addFreshEntity(entity);
                if (fuel > 0) {
                    fuel--;
                }
            }
        }
        spawnDelay = nextDelay(level.getRandom(), slothful);
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
        output.putInt("Special", special);
        output.putInt("Wrath", wrath);
        output.putInt("Sloth", sloth);
        output.putInt("Fuel", fuel);
        output.putInt("Delay", spawnDelay);
        output.putBoolean("Slothful", slothful);
        output.putInt("Mode", mode);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        mobId = input.getString("MobId").orElse(null);
        special = input.getIntOr("Special", 0);
        wrath = input.getIntOr("Wrath", 0);
        sloth = input.getIntOr("Sloth", 0);
        fuel = input.getIntOr("Fuel", 0);
        spawnDelay = input.getIntOr("Delay", 20);
        slothful = input.getBooleanOr("Slothful", false);
        mode = input.getIntOr("Mode", 0);
    }
}
