package net.claustra01.tfcm.common.processing;

import java.util.Locale;

public enum ProcessingSeries {
    NATIVE_COPPER(SandPile.RED, "basalt", "andesite", "opal", "hematite", "tfc:metal/copper", 1080, true, false),
    NATIVE_GOLD(SandPile.YELLOW, "quartzite", "granite", "quartz", "native_silver", "tfc:metal/gold", 1060, true, false),
    HEMATITE(SandPile.RED, "quartzite", "shale", null, "sulfur", "tfc:metal/cast_iron", 1535, true, false),
    NATIVE_SILVER(SandPile.WHITE, "gneiss", "granite", "amethyst", "galena", "tfc:metal/silver", 961, true, false),
    CASSITERITE(SandPile.WHITE, "granite", "gneiss", "topaz", "bismuthinite", "tfc:metal/tin", 230, true, false),
    BISMUTHINITE(SandPile.BLACK, "granite", "schist", "pyrite", "galena", "tfc:metal/bismuth", 270, true, false),
    GARNIERITE(SandPile.GREEN, "gabbro", "basalt", "opal", "limonite", "tfc:metal/nickel", 1453, true, false),
    MALACHITE(SandPile.GREEN, "limestone", "dolomite", "opal", "limonite", "tfc:metal/copper", 1080, true, false),
    MAGNETITE(SandPile.BLACK, "gabbro", "basalt", null, "hematite", "tfc:metal/cast_iron", 1535, true, false),
    LIMONITE(SandPile.BROWN, "claystone", "shale", "opal", "sulfur", "tfc:metal/cast_iron", 1535, true, false),
    SPHALERITE(SandPile.BROWN, "dolomite", "limestone", "amethyst", "galena", "tfc:metal/zinc", 420, true, false),
    TETRAHEDRITE(SandPile.BLACK, "schist", "dolomite", "pyrite", "sphalerite", "tfc:metal/copper", 1080, true, false),
    GALENA(SandPile.BLACK, "limestone", "dolomite", "amethyst", "sphalerite", "tfcm:metal/lead", 330, true, false),
    MAGNESITE(SandPile.WHITE, "dolomite", "gabbro", "opal", "limonite", "tfcm:metal/magnesium", 650, true, false),
    BAUXITE(SandPile.RED, "claystone", "shale", "sapphire", "hematite", "tfcm:metal/aluminum", 1740, true, false),
    URANINITE(SandPile.BLACK, "granite", "gneiss", null, "galena", "tfcm:metal/uranium", 1130, true, false),
    CHROMITE(SandPile.BLACK, "gabbro", "basalt", "ruby", "cobaltite", "firmalife:metal/chromium", 1250, true, true),
    RUTILE(SandPile.RED, "gneiss", "schist", "sapphire", "hematite", "tfcm:metal/titanium", 1700, true, false),
    ZIRCON(SandPile.BROWN, "granite", "gneiss", "topaz", "cassiterite", "tfcm:metal/zirconium", 1855, true, false),
    COBALTITE(SandPile.BLACK, "gabbro", "schist", "pyrite", "sulfur", "tfcm:metal/cobalt", 1700, true, false),
    STIBNITE(SandPile.BLACK, "shale", "slate", "amethyst", "galena", "tfcm:metal/antimony", 630, true, false),
    NATIVE_PLATINUM(SandPile.BLACK, "gabbro", "basalt", null, "chromite", "tfcm:metal/platinum", 1730, true, false),
    WOLFRAMITE(SandPile.BLACK, "granite", "gneiss", "quartz", "cassiterite", "tfcm:metal/tungsten", 1535, true, false),
    THORIANITE(SandPile.BLACK, "granite", "gneiss", null, "galena", "tfcm:metal/thorium", 1750, true, false),
    NATIVE_IRIDIUM(SandPile.BLACK, "gabbro", "basalt", null, "native_platinum", "tfcm:metal/iridium", 1535, true, false),
    NATIVE_OSMIUM(SandPile.BLACK, "gabbro", "basalt", null, "native_platinum", "tfcm:metal/osmium", 1535, true, false),
    SPODUMENE(SandPile.WHITE, "granite", "gneiss", "topaz", "cassiterite", "tfcm:metal/lithium", 181, true, false),
    CAROBBIITE(SandPile.WHITE, "granite", "gneiss", "emerald", "native_platinum", "tfcm:metal/beryllium", 1287, false, false),
    NATIVE_NAQUADAH(SandPile.BLACK, "gabbro", "basalt", null, "native_osmium", "tfcm:metal/naquadah", 2000, true, false),
    MITHRIL_MATRIX(SandPile.WHITE, "gneiss", "granite", "amethyst", "native_platinum", "tfcm:metal/mithril", 940, true, false),
    CINNABAR(SandPile.RED, "quartzite", "schist", "ruby", "sulfur", null, null, false, true, false),
    CRYOLITE(SandPile.WHITE, "granite", "gneiss", "topaz", "spodumene", null, null, false, true, false),
    GRAPHITE(SandPile.BLACK, "schist", "phyllite", "diamond", "hematite", null, null, false, true, false),
    SULFUR(SandPile.YELLOW, "tuff", "basalt", "pyrite", "hematite", null, null, false, true, false),
    BORAX(SandPile.WHITE, "granite", "gneiss", "topaz", "graphite", "tfcm:metal/boron", 2076,
            false, true, false);

    private final SandPile sand;
    private final String primaryRock;
    private final String secondaryRock;
    private final String gem;
    private final String byproduct;
    private final String recoveryFluid;
    private final Integer recoveryTemperature;
    private final boolean graded;
    private final boolean nonMetal;
    private final boolean firmaLifeOnly;

    ProcessingSeries(SandPile sand, String primaryRock, String secondaryRock, String gem,
            String byproduct, String recoveryFluid, int recoveryTemperature, boolean graded,
            boolean firmaLifeOnly) {
        this(sand, primaryRock, secondaryRock, gem, byproduct, recoveryFluid, recoveryTemperature,
                graded, false, firmaLifeOnly);
    }

    ProcessingSeries(SandPile sand, String primaryRock, String secondaryRock, String gem,
            String byproduct, String recoveryFluid, Integer recoveryTemperature, boolean graded,
            boolean nonMetal, boolean firmaLifeOnly) {
        this.sand = sand;
        this.primaryRock = primaryRock;
        this.secondaryRock = secondaryRock;
        this.gem = gem;
        this.byproduct = byproduct;
        this.recoveryFluid = recoveryFluid;
        this.recoveryTemperature = recoveryTemperature;
        this.graded = graded;
        this.nonMetal = nonMetal;
        this.firmaLifeOnly = firmaLifeOnly;
    }

    public String powderItem() {
        return switch (this) {
            case CINNABAR, CRYOLITE -> "minecraft:redstone";
            case GRAPHITE, SULFUR -> "tfc:powder/" + id();
            default -> "tfcm:ore_processing/powder/" + id();
        };
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public SandPile sand() {
        return sand;
    }

    public String primaryRock() {
        return primaryRock;
    }

    public String secondaryRock() {
        return secondaryRock;
    }

    public String gem() {
        return gem;
    }

    public String byproduct() {
        return byproduct;
    }

    public boolean graded() {
        return graded;
    }

    public boolean firmaLifeOnly() {
        return firmaLifeOnly;
    }

    public boolean nonMetal() {
        return nonMetal;
    }

    public String recoveryFluid() {
        return recoveryFluid;
    }

    public int recoveryTemperature() {
        return recoveryTemperature;
    }
}
