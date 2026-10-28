package net.claustra01.tfcm.common.metal;

import java.util.Locale;

public enum Metal {
    COMPRESSED_IRON(3, 1535, false, 0xFF777777, 0.008571429f),
    PLATINUM(3, 1730, false, 0xFFE5E4E2, 0.008571429f),
    NAQUADAH(7, 2000, false, 0xFF31383A, 0.008571429f),
    IRIDIUM(3, 1535, false, 0xFFD8D8E8, 0.008571429f),
    OSMIUM(3, 1535, false, 0xFFA7C6DA, 0.008571429f),
    OSMIRIDIUM(3, 1500, false, 0xFFB7BCC7, 0.008571429f),
    MITHRIL(2, 940, false, 0xFF8FD8E8, 0.008571429f),
    ARCANE(2, 940, false, 0xFF9056C7, 0.008571429f),
    REFINED_GLOWSTONE(2, 940, false, 0xFFFFD83D, 0.008571429f),
    REFINED_OBSIDIAN(2, 940, false, 0xFF553A73, 0.008571429f),
    ANTIMONY(1, 630, false, 0xFFB9BAC4, 0.012f),
    TITANIUM(5, 1700, true, 0xFFC7CBD1, 0.01f),
    COBALT(3, 1700, false, 0xFF3157C8, 0.01f),
    LITHIUM(3, 181, false, 0xFFD4D6D8, 0.01f),
    ALUMINUM(3, 1740, false, 0xFFD9E0E3, 0.01f),
    CONSTANTAN(1, 1250, false, 0xFFC98955, 0.01f),
    INVAR(3, 1250, true, 0xFFB7B18B, 0.01f),
    ELECTRUM(1, 1000, false, 0xFFFFD34E, 0.01f),
    LEAD(1, 330, false, 0xFF62677B, 0.01f),
    URANIUM(3, 1130, false, 0xFF6FA641, 0.01f),
    TUNGSTEN(3, 1535, false, 0xFF555D63, 0.015f),
    SOLDER(3, 400, false, 0xFFBFC8C8, 0.008571429f),
    TUNGSTEN_STEEL(7, 1535, true, 0xFF434D56, 0.015f),
    NETHERITE(6, 1535, true, 0xFF4B3A3A, 0.015f),
    DAWNSTONE(1, 1000, false, 0xFFE88D42, 0.01f),
    ANDESITE_ALLOY(2, 520, false, 0xFF81837B, 0.01f),
    DRACONIUM(7, 2000, false, 0xFF7950C9, 0.01f),
    AWAKENED_DRACONIUM(7, 2000, false, 0xFFFF8A23, 0.01f),
    BORON(3, 2076, false, 0xFF5B6469, 0.01f),
    THORIUM(3, 1750, false, 0xFF475548, 0.01f),
    MAGNESIUM(2, 650, false, 0xFFE2E5E1, 0.01f),
    BERYLLIUM(3, 1287, false, 0xFF9DCB91, 0.01f),
    ZIRCONIUM(2, 1855, false, 0xFFD0D6D4, 0.01f),
    NEUTRONIUM(7, 3000, false, 0xFF202225, 0.01f),
    FERROBORON(5, 1500, false, 0xFF4A5055, 0.01f),
    TOUGH_ALLOY(5, 1600, false, 0xFF625C68, 0.01f),
    ZIRCALOY(5, 1855, false, 0xFFABB5B4, 0.01f),
    HSLA_STEEL(5, 1450, false, 0xFF657078, 0.01f),
    SUPER_ALLOY(5, 1800, false, 0xFF7B7182, 0.01f);

    private final String id;
    private final int forgingTier;
    private final int meltingPoint;
    private final boolean providesTools;
    private final int color;
    private final float specificHeat;

    Metal(int forgingTier, int meltingPoint, boolean providesTools, int color, float specificHeat) {
        this.id = name().toLowerCase(Locale.ROOT);
        this.forgingTier = forgingTier;
        this.meltingPoint = meltingPoint;
        this.providesTools = providesTools;
        this.color = color;
        this.specificHeat = specificHeat;
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

    public float specificHeat() {
        return specificHeat;
    }

    public float forgingTemperature() {
        return this == DRACONIUM || this == AWAKENED_DRACONIUM ? 1200 : (float) (meltingPoint * 0.6);
    }

    public float weldingTemperature() {
        return this == DRACONIUM || this == AWAKENED_DRACONIUM ? 1600 : (float) (meltingPoint * 0.8);
    }

}
