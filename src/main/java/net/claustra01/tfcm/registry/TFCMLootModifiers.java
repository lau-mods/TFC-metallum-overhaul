package net.claustra01.tfcm.registry;

import com.mojang.serialization.MapCodec;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.material.GrassPlantFiberLootModifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class TFCMLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, TFCM.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<GrassPlantFiberLootModifier>>
            GRASS_PLANT_FIBER = LOOT_MODIFIERS.register("grass_plant_fiber", () -> GrassPlantFiberLootModifier.CODEC);

    private TFCMLootModifiers() {
    }
}
