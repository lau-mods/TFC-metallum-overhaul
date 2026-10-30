package net.claustra01.tfcm.registry;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.tool.ToolMetal;
import net.claustra01.tfcm.config.TFCMConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TFCMArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, TFCM.MOD_ID);
    public static final Map<ToolMetal, DeferredHolder<ArmorMaterial, ArmorMaterial>> MATERIALS =
            new EnumMap<>(ToolMetal.class);

    static {
        register(ToolMetal.INVAR, 1, 4, 5, 2, 12, 0, 0);
        register(ToolMetal.TITANIUM, 2, 5, 6, 2, 17, 2, .05f);
        register(ToolMetal.NETHERITE, 3, 6, 8, 3, 23, 3, .1f);
        register(ToolMetal.TUNGSTEN_STEEL, 4, 7, 9, 4, 25, 4, .15f);
    }

    private TFCMArmorMaterials() {
    }

    private static void register(ToolMetal metal, int boots, int leggings, int chestplate, int helmet,
            int enchantment, float toughness, float knockbackResistance) {
        if (!TFCMConfig.isMetalEnabled(metal.metal())) return;
        MATERIALS.put(metal, ARMOR_MATERIALS.register(metal.getSerializedName(), () -> {
            Map<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
            defense.put(ArmorItem.Type.BOOTS, boots);
            defense.put(ArmorItem.Type.LEGGINGS, leggings);
            defense.put(ArmorItem.Type.CHESTPLATE, chestplate);
            defense.put(ArmorItem.Type.HELMET, helmet);
            defense.put(ArmorItem.Type.BODY, chestplate);
            return new ArmorMaterial(defense, enchantment, SoundEvents.ARMOR_EQUIP_IRON,
                    metal::repairIngredient,
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath("tfc", switch (metal) {
                        case INVAR -> "wrought_iron";
                        case TITANIUM -> "black_steel";
                        case NETHERITE -> "red_steel";
                        case TUNGSTEN_STEEL -> "blue_steel";
                    }))), toughness, knockbackResistance);
        }));
    }
}
