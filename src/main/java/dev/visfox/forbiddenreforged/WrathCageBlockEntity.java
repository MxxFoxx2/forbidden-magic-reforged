package dev.visfox.forbiddenreforged;

import dev.visfox.forbiddenreforged.core.block.WrathCageBehavior;
import dev.visfox.forbiddenreforged.core.block.WrathCageStorage;
import dev.visfox.forbiddenreforged.core.block.WrathCageState;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import dev.visfox.forbiddenreforged.integration.api.HostAdapterRegistry;
import dev.visfox.forbiddenreforged.integration.tc4.Tc4AspectAdapter;
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
    private static final int MAX_NEARBY = 6;
    private static final int SPAWN_RANGE = 4;
    private static final int SPAWN_COUNT = 3;
    private static final int PLAYER_RANGE = 16;
    private static final int SUCTION = 128;

    private final WrathCageState cageState = new WrathCageState();

    public WrathCageBlockEntity(BlockPos pos, BlockState state) {
        this(FmBlockEntities.WRATH_CAGE.get(), pos, state);
    }

    protected WrathCageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public @Nullable String getMobId() {
        return cageState.mobId();
    }

    public void cycleMode() {
        cageState.storage().cycleMode();
        sync();
    }

    public void setMobId(@Nullable String mobId) {
        cageState.setMobId(mobId);
        sync();
    }

    public static boolean shouldSpawn(boolean powered, boolean mobSet, int fuel, int cost, int availableEssentia) {
        return WrathCageBehavior.shouldSpawn(powered, mobSet, fuel, cost, availableEssentia);
    }

    public static int nextDelay(RandomSource random, boolean slothful) {
        return WrathCageBehavior.nextDelay(random.nextInt(100), slothful);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WrathCageBlockEntity cage) {
        if (level.isClientSide() || !FmConfig.wrathCage()) {
            return;
        }
        int cost = FmConfig.wrathCost();
        if (cost > 0 && cage.cageState.mobId() != null && cage.hasRoom()) {
            cage.drawEssentia(level, pos);
        }
        boolean powered = level.hasNeighborSignal(pos);
        if (!cage.cageState.canSpawn(powered, cost)) {
            return;
        }
        if (cost > 0 && cage.cageState.fuel() <= 0
            && cage.cageState.loadFuel(cost, FmConfig.wrathEff())) {
            cage.sync();
        }
        if (cage.cageState.fuel() <= 0 && cost > 0) {
            return;
        }
        if (cage.cageState.advanceSpawnDelay()) {
            return;
        }
        if (level instanceof ServerLevel serverLevel) {
            cage.spawnRound(serverLevel, pos);
        }
    }

    private boolean hasRoom() {
        return cageState.storage().hasRoom();
    }

    private ContentId mobAspectId() {
        return Tc4AspectAdapter.toContentId(mobAspect());
    }

    private @Nullable ContentId wrathAspectId() {
        return FmAspects.WRATH == null ? null : Tc4AspectAdapter.toContentId(FmAspects.WRATH);
    }

    private @Nullable ContentId slothAspectId() {
        return FmAspects.SLOTH == null ? null : Tc4AspectAdapter.toContentId(FmAspects.SLOTH);
    }

    private @Nullable WrathCageStorage.Channel channelFor(Aspect aspect) {
        return cageState.storage().channelFor(
            Tc4AspectAdapter.toContentId(aspect),
            mobAspectId(),
            wrathAspectId(),
            slothAspectId()
        );
    }

    private @Nullable ContentId selectedAspectId() {
        return switch (cageState.storage().selectedChannel()) {
            case MOB -> mobAspectId();
            case WRATH -> wrathAspectId();
            case SLOTH -> slothAspectId();
        };
    }

    private void drawEssentia(Level level, BlockPos pos) {
        int cost = FmConfig.wrathCost();
        var extractionOrder = cageState.storage().extractionOrder(cost);
        if (extractionOrder.isEmpty()) {
            return;
        }

        ContentId mobAspectId = mobAspectId();
        ContentId wrathAspectId = wrathAspectId();
        ContentId slothAspectId = slothAspectId();
        var transport = HostAdapterRegistry.requireEssentiaTransport();

        for (Direction direction : Direction.values()) {
            var source = level.getBlockEntity(pos.relative(direction));
            if (source == null) {
                continue;
            }
            Direction outputSide = direction.getOpposite();
            for (WrathCageStorage.Channel channel : extractionOrder) {
                ContentId aspect = switch (channel) {
                    case MOB -> mobAspectId;
                    case WRATH -> wrathAspectId;
                    case SLOTH -> slothAspectId;
                };
                if (aspect != null && transport.takeOneFromOutput(source, outputSide, aspect)) {
                    cageState.storage().add(channel, 1);
                    sync();
                    return;
                }
            }
        }
    }

    private void spawnRound(ServerLevel level, BlockPos pos) {
        if (level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, PLAYER_RANGE, false) == null) {
            return;
        }
        ResourceLocation id = cageState.mobId() == null ? null : ResourceLocation.tryParse(cageState.mobId());
        if (id == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
            cageState.setSpawnDelay(nextDelay(level.getRandom(), cageState.slothful()));
            return;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
        if (type == null) {
            cageState.setSpawnDelay(nextDelay(level.getRandom(), cageState.slothful()));
            return;
        }
        AABB nearby = new AABB(pos.getX() - 4, pos.getY() - 1, pos.getZ() - 4, pos.getX() + 5, pos.getY() + 2, pos.getZ() + 5);
        int cost = FmConfig.wrathCost();
        for (int attempt = 0; attempt < SPAWN_COUNT && (cageState.fuel() > 0 || cost <= 0); attempt++) {
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
                cageState.consumeFuel();
            }
        }
        cageState.setSpawnDelay(nextDelay(level.getRandom(), cageState.slothful()));
    }

    private Aspect mobAspect() {
        Aspect aspect = FmSpawnerMobs.aspect(cageState.mobId());
        return aspect == null ? Aspect.GREED : aspect;
    }

    public static ItemStack crystalForBreak(WrathCageBlockEntity cage) {
        ItemStack stack = new ItemStack(FmItems.MOB_CRYSTAL.get());
        if (cage.cageState.mobId() != null) {
            MobCrystalItem.setMob(stack, cage.cageState.mobId());
        }
        return stack;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (cageState.mobId() != null && level != null) {
            ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, crystalForBreak(this));
            level.addFreshEntity(drop);
        }
    }

    @Override
    public AspectList getAspects() {
        AspectList aspects = new AspectList();
        if (FmConfig.wrathCost() > 0 && cageState.mobId() != null) {
            ContentId selectedAspect = selectedAspectId();
            if (selectedAspect != null) {
                aspects.add(Tc4AspectAdapter.resolveAspect(selectedAspect), cageState.storage().selectedAmount());
            }
        }
        return aspects;
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return FmConfig.wrathCost() > 0 && channelFor(aspect) != null;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        if (FmConfig.wrathCost() <= 0) {
            return amount;
        }
        WrathCageStorage.Channel channel = channelFor(aspect);
        return channel == null ? amount : amount - cageState.storage().add(channel, amount);
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        WrathCageStorage.Channel channel = channelFor(aspect);
        return channel != null && cageState.storage().contains(channel, amount);
    }

    @Override
    public int containerContains(Aspect aspect) {
        WrathCageStorage.Channel channel = channelFor(aspect);
        return channel == null ? 0 : cageState.storage().amount(channel);
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
        ContentId selectedAspect = selectedAspectId();
        return selectedAspect == null ? null : Tc4AspectAdapter.resolveAspect(selectedAspect);
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
        WrathCageStorage.Channel channel = channelFor(aspect);
        return channel == null ? amount : cageState.storage().add(channel, amount);
    }

    @Override
    public @Nullable Aspect getEssentiaType(Direction face) {
        return getSuctionType(face);
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return cageState.storage().selectedAmount();
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
        WrathCageState.Snapshot saved = cageState.snapshot();
        if (saved.mobId() != null) {
            output.putString("MobId", saved.mobId());
        }
        output.putInt("Special", saved.storage().mob());
        output.putInt("Wrath", saved.storage().wrath());
        output.putInt("Sloth", saved.storage().sloth());
        output.putInt("Fuel", saved.fuel());
        output.putInt("Delay", saved.spawnDelay());
        output.putBoolean("Slothful", saved.slothful());
        output.putInt("Mode", saved.storage().mode());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        cageState.restore(new WrathCageState.Snapshot(
            input.getString("MobId").orElse(null),
            new WrathCageStorage.State(
                input.getIntOr("Special", 0),
                input.getIntOr("Wrath", 0),
                input.getIntOr("Sloth", 0),
                input.getIntOr("Mode", 0)
            ),
            input.getIntOr("Fuel", 0),
            input.getIntOr("Delay", 20),
            input.getBooleanOr("Slothful", false)
        ));
    }
}
