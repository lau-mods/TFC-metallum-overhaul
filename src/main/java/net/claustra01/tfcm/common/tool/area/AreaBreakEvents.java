package net.claustra01.tfcm.common.tool.area;

import net.claustra01.tfcm.TFCM;
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

    private AreaBreakEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled()) return;
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
        PENDING.set(new PendingBreak(player, event.getPos().immutable(), blockHit.getDirection()));
    }

    static Direction takeFace(ServerPlayer player, BlockPos pos) {
        PendingBreak pending = PENDING.get();
        PENDING.remove();
        return pending != null && pending.player() == player && pending.pos().equals(pos) ? pending.face() : null;
    }

    private record PendingBreak(ServerPlayer player, BlockPos pos, Direction face) {
    }
}
