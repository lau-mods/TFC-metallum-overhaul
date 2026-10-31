package net.claustra01.tfcm.client;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.registry.TFCMBlocks;
import net.claustra01.tfcm.registry.TFCMItems;
import net.claustra01.tfcm.common.tool.ToolPart;
import net.dries007.tfc.client.render.entity.ThrownJavelinRenderer;
import net.dries007.tfc.common.items.TFCFishingRodItem;
import net.dries007.tfc.common.items.JavelinItem;
import net.dries007.tfc.client.extensions.ItemRendererExtension;
import net.dries007.tfc.client.render.blockentity.JavelinItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Monster;
import net.claustra01.tfcm.registry.TFCMFluids;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.dries007.tfc.client.extensions.FluidRendererExtension;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;
import net.neoforged.neoforge.registries.DeferredItem;

@EventBusSubscriber(modid = TFCM.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class TFCMClient {
    private static final ResourceLocation STILL = ResourceLocation.fromNamespaceAndPath("tfc", "block/molten_still");
    private static final ResourceLocation FLOW = ResourceLocation.fromNamespaceAndPath("tfc", "block/molten_flow");

    private TFCMClient() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> TFCMItems.METAL_ITEMS.forEach((metal, parts) -> {
            ItemProperties.register(parts.get(ToolPart.FISHING_ROD).get(), ResourceLocation.parse("tfc:cast"),
                    (stack, level, entity, seed) -> entity instanceof Player player
                            && TFCFishingRodItem.isThisTheHeldRod(player, stack) && player.fishing != null ? 1 : 0);
            ItemProperties.register(parts.get(ToolPart.SHIELD).get(), ResourceLocation.parse("minecraft:blocking"),
                    (stack, level, entity, seed) -> entity instanceof Player && entity.isUsingItem()
                            && entity.getUseItem() == stack ? 1 : 0);
            ItemProperties.register(parts.get(ToolPart.JAVELIN).get(), ResourceLocation.parse("tfc:throwing"),
                    (stack, level, entity, seed) -> entity != null && (entity.isUsingItem()
                            && entity.getUseItem() == stack || entity instanceof Monster monster && monster.isAggressive()) ? 1 : 0);
            ThrownJavelinRenderer.JAVELIN_TEXTURES.put(parts.get(ToolPart.JAVELIN).get(),
                    ResourceLocation.fromNamespaceAndPath(TFCM.MOD_ID,
                            "textures/entity/projectiles/" + metal.getSerializedName() + "_javelin.png"));
        }));
        TFCMBlocks.ORES.values().forEach(blocks -> blocks.values().forEach(
                block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout())));
        TFCMBlocks.SMALL_ORES.values().forEach(
                block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout()));
        TFCMBlocks.ORE_GROUNDCOVER.values().forEach(
                block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout()));
        TFCMBlocks.TFC_ORE_GROUNDCOVER.values().forEach(
                block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout()));
        TFCMBlocks.TFC_ORES.values().forEach(
                block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout()));
        TFCMBlocks.CHROMITE_ORES.values().forEach(
                block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout()));
    }

    // apply_tint in the bucket model needs this handler to color the fluid layer
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(new DynamicFluidContainerModel.Colors(), TFCMItems.METAL_BUCKETS.values().stream()
                .map(DeferredItem::get).toArray(ItemLike[]::new));
        event.register(new DynamicFluidContainerModel.Colors(), TFCMItems.MOLTEN_ANDESITE_BUCKET.get());
    }

    // TFC only registers the Pan models of its own deposits
    @SubscribeEvent
    public static void registerPanModels(ModelEvent.RegisterAdditional event) {
        Minecraft.getInstance().getResourceManager().listResources("models/item/pan",
                file -> file.getNamespace().equals(TFCM.MOD_ID) && file.getPath().endsWith(".json")).keySet()
                .forEach(file -> event.register(ModelResourceLocation.standalone(file.withPath(path -> path
                        .substring("models/".length(), path.length() - ".json".length())))));
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        TFCMItems.METAL_ITEMS.values().forEach(parts -> {
            JavelinItem javelin = (JavelinItem) parts.get(ToolPart.JAVELIN).get();
            event.registerItem(ItemRendererExtension.cached(() -> new JavelinItemRenderer(javelin)), javelin);
        });
        TFCMFluids.METALS.forEach((metal, fluid) -> event.registerFluidType(
                new FluidRendererExtension(metal.color(), STILL, FLOW, null, null), fluid.getType()));
        event.registerFluidType(new FluidRendererExtension(0xFF74736C, STILL, FLOW, null, null),
                TFCMFluids.MOLTEN_ANDESITE.getType());
    }
}
