package net.claustra01.tfcm.gametest;

import java.util.ArrayList;
import java.util.List;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.tool.ToolMetal;
import net.claustra01.tfcm.common.tool.ToolPart;
import net.claustra01.tfcm.registry.TFCMBlocks;
import net.claustra01.tfcm.registry.TFCMItems;
import net.dries007.tfc.common.blockentities.AnvilBlockEntity;
import net.dries007.tfc.common.blocks.devices.AnvilBlock;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.heat.HeatDefinition;
import net.dries007.tfc.common.component.size.ItemSizeManager;
import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.dries007.tfc.common.recipes.WeldingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TFCM.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TFCMEquipmentGameTests {
    private static final String BATCH = "tfcm_foundation";

    private TFCMEquipmentGameTests() {
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void fourMetalsProvideCompleteTfcEquipment(GameTestHelper helper) {
        helper.succeedIf(() -> {
            int[] levels = {3, 5, 6, 7};
            int[] uses = {2200, 3300, 6500, 8125};
            float[] speeds = {8, 11, 12, 13};
            float[] attacks = {4.75f, 7, 9, 10.5f};
            int[] enchantments = {12, 17, 22, 25};
            for (int index = 0; index < ToolMetal.values().length; index++) {
                ToolMetal metal = ToolMetal.values()[index];
                require(TFCMItems.METAL_ITEMS.get(metal).size() == ToolPart.values().length,
                        "Incomplete equipment for " + metal.getSerializedName());
                require(metal.toolTier().level() == levels[index] && metal.toolTier().getUses() == uses[index]
                        && metal.toolTier().getSpeed() == speeds[index]
                        && metal.toolTier().getAttackDamageBonus() == attacks[index]
                        && metal.toolTier().getEnchantmentValue() == enchantments[index],
                        "Wrong tier values for " + metal.getSerializedName());
                for (ToolPart part : ToolPart.values()) require(TFCMItems.METAL_ITEMS.get(metal).get(part).isBound(),
                        "Missing " + part.id() + " for " + metal.getSerializedName());
                checkArmor(metal);
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void assemblyRequiresSpecifiedPartsAndRepairsWithIngot(GameTestHelper helper) {
        helper.succeedIf(() -> {
            for (ToolMetal metal : ToolMetal.values()) {
                CraftingRecipe pickaxe = (CraftingRecipe) recipe(helper, "crafting/metal/pickaxe/" + metal.getSerializedName());
                CraftingRecipe sword = (CraftingRecipe) recipe(helper, "crafting/metal/sword/" + metal.getSerializedName());
                ItemStack empty = ItemStack.EMPTY;
                List<ItemStack> pickaxeParts = List.of(
                        empty, item("metal/rivet/" + metal.getSerializedName()),
                        TFCMItems.METAL_ITEMS.get(metal).get(ToolPart.PICKAXE_HEAD).toStack(),
                        BuiltInRegistries.ITEM.get(ResourceLocation.parse("tfc:glue")).getDefaultInstance(),
                        net.minecraft.world.item.Items.STICK.getDefaultInstance(), item("metal/ring/" + metal.getSerializedName()),
                        item("metal/ring/" + metal.getSerializedName()), item("leather_strap"), empty);
                List<ItemStack> swordParts = List.of(
                        item("leather_strap"), empty, TFCMItems.METAL_ITEMS.get(metal).get(ToolPart.SWORD_BLADE).toStack(),
                        item("metal/rivet/" + metal.getSerializedName()), item("metal/crossguard/" + metal.getSerializedName()), empty,
                        item("metal/pommel/" + metal.getSerializedName()), item("metal/ring/" + metal.getSerializedName()), item("leather_strap"));
                CraftingInput pickaxeInput = CraftingInput.of(3, 3, pickaxeParts);
                CraftingInput swordInput = CraftingInput.of(3, 3, swordParts);
                require(pickaxe.matches(pickaxeInput, helper.getLevel()),
                        "Pickaxe does not require head, binding, ring, and handle");
                require(sword.matches(swordInput, helper.getLevel()),
                        "Sword does not require blade, crossguard, handle, and pommel");
                requireEachPart(pickaxe, pickaxeParts, helper);
                requireEachPart(sword, swordParts, helper);
                ItemStack stack = pickaxe.assemble(pickaxeInput, helper.getLevel().registryAccess());
                require(stack.is(TFCMItems.METAL_ITEMS.get(metal).get(ToolPart.PICKAXE)),
                        "Pickaxe recipe returned wrong item");
                require(((DiggerItem) stack.getItem()).getTier() == metal.toolTier(), "Pickaxe has wrong tier");
                require(metal.toolTier().getRepairIngredient().test(item("metal/ingot/" + metal.getSerializedName())),
                        "Repair ingot does not match " + metal.getSerializedName());
                ItemStack swordResult = sword.assemble(swordInput, helper.getLevel().registryAccess());
                require(swordResult.is(TFCMItems.METAL_ITEMS.get(metal).get(ToolPart.SWORD)),
                        "Sword recipe returned wrong item");
                require(((TieredItem) swordResult.getItem()).getTier() == metal.toolTier()
                        && metal.toolTier().getRepairIngredient().test(
                                item("metal/ingot/" + metal.getSerializedName())),
                        "Sword has the wrong tier or repair ingredient");
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void equipmentHeatSizeRackAndToolTagsAgree(GameTestHelper helper) {
        helper.succeedIf(() -> {
            TagKey<net.minecraft.world.item.Item> rack = TagKey.create(Registries.ITEM,
                    ResourceLocation.parse("tfc:usable_on_tool_rack"));
            for (ToolMetal metal : ToolMetal.values()) {
                ItemStack ingot = item("metal/ingot/" + metal.getSerializedName());
                for (ToolPart part : ToolPart.values()) {
                    ItemStack stack = TFCMItems.METAL_ITEMS.get(metal).get(part).toStack();
                    require(HeatCapability.getDefinition(stack) != null, "Missing heat for " + part.id());
                    require(ItemSizeManager.getDefinition(stack) != null, "Missing size for " + part.id());
                    if (part.isFinishedTool()) require(stack.is(rack), "Tool rack tag missing for " + part.id());
                }
                ItemStack pickaxe = TFCMItems.METAL_ITEMS.get(metal).get(ToolPart.PICKAXE).toStack();
                pickaxe.setDamageValue(100);
                require(pickaxe.isDamaged() && metal.toolTier().getRepairIngredient().test(ingot),
                        "Damaged pickaxe cannot use its metal ingot for repair");
                require(pickaxe.is(ItemTags.PICKAXES)
                        && ItemSizeManager.getDefinition(pickaxe).size() == Size.VERY_LARGE
                        && ItemSizeManager.getDefinition(pickaxe).weight() == Weight.VERY_HEAVY,
                        "Pickaxe tool tag or TFC storage attributes disagree");
                HeatDefinition heat = HeatCapability.getDefinition(pickaxe);
                require(close(heat.heatCapacity(), metal.metal().specificHeat() * 333.33334f),
                        "Pickaxe heat capacity does not match one ingot");
                require(close(heat.forgingTemperature(), metal.metal().forgingTemperature())
                        && close(heat.weldingTemperature(), metal.metal().weldingTemperature()),
                        "Pickaxe working temperatures do not match its metal");
                HeatCapability.setTemperature(pickaxe, metal.metal().forgingTemperature());
                require(close(HeatCapability.getTemperature(pickaxe), metal.metal().forgingTemperature()),
                        "Pickaxe did not retain its heated state");
                HeatingRecipe pickaxeHeating = (HeatingRecipe) recipe(helper,
                        "heating/metal/pickaxe/" + metal.getSerializedName());
                require(pickaxeHeating.matches(pickaxe) && pickaxeHeating.assembleFluid(pickaxe).getAmount() == (int) (100f * (pickaxe.getMaxDamage() - 100) / pickaxe.getMaxDamage()),
                        "Pickaxe does not melt to its one-ingot metal amount");

                ItemStack chestplate = TFCMItems.METAL_ITEMS.get(metal).get(ToolPart.CHESTPLATE).toStack();
                chestplate.setDamageValue(100);
                require(chestplate.isDamaged() && metal.armorMaterial().value().repairIngredient().get().test(ingot),
                        "Damaged armor cannot use its metal ingot for repair");
                HeatingRecipe armorHeating = (HeatingRecipe) recipe(helper,
                        "heating/metal/chestplate/" + metal.getSerializedName());
                require(armorHeating.matches(chestplate) && armorHeating.assembleFluid(chestplate).getAmount() == (int) (800f * (chestplate.getMaxDamage() - 100) / chestplate.getMaxDamage()),
                        "Chestplate heat and metal amount disagree");
                HeatDefinition armorHeat = HeatCapability.getDefinition(chestplate);
                require(armorHeat != null
                        && close(armorHeat.heatCapacity(), metal.metal().specificHeat() * 2666.6667f)
                        && close(armorHeat.forgingTemperature(), metal.metal().forgingTemperature())
                        && close(armorHeat.weldingTemperature(), metal.metal().weldingTemperature()),
                        "Chestplate heat definition does not match its metal amount");
                require(ItemSizeManager.getDefinition(chestplate).size() == Size.LARGE
                        && ItemSizeManager.getDefinition(chestplate).weight() == Weight.VERY_HEAVY,
                        "Armor TFC storage attributes disagree");
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void tongsExistOnlyWithHotOrNot(GameTestHelper helper) {
        helper.succeedIf(() -> {
            boolean loaded = ModList.get().isLoaded("tfchotornot");
            TagKey<Item> tongsTag = TagKey.create(Registries.ITEM, ResourceLocation.parse("tfchotornot:tongs"));
            for (ToolMetal metal : ToolMetal.values()) {
                require(BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath("tfcm",
                        "metal/tongs/" + metal.getSerializedName())) == loaded, "Wrong tongs registration state");
                require(BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath("tfcm",
                        "metal/tong_part/" + metal.getSerializedName())) == loaded, "Wrong tong part registration state");
                if (loaded) {
                    ItemStack tongs = item("metal/tongs/" + metal.getSerializedName());
                    ItemStack tongPart = item("metal/tong_part/" + metal.getSerializedName());
                    require(tongs.getItem().getClass().getName().equals("tfchotornot.common.items.TongsItem"),
                            "Tongs do not use the Hot or Not public item implementation");
                    require(tongs.is(tongsTag), "Tongs are not integrated with Hot or Not");
                    require(HeatCapability.getDefinition(tongs) != null
                            && HeatCapability.getDefinition(tongPart) != null
                            && ItemSizeManager.getDefinition(tongs) != null
                            && ItemSizeManager.getDefinition(tongPart) != null,
                            "Tongs or tong part is missing TFC heat or storage data");
                }
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void customAnvilsUseTfcUiTierAndSevenDoubleIngots(GameTestHelper helper) {
        helper.succeedIf(() -> {
            for (ToolMetal metal : ToolMetal.values()) {
                AnvilBlock anvil = TFCMBlocks.ANVILS.get(metal).get();
                require(anvil.getTier() == metal.toolTier().level(), "Wrong anvil tier " + metal.getSerializedName());
                CraftingRecipe crafting = (CraftingRecipe) recipe(helper,
                        "crafting/metal/anvil/" + metal.getSerializedName());
                ItemStack doubleIngot = item("metal/double_ingot/" + metal.getSerializedName());
                require(crafting.matches(CraftingInput.of(3, 3, List.of(doubleIngot, doubleIngot, doubleIngot,
                        ItemStack.EMPTY, doubleIngot, ItemStack.EMPTY, doubleIngot, doubleIngot, doubleIngot)),
                        helper.getLevel()), "Anvil does not consume exactly seven double ingots");
                BlockPos position = helper.absolutePos(new BlockPos(metal.ordinal() + 1, 1, 1));
                helper.getLevel().setBlock(position, anvil.defaultBlockState(), 3);
                require(helper.getLevel().getBlockEntity(position) instanceof AnvilBlockEntity,
                        "TFC anvil block entity missing " + metal.getSerializedName());
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void customAnvilsMeltToFourteenHundred(GameTestHelper helper) {
        helper.succeedIf(() -> {
            for (ToolMetal metal : ToolMetal.values()) {
                ItemStack anvil = item("metal/anvil/" + metal.getSerializedName());
                HeatingRecipe heating = (HeatingRecipe) recipe(helper, "heating/metal/anvil/" + metal.getSerializedName());
                require(heating.matches(anvil) && heating.assembleFluid(anvil).getAmount() == 1400
                        && BuiltInRegistries.FLUID.getKey(heating.assembleFluid(anvil).getFluid()).equals(
                                ResourceLocation.fromNamespaceAndPath("tfcm", "metal/" + metal.getSerializedName())),
                        "Wrong anvil melt route " + metal.getSerializedName());
                HeatDefinition heat = HeatCapability.getDefinition(anvil);
                require(heat != null && close(heat.heatCapacity(), metal.metal().specificHeat() * 4666.6665f),
                        "Anvil item heat does not represent 1400 mB " + metal.getSerializedName());
                require(TFCMBlocks.ANVILS.get(metal).get().defaultBlockState().is(
                        TagKey.create(Registries.BLOCK, ResourceLocation.parse("tfc:anvils"))),
                        "Anvil workstation tag missing " + metal.getSerializedName());
                AnvilRecipe forging = (AnvilRecipe) recipe(helper,
                        "anvil/metal/pickaxe_head/" + metal.getSerializedName());
                WeldingRecipe welding = (WeldingRecipe) recipe(helper,
                        "welding/metal/shears/" + metal.getSerializedName());
                require(forging.isCorrectTier(metal.toolTier().level())
                        && welding.isCorrectTier(metal.toolTier().level()),
                        "Anvil does not support its metal's forging and welding recipes");
            }
        });
    }

    private static void requireEachPart(CraftingRecipe recipe, List<ItemStack> parts, GameTestHelper helper) {
        for (int index = 0; index < parts.size(); index++) {
            if (!parts.get(index).isEmpty()) {
                List<ItemStack> missing = new ArrayList<>(parts);
                missing.set(index, ItemStack.EMPTY);
                require(!recipe.matches(CraftingInput.of(3, 3, missing), helper.getLevel()),
                        "Recipe still matches with required part " + index + " removed");
            }
        }
    }

    private static boolean close(float first, float second) {
        return Math.abs(first - second) < .001f;
    }

    private static void checkArmor(ToolMetal metal) {
        int index = metal.ordinal();
        int[][] defense = {{1,4,5,2}, {2,5,6,2}, {3,6,8,3}, {4,7,9,4}};
        int[][] durability = {{429,495,528,370}, {650,750,800,550}, {884,1020,1010,715}, {1105,1275,1360,935}};
        int[] enchantments = {12, 17, 23, 25};
        float[] toughness = {0, 2, 3, 4};
        float[] knockbackResistance = {0, .05f, .1f, .15f};
        require(metal.armorMaterial().value().enchantmentValue() == enchantments[index]
                && close(metal.armorMaterial().value().toughness(), toughness[index])
                && close(metal.armorMaterial().value().knockbackResistance(), knockbackResistance[index]),
                "Wrong armor material values for " + metal.getSerializedName());
        List<ToolPart> parts = List.of(ToolPart.BOOTS, ToolPart.GREAVES, ToolPart.CHESTPLATE, ToolPart.HELMET);
        List<ArmorItem.Type> types = List.of(ArmorItem.Type.BOOTS, ArmorItem.Type.LEGGINGS,
                ArmorItem.Type.CHESTPLATE, ArmorItem.Type.HELMET);
        for (int slot = 0; slot < parts.size(); slot++) {
            ArmorItem armor = (ArmorItem) TFCMItems.METAL_ITEMS.get(metal).get(parts.get(slot)).get();
            require(armor.getDefense() == defense[index][slot], "Wrong armor defense " + parts.get(slot).id());
            require(armor.getDefaultInstance().getMaxDamage() == durability[index][slot],
                    "Wrong armor durability " + parts.get(slot).id());
            require(armor.getType() == types.get(slot), "Wrong armor slot " + parts.get(slot).id());
        }
    }

    private static Recipe<?> recipe(GameTestHelper helper, String id) {
        return helper.getLevel().getServer().getRecipeManager().byKey(
                ResourceLocation.fromNamespaceAndPath("tfcm", id)).orElseThrow().value();
    }

    private static ItemStack item(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("tfcm", id)).getDefaultInstance();
    }

    private static void require(boolean value, String message) {
        if (!value) throw new GameTestAssertException(message);
    }
}
