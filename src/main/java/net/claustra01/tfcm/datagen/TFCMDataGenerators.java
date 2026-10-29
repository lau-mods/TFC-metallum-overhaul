package net.claustra01.tfcm.datagen;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class TFCMDataGenerators {
    private TFCMDataGenerators() {
    }

    public static void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(event.includeServer(), new MetalDataProvider(event.getGenerator().getPackOutput()));
        event.getGenerator().addProvider(event.includeServer(), new MaterialDataProvider(event.getGenerator().getPackOutput()));
        event.getGenerator().addProvider(event.includeServer(),
                new EquipmentDataProvider(event.getGenerator().getPackOutput()));
        event.getGenerator().addProvider(event.includeServer(), new OreDataProvider(event.getGenerator().getPackOutput()));
        event.getGenerator().addProvider(event.includeServer(),
                new ProcessingDataProvider(event.getGenerator().getPackOutput()));
        event.getGenerator().addProvider(event.includeServer(),
                new SurfaceResourceDataProvider(event.getGenerator().getPackOutput()));
    }
}
