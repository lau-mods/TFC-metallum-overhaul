package net.claustra01.tfcm.common.ore;

import java.util.List;
import java.util.stream.Stream;

public final class TfcOre {
    public static final List<String> GRADED = List.of("bismuthinite", "cassiterite", "garnierite", "hematite",
            "limonite", "magnetite", "malachite", "native_copper", "native_gold", "native_silver",
            "sphalerite", "tetrahedrite");
    public static final List<String> UNGRADED = List.of("amethyst", "borax", "cinnabar", "cryolite", "diamond",
            "emerald", "graphite", "gypsum", "lapis_lazuli", "opal", "pyrite", "ruby", "saltpeter",
            "sapphire", "sulfur", "sylvite", "topaz");
    /** Surface samples for raw items without any TFCM ore block. */
    public static final List<String> SAMPLE_ONLY = List.of("bituminous_coal", "halite", "lignite", "nether_quartz");

    public static List<String> samples() {
        return Stream.concat(UNGRADED.stream(), SAMPLE_ONLY.stream()).toList();
    }

    public static String sampleDrop(String ore) {
        return ore.equals("nether_quartz") ? "minecraft:quartz" : "tfc:ore/" + ore;
    }

    public static String sampleTexture(String ore) {
        return ore.equals("nether_quartz") ? "minecraft:item/quartz" : "tfc:item/ore/" + ore;
    }

    private TfcOre() {
    }
}
