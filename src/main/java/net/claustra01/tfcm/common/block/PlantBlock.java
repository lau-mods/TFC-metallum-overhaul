package net.claustra01.tfcm.common.block;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class PlantBlock extends Block {
    private static final float DRY_CHANCE = .1f;

    public PlantBlock(Properties properties) {
        super(properties.randomTicks());
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < DRY_CHANCE) level.setBlock(pos, TFCBlocks.THATCH.get().defaultBlockState(), Block.UPDATE_ALL);
    }
}
