package net.claustra01.tfcm.common.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Locale;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.metal.Metal;
import net.claustra01.tfcm.config.TFCMConfig;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public record MetalEnabledCondition(Metal metal) implements ICondition {
    public static final MapCodec<MetalEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            com.mojang.serialization.Codec.STRING.xmap(value -> Metal.valueOf(value.toUpperCase(Locale.ROOT)), Metal::id)
                    .fieldOf("metal").forGetter(MetalEnabledCondition::metal)).apply(instance, MetalEnabledCondition::new));
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS = DeferredRegister.create(
            NeoForgeRegistries.Keys.CONDITION_CODECS, TFCM.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<MetalEnabledCondition>> TYPE =
            CONDITIONS.register("metal_enabled", () -> CODEC);

    @Override
    public boolean test(IContext context) {
        return TFCMConfig.isMetalEnabled(metal);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
