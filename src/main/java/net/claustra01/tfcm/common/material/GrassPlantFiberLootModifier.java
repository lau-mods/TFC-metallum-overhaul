package net.claustra01.tfcm.common.material;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.claustra01.tfcm.registry.TFCMItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;

public final class GrassPlantFiberLootModifier extends LootModifier {
    public static final MapCodec<GrassPlantFiberLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance).apply(instance, GrassPlantFiberLootModifier::new));
    private static final TagKey<Block> GRASS = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("c", "plant/grass"));
    private static final TagKey<Item> KNIVES = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("c", "tools/knife"));

    public GrassPlantFiberLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (state != null && state.is(GRASS)) {
            ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
            int count = tool != null && tool.is(KNIVES) ? 2 : 1;
            loot.add(new ItemStack(TFCMItems.MATERIAL_PARTS.get(MaterialPart.PLANT_FIBER).get(), count));
        }
        return loot;
    }

    @Override
    public MapCodec<GrassPlantFiberLootModifier> codec() {
        return CODEC;
    }
}
