package net.claustra01.tfcm.compat.hotornot;

import java.util.EnumMap;
import java.util.Map;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.tool.ToolMetal;
import net.claustra01.tfcm.config.TFCMConfig;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import tfchotornot.common.items.TongsItem;

public final class HotOrNotCompat {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TFCM.MOD_ID);
    public static final Map<ToolMetal, DeferredItem<Item>> TONGS = new EnumMap<>(ToolMetal.class);
    public static final Map<ToolMetal, DeferredItem<Item>> TONG_PARTS = new EnumMap<>(ToolMetal.class);

    static {
        for (ToolMetal metal : ToolMetal.values()) {
            if (!TFCMConfig.isMetalEnabled(metal.metal())) continue;
            TONG_PARTS.put(metal, ITEMS.registerSimpleItem("metal/tong_part/" + metal.getSerializedName()));
            TONGS.put(metal, ITEMS.register("metal/tongs/" + metal.getSerializedName(),
                    () -> new TongsItem(new Item.Properties().durability(metal.toolTier().getUses()),
                            metal.toolTier())));
        }
    }

    private HotOrNotCompat() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
