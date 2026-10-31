package net.claustra01.tfcm;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import net.claustra01.tfcm.common.condition.MetalEnabledCondition;
import net.claustra01.tfcm.common.condition.OreEnabledCondition;
import net.claustra01.tfcm.common.processing.ProcessingForm;
import net.claustra01.tfcm.common.processing.ProcessingSeries;
import net.claustra01.tfcm.common.processing.SluiceRecipeSync;
import net.claustra01.tfcm.config.TFCMConfig;
import net.claustra01.tfcm.compat.firmalife.FirmaLifeCompat;
import net.claustra01.tfcm.compat.hotornot.HotOrNotCompat;
import net.claustra01.tfcm.datagen.TFCMDataGenerators;
import net.claustra01.tfcm.registry.TFCMArmorMaterials;
import net.claustra01.tfcm.registry.TFCMBiomeModifiers;
import net.claustra01.tfcm.registry.TFCMBlocks;
import net.claustra01.tfcm.registry.TFCMFluids;
import net.claustra01.tfcm.registry.TFCMItems;
import net.claustra01.tfcm.registry.TFCMLootModifiers;
import net.claustra01.tfcm.worldgen.YamlWorldgenLoader;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.capabilities.ItemCapabilities;
import net.dries007.tfc.util.data.Deposit;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(TFCM.MOD_ID)
public final class TFCM {
    public static final String MOD_ID = "tfcm";

    public TFCM(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, TFCMConfig.SPEC);
        TFCMConfig.loadDisabledContent();
        YamlWorldgenLoader.installDefaults();
        TFCMBiomeModifiers.SERIALIZERS.register(modBus);
        TFCMArmorMaterials.ARMOR_MATERIALS.register(modBus);
        if (ModList.get().isLoaded("firmalife")) {
            FirmaLifeCompat.register();
        }
        if (ModList.get().isLoaded("tfchotornot")) {
            HotOrNotCompat.register(modBus);
        }
        TFCMItems.ITEMS.register(modBus);
        var creativeTabs = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);
        creativeTabs.register("items", () -> CreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.tfcm"))
                        .icon(() -> new ItemStack(TFCMItems.CERTUS_QUARTZ.get()))
                        .displayItems((parameters, output) -> BuiltInRegistries.ITEM.stream()
                                .filter(item -> BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(MOD_ID))
                                .forEach(output::accept))
                        .build());
        creativeTabs.register(modBus);
        TFCMBlocks.BLOCKS.register(modBus);
        TFCMFluids.FLUID_TYPES.register(modBus);
        TFCMFluids.FLUIDS.register(modBus);
        TFCMLootModifiers.LOOT_MODIFIERS.register(modBus);
        MetalEnabledCondition.CONDITIONS.register(modBus);
        OreEnabledCondition.CONDITIONS.register(modBus);
        modBus.addListener(TFCMDataGenerators::gatherData);
        modBus.addListener(TFCM::addAnvilBlocks);
        modBus.addListener(TFCM::registerCapabilities);
        modBus.addListener(SluiceRecipeSync::registerPayloads);
        NeoForge.EVENT_BUS.addListener(SluiceRecipeSync::onDatapackSync);
        NeoForge.EVENT_BUS.addListener(TFCM::addReloadListeners);
    }

    private static void addAnvilBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(TFCBlockEntities.ANVIL.get(), TFCMBlocks.ANVILS.values().stream()
                .map(Supplier::get).toArray(Block[]::new));
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        var molds = TFCMItems.AREA_TOOL_MOLDS.values().stream()
                .map(Supplier::get).toArray(ItemLike[]::new);
        event.registerItem(ItemCapabilities.MOLD, ItemCapabilities::forMold, molds);
        event.registerItem(ItemCapabilities.HEAT, ItemCapabilities::forMold, molds);
        event.registerItem(ItemCapabilities.FLUID, ItemCapabilities::forMold, molds);
    }

    private static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener((ResourceManagerReloadListener) resourceManager -> {
            var deposits = new HashMap<>(Deposit.MANAGER.getElements());
            // Chromite's items only exist with FirmaLife, and TFC's Deposit data ignores load conditions
            if (ModList.get().isLoaded("firmalife") && TFCMConfig.isOreEnabled("chromite")) {
                for (ProcessingForm form : List.of(ProcessingForm.ROCKY_CHUNKS, ProcessingForm.DIRTY_DUST)) {
                    String name = form.id() + "/chromite";
                    deposits.put(ResourceLocation.fromNamespaceAndPath(MOD_ID, "ore_processing/" + name), new Deposit(
                            Ingredient.of(TFCMItems.ORE_PROCESSING.get(ProcessingSeries.CHROMITE).get(form).get()),
                            ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "ore_processing/" + name)),
                            List.of(ResourceLocation.fromNamespaceAndPath(MOD_ID, "item/pan/" + name + "_full"),
                                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "item/pan/" + name + "_half"),
                                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "item/pan/empty"))));
                }
            }
            for (String ore : TFCMConfig.oreIds()) {
                if (TFCMConfig.isOreEnabled(ore)) continue;
                deposits.remove(ResourceLocation.fromNamespaceAndPath(MOD_ID,
                        "ore_processing/rocky_chunks/" + ore));
                deposits.remove(ResourceLocation.fromNamespaceAndPath(MOD_ID,
                        "ore_processing/dirty_dust/" + ore));
            }
            Deposit.MANAGER.bindValues(deposits);
            Deposit.CACHE.reload(deposits.values());
        });
    }
}
