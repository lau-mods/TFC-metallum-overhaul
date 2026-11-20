package net.claustra01.tfcm.common.metal;

import java.util.Locale;

import net.minecraft.world.item.Rarity;

public enum Metal {
    COMPRESSED_IRON(3, 1535, false, 0xFF5C5958, 0.008571429f, Rarity.UNCOMMON),
    PLATINUM(3, 1730, false, 0xFFB2B6B8, 0.008571429f, Rarity.RARE),
    NAQUADAH(7, 2000, false, 0xFF23281D, 0.008571429f, Rarity.RARE),
    IRIDIUM(3, 1535, false, 0xFFADC4CE, 0.008571429f, Rarity.UNCOMMON),
    OSMIUM(3, 1535, false, 0xFF5B6B83, 0.008571429f, Rarity.UNCOMMON),
    OSMIRIDIUM(3, 1500, false, 0xFF718383, 0.008571429f, Rarity.UNCOMMON),
    MITHRIL(2, 940, false, 0xFF8DB4BC, 0.008571429f, Rarity.COMMON),
    ARCANE(2, 940, false, 0xFF888CCC, 0.008571429f, Rarity.RARE),
    REFINED_GLOWSTONE(2, 940, false, 0xFFDCC37F, 0.008571429f, Rarity.UNCOMMON),
    REFINED_OBSIDIAN(2, 940, false, 0xFF6D5887, 0.008571429f, Rarity.RARE),
    ANTIMONY(1, 630, false, 0xFFB4AFBD, 0.012f, Rarity.COMMON),
    TITANIUM(5, 1700, true, 0xFF949396, 0.01f, Rarity.UNCOMMON),
    COBALT(3, 1700, false, 0xFF808A9D, 0.01f, Rarity.UNCOMMON),
    LITHIUM(3, 181, false, 0xFF9FA396, 0.01f, Rarity.COMMON),
    ALUMINUM(3, 660, false, 0xFF929799, 0.0042857146f, Rarity.COMMON),
    CONSTANTAN(1, 1250, false, 0xFFA45B49, 0.008571429f, Rarity.COMMON),
    INVAR(3, 1250, true, 0xFF869A97, 0.00857f, Rarity.COMMON),
    ELECTRUM(1, 1000, false, 0xFFA67F3A, 0.0054545454f, Rarity.UNCOMMON),
    LEAD(1, 330, false, 0xFF393D4A, 0.023076924f, Rarity.COMMON),
    URANIUM(3, 1130, false, 0xFF4E5946, 0.025f, Rarity.UNCOMMON),
    TUNGSTEN(3, 1535, false, 0xFF728692, 0.015f, Rarity.EPIC),
    SOLDER(3, 400, false, 0xFF888888, 0.008571429f, Rarity.UNCOMMON),
    TUNGSTEN_STEEL(7, 1535, true, 0xFF2F353E, 0.015f, Rarity.EPIC),
    NETHERITE(6, 1535, true, 0xFF3F393B, 0.015f, Rarity.RARE),
    DAWNSTONE(1, 1000, false, 0xFFB18143, 0.0054545454f, Rarity.UNCOMMON),
    ANDESITE_ALLOY(2, 520, false, 0xFF757E76, 0.012f, Rarity.COMMON),
    DRACONIUM(7, 2000, false, 0xFF6C3F99, 0.008571429f, Rarity.EPIC),
    AWAKENED_DRACONIUM(7, 2000, false, 0xFFE06100, 0.008571429f, Rarity.EPIC),
    BORON(3, 2076, false, 0xFF676767, 0.008571429f, Rarity.UNCOMMON),
    THORIUM(3, 1750, false, 0xFF9F75C2, 0.008571429f, Rarity.UNCOMMON),
    MAGNESIUM(2, 650, false, 0xFF89797F, 0.008571429f, Rarity.COMMON),
    BERYLLIUM(3, 1287, false, 0xFFD3DCC3, 0.008571429f, Rarity.UNCOMMON),
    ZIRCONIUM(2, 1855, false, 0xFF728557, 0.008571429f, Rarity.UNCOMMON),
    MANGANESE(3, 1246, false, 0xFF766862, 0.008571429f, Rarity.UNCOMMON),
    MOLYBDENUM(3, 1535, false, 0xFF8DAEB1, 0.008571429f, Rarity.UNCOMMON),
    NEODYMIUM(3, 1024, false, 0xFF726440, 0.008571429f, Rarity.UNCOMMON),
    VANADIUM(3, 1700, false, 0xFF87201E, 0.008571429f, Rarity.UNCOMMON),
    HASTELLOY(4, 1370, false, 0xFF73969B, 0.008571429f, Rarity.UNCOMMON),
    HIGH_SPEED_STEEL(5, 1450, false, 0xFF848E9F, 0.008571429f, Rarity.UNCOMMON),
    SIGNALUM(4, 1500, false, 0xFFCE3912, 0.00857f, Rarity.RARE),
    LUMIUM(4, 1500, false, 0xFFE2B773, 0.00857f, Rarity.RARE),
    ENDERIUM(5, 1700, false, 0xFF16576C, 0.00857f, Rarity.RARE);

    private final String id;
    private final int forgingTier;
    private final int meltingPoint;
    private final boolean providesTools;
    private final int color;
    private final float specificHeat;
    private final Rarity rarity;

    Metal(int forgingTier, int meltingPoint, boolean providesTools, int color, float specificHeat, Rarity rarity) {
        this.id = name().toLowerCase(Locale.ROOT);
        this.forgingTier = forgingTier;
        this.meltingPoint = meltingPoint;
        this.providesTools = providesTools;
        this.color = color;
        this.specificHeat = specificHeat;
        this.rarity = rarity;
    }

    public String id() {
        return id;
    }

    public int forgingTier() {
        return forgingTier;
    }

    public int meltingPoint() {
        return meltingPoint;
    }

    public boolean providesTools() {
        return providesTools;
    }

    public int color() {
        return color;
    }

    public Rarity rarity() {
        return rarity;
    }

    public float specificHeat() {
        return specificHeat;
    }

    public float itemHeatCapacity(int amount) {
        return switch (this) {
            // Metallum U rounds item heat independently of its fluid specific heat.
            case SIGNALUM, LUMIUM, ENDERIUM -> Math.round(amount * 1000f / 35f) / 1000f;
            default -> specificHeat * 333.33334f * amount / 100f;
        };
    }

    public float forgingTemperature() {
        return this == DRACONIUM || this == AWAKENED_DRACONIUM ? 1200 : (float) (meltingPoint * 0.6);
    }

    public float weldingTemperature() {
        return this == DRACONIUM || this == AWAKENED_DRACONIUM ? 1600 : (float) (meltingPoint * 0.8);
    }

}
