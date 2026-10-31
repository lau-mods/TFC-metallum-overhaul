package net.claustra01.tfcm.common.block.geode;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystBlock;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class MineralBuddingBlock extends AmethystBlock {
    private final Supplier<? extends Block> smallBud;
    private final Supplier<? extends Block> mediumBud;
    private final Supplier<? extends Block> largeBud;
    private final Supplier<? extends Block> cluster;

    public MineralBuddingBlock(Supplier<? extends Block> smallBud, Supplier<? extends Block> mediumBud,
            Supplier<? extends Block> largeBud, Supplier<? extends Block> cluster,
            BlockBehaviour.Properties properties) {
        super(properties);
        this.smallBud = smallBud;
        this.mediumBud = mediumBud;
        this.largeBud = largeBud;
        this.cluster = cluster;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos position, RandomSource random) {
        if (random.nextInt(5) == 0) {
            Direction direction = Direction.values()[random.nextInt(Direction.values().length)];
            grow(level, position.relative(direction), direction);
        }
    }

    public boolean grow(ServerLevel level, BlockPos position, Direction direction) {
        BlockState state = level.getBlockState(position);
        Block next = null;
        if (canGrowAt(state)) {
            next = smallBud.get();
        } else if (state.is(smallBud.get()) && state.getValue(AmethystClusterBlock.FACING) == direction) {
            next = mediumBud.get();
        } else if (state.is(mediumBud.get()) && state.getValue(AmethystClusterBlock.FACING) == direction) {
            next = largeBud.get();
        } else if (state.is(largeBud.get()) && state.getValue(AmethystClusterBlock.FACING) == direction) {
            next = cluster.get();
        }
        if (next == null) return false;
        return level.setBlockAndUpdate(position, next.defaultBlockState()
                .setValue(AmethystClusterBlock.FACING, direction)
                .setValue(AmethystClusterBlock.WATERLOGGED, state.getFluidState().isSource()));
    }

    private static boolean canGrowAt(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) && state.getFluidState().isSource();
    }
}
