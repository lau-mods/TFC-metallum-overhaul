package net.claustra01.tfcm;

import java.util.function.Supplier;

import net.claustra01.tfcm.common.condition.MetalEnabledCondition;
import net.claustra01.tfcm.compat.hotornot.HotOrNotCompat;
import net.claustra01.tfcm.datagen.TFCMDataGenerators;
import net.claustra01.tfcm.registry.TFCMArmorMaterials;
import net.claustra01.tfcm.registry.TFCMBlocks;
import net.claustra01.tfcm.registry.TFCMFluids;
import net.claustra01.tfcm.registry.TFCMItems;
import net.claustra01.tfcm.registry.TFCMLootModifiers;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;

@Mod(TFCM.MOD_ID)
public final class TFCM {
    public static final String MOD_ID = "tfcm";

    public TFCM(IEventBus modBus) {
        TFCMArmorMaterials.ARMOR_MATERIALS.register(modBus);
        if (ModList.get().isLoaded("tfchotornot")) {
            HotOrNotCompat.register(modBus);
        }
        TFCMItems.ITEMS.register(modBus);
        TFCMBlocks.BLOCKS.register(modBus);
        TFCMFluids.FLUID_TYPES.register(modBus);
        TFCMFluids.FLUIDS.register(modBus);
        TFCMLootModifiers.LOOT_MODIFIERS.register(modBus);
        MetalEnabledCondition.CONDITIONS.register(modBus);
        modBus.addListener(TFCMDataGenerators::gatherData);
        modBus.addListener(TFCM::addAnvilBlocks);
    }

    private static void addAnvilBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(TFCBlockEntities.ANVIL.get(), TFCMBlocks.ANVILS.values().stream()
                .map(Supplier::get).toArray(Block[]::new));
    }
}
