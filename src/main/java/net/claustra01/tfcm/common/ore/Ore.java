package net.claustra01.tfcm.common.ore;

import java.util.Locale;

public enum Ore {
    NATIVE_PLATINUM(true, "native_silver"),
    NATIVE_NAQUADAH(true, "garnierite"),
    NATIVE_IRIDIUM(true, "native_silver"),
    NATIVE_OSMIUM(true, "magnetite"),
    RUTILE(true, "hematite"),
    COBALTITE(true, "tetrahedrite"),
    SPODUMENE(true, "cassiterite"),
    BAUXITE(true, "hematite"),
    GALENA(true, "sphalerite"),
    URANINITE(true, "magnetite"),
    MITHRIL_MATRIX(true, "native_silver"),
    STIBNITE(true, "tetrahedrite"),
    WOLFRAMITE(true, "magnetite"),
    THORIANITE(true, "magnetite"),
    MAGNESITE(true, "cassiterite"),
    ZIRCON(true, "garnierite"),
    FLUORITE(false, "amethyst"),
    QUARTZ(false, "opal"),
    CAROBBIITE(false, "emerald");

    private final boolean graded;
    private final String textureSource;

    Ore(boolean graded, String textureSource) {
        this.graded = graded;
        this.textureSource = textureSource;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean graded() {
        return graded;
    }

    public String textureSource() {
        return textureSource;
    }

    public String itemName(OreGrade grade) {
        return grade == null ? id() : grade.id() + "_" + id();
    }
}
