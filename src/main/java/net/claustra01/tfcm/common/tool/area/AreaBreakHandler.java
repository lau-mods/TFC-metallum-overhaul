package net.claustra01.tfcm.common.tool.area;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class AreaBreakHandler {
    private static final ThreadLocal<Boolean> BREAKING = ThreadLocal.withInitial(() -> false);

    private AreaBreakHandler() {
    }

    public static void breakArea(ServerLevel level, ServerPlayer player, ItemStack tool, BlockPos origin,
            Direction face, AreaToolType type) {
        if (BREAKING.get()) return;
        BREAKING.set(true);
        try {
            for (BlockPos pos : positions(origin, face, type)) {
                if (tool.isEmpty()) break;
                BlockState state = level.getBlockState(pos);
                if (!state.is(type.mineable()) || !tool.isCorrectToolForDrops(state)) continue;
                if (state.hasProperty(BlockStateProperties.LAYERS)) {
                    peelLayer(level, player, pos, state);
                } else {
                    player.gameMode.destroyBlock(pos);
                }
            }
        } finally {
            BREAKING.remove();
        }
    }

    public static List<BlockPos> positions(BlockPos origin, Direction face, AreaToolType type) {
        List<BlockPos> positions = new ArrayList<>();
        int radius = type.radius();
        boolean verticalFace = face.getAxis().isHorizontal();
        for (int u = -radius; u <= radius; u++) {
            for (int v = -radius; v <= radius; v++) {
                int verticalOffset = type == AreaToolType.QUARRY_HAMMER && verticalFace ? v + 1 : v;
                BlockPos pos;
                if (verticalFace) {
                    pos = face.getAxis() == Direction.Axis.Z
                            ? origin.offset(u, verticalOffset, 0)
                            : origin.offset(0, verticalOffset, u);
                } else {
                    pos = origin.offset(u, 0, v);
                }
                if (!pos.equals(origin)) positions.add(pos.immutable());
            }
        }
        return positions;
    }

    private static void peelLayer(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state) {
        int layers = state.getValue(BlockStateProperties.LAYERS);
        level.setBlock(pos, state.setValue(BlockStateProperties.LAYERS, 1), Block.UPDATE_CLIENTS);
        boolean destroyed = player.gameMode.destroyBlock(pos);
        if (destroyed) {
            if (layers > 1) level.setBlock(pos, state.setValue(BlockStateProperties.LAYERS, layers - 1), Block.UPDATE_ALL);
        } else {
            level.setBlock(pos, state, Block.UPDATE_ALL);
        }
    }
}
