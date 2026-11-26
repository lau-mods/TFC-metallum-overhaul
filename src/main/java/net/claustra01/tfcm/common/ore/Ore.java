package net.claustra01.tfcm.common.ore;

import java.util.Locale;

public enum Ore {
    NATIVE_PLATINUM(true),
    NATIVE_NAQUADAH(true),
    NATIVE_IRIDIUM(true),
    NATIVE_OSMIUM(true),
    RUTILE(true),
    COBALTITE(true),
    SPODUMENE(true),
    BAUXITE(true),
    GALENA(true),
    URANINITE(true),
    MITHRIL_MATRIX(true),
    STIBNITE(true),
    WOLFRAMITE(true),
    THORIANITE(true),
    MAGNESITE(true),
    ZIRCON(true),
    PYROLUSITE(true),
    MONAZITE(true),
    FLUORITE(false),
    QUARTZ(false),
    CAROBBIITE(false),
    ACANTHITE(true),
    ALUMINA(true),
    ANATASE(true),
    CHALCOCITE(true),
    CHALCOPYRITE(true),
    CUPRITE(true),
    ILMENITE(true),
    NATIVE_LEAD(true),
    MILLERITE(true),
    MOLYBDENITE(true),
    SCHEELITE(true),
    SMITHSONITE(true),
    THORITE(true),
    VANADINITE(true);

    private final boolean graded;

    Ore(boolean graded) {
        this.graded = graded;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean graded() {
        return graded;
    }

    public String itemName(OreGrade grade) {
        return grade == null ? id() : grade.id() + "_" + id();
    }
}
