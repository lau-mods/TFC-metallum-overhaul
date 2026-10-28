package net.claustra01.tfcm.gametest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonParser;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.metal.Metal;
import net.claustra01.tfcm.common.metal.MetalForm;
import net.claustra01.tfcm.registry.TFCMBlocks;
import net.claustra01.tfcm.registry.TFCMFluids;
import net.claustra01.tfcm.registry.TFCMItems;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.heat.HeatDefinition;
import net.dries007.tfc.common.component.fluid.FluidContainerInfo;
import net.dries007.tfc.common.component.mold.IMold;
import net.dries007.tfc.common.component.size.ItemSizeManager;
import net.dries007.tfc.common.recipes.AlloyRecipe;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.dries007.tfc.common.recipes.CastingRecipe;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.dries007.tfc.common.recipes.QuernRecipe;
import net.dries007.tfc.common.recipes.WeldingRecipe;
import net.dries007.tfc.util.FluidAlloy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TFCM.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TFCMMetalGameTests {
    private static final String BATCH = "tfcm_foundation";
    private static final List<MetalForm> EXTENDED = List.of(MetalForm.FOIL, MetalForm.GEAR,
            MetalForm.HEAVY_SHEET, MetalForm.NAIL, MetalForm.RING, MetalForm.RIVET,
            MetalForm.SCREW, MetalForm.STAMEN, MetalForm.WIRE);

    private TFCMMetalGameTests() {
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void allExtendedFormsAreRegistered(GameTestHelper helper) {
        helper.succeedIf(() -> {
            for (Metal metal : Metal.values()) {
                for (MetalForm metalForm : EXTENDED) {
                    String form = metalForm.id;
                    require(item("metal/" + form + "/" + metal.id()), "Missing " + form + " for " + metal.id());
                }
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void extendedFormsHaveWorkingAndHeating(GameTestHelper helper) {
        helper.succeedIf(() -> {
            for (Metal metal : Metal.values()) {
                for (MetalForm metalForm : EXTENDED) {
                    if (metalForm == MetalForm.HEAVY_SHEET) continue;
                    String form = metalForm.id;
                    AnvilRecipe anvil = (AnvilRecipe) recipe(helper, "anvil/metal/" + form + "/" + metal.id());
                    require(anvil.getMinTier() == metal.forgingTier(), "Wrong anvil tier for " + form + " " + metal.id());
                    String input = metalForm.anvilInput().id;
                    require(anvil.matches(anvilInput(itemValue("metal/" + input + "/" + metal.id()).getDefaultInstance(),
                            metal.forgingTier()), helper.getLevel()), "Wrong anvil input for " + form + " " + metal.id());
                    List<String> rules = metalForm.anvilRules();
                    require(anvil.getRules().stream().map(rule -> rule.getSerializedName()).toList().equals(rules),
                            "Wrong forge rules for " + form + " " + metal.id());
                    int count = metalForm.anvilCount();
                    ItemStack output = anvil.getResultItem(helper.getLevel().registryAccess());
                    require(output.is(itemValue("metal/" + form + "/" + metal.id())) && output.getCount() == count,
                            "Wrong anvil output for " + form + " " + metal.id());
                }
                WeldingRecipe heavy = (WeldingRecipe) recipe(helper, "welding/metal/heavy_sheet/" + metal.id());
                require(heavy.matches(weldingInput(itemValue("metal/double_sheet/" + metal.id()).getDefaultInstance(),
                        itemValue("metal/sheet/" + metal.id()).getDefaultInstance(), metal.forgingTier())),
                        "Heavy sheet welding does not match " + metal.id());
                require(!heavy.matches(weldingInput(itemValue("metal/double_sheet/" + metal.id()).getDefaultInstance(),
                        itemValue("metal/double_sheet/" + metal.id()).getDefaultInstance(), metal.forgingTier())),
                        "Heavy sheet welding accepts two double sheets " + metal.id());
                for (MetalForm metalForm : EXTENDED) {
                    String form = metalForm.id;
                    ItemStack stack = itemValue("metal/" + form + "/" + metal.id()).getDefaultInstance();
                    HeatingRecipe heat = (HeatingRecipe) recipe(helper, "heating/metal/" + form + "/" + metal.id());
                    int amount = metalForm.amount;
                    require(heat.matches(stack) && heat.getTemperature() == metal.meltingPoint()
                            && heat.assembleFluid(stack).getFluid() == TFCMFluids.METALS.get(metal).source().get()
                            && heat.assembleFluid(stack).getAmount() == amount,
                            "Missing melting route for " + form + " " + metal.id());
                    require(HeatCapability.getDefinition(stack) != null, "Missing heat for " + form + " " + metal.id());
                    require(ItemSizeManager.getDefinition(stack) != null, "Missing size for " + form + " " + metal.id());
                    String tag = metalForm.tag();
                    require(stack.is(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", tag + "/" + metal.id()))),
                            "Missing common tag for " + form + " " + metal.id());
                    checkModelTexture(form, metal);
                }
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void platesUseAnvilAndMeltToOneIngot(GameTestHelper helper) {
        helper.succeedIf(() -> {
            AnvilRecipe tfcSheet = (AnvilRecipe) helper.getLevel().getServer().getRecipeManager()
                    .byKey(ResourceLocation.parse("tfc:anvil/metal/sheet/copper")).orElseThrow().value();
            for (Metal metal : Metal.values()) {
                AnvilRecipe anvil = (AnvilRecipe) recipe(helper, "anvil/metal/plate/" + metal.id());
                ItemStack ingot = itemValue("metal/ingot/" + metal.id()).getDefaultInstance();
                require(anvil.matches(anvilInput(ingot, metal.forgingTier()), helper.getLevel()), "Plate anvil input fails " + metal.id());
                require(anvil.getRules().equals(tfcSheet.getRules()), "Plate does not use TFC metal forming rules " + metal.id());
                require(anvil.isCorrectTier(metal.forgingTier()), "Plate rejects correct tier " + metal.id());
                require(!anvil.isCorrectTier(metal.forgingTier() - 1), "Plate accepts lower tier " + metal.id());
                ItemStack plate = anvil.getResultItem(helper.getLevel().registryAccess());
                require(plate.is(itemValue("metal/plate/" + metal.id())) && plate.getCount() == 1,
                        "Wrong plate result " + metal.id());
                require(!plate.is(itemValue("metal/sheet/" + metal.id())), "Plate aliases sheet " + metal.id());
                HeatingRecipe heating = (HeatingRecipe) recipe(helper, "heating/metal/plate/" + metal.id());
                require(heating.matches(plate) && heating.getTemperature() == metal.meltingPoint()
                        && heating.assembleFluid(plate).getFluid() == TFCMFluids.METALS.get(metal).source().get()
                        && heating.assembleFluid(plate).getAmount() == 100, "Plate melt route is wrong " + metal.id());
                require(HeatCapability.getDefinition(plate) != null, "Plate heat is missing " + metal.id());
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void nuggetsCraftBothWaysAndMeltAtTen(GameTestHelper helper) {
        helper.succeedIf(() -> {
            for (Metal metal : Metal.values()) {
                ItemStack ingot = itemValue("metal/ingot/" + metal.id()).getDefaultInstance();
                ItemStack nugget = itemValue("metal/nugget/" + metal.id()).getDefaultInstance();
                CraftingRecipe split = (CraftingRecipe) recipe(helper, "crafting/metal/nugget/" + metal.id());
                CraftingRecipe join = (CraftingRecipe) recipe(helper, "crafting/metal/ingot_from_nuggets/" + metal.id());
                require(split.matches(CraftingInput.of(1, 1, List.of(ingot)), helper.getLevel()),
                        "Ingot does not split into nuggets " + metal.id());
                require(split.getResultItem(helper.getLevel().registryAccess()).is(nugget.getItem())
                        && split.getResultItem(helper.getLevel().registryAccess()).getCount() == 9,
                        "Wrong nugget split count " + metal.id());
                require(join.matches(CraftingInput.of(3, 3, Collections.nCopies(9, nugget)), helper.getLevel()),
                        "Nine nuggets do not rejoin " + metal.id());
                require(join.getResultItem(helper.getLevel().registryAccess()).is(ingot.getItem())
                        && join.getResultItem(helper.getLevel().registryAccess()).getCount() == 1,
                        "Wrong nugget join output " + metal.id());
                HeatingRecipe heating = (HeatingRecipe) recipe(helper, "heating/metal/nugget/" + metal.id());
                require(heating.matches(nugget)
                        && heating.assembleFluid(nugget).getFluid() == TFCMFluids.METALS.get(metal).source().get()
                        && heating.assembleFluid(nugget).getAmount() == 10, "Nugget melt route is wrong " + metal.id());
                int melted = 0;
                for (ItemStack input : Collections.nCopies(9, nugget)) melted += heating.assembleFluid(input).getAmount();
                require(melted == 90, "Nine nuggets must melt to 90 " + metal.id());
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void ingotsQuernToSeparateMetalDust(GameTestHelper helper) {
        helper.succeedIf(() -> {
            for (Metal metal : Metal.values()) {
                QuernRecipe quern = (QuernRecipe) recipe(helper, "quern/metal/dust/" + metal.id());
                ItemStack ingot = itemValue("metal/ingot/" + metal.id()).getDefaultInstance();
                require(quern.matches(ingot), "Quern rejects ingot " + metal.id());
                ItemStack dust = quern.assemble(ingot);
                require(dust.is(itemValue("metal/dust/" + metal.id())) && dust.getCount() == 1,
                        "Quern does not produce one metal dust " + metal.id());
                HeatingRecipe heating = (HeatingRecipe) recipe(helper, "heating/metal/dust/" + metal.id());
                require(heating.matches(dust)
                        && heating.assembleFluid(dust).getFluid() == TFCMFluids.METALS.get(metal).source().get()
                        && heating.assembleFluid(dust).getAmount() == 100, "Dust melt route is wrong " + metal.id());
                require(!dust.is(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "powders"))),
                        "Metal dust entered ore powder tag " + metal.id());
                for (String powderTag : List.of("gem_powders", "bowl_powders", "glass_powders")) {
                    require(!dust.is(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("tfc", powderTag))),
                            "Metal dust entered TFC powder tag " + metal.id());
                }
                require(dust.is(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "dusts/" + metal.id()))),
                        "Dust is missing its separate common tag " + metal.id());
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void allMetalsHavePlateNuggetDustDataAndAssets(GameTestHelper helper) {
        helper.succeedIf(() -> {
            for (Metal metal : Metal.values()) {
                ItemStack ingot = itemValue("metal/ingot/" + metal.id()).getDefaultInstance();
                AnvilRecipe plateRecipe = (AnvilRecipe) recipe(helper, "anvil/metal/plate/" + metal.id());
                require(plateRecipe.matches(anvilInput(ingot, metal.forgingTier()), helper.getLevel())
                        && plateRecipe.getResultItem(helper.getLevel().registryAccess()).is(itemValue("metal/plate/" + metal.id())),
                        "Missing plate recipe " + metal.id());
                CraftingRecipe split = (CraftingRecipe) recipe(helper, "crafting/metal/nugget/" + metal.id());
                ItemStack nugget = itemValue("metal/nugget/" + metal.id()).getDefaultInstance();
                CraftingRecipe join = (CraftingRecipe) recipe(helper, "crafting/metal/ingot_from_nuggets/" + metal.id());
                require(split.matches(CraftingInput.of(1, 1, List.of(ingot)), helper.getLevel())
                        && split.getResultItem(helper.getLevel().registryAccess()).is(nugget.getItem())
                        && join.matches(CraftingInput.of(3, 3, Collections.nCopies(9, nugget)), helper.getLevel())
                        && join.getResultItem(helper.getLevel().registryAccess()).is(ingot.getItem()),
                        "Missing nugget recipes " + metal.id());
                QuernRecipe quern = (QuernRecipe) recipe(helper, "quern/metal/dust/" + metal.id());
                require(quern.matches(ingot) && quern.assemble(ingot).is(itemValue("metal/dust/" + metal.id())),
                        "Missing dust recipe " + metal.id());
                for (MetalForm metalForm : List.of(MetalForm.PLATE, MetalForm.NUGGET, MetalForm.DUST)) {
                    String form = metalForm.id;
                    ItemStack stack = itemValue("metal/" + form + "/" + metal.id()).getDefaultInstance();
                    HeatingRecipe heating = (HeatingRecipe) recipe(helper, "heating/metal/" + form + "/" + metal.id());
                    require(heating.matches(stack) && heating.assembleFluid(stack).getFluid() == TFCMFluids.METALS.get(metal).source().get(),
                            "Missing form heating " + form + " " + metal.id());
                    require(HeatCapability.getDefinition(stack) != null, "Missing heat for " + form + " " + metal.id());
                    require(ItemSizeManager.getDefinition(stack) != null, "Missing size for " + form + " " + metal.id());
                    require(stack.is(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", metalForm.tag() + "/" + metal.id()))),
                            "Missing tag for " + form + " " + metal.id());
                    checkTagEntries(metalForm.tag(), metal);
                    checkModelTexture(form, metal);
                }
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void allMetalsAndBasicFormsAreRegistered(GameTestHelper helper) {
        helper.succeedIf(() -> {
            require(Metal.values().length == 39, "Expected exactly 39 TFCM metals");
            Set<String> ids = new java.util.HashSet<>();
            for (Metal metal : Metal.values()) {
                require(ids.add(metal.id()), "Duplicate metal id " + metal.id());
                for (String form : new String[] {"ingot", "double_ingot", "sheet", "double_sheet", "rod"}) {
                    require(item("metal/" + form + "/" + metal.id()), "Missing " + form + " for " + metal.id());
                }
                require(block("metal/block/" + metal.id()), "Missing block for " + metal.id());
                require(block("metal/block/" + metal.id() + "_slab"), "Missing slab for " + metal.id());
                require(block("metal/block/" + metal.id() + "_stairs"), "Missing stairs for " + metal.id());
                require(fluid("metal/" + metal.id()), "Missing molten fluid for " + metal.id());
                FlowingFluid source = TFCMFluids.METALS.get(metal).source().get();
                require(source.getBucket() == TFCMItems.METAL_BUCKETS.get(metal).get(), "Wrong bucket for " + metal.id());
                require(source.defaultFluidState().createLegacyBlock().getBlock() == TFCMBlocks.METAL_FLUID_BLOCKS.get(metal).get(),
                        "Wrong fluid block for " + metal.id());
                require(((BucketItem) TFCMItems.METAL_BUCKETS.get(metal).get()).content == source,
                        "Bucket does not contain " + metal.id());
                require(((BlockItem) itemValue("metal/block/" + metal.id())).getBlock() == TFCMBlocks.METAL_BLOCKS.get(metal).get(),
                        "Block item does not reference " + metal.id());
                require(source.is(TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("tfc", "molten_metals"))),
                        "Molten metal tag is missing " + metal.id());
            }
            require(fluid("molten_andesite"), "Missing molten andesite");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void metalSpecificationValuesMatch(GameTestHelper helper) {
        helper.succeedIf(() -> {
            int[] tiers = {3,3,7,3,3,3,2,2,2,2,1,5,3,3,3,1,3,1,1,3,3,3,7,6,1,2,7,7,3,3,2,3,2,7,5,5,5,5,5};
            int[] melts = {1535,1730,2000,1535,1535,1500,940,940,940,940,630,1700,1700,181,1740,1250,1250,1000,330,1130,1535,400,1535,1535,1000,520,2000,2000,2076,1750,650,1287,1855,3000,1500,1600,1855,1450,1800};
            Set<Metal> tools = Set.of(Metal.TITANIUM, Metal.INVAR, Metal.TUNGSTEN_STEEL, Metal.NETHERITE);
            Metal[] metals = Metal.values();
            for (int index = 0; index < metals.length; index++) {
                require(metals[index].forgingTier() == tiers[index], "Wrong tier for " + metals[index].id());
                require(metals[index].meltingPoint() == melts[index], "Wrong melting point for " + metals[index].id());
                require(metals[index].providesTools() == tools.contains(metals[index]), "Wrong tool flag for " + metals[index].id());
                require(metals[index].specificHeat() > 0, "Missing specific heat for " + metals[index].id());
                ItemStack ingot = itemValue("metal/ingot/" + metals[index].id()).getDefaultInstance();
                HeatDefinition heat = HeatCapability.getDefinition(ingot);
                AnvilRecipe sheet = (AnvilRecipe) recipe(helper, "anvil/metal/sheet/" + metals[index].id());
                HeatingRecipe melting = (HeatingRecipe) recipe(helper, "heating/metal/ingot/" + metals[index].id());
                require(sheet.getMinTier() == tiers[index], "Loaded tier is wrong for " + metals[index].id());
                require(melting.getTemperature() == melts[index], "Loaded melting point is wrong for " + metals[index].id());
                require(heat != null && heat.forgingTemperature() == metals[index].forgingTemperature(),
                        "Loaded forging temperature is wrong for " + metals[index].id());
                require(heat.weldingTemperature() == metals[index].weldingTemperature(),
                        "Loaded welding temperature is wrong for " + metals[index].id());
                if (!metals[index].providesTools()) {
                    require(!item("metal/pickaxe/" + metals[index].id()), "Unexpected tool set for " + metals[index].id());
                }
            }
            require(Metal.DRACONIUM.forgingTemperature() == 1200 && Metal.DRACONIUM.weldingTemperature() == 1600,
                    "Draconium temperatures are not fixed");
            require(Metal.AWAKENED_DRACONIUM.forgingTemperature() == 1200
                    && Metal.AWAKENED_DRACONIUM.weldingTemperature() == 1600,
                    "Awakened Draconium temperatures are not fixed");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void highCarbonTungstenSteelIsAnIntermediate(GameTestHelper helper) {
        helper.succeedIf(() -> {
            require(item("metal/ingot/high_carbon_tungsten_steel"), "High carbon tungsten steel is missing");
            Recipe<?> recipe = recipe(helper, "anvil/metal/ingot/tungsten_steel");
            require(recipe instanceof AnvilRecipe anvil && anvil.getMinTier() == 7,
                    "High carbon tungsten steel does not use the tier 7 anvil route");
            HeatingRecipe heating = (HeatingRecipe) recipe(helper, "heating/metal/ingot/high_carbon_tungsten_steel");
            require(heating.getDisplayOutputFluid().getAmount() == 100,
                    "High carbon tungsten steel does not retain one ingot of metal");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void bothIngotMoldsCastWithSpecifiedBreakChance(GameTestHelper helper) {
        helper.succeedIf(() -> {
            CastingRecipe normal = (CastingRecipe) recipe(helper, "casting/platinum_ingot");
            CastingRecipe fire = (CastingRecipe) recipe(helper, "casting/platinum_fire_ingot");
            require(normal.getBreakChance() == 0.1f,
                    "Ingot mold break chance is not 10%");
            require(fire.getBreakChance() == 0.01f,
                    "Fire ingot mold break chance is not 1%");
            assertCasts(normal, "tfc:ceramic/ingot_mold");
            assertCasts(fire, "tfc:ceramic/fire_ingot_mold");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void anvilWeldingAndHeatingPreserveMetalAmounts(GameTestHelper helper) {
        helper.succeedIf(() -> {
            AnvilRecipe sheet = (AnvilRecipe) recipe(helper, "anvil/metal/sheet/titanium");
            AnvilRecipe rodRecipe = (AnvilRecipe) recipe(helper, "anvil/metal/rod/titanium");
            WeldingRecipe doubleIngot = (WeldingRecipe) recipe(helper, "welding/metal/double_ingot/titanium");
            WeldingRecipe doubleSheet = (WeldingRecipe) recipe(helper, "welding/metal/double_sheet/titanium");
            ItemStack ingot = itemValue("metal/ingot/titanium").getDefaultInstance();
            ItemStack sheetStack = itemValue("metal/sheet/titanium").getDefaultInstance();
            require(sheet.matches(anvilInput(itemValue("metal/double_ingot/titanium").getDefaultInstance(), 5), helper.getLevel()),
                    "Titanium sheet does not match at tier 5");
            require(!sheet.matches(anvilInput(itemValue("metal/double_ingot/titanium").getDefaultInstance(), 4), helper.getLevel()),
                    "Titanium sheet accepts tier 4");
            require(rodRecipe.matches(anvilInput(ingot, 5), helper.getLevel()), "Titanium rod input does not match");
            require(doubleIngot.matches(weldingInput(ingot, ingot, 5)), "Titanium double ingot does not match");
            require(!doubleIngot.isCorrectTier(4), "Titanium welding accepts tier 4");
            require(doubleSheet.matches(weldingInput(sheetStack, sheetStack, 5)), "Titanium double sheet does not match");
            HeatingRecipe rod = (HeatingRecipe) recipe(helper, "heating/metal/rod/titanium");
            HeatingRecipe block = (HeatingRecipe) recipe(helper, "heating/metal/block/titanium");
            require(rod.getDisplayOutputFluid().getAmount() == 50, "Rod does not melt to 50 mB");
            require(block.getDisplayOutputFluid().getAmount() == 100, "Block does not melt to 100 mB");
            require(rod.getTemperature() == 1700, "Titanium uses the wrong melting temperature");
            HeatDefinition heat = HeatCapability.getDefinition(ingot);
            require(heat.forgingTemperature() == 1020 && heat.weldingTemperature() == 1360,
                    "Titanium loaded heat thresholds are wrong");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void onlySpecifiedAlloyRecipesLoad(GameTestHelper helper) {
        helper.succeedIf(() -> {
            String[] alloys = {"constantan", "electrum", "invar", "osmiridium", "solder", "ferroboron",
                    "tough_alloy", "zircaloy", "hsla_steel", "super_alloy", "andesite_alloy_zinc", "andesite_alloy_tin"};
            for (String alloy : alloys) {
                require(recipe(helper, "alloy/" + alloy) instanceof AlloyRecipe, "Missing alloy recipe " + alloy);
            }
            AlloyRecipe constantan = (AlloyRecipe) recipe(helper, "alloy/constantan");
            require(constantan.matches(alloy(50, "tfc:metal/copper", 50, "tfc:metal/nickel")),
                    "Constantan rejects a valid 50/50 alloy");
            require(!constantan.matches(alloy(70, "tfc:metal/copper", 30, "tfc:metal/nickel")),
                    "Constantan accepts an invalid 70/30 alloy");
            long count = helper.getLevel().getServer().getRecipeManager().getRecipes().stream()
                    .filter(holder -> holder.id().getNamespace().equals(TFCM.MOD_ID))
                    .filter(holder -> holder.value() instanceof AlloyRecipe).count();
            require(count == alloys.length, "An undefined special-metal alloy recipe was loaded");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void andesiteMeltsAndAlloysAtSpecifiedAmounts(GameTestHelper helper) {
        helper.succeedIf(() -> {
            HeatingRecipe heating = (HeatingRecipe) recipe(helper, "heating/loose_andesite");
            require(heating.getTemperature() == 1300, "Andesite melting temperature is not 1300 C");
            require(heating.getDisplayOutputFluid().getAmount() == 90, "Loose andesite does not melt to 90 mB");
            AlloyRecipe zinc = (AlloyRecipe) recipe(helper, "alloy/andesite_alloy_zinc");
            AlloyRecipe tin = (AlloyRecipe) recipe(helper, "alloy/andesite_alloy_tin");
            require(zinc.contents().size() == 2 && tin.contents().size() == 2, "Andesite alloy alternatives are incomplete");
            require(zinc.contents().getFirst().min() == 0.89 && zinc.contents().getFirst().max() == 0.91,
                    "Molten andesite range is not 89-91%");
            require(zinc.matches(alloy(90, "tfcm:molten_andesite", 10, "tfc:metal/zinc")),
                    "90 mB andesite and 10 mB zinc do not alloy");
            require(tin.matches(alloy(90, "tfcm:molten_andesite", 10, "tfc:metal/tin")),
                    "90 mB andesite and 10 mB tin do not alloy");
            require(zinc.result() == TFCMFluids.METALS.get(Metal.ANDESITE_ALLOY).source().get(),
                    "Andesite alloy has the wrong result fluid");
            require(HeatCapability.getDefinition(BuiltInRegistries.ITEM.get(ResourceLocation.parse("tfc:rock/loose/andesite"))
                    .getDefaultInstance()) != null, "Loose andesite has no loaded heat data");
        });
    }

    private static void assertCasts(CastingRecipe recipe, String moldId) {
        ItemStack stack = BuiltInRegistries.ITEM.get(ResourceLocation.parse(moldId)).getDefaultInstance();
        IMold mold = IMold.get(stack);
        require(mold != null, "Missing mold capability for " + moldId);
        require(mold.fill(new FluidStack(TFCMFluids.METALS.get(Metal.PLATINUM).source().get(), 100), FluidAction.EXECUTE) == 100,
                "Could not fill " + moldId);
        HeatCapability.setTemperature(stack, 0);
        require(recipe.matches(mold), "Casting recipe does not match " + moldId);
        require(recipe.assemble(mold).is(itemValue("metal/ingot/platinum")), "Casting did not produce platinum ingot");
    }

    private static FluidAlloy alloy(int firstAmount, String first, int secondAmount, String second) {
        FluidAlloy alloy = FluidAlloy.empty();
        alloy.fill(new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse(first)), firstAmount), FluidAction.EXECUTE,
                alloyContainer());
        alloy.fill(new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse(second)), secondAmount), FluidAction.EXECUTE,
                alloyContainer());
        return alloy;
    }

    private static FluidContainerInfo alloyContainer() {
        return new FluidContainerInfo() {
            @Override
            public boolean canContainFluid(Fluid fluid) {
                return true;
            }

            @Override
            public int fluidCapacity() {
                return 1000;
            }
        };
    }

    private static AnvilRecipe.Inventory anvilInput(ItemStack stack, int tier) {
        return new AnvilRecipe.Inventory() {
            @Override
            public ItemStack getItem() {
                return stack;
            }

            @Override
            public int getTier() {
                return tier;
            }

            @Override
            public long getSeed() {
                return 0;
            }

            @Override
            public ItemStack getItem(int slot) {
                return stack;
            }

            @Override
            public int size() {
                return 1;
            }
        };
    }

    private static WeldingRecipe.Inventory weldingInput(ItemStack first, ItemStack second, int tier) {
        return new WeldingRecipe.Inventory() {
            @Override
            public ItemStack getMain() {
                return first;
            }

            @Override
            public ItemStack getSecondary() {
                return second;
            }

            @Override
            public int getTier() {
                return tier;
            }

            @Override
            public ItemStack getItem(int slot) {
                return slot == 0 ? first : second;
            }

            @Override
            public int size() {
                return 2;
            }
        };
    }

    private static void checkTagEntries(String form, Metal metal) {
        String path = "/data/c/tags/item/" + form + "/" + metal.id() + ".json";
        try (var stream = TFCM.class.getResourceAsStream(path)) {
            require(stream != null, "Missing tag file " + path);
            var values = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonArray("values");
            require(values.size() > 0, "Empty tag " + path);
            values.forEach(value -> require(BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(value.getAsString())),
                    "Unregistered tag item " + value.getAsString()));
        } catch (IOException error) {
            throw new GameTestAssertException("Cannot read tag " + path);
        }
    }

    private static void checkModelTexture(String form, Metal metal) {
        String path = "/assets/tfcm/models/item/metal/" + form + "/" + metal.id() + ".json";
        try (var stream = TFCM.class.getResourceAsStream(path)) {
            require(stream != null, "Missing model for " + form + " " + metal.id());
            String texture = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonObject("textures").get("layer0").getAsString();
            ResourceLocation id = ResourceLocation.parse(texture);
            require(TFCM.class.getResource("/assets/" + id.getNamespace() + "/textures/" + id.getPath() + ".png") != null,
                    "Missing model texture " + texture);
        } catch (IOException error) {
            throw new GameTestAssertException("Cannot read model " + path);
        }
    }

    private static Recipe<?> recipe(GameTestHelper helper, String path) {
        return helper.getLevel().getServer().getRecipeManager()
                .byKey(ResourceLocation.fromNamespaceAndPath(TFCM.MOD_ID, path)).orElseThrow().value();
    }

    private static boolean item(String path) {
        return BuiltInRegistries.ITEM.containsKey(ResourceLocation.fromNamespaceAndPath(TFCM.MOD_ID, path));
    }

    private static net.minecraft.world.item.Item itemValue(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(TFCM.MOD_ID, path));
    }

    private static boolean block(String path) {
        return BuiltInRegistries.BLOCK.containsKey(ResourceLocation.fromNamespaceAndPath(TFCM.MOD_ID, path));
    }

    private static boolean fluid(String path) {
        return BuiltInRegistries.FLUID.containsKey(ResourceLocation.fromNamespaceAndPath(TFCM.MOD_ID, path));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }
}
