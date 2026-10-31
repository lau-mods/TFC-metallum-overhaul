package net.claustra01.tfcm.registry;

import java.util.EnumMap;
import java.util.Map;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.metal.Metal;
import net.claustra01.tfcm.config.TFCMConfig;
import net.dries007.tfc.common.fluids.FluidHolder;
import net.dries007.tfc.common.fluids.MoltenFluid;
import net.dries007.tfc.util.registry.RegistrationHelpers;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TFCMFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(
            net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.FLUID_TYPES, TFCM.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, TFCM.MOD_ID);
    public static final Map<Metal, FluidHolder<MoltenFluid>> METALS = new EnumMap<>(Metal.class);
    public static final FluidHolder<MoltenFluid> MOLTEN_ANDESITE = RegistrationHelpers.registerFluid(
            FLUID_TYPES, FLUIDS, "molten_andesite", "molten_andesite", "flowing_molten_andesite",
            properties -> properties.bucket(TFCMItems.MOLTEN_ANDESITE_BUCKET)
                    .block(TFCMBlocks.MOLTEN_ANDESITE).explosionResistance(100f),
            () -> new FluidType(moltenProperties()), MoltenFluid.Source::new, MoltenFluid.Flowing::new);

    static {
        for (Metal metal : Metal.values()) {
            if (!TFCMConfig.isMetalEnabled(metal)) continue;
            String name = "metal/" + metal.id();
            METALS.put(metal, RegistrationHelpers.registerFluid(
                    FLUID_TYPES, FLUIDS, name, name, "metal/flowing_" + metal.id(),
                    properties -> properties.bucket(() -> TFCMItems.METAL_BUCKETS.get(metal).get())
                            .block(() -> TFCMBlocks.METAL_FLUID_BLOCKS.get(metal).get()),
                    () -> new FluidType(moltenProperties().rarity(metal.rarity())),
                    MoltenFluid.Source::new, MoltenFluid.Flowing::new));
        }
    }

    private TFCMFluids() {
    }

    private static FluidType.Properties moltenProperties() {
        return FluidType.Properties.create()
                .adjacentPathType(PathType.LAVA)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                .lightLevel(15).density(3000).viscosity(6000).temperature(1300)
                .canConvertToSource(false).canDrown(false).canExtinguish(false)
                .canHydrate(false).canPushEntity(false).canSwim(false).supportsBoating(false);
    }
}
