package dev.visfox.forbiddenreforged;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.compat.ValueInput;
import thaumcraft.compat.ValueOutput;
import net.minecraft.world.phys.AABB;
import javax.annotation.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectSource;
import thaumcraft.blockentity.TcBlockEntity;
import thaumcraft.essentia.EssentiaTransport;

public class WrathCageBlockEntity extends TcBlockEntity implements AspectSource, EssentiaTransport {
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

    private boolean hasRoom() {
        return special < MAX_STORED || wrath < MAX_STORED || sloth < MAX_STORED;
    }

    private void drawEssentia(Level level, BlockPos pos) {
        Aspect mobAspect = mobAspect();
        int cost = FmConfig.wrathCost();
        for (Direction direction : Direction.values()) {
            if (!(level.getBlockEntity(pos.relative(direction)) instanceof EssentiaTransport transport)) {
                continue;
            }
            Direction side = direction.getOpposite();
            if (!transport.canOutputTo(side)) {
                continue;
            }
            if (special < MAX_STORED && transport.takeEssentia(mobAspect, 1, side) == 1) {
                special++;
                sync();
                return;
            }
            if (FmAspects.WRATH != null && wrath < MAX_STORED && special < cost
                && transport.takeEssentia(FmAspects.WRATH, 1, side) == 1) {
                wrath++;
                sync();
                return;
            }
            if (FmAspects.SLOTH != null && sloth < MAX_STORED && special < cost && wrath < cost
                && transport.takeEssentia(FmAspects.SLOTH, 1, side) == 1) {
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
        ResourceLocation id = mobId == null ? null : ResourceLocation.tryParse(mobId);
        if (id == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
            spawnDelay = nextDelay(level.getRandom(), slothful);
            return;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
        if (type == null) {
            spawnDelay = nextDelay(level.getRandom(), slothful);
            return;
        }
        AABB nearby = new AABB(pos.getX() - 4, pos.getY() - 1, pos.getZ() - 4, pos.getX() + 5, pos.getY() + 2, pos.getZ() + 5);
        int cost = FmConfig.wrathCost();
        for (int attempt = 0; attempt < SPAWN_COUNT && (fuel > 0 || cost <= 0); attempt++) {
            // Minecraft 1.21.1 exposes the spawn-aware overload with level, consumer, position and flags.
            Entity entity = type.create(level, null, pos, MobSpawnType.SPAWNER, false, false);
            if (entity == null) {
                break;
            }
            if (level.getEntitiesOfClass(Entity.class, nearby, other -> other.getType() == type).size() >= MAX_NEARBY) {
                break;
            }
            double x = pos.getX() + 0.5 + (level.getRandom().nextDouble() - level.getRandom().nextDouble()) * SPAWN_RANGE;
            double y = pos.getY() + level.getRandom().nextInt(3) - 1;
            double z = pos.getZ() + 0.5 + (level.getRandom().nextDouble() - level.getRandom().nextDouble()) * SPAWN_RANGE;
            entity.moveTo(x, y, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
            if (level.noCollision(entity) && !level.containsAnyLiquid(entity.getBoundingBox())) {
                level.addFreshEntity(entity);
                if (fuel > 0) {
                    fuel--;
                }
            }
        }
        spawnDelay = nextDelay(level.getRandom(), slothful);
    }

    private Aspect mobAspect() {
        Aspect aspect = FmSpawnerMobs.aspect(mobId);
        return aspect == null ? Aspect.GREED : aspect;
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
    public AspectList getAspects() {
        AspectList aspects = new AspectList();
        if (FmConfig.wrathCost() > 0 && mobId != null) {
            if (mode == 0) {
                aspects.add(mobAspect(), special);
            } else if (mode == 1) {
                if (FmAspects.WRATH != null) {
                    aspects.add(FmAspects.WRATH, wrath);
                }
            } else if (FmAspects.SLOTH != null) {
                aspects.add(FmAspects.SLOTH, sloth);
            }
        }
        return aspects;
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return FmConfig.wrathCost() > 0 && (aspect == FmAspects.WRATH || aspect == FmAspects.SLOTH || aspect == mobAspect());
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        if (FmConfig.wrathCost() <= 0) {
            return amount;
        }
        if (aspect == FmAspects.WRATH) {
            wrath += amount;
            return 0;
        }
        if (aspect == FmAspects.SLOTH) {
            sloth += amount;
            return 0;
        }
        if (aspect == mobAspect()) {
            special += amount;
            return 0;
        }
        return amount;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        if (aspect == FmAspects.WRATH) {
            return wrath >= amount;
        }
        if (aspect == FmAspects.SLOTH) {
            return sloth >= amount;
        }
        if (aspect == mobAspect()) {
            return special >= amount;
        }
        return false;
    }

    @Override
    public int containerContains(Aspect aspect) {
        if (aspect == FmAspects.WRATH) {
            return wrath;
        }
        if (aspect == FmAspects.SLOTH) {
            return sloth;
        }
        if (aspect == mobAspect()) {
            return special;
        }
        return 0;
    }

    @Override
    public boolean isConnectable(Direction face) {
        return FmConfig.wrathCost() > 0;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return true;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Aspect aspect, int amount) {
    }

    @Override
    public @Nullable Aspect getSuctionType(Direction face) {
        if (FmConfig.wrathCost() <= 0) {
            return null;
        }
        if (mode == 0) {
            return mobAspect();
        }
        return mode == 1 ? FmAspects.WRATH : FmAspects.SLOTH;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return FmConfig.wrathCost() > 0 ? SUCTION : 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        int accepted = Math.min(amount, MAX_STORED);
        if (aspect == mobAspect()) {
            special += accepted;
            return accepted;
        }
        if (aspect == FmAspects.WRATH) {
            wrath += accepted;
            return accepted;
        }
        if (aspect == FmAspects.SLOTH) {
            sloth += accepted;
            return accepted;
        }
        return amount;
    }

    @Override
    public @Nullable Aspect getEssentiaType(Direction face) {
        return getSuctionType(face);
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        if (mode == 0) {
            return special;
        }
        return mode == 1 ? wrath : sloth;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
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
