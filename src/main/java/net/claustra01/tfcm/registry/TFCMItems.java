package net.claustra01.tfcm.registry;

import java.util.EnumMap;
import java.util.Map;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.metal.Metal;
import net.claustra01.tfcm.common.metal.MetalForm;
import net.claustra01.tfcm.common.material.MaterialPart;
import net.claustra01.tfcm.common.ore.Ore;
import net.claustra01.tfcm.common.ore.OreGrade;
import net.claustra01.tfcm.common.tool.ToolMetal;
import net.claustra01.tfcm.common.tool.ToolPart;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TFCMItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TFCM.MOD_ID);
    public static final Map<Metal, DeferredItem<BucketItem>> METAL_BUCKETS = new EnumMap<>(Metal.class);
    public static final Map<MaterialPart, DeferredItem<Item>> MATERIAL_PARTS = new EnumMap<>(MaterialPart.class);
    public static final Map<ToolMetal, Map<ToolPart, DeferredItem<Item>>> METAL_ITEMS = new EnumMap<>(ToolMetal.class);
    public static final Map<Ore, Map<OreGrade, DeferredItem<Item>>> GRADED_ORES = new EnumMap<>(Ore.class);
    public static final Map<Ore, DeferredItem<Item>> ORES = new EnumMap<>(Ore.class);
    public static final Map<Ore, DeferredItem<BlockItem>> SMALL_ORES = new EnumMap<>(Ore.class);
    public static final Map<Ore, DeferredItem<Item>> ORE_POWDERS = new EnumMap<>(Ore.class);
    public static final DeferredItem<Item> CUT_QUARTZ = ITEMS.registerSimpleItem("gem/cut_quartz");
    public static final DeferredItem<Item> CERTUS_QUARTZ = ITEMS.registerSimpleItem("gem/certus_quartz");
    public static final DeferredItem<BlockItem> PLANT_BLOCK = ITEMS.register("plant_block",
            () -> new BlockItem(TFCMBlocks.PLANT_BLOCK.get(), new Item.Properties()));
    public static final DeferredItem<Item> HIGH_CARBON_TUNGSTEN_STEEL = ITEMS.registerSimpleItem(
            "metal/ingot/high_carbon_tungsten_steel");
    public static final DeferredItem<BucketItem> MOLTEN_ANDESITE_BUCKET = ITEMS.register(
            "molten_andesite_bucket", () -> new BucketItem(TFCMFluids.MOLTEN_ANDESITE_SOURCE.get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    static {
        for (MaterialPart part : MaterialPart.values()) {
            MATERIAL_PARTS.put(part, ITEMS.registerSimpleItem(part.id()));
        }
        for (ToolMetal metal : ToolMetal.values()) {
            Map<ToolPart, DeferredItem<Item>> parts = new EnumMap<>(ToolPart.class);
            for (ToolPart part : ToolPart.values()) {
                parts.put(part, ITEMS.register("metal/" + part.id() + "/" + metal.getSerializedName(),
                        () -> part.itemType().create(metal)));
            }
            METAL_ITEMS.put(metal, parts);
            ITEMS.register("metal/anvil/" + metal.getSerializedName(),
                    () -> new BlockItem(TFCMBlocks.ANVILS.get(metal).get(), new Item.Properties()));
        }
        for (Metal metal : Metal.values()) {
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
        ORE_POWDERS.put(Ore.QUARTZ, ITEMS.registerSimpleItem("powder/quartz"));
        ORE_POWDERS.put(Ore.FLUORITE, ITEMS.registerSimpleItem("powder/fluorite"));
        TFCMBlocks.ORES.values().forEach(blocks -> blocks.forEach((name, block) -> ITEMS.register("ore/" + name,
                () -> new BlockItem(block.get(), new Item.Properties()))));
        TFCMBlocks.TFC_ORES.forEach((name, block) -> ITEMS.register("ore/" + name,
                () -> new BlockItem(block.get(), new Item.Properties())));
        TFCMBlocks.CHROMITE_ORES.forEach((name, block) -> ITEMS.register("ore/" + name,
                () -> new BlockItem(block.get(), new Item.Properties())));
    }

    private TFCMItems() {
    }
}
