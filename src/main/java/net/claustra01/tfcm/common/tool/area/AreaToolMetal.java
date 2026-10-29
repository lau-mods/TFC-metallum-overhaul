package net.claustra01.tfcm.common.tool.area;

import java.util.Locale;
import java.util.function.Supplier;

import net.claustra01.tfcm.common.tool.ToolMetal;
import net.dries007.tfc.util.registry.RegistryMetal;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public enum AreaToolMetal {
    COPPER(() -> net.dries007.tfc.util.Metal.COPPER, 1, 1080, 648, 864, 2.857143f, false),
    BISMUTH_BRONZE(() -> net.dries007.tfc.util.Metal.BISMUTH_BRONZE, 2, 985, 591, 788, 2.857143f, false),
    BLACK_BRONZE(() -> net.dries007.tfc.util.Metal.BLACK_BRONZE, 2, 1070, 642, 856, 2.857143f, false),
    BRONZE(() -> net.dries007.tfc.util.Metal.BRONZE, 2, 950, 570, 760, 2.857143f, false),
    WROUGHT_IRON(() -> net.dries007.tfc.util.Metal.WROUGHT_IRON, 3, 1535, 921.00006f, 1228, 2.857143f, false),
    STEEL(() -> net.dries007.tfc.util.Metal.STEEL, 4, 1540, 924.00006f, 1232, 2.857143f, false),
    BLACK_STEEL(() -> net.dries007.tfc.util.Metal.BLACK_STEEL, 5, 1485, 891.00006f, 1188, 2.857143f, false),
    BLUE_STEEL(() -> net.dries007.tfc.util.Metal.BLUE_STEEL, 6, 1540, 924.00006f, 1232, 2.857143f, false),
    RED_STEEL(() -> net.dries007.tfc.util.Metal.RED_STEEL, 6, 1540, 924.00006f, 1232, 2.857143f, false),
    INVAR(() -> ToolMetal.INVAR, 3, 1250, 750, 1000, 3.3333335f, true),
    TITANIUM(() -> ToolMetal.TITANIUM, 5, 1700, 1020, 1360, 3.3333335f, true),
    NETHERITE(() -> ToolMetal.NETHERITE, 6, 1535, 921, 1228, 5, true),
    TUNGSTEN_STEEL(() -> ToolMetal.TUNGSTEN_STEEL, 7, 1535, 921, 1228, 5, true);

    private final Supplier<RegistryMetal> metal;
    private final int tierLevel;
    private final float meltingPoint;
    private final float forgingTemperature;
    private final float weldingTemperature;
    private final float heatCapacity;
    private final boolean tfcm;

    AreaToolMetal(Supplier<RegistryMetal> metal, int tierLevel, float meltingPoint, float forgingTemperature,
            float weldingTemperature, float heatCapacity, boolean tfcm) {
        this.metal = metal;
        this.tierLevel = tierLevel;
        this.meltingPoint = meltingPoint;
        this.forgingTemperature = forgingTemperature;
        this.weldingTemperature = weldingTemperature;
        this.heatCapacity = heatCapacity;
        this.tfcm = tfcm;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public RegistryMetal metal() {
        return metal.get();
    }

    public Tier tier() {
        return metal().toolTier();
    }

    public Ingredient repairIngredient() {
        return Ingredient.of(TagKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath("c", "ingots/" + id())));
    }

    public boolean isTFCM() {
        return tfcm;
    }

    public String namespace() {
        return isTFCM() ? "tfcm" : "tfc";
    }

    public int tierLevel() {
        return tierLevel;
    }

    public float meltingPoint() {
        return meltingPoint;
    }

    public float forgingTemperature() {
        return forgingTemperature;
    }

    public float weldingTemperature() {
        return weldingTemperature;
    }

    public float heatCapacity() {
        return heatCapacity;
    }

    public boolean isCastable() {
        return this == COPPER || this == BISMUTH_BRONZE || this == BLACK_BRONZE || this == BRONZE;
    }
}
