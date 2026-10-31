package net.claustra01.tfcm.common.material;

import net.claustra01.tfcm.registry.TFCMItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Right-clicking grass yields Plant Fiber: with an empty main hand, or with a Knife in either hand. */
public final class PlantFiberHarvest {
    private static final TagKey<Block> GRASS = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("c", "plant/grass"));
    private static final TagKey<Item> KNIVES = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("c", "tools/knife"));
    private static final int COOLDOWN_TICKS = 20;
    private static final float REMOVE_CHANCE = .4f;

    private PlantFiberHarvest() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        ItemStack tool = event.getItemStack();
        boolean usable = tool.is(KNIVES) || tool.isEmpty() && event.getHand() == InteractionHand.MAIN_HAND;
        if (!usable || !level.getBlockState(event.getPos()).is(GRASS)
                || event.getEntity().getCooldowns().isOnCooldown(tool.getItem())) return;
        if (level instanceof ServerLevel server) harvest(server, event.getPos(), event.getEntity(), event.getHand());
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
    }

    public static void harvest(ServerLevel level, BlockPos pos, Player player, InteractionHand hand) {
        ItemStack tool = player.getItemInHand(hand);
        boolean knife = tool.is(KNIVES);
        RandomSource random = level.getRandom();
        int fibers = fibers(random, knife);
        if (fibers > 0) Block.popResource(level, pos,
                new ItemStack(TFCMItems.MATERIAL_PARTS.get(MaterialPart.PLANT_FIBER).get(), fibers));
        player.getCooldowns().addCooldown(tool.getItem(), COOLDOWN_TICKS);
        level.sendParticles(ParticleTypes.ASH, pos.getX() + .5, pos.getY() + .7, pos.getZ() + .5, 30, .1, .1, .1, 0);
        level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS);
        if (knife) tool.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        if (random.nextFloat() < REMOVE_CHANCE) level.destroyBlock(pos, false);
    }

    public static int fibers(RandomSource random, boolean knife) {
        return knife ? (random.nextFloat() < .9f ? 3 : 0) + (random.nextFloat() < .5f ? 2 : 0)
                : (random.nextFloat() < .8f ? 1 : 0) + (random.nextFloat() < .3f ? 1 : 0);
    }
}
