package net.claustra01.tfcm.common.tool.area;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.dries007.tfc.common.items.ToolItem;
import net.dries007.tfc.common.items.ToolItem;

public final class AreaMiningTool extends ToolItem {
    private final AreaToolType type;

    public AreaMiningTool(AreaToolType type, Tier sourceTier, Ingredient repairIngredient, Rarity rarity) {
        super(new MiningTier(sourceTier, repairIngredient), type.mineable(), properties(type, sourceTier, rarity));
        this.type = type;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        boolean result = super.mineBlock(stack, level, state, pos, miner);
        if (level instanceof ServerLevel serverLevel && miner instanceof ServerPlayer player) {
            Direction face = AreaBreakEvents.takeFace(player, pos);
            if (face != null) AreaBreakHandler.breakArea(serverLevel, player, stack, pos, face, type);
        }
        return result;
    }

    AreaToolType type() {
        return type;
    }

    boolean canStart(BlockState state) {
        return state.is(type.mineable());
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return getTier().getRepairIngredient().test(repairCandidate);
    }

    private static Item.Properties properties(AreaToolType type, Tier tier, Rarity rarity) {
        float attackDamage = type == AreaToolType.EXCAVATOR ? .9f : 1;
        return new Item.Properties().durability(tier.getUses()).rarity(rarity)
                .attributes(ToolItem.productAttributes(tier, attackDamage, -3.1f));
    }

    private record MiningTier(Tier source, Ingredient repairIngredient) implements Tier {
        @Override
        public int getUses() {
            return source.getUses();
        }

        @Override
        public float getSpeed() {
            return source.getSpeed() * .4f;
        }

        @Override
        public float getAttackDamageBonus() {
            return source.getAttackDamageBonus();
        }

        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return source.getIncorrectBlocksForDrops();
        }

        @Override
        public int getEnchantmentValue() {
            return source.getEnchantmentValue();
        }

        @Override
        public Ingredient getRepairIngredient() {
            return repairIngredient;
        }
    }
}
