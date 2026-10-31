package net.claustra01.tfcm.registry;

import com.mojang.serialization.MapCodec;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.worldgen.CustomWorldgenBiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class TFCMBiomeModifiers {
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, TFCM.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<CustomWorldgenBiomeModifier>>
            CUSTOM_WORLDGEN = SERIALIZERS.register("custom_worldgen", () -> CustomWorldgenBiomeModifier.CODEC);

    private TFCMBiomeModifiers() {
    }
}
