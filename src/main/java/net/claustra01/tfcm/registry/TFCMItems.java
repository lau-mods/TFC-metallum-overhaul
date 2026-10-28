package net.claustra01.tfcm.registry;

import java.util.EnumMap;
import java.util.Map;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.metal.Metal;
import net.claustra01.tfcm.common.metal.MetalForm;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TFCMItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TFCM.MOD_ID);
    public static final Map<Metal, DeferredItem<BucketItem>> METAL_BUCKETS = new EnumMap<>(Metal.class);
    public static final DeferredItem<Item> HIGH_CARBON_TUNGSTEN_STEEL = ITEMS.registerSimpleItem(
            "metal/ingot/high_carbon_tungsten_steel");
    public static final DeferredItem<BucketItem> MOLTEN_ANDESITE_BUCKET = ITEMS.register(
            "molten_andesite_bucket", () -> new BucketItem(TFCMFluids.MOLTEN_ANDESITE_SOURCE.get(),
                    new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    static {
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
    }

    private TFCMItems() {
    }
}
