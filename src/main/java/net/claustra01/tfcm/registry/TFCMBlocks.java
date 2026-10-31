package net.claustra01.tfcm.registry;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.block.PlantBlock;
import net.claustra01.tfcm.common.block.geode.GeodeMineral;
import net.claustra01.tfcm.common.block.geode.MineralBuddingBlock;
import net.claustra01.tfcm.common.metal.Metal;
import net.claustra01.tfcm.common.ore.Ore;
import net.claustra01.tfcm.common.ore.OreGrade;
import net.claustra01.tfcm.common.ore.TfcOre;
import net.claustra01.tfcm.common.tool.ToolMetal;
import net.claustra01.tfcm.config.TFCMConfig;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.GroundcoverBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.AnvilBlock;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.Blocks;
import net.dries007.tfc.common.blocks.MoltenFluidBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
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
    public static final Map<Ore, Map<String, DeferredBlock<Block>>> ORES = new EnumMap<>(Ore.class);
    public static final Map<Ore, DeferredBlock<GroundcoverBlock>> SMALL_ORES = new EnumMap<>(Ore.class);
    public static final Map<String, DeferredBlock<Block>> TFC_ORES = new LinkedHashMap<>();
    public static final Map<String, DeferredBlock<Block>> CHROMITE_ORES = new LinkedHashMap<>();
    public static final Map<Ore, DeferredBlock<GroundcoverBlock>> ORE_GROUNDCOVER = new EnumMap<>(Ore.class);
    public static final Map<String, DeferredBlock<GroundcoverBlock>> TFC_ORE_GROUNDCOVER = new LinkedHashMap<>();
    public static final Map<GeodeMineral, DeferredBlock<Block>> MINERAL_BLOCKS = new EnumMap<>(GeodeMineral.class);
    public static final Map<GeodeMineral, DeferredBlock<MineralBuddingBlock>> BUDDING_BLOCKS =
            new EnumMap<>(GeodeMineral.class);
    public static final Map<GeodeMineral, DeferredBlock<AmethystClusterBlock>> SMALL_BUDS =
            new EnumMap<>(GeodeMineral.class);
    public static final Map<GeodeMineral, DeferredBlock<AmethystClusterBlock>> MEDIUM_BUDS =
            new EnumMap<>(GeodeMineral.class);
    public static final Map<GeodeMineral, DeferredBlock<AmethystClusterBlock>> LARGE_BUDS =
            new EnumMap<>(GeodeMineral.class);
    public static final Map<GeodeMineral, DeferredBlock<AmethystClusterBlock>> CLUSTERS =
            new EnumMap<>(GeodeMineral.class);
    public static final DeferredBlock<PlantBlock> PLANT_BLOCK = BLOCKS.register("plant_block",
            () -> new PlantBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).strength(.5f)));
    public static final DeferredBlock<LiquidBlock> MOLTEN_ANDESITE = BLOCKS.register("molten_andesite",
            () -> new MoltenFluidBlock(TFCMFluids.MOLTEN_ANDESITE.source(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).noLootTable()));

    static {
        for (Metal metal : Metal.values()) {
            if (!TFCMConfig.isMetalEnabled(metal)) continue;
            DeferredBlock<Block> block = BLOCKS.register("metal/block/" + metal.id(), () -> new Block(metalProperties()));
            METAL_BLOCKS.put(metal, block);
            METAL_SLABS.put(metal, BLOCKS.register("metal/block/" + metal.id() + "_slab",
                    () -> new SlabBlock(metalProperties())));
            METAL_STAIRS.put(metal, BLOCKS.register("metal/block/" + metal.id() + "_stairs",
                    () -> new StairBlock(block.get().defaultBlockState(), metalProperties())));
            METAL_FLUID_BLOCKS.put(metal, BLOCKS.register("fluid/metal/" + metal.id(),
                    () -> new MoltenFluidBlock(TFCMFluids.METALS.get(metal).source(),
                            BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA).noLootTable())));
        }
        for (ToolMetal metal : ToolMetal.values()) {
            if (!TFCMConfig.isMetalEnabled(metal.metal())) continue;
            ANVILS.put(metal, BLOCKS.register("metal/anvil/" + metal.getSerializedName(),
                    () -> new AnvilBlock(ExtendedProperties.of().mapColor(metal.mapColor()).noOcclusion()
                            .sound(SoundType.ANVIL).strength(10, 10)
                            .requiresCorrectToolForDrops().blockEntity(TFCBlockEntities.ANVIL),
                            metal.toolTier().level())));
        }
        registerOres();
        registerGeodeBlocks();
    }

    private TFCMBlocks() {
    }

    private static BlockBehaviour.Properties metalProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(5f, 6f);
    }

    private static void registerOres() {
        for (Ore ore : Ore.values()) {
            if (!TFCMConfig.isOreEnabled(ore.id())) continue;
            Map<String, DeferredBlock<Block>> blocks = new LinkedHashMap<>();
            ORES.put(ore, blocks);
            if (ore.graded()) {
                SMALL_ORES.put(ore, registerGroundcover(ore.id()));
                for (OreGrade grade : OreGrade.values()) registerCustomOre(blocks, ore, grade);
            } else {
                registerCustomOre(blocks, ore, null);
                ORE_GROUNDCOVER.put(ore, registerGroundcover(ore.id()));
            }
        }
        for (String ore : TfcOre.GRADED) {
            if (!TFCMConfig.isOreEnabled(ore)) continue;
            for (OreGrade grade : OreGrade.values()) registerDimensionOre(TFC_ORES, grade.id() + "_" + ore);
        }
        for (String ore : TfcOre.UNGRADED) {
            if (TFCMConfig.isOreEnabled(ore)) registerDimensionOre(TFC_ORES, ore);
        }
        for (String ore : TfcOre.samples()) {
            if (TFCMConfig.isOreEnabled(ore)) TFC_ORE_GROUNDCOVER.put(ore, registerGroundcover(ore));
        }
    }

    private static DeferredBlock<GroundcoverBlock> registerGroundcover(String ore) {
        return BLOCKS.register("ore/small_" + ore,
                () -> GroundcoverBlock.looseOre(BlockBehaviour.Properties.of().strength(.4f).noOcclusion()));
    }

    private static void registerGeodeBlocks() {
        for (GeodeMineral mineral : GeodeMineral.values()) {
            String id = mineral.id();
            MINERAL_BLOCKS.put(mineral, BLOCKS.register("mineral/" + id + "_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK))));
            SMALL_BUDS.put(mineral, BLOCKS.register("mineral/small_" + id + "_bud",
                    () -> new AmethystClusterBlock(3, 4,
                            BlockBehaviour.Properties.ofFullCopy(Blocks.SMALL_AMETHYST_BUD))));
            MEDIUM_BUDS.put(mineral, BLOCKS.register("mineral/medium_" + id + "_bud",
                    () -> new AmethystClusterBlock(4, 3,
                            BlockBehaviour.Properties.ofFullCopy(Blocks.MEDIUM_AMETHYST_BUD))));
            LARGE_BUDS.put(mineral, BLOCKS.register("mineral/large_" + id + "_bud",
                    () -> new AmethystClusterBlock(5, 3,
                            BlockBehaviour.Properties.ofFullCopy(Blocks.LARGE_AMETHYST_BUD))));
            CLUSTERS.put(mineral, BLOCKS.register("mineral/" + id + "_cluster",
                    () -> new AmethystClusterBlock(7, 3,
                            BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_CLUSTER))));
            BUDDING_BLOCKS.put(mineral, BLOCKS.register("mineral/budding_" + id,
                    () -> new MineralBuddingBlock(SMALL_BUDS.get(mineral), MEDIUM_BUDS.get(mineral),
                            LARGE_BUDS.get(mineral), CLUSTERS.get(mineral),
                            BlockBehaviour.Properties.ofFullCopy(Blocks.BUDDING_AMETHYST).randomTicks())));
        }
    }

    private static void registerCustomOre(Map<String, DeferredBlock<Block>> blocks, Ore ore, OreGrade grade) {
        String name = ore.itemName(grade);
        for (Rock rock : Rock.values()) {
            registerOre(blocks, name, rock.getSerializedName(), rock.category().hardness(6.5f), 10);
        }
        registerOre(blocks, name, "netherrack", 3, 10);
        registerOre(blocks, name, "end_stone", 3, 9);
    }

    public static void registerDimensionOre(Map<String, DeferredBlock<Block>> target, String name) {
        registerOre(target, name, "netherrack", 3, 10);
        registerOre(target, name, "end_stone", 3, 9);
    }

    private static void registerOre(Map<String, DeferredBlock<Block>> target, String name, String host,
            float hardness, float resistance) {
        String key = name + "/" + host;
        target.put(key, BLOCKS.register("ore/" + key, () -> new Block(oreProperties(hardness, resistance))));
    }

    private static BlockBehaviour.Properties oreProperties(float hardness, float resistance) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops()
                .strength(hardness, resistance);
    }
}
