package net.claustra01.tfcm.client;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.metal.Metal;
import net.claustra01.tfcm.registry.TFCMBlocks;
import net.claustra01.tfcm.registry.TFCMFluids;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = TFCM.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class TFCMClient {
    private static final ResourceLocation STILL = ResourceLocation.fromNamespaceAndPath(TFCM.MOD_ID, "block/molten_still");
    private static final ResourceLocation FLOW = ResourceLocation.fromNamespaceAndPath(TFCM.MOD_ID, "block/molten_flow");

    private TFCMClient() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        TFCMBlocks.ORES.values().forEach(blocks -> blocks.values().forEach(
                block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout())));
        TFCMBlocks.SMALL_ORES.values().forEach(
                block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout()));
        TFCMBlocks.ORE_GROUNDCOVER.values().forEach(
                block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout()));
        TFCMBlocks.TFC_ORES.values().forEach(
                block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout()));
        TFCMBlocks.CHROMITE_ORES.values().forEach(
                block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout()));
    }

    @SubscribeEvent
    public static void registerFluidExtensions(RegisterClientExtensionsEvent event) {
        for (Metal metal : Metal.values()) {
            event.registerFluidType(extension(metal.color()), TFCMFluids.METALS.get(metal).type().get());
        }
        event.registerFluidType(extension(0xFF77736B), TFCMFluids.MOLTEN_ANDESITE_TYPE.get());
    }

    private static IClientFluidTypeExtensions extension(int color) {
        return new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return FLOW;
            }

            @Override
            public int getTintColor() {
                return color;
            }
        };
    }
}
