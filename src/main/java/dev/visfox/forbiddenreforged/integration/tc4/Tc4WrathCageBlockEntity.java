package dev.visfox.forbiddenreforged.integration.tc4;

import dev.visfox.forbiddenreforged.FmBlockEntities;
import dev.visfox.forbiddenreforged.FmSpawnerMobs;
import dev.visfox.forbiddenreforged.WrathCageBlockEntity;
import dev.visfox.forbiddenreforged.core.model.ContentId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.aspect.AspectSource;
import thaumcraft.essentia.EssentiaTransport;

public final class Tc4WrathCageBlockEntity extends WrathCageBlockEntity implements AspectSource, EssentiaTransport {
    public Tc4WrathCageBlockEntity(BlockPos pos, BlockState state) {
        this(FmBlockEntities.WRATH_CAGE.get(), pos, state);
    }

    public Tc4WrathCageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected ContentId mobEssentiaType() {
        ContentId aspect = FmSpawnerMobs.aspect(getMobId());
        return aspect == null ? ContentId.parse("thaumcraft:lucrum") : aspect;
    }

    @Override
    protected List<EssentiaInput> adjacentEssentiaInputs() {
        Level level = getLevel();
        if (level == null) {
            return List.of();
        }
        BlockPos pos = getBlockPos();
        List<EssentiaInput> inputs = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(direction));
            if (!(neighbor instanceof EssentiaTransport transport)) {
                continue;
            }
            Direction side = direction.getOpposite();
            if (transport.canOutputTo(side)) {
                inputs.add((aspect, amount) -> transport.takeEssentia(
                    Tc4AspectAdapter.resolveAspect(aspect),
                    amount,
                    side
                ));
            }
        }
        return List.copyOf(inputs);
    }

    @Override
    public AspectList getAspects() {
        AspectList aspects = new AspectList();
        for (Map.Entry<ContentId, Integer> entry : visibleEssentia().entrySet()) {
            aspects.add(Tc4AspectAdapter.resolveAspect(entry.getKey()), entry.getValue());
        }
        return aspects;
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        return acceptsEssentia(Tc4AspectAdapter.toContentId(aspect));
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        return addEssentiaToContainer(Tc4AspectAdapter.toContentId(aspect), amount);
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return containsEssentia(Tc4AspectAdapter.toContentId(aspect), amount);
    }

    @Override
    public int containerContains(Aspect aspect) {
        return essentiaAmount(Tc4AspectAdapter.toContentId(aspect));
    }

    @Override
    public boolean isConnectable(Direction face) {
        return canConnectEssentia();
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
        ContentId type = suctionEssentiaType();
        return type == null ? null : Tc4AspectAdapter.resolveAspect(type);
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return suctionEssentiaAmount();
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return addTransportEssentia(Tc4AspectAdapter.toContentId(aspect), amount);
    }

    @Override
    public @Nullable Aspect getEssentiaType(Direction face) {
        return getSuctionType(face);
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return transportEssentiaAmount();
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean renderExtendedTube() {
        return false;
    }
}
