package net.claustra01.tfcm.registry;

import java.util.EnumMap;
import java.util.Map;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.block.PlantBlock;
import net.claustra01.tfcm.common.metal.Metal;
import net.claustra01.tfcm.common.tool.ToolMetal;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TFCMBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TFCM.MOD_ID);
    public static final Map<Metal, DeferredBlock<Block>> METAL_BLOCKS = new EnumMap<>(Metal.class);
    public static final Map<Metal, DeferredBlock<SlabBlock>> METAL_SLABS = new EnumMap<>(Metal.class);
    public static final Map<Metal, DeferredBlock<StairBlock>> METAL_STAIRS = new EnumMap<>(Metal.class);
    public static final Map<Metal, DeferredBlock<LiquidBlock>> METAL_FLUID_BLOCKS = new EnumMap<>(Metal.class);
    public static final Map<ToolMetal, DeferredBlock<AnvilBlock>> ANVILS = new EnumMap<>(ToolMetal.class);
    public static final DeferredBlock<PlantBlock> PLANT_BLOCK = BLOCKS.register("plant_block",
            () -> new PlantBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(.5f)));
    public static final DeferredBlock<LiquidBlock> MOLTEN_ANDESITE = BLOCKS.register("molten_andesite",
            () -> new LiquidBlock(TFCMFluids.MOLTEN_ANDESITE_SOURCE.get(),
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).noCollission().strength(100f).noLootTable()));

    static {
        for (Metal metal : Metal.values()) {
            DeferredBlock<Block> block = BLOCKS.register("metal/block/" + metal.id(), () -> new Block(metalProperties()));
            METAL_BLOCKS.put(metal, block);
            METAL_SLABS.put(metal, BLOCKS.register("metal/block/" + metal.id() + "_slab",
                    () -> new SlabBlock(metalProperties())));
            METAL_STAIRS.put(metal, BLOCKS.register("metal/block/" + metal.id() + "_stairs",
                    () -> new StairBlock(block.get().defaultBlockState(), metalProperties())));
            METAL_FLUID_BLOCKS.put(metal, BLOCKS.register("fluid/metal/" + metal.id(),
                    () -> new LiquidBlock(TFCMFluids.METALS.get(metal).source().get(),
                            BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).noCollission().strength(100f).noLootTable())));
        }
        for (ToolMetal metal : ToolMetal.values()) {
            ANVILS.put(metal, BLOCKS.register("metal/anvil/" + metal.getSerializedName(),
                    () -> new AnvilBlock(ExtendedProperties.of(MapColor.METAL).strength(5, 10)
                            .requiresCorrectToolForDrops().blockEntity(TFCBlockEntities.ANVIL),
                            metal.toolTier().level())));
        }
    }

    private TFCMBlocks() {
    }

    private static BlockBehaviour.Properties metalProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(5f, 6f);
    }
}
