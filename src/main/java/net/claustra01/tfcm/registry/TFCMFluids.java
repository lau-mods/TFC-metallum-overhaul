package net.claustra01.tfcm.registry;

import java.util.EnumMap;
import java.util.Map;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.metal.Metal;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TFCMFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(
            net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.FLUID_TYPES, TFCM.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, TFCM.MOD_ID);
    public static final Map<Metal, MetalFluid> METALS = new EnumMap<>(Metal.class);
    public static final DeferredHolder<FluidType, FluidType> MOLTEN_ANDESITE_TYPE = FLUID_TYPES.register(
            "molten_andesite", () -> new FluidType(FluidType.Properties.create().density(3000).viscosity(6000).temperature(1573)));
    public static final DeferredHolder<Fluid, FlowingFluid> MOLTEN_ANDESITE_SOURCE;

    static {
        DeferredHolder<Fluid, FlowingFluid>[] andesiteSource = new DeferredHolder[1];
        DeferredHolder<Fluid, FlowingFluid>[] andesiteFlowing = new DeferredHolder[1];
        BaseFlowingFluid.Properties andesiteProperties = new BaseFlowingFluid.Properties(
                MOLTEN_ANDESITE_TYPE, () -> andesiteSource[0].get(), () -> andesiteFlowing[0].get())
                .bucket(TFCMItems.MOLTEN_ANDESITE_BUCKET).block(TFCMBlocks.MOLTEN_ANDESITE).slopeFindDistance(2).levelDecreasePerBlock(2);
        andesiteSource[0] = FLUIDS.register("molten_andesite", () -> new BaseFlowingFluid.Source(andesiteProperties));
        andesiteFlowing[0] = FLUIDS.register("flowing_molten_andesite", () -> new BaseFlowingFluid.Flowing(andesiteProperties));
        MOLTEN_ANDESITE_SOURCE = andesiteSource[0];

        for (Metal metal : Metal.values()) {
            DeferredHolder<FluidType, FluidType> type = FLUID_TYPES.register("metal/" + metal.id(),
                    () -> new FluidType(FluidType.Properties.create().density(3000).viscosity(6000)
                            .temperature(metal.meltingPoint() + 273)));
            DeferredHolder<Fluid, FlowingFluid>[] source = new DeferredHolder[1];
            DeferredHolder<Fluid, FlowingFluid>[] flowing = new DeferredHolder[1];
            BaseFlowingFluid.Properties properties = new BaseFlowingFluid.Properties(
                    type, () -> source[0].get(), () -> flowing[0].get())
                    .bucket(() -> TFCMItems.METAL_BUCKETS.get(metal).get())
                    .block(() -> TFCMBlocks.METAL_FLUID_BLOCKS.get(metal).get())
                    .slopeFindDistance(2).levelDecreasePerBlock(2);
            source[0] = FLUIDS.register("metal/" + metal.id(), () -> new BaseFlowingFluid.Source(properties));
            flowing[0] = FLUIDS.register("metal/flowing_" + metal.id(), () -> new BaseFlowingFluid.Flowing(properties));
            METALS.put(metal, new MetalFluid(type, source[0]));
        }
    }

    private TFCMFluids() {
    }

    public record MetalFluid(DeferredHolder<FluidType, FluidType> type,
            DeferredHolder<Fluid, FlowingFluid> source) {
    }
}
