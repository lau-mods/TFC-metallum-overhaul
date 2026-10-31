package net.claustra01.tfcm.common.metal;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * TFC 4.2.12 metals that have sheets and rods. TFCM gives them the same added forms as its own metals.
 * Tier, melting point and heat values are those of TFC's own anvil recipes, fluid heat and ingot item heat;
 * a tier of 0 means TFC's recipes for the metal do not set one. Chromium and Stainless Steel are FirmaLife 3.0.15's
 * metals: their values are FirmaLife's, they exist only while it is loaded and get just the TFC More Items forms.
 */
public enum TfcMetal {
    BISMUTH(0, 270, 7.142857f, 162, 216),
    BISMUTH_BRONZE(2, 985, 2.857143f, 591, 788),
    BLACK_BRONZE(2, 1070, 2.857143f, 642, 856),
    BLACK_STEEL(5, 1485, 2.857143f, 891.00006f, 1188),
    BLUE_STEEL(6, 1540, 2.857143f, 924.00006f, 1232),
    BRASS(0, 930, 2.857143f, 558, 744),
    BRONZE(2, 950, 2.857143f, 570, 760),
    CAST_IRON(0, 1535, 2.857143f, 921.00006f, 1228),
    COPPER(1, 1080, 2.857143f, 648, 864),
    GOLD(0, 1060, 1.6666666f, 636, 848),
    NICKEL(0, 1453, 2.0833333f, 871.80005f, 1162.4f),
    RED_STEEL(6, 1540, 2.857143f, 924.00006f, 1232),
    ROSE_GOLD(0, 960, 2.857143f, 576, 768),
    SILVER(0, 961, 2.0833333f, 576.60004f, 768.8f),
    STEEL(4, 1540, 2.857143f, 924.00006f, 1232),
    STERLING_SILVER(0, 950, 2.857143f, 570, 760),
    TIN(0, 230, 7.142857f, 138, 184),
    WROUGHT_IRON(3, 1535, 2.857143f, 921.00006f, 1228),
    ZINC(0, 420, 4.761905f, 252.00002f, 336),
    CHROMIUM(4, 1250, 2.857143f, 750, 1000),
    STAINLESS_STEEL(4, 1540, 2.857143f, 924.00006f, 1232);

    private static final List<MetalForm> FIRMALIFE_FORMS = List.of(MetalForm.FOIL, MetalForm.GEAR,
            MetalForm.HEAVY_SHEET, MetalForm.NAIL, MetalForm.RING, MetalForm.RIVET, MetalForm.SCREW, MetalForm.STAMEN,
            MetalForm.WIRE);

    private final String id;
    private final int tier;
    private final float meltingPoint;
    private final float heatCapacity;
    private final float forgingTemperature;
    private final float weldingTemperature;

    TfcMetal(int tier, float meltingPoint, float heatCapacity, float forgingTemperature, float weldingTemperature) {
        this.id = name().toLowerCase(Locale.ROOT);
        this.tier = tier;
        this.meltingPoint = meltingPoint;
        this.heatCapacity = heatCapacity;
        this.forgingTemperature = forgingTemperature;
        this.weldingTemperature = weldingTemperature;
    }

    public String id() {
        return id;
    }

    public int tier() {
        return tier;
    }

    public float meltingPoint() {
        return meltingPoint;
    }

    /** Heat capacity of 100 mB of the metal, as on the TFC ingot. */
    public float heatCapacity() {
        return heatCapacity;
    }

    public float forgingTemperature() {
        return forgingTemperature;
    }

    public float weldingTemperature() {
        return weldingTemperature;
    }

    public boolean isFirmaLife() {
        return this == CHROMIUM || this == STAINLESS_STEEL;
    }

    /** TFC only makes tools from the metals its recipes give a tier. */
    public boolean hasTools() {
        return tier > 0 && !isFirmaLife();
    }

    public List<MetalForm> addedForms() {
        return isFirmaLife() ? FIRMALIFE_FORMS
                : Arrays.stream(MetalForm.values()).filter(MetalForm::isAddedForm).toList();
    }

    public String fluid() {
        return (isFirmaLife() ? "firmalife" : "tfc") + ":metal/" + id;
    }

    public String item(MetalForm form) {
        return "tfcm:metal/" + form.id + "/" + id;
    }
}
