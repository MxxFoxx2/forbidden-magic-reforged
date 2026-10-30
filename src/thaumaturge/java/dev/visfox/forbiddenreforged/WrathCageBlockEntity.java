package dev.visfox.forbiddenreforged;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public class WrathCageBlockEntity extends BlockEntity implements IEssentiaTransport {
    private static final int MAX_STORED = 64;
    private static final int MAX_NEARBY = 6;
    private static final int SPAWN_RANGE = 4;
    private static final int SPAWN_COUNT = 3;
    private static final int PLAYER_RANGE = 16;
    private static final int SUCTION = 128;

    private @Nullable String mobId;
    private int special;
    private int wrath;
    private int sloth;
    private int fuel;
    private int spawnDelay = 20;
    private boolean slothful;
    private int mode;

    public WrathCageBlockEntity(BlockPos pos, BlockState state) {
        this(FmBlockEntities.WRATH_CAGE.get(), pos, state);
    }

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
            cage.drawEssentia(level, pos);
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

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private boolean hasRoom() {
        return special < MAX_STORED || wrath < MAX_STORED || sloth < MAX_STORED;
    }

    private void drawEssentia(Level level, BlockPos pos) {
        ResourceKey<IAspect> mobAspect = mobAspect();
        int cost = FmConfig.wrathCost();
        for (Direction direction : Direction.values()) {
            Direction side = direction.getOpposite();
            IEssentiaTransport transport = EssentiaAccess.transport(level, pos.relative(direction), side);
            if (transport == null || !transport.canOutputTo(side)) {
                continue;
            }
            Holder<IAspect> mobHolder = holder(mobAspect);
            if (mobHolder != null && special < MAX_STORED && transport.takeEssentia(mobHolder, 1, side) == 1) {
                special++;
                sync();
                return;
            }
            Holder<IAspect> wrathHolder = holder(FmAspects.IRA);
            if (wrathHolder != null && wrath < MAX_STORED && special < cost
                && transport.takeEssentia(wrathHolder, 1, side) == 1) {
                wrath++;
                sync();
                return;
            }
            Holder<IAspect> slothHolder = holder(FmAspects.DESIDIA);
            if (slothHolder != null && sloth < MAX_STORED && special < cost && wrath < cost
                && transport.takeEssentia(slothHolder, 1, side) == 1) {
                sloth++;
                sync();
                return;
            }
        }
    }

    private void spawnRound(ServerLevel level, BlockPos pos) {
        if (level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, PLAYER_RANGE, false) == null) {
            return;
        }
        Identifier id = mobId == null ? null : Identifier.tryParse(mobId);
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

    private ResourceKey<IAspect> mobAspect() {
        ResourceKey<IAspect> aspect = FmSpawnerMobs.aspect(mobId);
        return aspect == null ? com.leclowndu93150.thaumaturge.api.aspect.TTAspects.DESIDERIUM : aspect;
    }

    private @Nullable Holder<IAspect> holder(ResourceKey<IAspect> key) {
        return level == null ? null : level.registryAccess().lookup(IAspect.REGISTRY_KEY)
            .flatMap(lookup -> lookup.get(key)).orElse(null);
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

    @Override
    public int addEssentia(Holder<IAspect> type, int count, Direction side) {
        int accepted = Math.min(count, MAX_STORED);
        if (matches(type, mobAspect())) {
            special += accepted;
            sync();
            return accepted;
        }
        if (matches(type, FmAspects.IRA)) {
            wrath += accepted;
            sync();
            return accepted;
        }
        if (matches(type, FmAspects.DESIDIA)) {
            sloth += accepted;
            sync();
            return accepted;
        }
        return count;
    }

    @Override
    public int takeEssentia(Holder<IAspect> type, int count, Direction side) {
        return 0;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(@Nullable Direction side) {
        return holder(mobAspect());
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction side) {
        return mode == 0 ? special : mode == 1 ? wrath : sloth;
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> type, int count) {
    }

    @Override
    public @Nullable Holder<IAspect> getSuctionType(@Nullable Direction side) {
        return FmConfig.wrathCost() <= 0 ? null : holder(mobAspect());
    }

    @Override
    public int getSuctionAmount(@Nullable Direction side) {
        return FmConfig.wrathCost() > 0 ? SUCTION : 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean isConnectable(Direction side) {
        return FmConfig.wrathCage();
    }

    @Override
    public boolean canOutputTo(Direction side) {
        return false;
    }

    @Override
    public boolean canInputFrom(Direction side) {
        return true;
    }

    private static boolean matches(Holder<IAspect> holder, ResourceKey<IAspect> key) {
        return holder.unwrapKey().map(key::equals).orElse(false);
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
