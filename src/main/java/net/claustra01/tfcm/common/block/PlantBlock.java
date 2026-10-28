package net.claustra01.tfcm.common.block;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class PlantBlock extends Block {
    public static final IntegerProperty DRYING = IntegerProperty.create("drying", 0, 3);

    public PlantBlock(Properties properties) {
        super(properties.randomTicks());
        registerDefaultState(stateDefinition.any().setValue(DRYING, 0));
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int drying = state.getValue(DRYING);
        if (drying < 3) {
            level.setBlock(pos, state.setValue(DRYING, drying + 1), Block.UPDATE_ALL);
        } else {
            level.setBlock(pos, TFCBlocks.THATCH.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DRYING);
    }
}
