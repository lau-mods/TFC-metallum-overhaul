package net.claustra01.tfcm.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class TFCMConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLE_CUSTOM_VEIN_GENERATION;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        ENABLE_CUSTOM_VEIN_GENERATION = builder
                .comment("Replace TFC overworld veins and TFCM geodes with config/tfcm/*.yaml world generation")
                .define("enableCustomVeinGeneration", true);
        SPEC = builder.build();
    }

    private TFCMConfig() {
    }
}
