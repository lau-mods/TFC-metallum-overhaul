package net.claustra01.tfcm.registry;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.block.geode.GeodeMineral;
import net.claustra01.tfcm.common.metal.Metal;
import net.claustra01.tfcm.common.metal.MetalForm;
import net.claustra01.tfcm.common.material.MaterialPart;
import net.claustra01.tfcm.common.ore.Ore;
import net.claustra01.tfcm.common.ore.OreGrade;
import net.claustra01.tfcm.common.processing.ProcessingForm;
import net.claustra01.tfcm.common.processing.ProcessingSeries;
import net.claustra01.tfcm.common.processing.SandPile;
import net.claustra01.tfcm.common.tool.ToolMetal;
import net.claustra01.tfcm.common.tool.ToolPart;
import net.claustra01.tfcm.common.tool.area.AreaMiningTool;
import net.claustra01.tfcm.common.tool.area.AreaToolMetal;
import net.claustra01.tfcm.common.tool.area.AreaToolType;
import net.claustra01.tfcm.config.TFCMConfig;
import net.dries007.tfc.common.items.MoldItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TFCMItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TFCM.MOD_ID);
    public static final Map<Metal, DeferredItem<BucketItem>> METAL_BUCKETS = new EnumMap<>(Metal.class);
    public static final Map<MaterialPart, DeferredItem<Item>> MATERIAL_PARTS = new EnumMap<>(MaterialPart.class);
    public static final Map<ToolMetal, Map<ToolPart, DeferredItem<Item>>> METAL_ITEMS = new EnumMap<>(ToolMetal.class);
    public static final Map<AreaToolMetal, Map<AreaToolType, DeferredItem<AreaMiningTool>>> AREA_TOOLS =
            new EnumMap<>(AreaToolMetal.class);
    public static final Map<AreaToolMetal, Map<AreaToolType, DeferredItem<Item>>> AREA_TOOL_HEADS =
            new EnumMap<>(AreaToolMetal.class);
    public static final Map<AreaToolType, DeferredItem<Item>> UNFIRED_AREA_TOOL_MOLDS =
            new EnumMap<>(AreaToolType.class);
    public static final Map<AreaToolType, DeferredItem<MoldItem>> AREA_TOOL_MOLDS =
            new EnumMap<>(AreaToolType.class);
    public static final Map<Ore, Map<OreGrade, DeferredItem<Item>>> GRADED_ORES = new EnumMap<>(Ore.class);
    public static final Map<Ore, DeferredItem<Item>> ORES = new EnumMap<>(Ore.class);
    public static final Map<Ore, DeferredItem<BlockItem>> SMALL_ORES = new EnumMap<>(Ore.class);
    public static final Map<Ore, DeferredItem<Item>> ORE_POWDERS = new EnumMap<>(Ore.class);
    public static final Map<ProcessingSeries, Map<ProcessingForm, DeferredItem<Item>>> ORE_PROCESSING =
            new EnumMap<>(ProcessingSeries.class);
    public static final Map<SandPile, DeferredItem<Item>> SAND_PILES = new EnumMap<>(SandPile.class);
    public static final DeferredItem<Item> ROCK_POWDER = ITEMS.registerSimpleItem("ore_processing/rock_powder");
    public static final DeferredItem<Item> CUT_QUARTZ = ITEMS.registerSimpleItem("gem/cut_quartz");
    public static final DeferredItem<Item> CERTUS_QUARTZ = ITEMS.registerSimpleItem("gem/certus_quartz");
    public static final DeferredItem<BlockItem> PLANT_BLOCK = ITEMS.register("plant_block",
            () -> new BlockItem(TFCMBlocks.PLANT_BLOCK.get(), new Item.Properties()));
    public static final DeferredItem<BucketItem> MOLTEN_ANDESITE_BUCKET = ITEMS.register(
            "molten_andesite_bucket", () -> new BucketItem(TFCMFluids.MOLTEN_ANDESITE_SOURCE.get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    static {
        if (TFCMConfig.isMetalEnabled(Metal.TUNGSTEN_STEEL))
            ITEMS.registerSimpleItem("metal/ingot/high_carbon_tungsten_steel");
        TagKey<Fluid> moldFluids = TagKey.create(Registries.FLUID,
                ResourceLocation.fromNamespaceAndPath("tfc", "usable_in_tool_head_mold"));
        for (AreaToolType type : AreaToolType.values()) {
            UNFIRED_AREA_TOOL_MOLDS.put(type, ITEMS.registerSimpleItem("ceramic/unfired_" + type.id() + "_head_mold"));
            AREA_TOOL_MOLDS.put(type, ITEMS.register("ceramic/" + type.id() + "_head_mold",
                    () -> new MoldItem(() -> 200, moldFluids, new Item.Properties())));
        }
        for (AreaToolMetal metal : AreaToolMetal.values()) {
            if (metal.isTFCM() && !TFCMConfig.isMetalEnabled(Metal.valueOf(metal.name()))) continue;
            Map<AreaToolType, DeferredItem<AreaMiningTool>> tools = new EnumMap<>(AreaToolType.class);
            Map<AreaToolType, DeferredItem<Item>> heads = new EnumMap<>(AreaToolType.class);
            for (AreaToolType type : AreaToolType.values()) {
                tools.put(type, ITEMS.register("metal/" + type.id() + "/" + metal.id(),
                        () -> new AreaMiningTool(type, metal.tier(), metal.repairIngredient())));
                heads.put(type, ITEMS.registerSimpleItem("metal/" + type.id() + "_head/" + metal.id()));
            }
            AREA_TOOLS.put(metal, tools);
            AREA_TOOL_HEADS.put(metal, heads);
        }
        for (MaterialPart part : MaterialPart.values()) {
            MATERIAL_PARTS.put(part, ITEMS.registerSimpleItem(part.id()));
        }
        for (ToolMetal metal : ToolMetal.values()) {
            if (!TFCMConfig.isMetalEnabled(metal.metal())) continue;
            Map<ToolPart, DeferredItem<Item>> parts = new EnumMap<>(ToolPart.class);
            for (ToolPart part : ToolPart.values()) {
                parts.put(part, ITEMS.register("metal/" + part.id() + "/" + metal.getSerializedName(),
                        () -> part.itemType().create(metal)));
            }
            METAL_ITEMS.put(metal, parts);
            ITEMS.registerSimpleItem("metal/crossguard/" + metal.getSerializedName());
            ITEMS.registerSimpleItem("metal/pommel/" + metal.getSerializedName());
            ITEMS.register("metal/anvil/" + metal.getSerializedName(),
                    () -> new BlockItem(TFCMBlocks.ANVILS.get(metal).get(), new Item.Properties()));
        }
        for (Metal metal : Metal.values()) {
            if (!TFCMConfig.isMetalEnabled(metal)) continue;
            for (MetalForm form : MetalForm.values()) {
                if (form.isItem()) ITEMS.registerSimpleItem("metal/" + form.id + "/" + metal.id());
            }
            ITEMS.register("metal/block/" + metal.id(),
                    () -> new BlockItem(TFCMBlocks.METAL_BLOCKS.get(metal).get(), new Item.Properties()));
            ITEMS.register("metal/block/" + metal.id() + "_slab",
                    () -> new BlockItem(TFCMBlocks.METAL_SLABS.get(metal).get(), new Item.Properties()));
            ITEMS.register("metal/block/" + metal.id() + "_stairs",
                    () -> new BlockItem(TFCMBlocks.METAL_STAIRS.get(metal).get(), new Item.Properties()));
            METAL_BUCKETS.put(metal, ITEMS.register("bucket/metal/" + metal.id(),
                    () -> new BucketItem(TFCMFluids.METALS.get(metal).source().get(),
                            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))));
        }
        for (Ore ore : Ore.values()) {
            if (!TFCMConfig.isOreEnabled(ore.id())) continue;
            if (ore.graded()) {
                Map<OreGrade, DeferredItem<Item>> grades = new EnumMap<>(OreGrade.class);
                for (OreGrade grade : OreGrade.values()) grades.put(grade,
                        ITEMS.registerSimpleItem("ore/" + ore.itemName(grade)));
                GRADED_ORES.put(ore, grades);
                SMALL_ORES.put(ore, ITEMS.register("ore/small_" + ore.id(),
                        () -> new BlockItem(TFCMBlocks.SMALL_ORES.get(ore).get(), new Item.Properties())));
            } else {
                ORES.put(ore, ITEMS.registerSimpleItem("ore/" + ore.id()));
            }
        }
        if (TFCMConfig.isOreEnabled("quartz"))
            ORE_POWDERS.put(Ore.QUARTZ, ITEMS.registerSimpleItem("powder/quartz"));
        if (TFCMConfig.isOreEnabled("fluorite"))
            ORE_POWDERS.put(Ore.FLUORITE, ITEMS.registerSimpleItem("powder/fluorite"));
        for (SandPile sand : SandPile.values()) {
            SAND_PILES.put(sand, ITEMS.registerSimpleItem("ore_processing/sand_pile/" + sand.id()));
        }
        for (ProcessingSeries series : ProcessingSeries.values()) {
            if (!TFCMConfig.isOreEnabled(series.id())) continue;
            Map<ProcessingForm, DeferredItem<Item>> forms = new EnumMap<>(ProcessingForm.class);
            for (ProcessingForm form : ProcessingForm.values()) {
                if (series.nonMetal() && (form == ProcessingForm.PELLET || form == ProcessingForm.BRIQUET
                        || series != ProcessingSeries.BORAX && form == ProcessingForm.POWDER)) continue;
                forms.put(form, ITEMS.registerSimpleItem("ore_processing/" + form.id() + "/" + series.id()));
            }
            ORE_PROCESSING.put(series, forms);
        }
        TFCMBlocks.ORES.values().forEach(blocks -> blocks.forEach((name, block) -> ITEMS.register("ore/" + name,
                () -> new BlockItem(block.get(), new Item.Properties()))));
        TFCMBlocks.TFC_ORES.forEach((name, block) -> ITEMS.register("ore/" + name,
                () -> new BlockItem(block.get(), new Item.Properties())));
        TFCMBlocks.CHROMITE_ORES.forEach((name, block) -> ITEMS.register("ore/" + name,
                () -> new BlockItem(block.get(), new Item.Properties())));
        for (GeodeMineral mineral : GeodeMineral.values()) {
            registerGeodeBlock("mineral/" + mineral.id() + "_block", TFCMBlocks.MINERAL_BLOCKS.get(mineral));
            registerGeodeBlock("mineral/budding_" + mineral.id(), TFCMBlocks.BUDDING_BLOCKS.get(mineral));
            registerGeodeBlock("mineral/small_" + mineral.id() + "_bud", TFCMBlocks.SMALL_BUDS.get(mineral));
            registerGeodeBlock("mineral/medium_" + mineral.id() + "_bud", TFCMBlocks.MEDIUM_BUDS.get(mineral));
            registerGeodeBlock("mineral/large_" + mineral.id() + "_bud", TFCMBlocks.LARGE_BUDS.get(mineral));
            registerGeodeBlock("mineral/" + mineral.id() + "_cluster", TFCMBlocks.CLUSTERS.get(mineral));
        }
    }

    private static void registerGeodeBlock(String id, Supplier<? extends Block> block) {
        ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private TFCMItems() {
    }
}
