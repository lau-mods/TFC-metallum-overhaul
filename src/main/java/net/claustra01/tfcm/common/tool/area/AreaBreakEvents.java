package net.claustra01.tfcm.common.tool.area;

import java.util.ArrayList;
import java.util.List;

import net.claustra01.tfcm.TFCM;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = TFCM.MOD_ID)
public final class AreaBreakEvents {
    private static final ThreadLocal<PendingBreak> PENDING = new ThreadLocal<>();
    private static final List<CreativeBreak> CREATIVE = new ArrayList<>();

    private AreaBreakEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled() || AreaBreakHandler.isBreaking()) return;
        if (!(event.getLevel() instanceof ServerLevel) || !(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack tool = player.getMainHandItem();
        if (!(tool.getItem() instanceof AreaMiningTool areaTool) || !areaTool.canStart(event.getState())
                || !tool.isCorrectToolForDrops(event.getState())) return;
        Vec3 eye = player.getEyePosition();
        HitResult hit = player.level().clip(new ClipContext(eye,
                eye.add(player.getLookAngle().scale(player.blockInteractionRange() + 1)),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (!(hit instanceof BlockHitResult blockHit) || !blockHit.getBlockPos().equals(event.getPos())) return;
        if (player.isCreative()) {
            CREATIVE.add(new CreativeBreak(event, tool, blockHit.getDirection(), areaTool.type()));
        } else {
            PENDING.set(new PendingBreak(player, event.getPos().immutable(), blockHit.getDirection()));
        }
    }

    @SubscribeEvent
    public static void afterServerTick(ServerTickEvent.Post event) {
        List<CreativeBreak> pending = List.copyOf(CREATIVE);
        CREATIVE.clear();
        for (CreativeBreak entry : pending) {
            BlockEvent.BreakEvent broken = entry.event();
            ServerPlayer player = (ServerPlayer) broken.getPlayer();
            ServerLevel level = (ServerLevel) broken.getLevel();
            if (!broken.isCanceled() && player.isCreative() && player.serverLevel() == level
                    && player.getMainHandItem() == entry.tool()
                    && !level.getBlockState(broken.getPos()).is(broken.getState().getBlock())) {
                AreaBreakHandler.breakArea(level, player, entry.tool(), broken.getPos(), entry.face(), entry.type());
            }
        }
    }

    private record CreativeBreak(BlockEvent.BreakEvent event, ItemStack tool, Direction face, AreaToolType type) {
    }

    static Direction takeFace(ServerPlayer player, BlockPos pos) {
        PendingBreak pending = PENDING.get();
        PENDING.remove();
        return pending != null && pending.player() == player && pending.pos().equals(pos) ? pending.face() : null;
    }

    private record PendingBreak(ServerPlayer player, BlockPos pos, Direction face) {
    }
}
