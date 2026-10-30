package net.claustra01.tfcm;

import java.util.HashMap;
import java.util.function.Supplier;

import net.claustra01.tfcm.common.condition.MetalEnabledCondition;
import net.claustra01.tfcm.common.condition.OreEnabledCondition;
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
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.capabilities.ItemCapabilities;
import net.dries007.tfc.util.data.Deposit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
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

@Mod(TFCM.MOD_ID)
public final class TFCM {
    public static final String MOD_ID = "tfcm";

    public TFCM(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, TFCMConfig.SPEC);
        TFCMConfig.loadDisabledContent();
        TFCMBiomeModifiers.SERIALIZERS.register(modBus);
        TFCMArmorMaterials.ARMOR_MATERIALS.register(modBus);
        if (ModList.get().isLoaded("firmalife")) {
            FirmaLifeCompat.register();
        }
        if (ModList.get().isLoaded("tfchotornot")) {
            HotOrNotCompat.register(modBus);
        }
        TFCMItems.ITEMS.register(modBus);
        TFCMBlocks.BLOCKS.register(modBus);
        TFCMFluids.FLUID_TYPES.register(modBus);
        TFCMFluids.FLUIDS.register(modBus);
        TFCMLootModifiers.LOOT_MODIFIERS.register(modBus);
        MetalEnabledCondition.CONDITIONS.register(modBus);
        OreEnabledCondition.CONDITIONS.register(modBus);
        modBus.addListener(TFCMDataGenerators::gatherData);
        modBus.addListener(TFCM::addAnvilBlocks);
        modBus.addListener(TFCM::registerCapabilities);
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
            if (!ModList.get().isLoaded("firmalife")) {
                deposits.remove(ResourceLocation.fromNamespaceAndPath(MOD_ID,
                        "ore_processing/rocky_chunks/chromite"));
                deposits.remove(ResourceLocation.fromNamespaceAndPath(MOD_ID,
                        "ore_processing/dirty_dust/chromite"));
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
