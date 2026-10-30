package net.claustra01.tfcm.common.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.config.TFCMConfig;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public record OreEnabledCondition(String ore) implements ICondition {
    public static final MapCodec<OreEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            com.mojang.serialization.Codec.STRING.fieldOf("ore").forGetter(OreEnabledCondition::ore))
            .apply(instance, OreEnabledCondition::new));
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS = DeferredRegister.create(
            NeoForgeRegistries.Keys.CONDITION_CODECS, TFCM.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<OreEnabledCondition>> TYPE =
            CONDITIONS.register("ore_enabled", () -> CODEC);

    @Override
    public boolean test(IContext context) {
        return TFCMConfig.isOreEnabled(ore);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
