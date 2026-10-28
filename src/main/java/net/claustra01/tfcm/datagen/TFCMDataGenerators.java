package net.claustra01.tfcm.datagen;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class TFCMDataGenerators {
    private TFCMDataGenerators() {
    }

    public static void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(event.includeServer(), new MetalDataProvider(event.getGenerator().getPackOutput()));
    }
}
