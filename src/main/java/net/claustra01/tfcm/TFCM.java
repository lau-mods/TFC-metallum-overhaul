package net.claustra01.tfcm;

import net.claustra01.tfcm.registry.TFCMBlocks;
import net.claustra01.tfcm.registry.TFCMFluids;
import net.claustra01.tfcm.registry.TFCMItems;
import net.claustra01.tfcm.common.condition.MetalEnabledCondition;
import net.claustra01.tfcm.datagen.TFCMDataGenerators;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(TFCM.MOD_ID)
public final class TFCM {
    public static final String MOD_ID = "tfcm";

    public TFCM(IEventBus modBus) {
        TFCMItems.ITEMS.register(modBus);
        TFCMBlocks.BLOCKS.register(modBus);
        TFCMFluids.FLUID_TYPES.register(modBus);
        TFCMFluids.FLUIDS.register(modBus);
        MetalEnabledCondition.CONDITIONS.register(modBus);
        modBus.addListener(TFCMDataGenerators::gatherData);
    }
}
