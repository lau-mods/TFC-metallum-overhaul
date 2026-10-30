package net.claustra01.tfcm.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import net.claustra01.tfcm.common.metal.Metal;
import net.claustra01.tfcm.common.ore.Ore;
import net.claustra01.tfcm.common.ore.TfcOre;
import net.claustra01.tfcm.common.processing.ProcessingSeries;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class TFCMConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLE_CUSTOM_VEIN_GENERATION;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DISABLED_METALS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DISABLED_ORES;
    private static Set<String> disabledMetals = Set.of();
    private static Set<String> disabledOres = Set.of();

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        ENABLE_CUSTOM_VEIN_GENERATION = builder
                .comment("Replace TFC overworld veins and TFCM geodes with config/tfcm/*.yaml world generation")
                .define("enableCustomVeinGeneration", true);
        DISABLED_METALS = builder.comment("Content is disabled after the game is restarted.",
                "Accepted metal IDs: " + String.join(", ", metalIds()))
                .defineListAllowEmpty("disabledMetals", List::of, value -> value instanceof String);
        DISABLED_ORES = builder.comment("Unknown IDs are ignored. Content is disabled after the game is restarted.",
                "Accepted ore IDs: " + String.join(", ", oreIds()))
                .defineListAllowEmpty("disabledOres", List::of, value -> value instanceof String);
        SPEC = builder.build();
    }

    public static void loadDisabledContent() {
        Path path = FMLPaths.CONFIGDIR.get().resolve("tfcm-common.toml");
        if (!Files.isRegularFile(path)) return;
        try (CommentedFileConfig config = CommentedFileConfig.of(path)) {
            config.load();
            disabledMetals = acceptedValues(config.get("disabledMetals"), metalIds());
            disabledOres = acceptedValues(config.get("disabledOres"), oreIds());
        }
    }

    public static boolean isMetalEnabled(Metal metal) {
        return !disabledMetals.contains(metal.id());
    }

    public static boolean isOreEnabled(String ore) {
        return !disabledOres.contains(ore);
    }

    public static List<String> metalIds() {
        return Arrays.stream(Metal.values()).map(Metal::id).toList();
    }

    public static List<String> oreIds() {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        Arrays.stream(Ore.values()).map(Ore::id).forEach(ids::add);
        Arrays.stream(ProcessingSeries.values()).map(ProcessingSeries::id).forEach(ids::add);
        ids.addAll(TfcOre.GRADED);
        ids.addAll(TfcOre.UNGRADED);
        return List.copyOf(ids);
    }

    private static Set<String> acceptedValues(List<?> configured, List<String> accepted) {
        if (configured == null) return Set.of();
        Set<String> values = new LinkedHashSet<>();
        for (Object value : configured) if (value instanceof String name && accepted.contains(name)) values.add(name);
        return Set.copyOf(values);
    }

    private TFCMConfig() {
    }
}
