package net.claustra01.tfcm.gametest;

import java.util.Set;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.metal.Metal;
import net.claustra01.tfcm.registry.TFCMBlocks;
import net.claustra01.tfcm.registry.TFCMFluids;
import net.claustra01.tfcm.registry.TFCMItems;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.heat.HeatDefinition;
import net.dries007.tfc.common.component.fluid.FluidContainerInfo;
import net.dries007.tfc.common.component.mold.IMold;
import net.dries007.tfc.common.recipes.AlloyRecipe;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.dries007.tfc.common.recipes.CastingRecipe;
import net.dries007.tfc.common.recipes.HeatingRecipe;
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

    private TFCMMetalGameTests() {
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
