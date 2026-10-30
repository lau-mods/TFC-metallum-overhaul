package net.claustra01.tfcm.gametest;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.processing.ProcessingForm;
import net.claustra01.tfcm.common.processing.ProcessingSeries;
import net.claustra01.tfcm.registry.TFCMItems;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.dries007.tfc.common.recipes.QuernRecipe;
import net.dries007.tfc.util.data.Deposit;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TFCM.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TFCMProcessingGameTests {
    private static final String BATCH = "tfcm_foundation";
    private static final Map<String, Integer> GRADED_OUTPUTS = Map.of("small", 1, "poor", 3,
            "normal", 5, "rich", 7);
    private static final List<ExpectedSeries> EXPECTED = List.of(
            series("native_copper", "red", "basalt", "andesite", "opal", "hematite", "tfc:metal/copper", true, false, "tfc"),
            series("native_gold", "yellow", "quartzite", "granite", "quartz", "native_silver", "tfc:metal/gold", true, false, "tfc"),
            series("hematite", "red", "quartzite", "shale", null, "sulfur", "tfc:metal/cast_iron", true, false, "tfc"),
            series("native_silver", "white", "gneiss", "granite", "amethyst", "galena", "tfc:metal/silver", true, false, "tfc"),
            series("cassiterite", "white", "granite", "gneiss", "topaz", "bismuthinite", "tfc:metal/tin", true, false, "tfc"),
            series("bismuthinite", "black", "granite", "schist", "pyrite", "galena", "tfc:metal/bismuth", true, false, "tfc"),
            series("garnierite", "green", "gabbro", "basalt", "opal", "limonite", "tfc:metal/nickel", true, false, "tfc"),
            series("malachite", "green", "limestone", "dolomite", "opal", "limonite", "tfc:metal/copper", true, false, "tfc"),
            series("magnetite", "black", "gabbro", "basalt", null, "hematite", "tfc:metal/cast_iron", true, false, "tfc"),
            series("limonite", "brown", "claystone", "shale", "opal", "sulfur", "tfc:metal/cast_iron", true, false, "tfc"),
            series("sphalerite", "brown", "dolomite", "limestone", "amethyst", "galena", "tfc:metal/zinc", true, false, "tfc"),
            series("tetrahedrite", "black", "schist", "dolomite", "pyrite", "sphalerite", "tfc:metal/copper", true, false, "tfc"),
            series("galena", "black", "limestone", "dolomite", "amethyst", "sphalerite", "tfcm:metal/lead", true, false, "tfcm"),
            series("magnesite", "white", "dolomite", "gabbro", "opal", "limonite", "tfcm:metal/magnesium", true, false, "tfcm"),
            series("bauxite", "red", "claystone", "shale", "sapphire", "hematite", "tfcm:metal/aluminum", true, false, "tfcm"),
            series("uraninite", "black", "granite", "gneiss", null, "galena", "tfcm:metal/uranium", true, false, "tfcm"),
            series("chromite", "black", "gabbro", "basalt", "ruby", "cobaltite", "firmalife:metal/chromium", true, false, "firmalife"),
            series("rutile", "red", "gneiss", "schist", "sapphire", "hematite", "tfcm:metal/titanium", true, false, "tfcm"),
            series("zircon", "brown", "granite", "gneiss", "topaz", "cassiterite", "tfcm:metal/zirconium", true, false, "tfcm"),
            series("cobaltite", "black", "gabbro", "schist", "pyrite", "sulfur", "tfcm:metal/cobalt", true, false, "tfcm"),
            series("stibnite", "black", "shale", "slate", "amethyst", "galena", "tfcm:metal/antimony", true, false, "tfcm"),
            series("native_platinum", "black", "gabbro", "basalt", null, "chromite", "tfcm:metal/platinum", true, false, "tfcm"),
            series("wolframite", "black", "granite", "gneiss", "quartz", "cassiterite", "tfcm:metal/tungsten", true, false, "tfcm"),
            series("thorianite", "black", "granite", "gneiss", null, "galena", "tfcm:metal/thorium", true, false, "tfcm"),
            series("native_iridium", "black", "gabbro", "basalt", null, "native_platinum", "tfcm:metal/iridium", true, false, "tfcm"),
            series("native_osmium", "black", "gabbro", "basalt", null, "native_platinum", "tfcm:metal/osmium", true, false, "tfcm"),
            series("spodumene", "white", "granite", "gneiss", "topaz", "cassiterite", "tfcm:metal/lithium", true, false, "tfcm"),
            series("carobbiite", "white", "granite", "gneiss", "emerald", "native_platinum", "tfcm:metal/beryllium", false, false, "tfcm"),
            series("native_naquadah", "black", "gabbro", "basalt", null, "native_osmium", "tfcm:metal/naquadah", true, false, "tfcm"),
            series("mithril_matrix", "white", "gneiss", "granite", "amethyst", "native_platinum", "tfcm:metal/mithril", true, false, "tfcm"),
            series("cinnabar", "red", "quartzite", "schist", "ruby", "sulfur", null, false, true, "tfc"),
            series("cryolite", "white", "granite", "gneiss", "topaz", "spodumene", null, false, true, "tfc"),
            series("graphite", "black", "schist", "phyllite", "diamond", "hematite", null, false, true, "tfc"),
            series("sulfur", "yellow", "tuff", "basalt", "pyrite", "hematite", null, false, true, "tfc"),
            series("borax", "white", "granite", "gneiss", "topaz", "graphite", "tfcm:metal/boron", false, true, "tfc"));

    private TFCMProcessingGameTests() {
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void processingTableHasThirtyMetalAndFiveNonMetalSeries(GameTestHelper helper) {
        helper.succeedIf(() -> {
            require(ProcessingSeries.values().length == 35 && EXPECTED.size() == 35,
                    "Processing table must contain exactly 35 series");
            require(EXPECTED.stream().filter(ExpectedSeries::nonMetal).count() == 5,
                    "Processing table must contain exactly five non-metal series");
            require(EXPECTED.stream().filter(expected -> !expected.nonMetal()).count() == 30,
                    "Processing table must contain exactly 30 metal series");
            boolean firmaLife = ModList.get().isLoaded("firmalife");
            require(recipePresent(helper, "crafting/ore_processing/hammer/normal_chromite") == firmaLife,
                    "Chromite processing availability does not match FirmaLife");
            require((Deposit.get(stack(ProcessingSeries.CHROMITE, ProcessingForm.ROCKY_CHUNKS)) != null) == firmaLife,
                    "Chromite Sluice input availability does not match FirmaLife");
            for (ExpectedSeries expected : EXPECTED) {
                ProcessingSeries actual = ProcessingSeries.valueOf(expected.id().toUpperCase());
                require(TFCMItems.ORE_PROCESSING.get(actual).size() == ProcessingForm.values().length - (expected.nonMetal() && !expected.id().equals("borax") ? 1 : 0),
                        "Missing processing form for " + expected.id());
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void naquadahAndMithrilUseProjectProcessingDefinitions(GameTestHelper helper) {
        helper.succeedIf(() -> {
            for (String id : List.of("native_naquadah", "mithril_matrix")) {
                ExpectedSeries expected = expected(id);
                checkExpected(expected);
                checkPrimaryJson(expected);
                checkSecondaryJson(expected);
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void processingLootUsesOnlyDefinedOutputs(GameTestHelper helper) {
        helper.succeedIf(() -> {
            for (ExpectedSeries expected : EXPECTED) {
                checkExpected(expected);
                checkPrimaryJson(expected);
                checkSecondaryJson(expected);
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void gradedHammerCrushingYieldsOneThreeFiveSevenAndDamagesHammer(GameTestHelper helper) {
        helper.succeedIf(() -> {
            GRADED_OUTPUTS.forEach((grade, count) -> checkHammer(helper, "native_naquadah", grade, count, "tfcm"));
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void ungradedMetalAndMineralHammerCrushingYieldFiveAndSeven(GameTestHelper helper) {
        helper.succeedIf(() -> {
            checkHammer(helper, "carobbiite", null, 5, "tfcm");
            checkHammer(helper, "cinnabar", null, 7, "tfc");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void primarySluiceAlwaysHasChunkOutput(GameTestHelper helper) {
        helper.succeedIf(() -> {
            Deposit deposit = Deposit.get(stack(ProcessingSeries.NATIVE_NAQUADAH, ProcessingForm.ROCKY_CHUNKS));
            require(deposit != null && deposit.lootTable().location().equals(
                    id("ore_processing/rocky_chunks/native_naquadah")), "Rocky Chunk is not a TFC Sluice input");
            checkPool(primary(expected("native_naquadah")), 0, "chunk",
                    processingItem("chunks", "native_naquadah"), null);
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void primarySluiceUsesIndependentThirtyFifteenAndGemPools(GameTestHelper helper) {
        helper.succeedIf(() -> {
            checkPrimaryJson(expected("mithril_matrix"));
            checkPrimaryJson(expected("native_naquadah"));
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void chunkQuernsToOnlyMatchingDirtyDust(GameTestHelper helper) {
        helper.succeedIf(() -> {
            ProcessingSeries series = ProcessingSeries.NATIVE_OSMIUM;
            QuernRecipe recipe = (QuernRecipe) recipe(helper, "quern/ore_processing/" + series.id());
            ItemStack result = recipe.assemble(stack(series, ProcessingForm.CHUNKS));
            require(result.is(TFCMItems.ORE_PROCESSING.get(series).get(ProcessingForm.DIRTY_DUST))
                    && result.getCount() == 1, "Quern did not return one matching Dirty Dust");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void secondarySluiceUsesFourSpecifiedIndependentPools(GameTestHelper helper) {
        helper.succeedIf(() -> checkSecondaryJson(expected("native_copper")));
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void dirtyPileIsAbsentWhileSevenSandPilesRemain(GameTestHelper helper) {
        helper.succeedIf(() -> checkDirtyPile(helper));
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void naquadahAndMithrilSecondaryOutputsStayDistinct(GameTestHelper helper) {
        helper.succeedIf(() -> {
            checkSecondaryJson(expected("native_naquadah"));
            checkSecondaryJson(expected("mithril_matrix"));
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void metalPowderHeatingReturnsFiveMillibuckets(GameTestHelper helper) {
        helper.succeedIf(() -> {
            boolean firmaLife = ModList.get().isLoaded("firmalife");
            for (ExpectedSeries expected : EXPECTED) {
                if (expected.nonMetal() || expected.id().equals("chromite") && !firmaLife) continue;
                ResourceLocation fluid = ResourceLocation.parse(expected.recoveryFluid());
                require(BuiltInRegistries.FLUID.containsKey(fluid), "Recovery fluid is not registered: " + fluid);
                checkHeating(helper, ProcessingSeries.valueOf(expected.id().toUpperCase()),
                        ProcessingForm.POWDER, 5, expected.recoveryFluid());
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void pelletAndBriquetCompressionPreservesAmounts(GameTestHelper helper) {
        helper.succeedIf(() -> {
            ProcessingSeries series = ProcessingSeries.NATIVE_COPPER;
            checkCraft(helper, "pellet", series, ProcessingForm.POWDER, 4, ProcessingForm.PELLET, 1);
            checkCraft(helper, "briquet", series, ProcessingForm.PELLET, 4, ProcessingForm.BRIQUET, 1);
            checkCraft(helper, "unpack_pellet", series, ProcessingForm.PELLET, 1, ProcessingForm.POWDER, 4);
            checkCraft(helper, "unpack_briquet", series, ProcessingForm.BRIQUET, 1, ProcessingForm.PELLET, 4);
            checkHeating(helper, series, ProcessingForm.PELLET, 20, "tfc:metal/copper");
            checkHeating(helper, series, ProcessingForm.BRIQUET, 80, "tfc:metal/copper");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void nonMetalPowdersHaveNoInventedHeatingFluid(GameTestHelper helper) {
        helper.succeedIf(() -> {
            for (ExpectedSeries expected : EXPECTED.stream().filter(ExpectedSeries::nonMetal).toList()) {
                ProcessingSeries series = ProcessingSeries.valueOf(expected.id().toUpperCase());
                require(recipePresent(helper, "crafting/ore_processing/hammer/" + expected.id()),
                        "Non-metal Hammer recipe is missing: " + expected.id());
                QuernRecipe quern = (QuernRecipe) recipe(helper, "quern/ore_processing/" + expected.id());
                require(quern.assemble(stack(series, ProcessingForm.CHUNKS)).is(
                        TFCMItems.ORE_PROCESSING.get(series).get(ProcessingForm.DIRTY_DUST)),
                        "Non-metal Quern output is wrong: " + expected.id());
                checkPool(secondary(expected), 0, "powder", processingItem("powder", expected.id()), null);
                require(BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(processingItem("powder", expected.id()))),
                        "Non-metal Powder item is missing: " + expected.id());
                require(!recipePresent(helper, "heating/ore_processing/pellet/" + expected.id())
                        && !recipePresent(helper, "heating/ore_processing/briquet/" + expected.id()),
                        "Non-metal compressed form has an invented heating recipe: " + expected.id());
                require(recipePresent(helper, "heating/ore_processing/powder/" + expected.id())
                        == expected.id().equals("borax"), "Non-metal Powder heating is wrong: " + expected.id());
            }
            require(BuiltInRegistries.FLUID.containsKey(ResourceLocation.parse("tfcm:metal/boron")),
                    "Borax recovery fluid tfcm:metal/boron is not registered");
            checkHeating(helper, ProcessingSeries.BORAX, ProcessingForm.POWDER, 5, "tfcm:metal/boron");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void onlySmallRawOreMeltsDirectly(GameTestHelper helper) {
        helper.succeedIf(() -> {
            boolean firmaLife = ModList.get().isLoaded("firmalife");
            for (ExpectedSeries expected : EXPECTED) {
                if (!expected.graded() || expected.id().equals("chromite") && !firmaLife) continue;
                ResourceLocation small = ResourceLocation.fromNamespaceAndPath(expected.oreNamespace(),
                        "heating/ore/small_" + expected.id());
                require(helper.getLevel().getServer().getRecipeManager().byKey(small).isPresent(),
                        "Small Ore direct heating is missing: " + small);
                for (String grade : List.of("poor", "normal", "rich")) {
                    ResourceLocation raw = ResourceLocation.fromNamespaceAndPath(expected.oreNamespace(),
                            "heating/ore/" + grade + "_" + expected.id());
                    require(helper.getLevel().getServer().getRecipeManager().byKey(raw).isEmpty(),
                            "Graded raw ore still melts directly: " + raw);
                }
                require(recipePresent(helper, "heating/ore_processing/powder/" + expected.id()),
                        "Washed Powder cannot be heated: " + expected.id());
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void everySeriesCompletesFiveProcessingSteps(GameTestHelper helper) {
        helper.succeedIf(() -> {
            boolean firmaLife = ModList.get().isLoaded("firmalife");
            for (ExpectedSeries expected : EXPECTED) {
                if (expected.id().equals("chromite") && !firmaLife) continue;
                ProcessingSeries series = ProcessingSeries.valueOf(expected.id().toUpperCase());
                if (expected.graded()) {
                    GRADED_OUTPUTS.forEach((grade, count) -> checkHammer(helper, expected.id(), grade,
                            count, expected.oreNamespace()));
                } else {
                    checkHammer(helper, expected.id(), null, expected.nonMetal() ? 7 : 5,
                            expected.oreNamespace());
                }
                require(Deposit.get(stack(series, ProcessingForm.ROCKY_CHUNKS)) != null,
                        "Primary Sluice input is missing: " + expected.id());
                checkPrimaryJson(expected);
                QuernRecipe quern = (QuernRecipe) recipe(helper, "quern/ore_processing/" + expected.id());
                ItemStack dirtyDust = quern.assemble(stack(series, ProcessingForm.CHUNKS));
                require(dirtyDust.is(stack(series, ProcessingForm.DIRTY_DUST).getItem())
                        && dirtyDust.getCount() == 1, "Quern output is wrong: " + expected.id());
                require(Deposit.get(dirtyDust) != null, "Secondary Sluice input is missing: " + expected.id());
                checkSecondaryJson(expected);
                checkExpected(expected);
                if (expected.id().equals("native_naquadah")) {
                    require(expected.byproduct().equals("native_osmium")
                            && expected.recoveryFluid().equals("tfcm:metal/naquadah"),
                            "Native Naquadah recovery differs from the project table");
                }
                if (expected.id().equals("mithril_matrix")) {
                    require(expected.byproduct().equals("native_platinum")
                            && expected.recoveryFluid().equals("tfcm:metal/mithril"),
                            "Mithril Matrix recovery differs from the project table");
                }
                if (expected.recoveryFluid() != null) {
                    checkHeating(helper, series, ProcessingForm.POWDER, 5, expected.recoveryFluid());
                }
                checkCraft(helper, "pellet", series, ProcessingForm.POWDER, 4, ProcessingForm.PELLET, 1);
                checkCraft(helper, "briquet", series, ProcessingForm.PELLET, 4, ProcessingForm.BRIQUET, 1);
                checkCraft(helper, "unpack_pellet", series, ProcessingForm.PELLET, 1, ProcessingForm.POWDER, 4);
                checkCraft(helper, "unpack_briquet", series, ProcessingForm.BRIQUET, 1, ProcessingForm.PELLET, 4);
                if (expected.nonMetal()) {
                    require(!recipePresent(helper, "heating/ore_processing/pellet/" + expected.id())
                            && !recipePresent(helper, "heating/ore_processing/briquet/" + expected.id()),
                            "Non-metal compressed form has a heating recipe: " + expected.id());
                } else {
                    checkHeating(helper, series, ProcessingForm.PELLET, 20, expected.recoveryFluid());
                    checkHeating(helper, series, ProcessingForm.BRIQUET, 80, expected.recoveryFluid());
                }
            }
            checkDirtyPile(helper);
        });
    }

    private static void checkDirtyPile(GameTestHelper helper) {
        require(TFCMItems.SAND_PILES.size() == 7, "Seven Sand Piles must remain registered");
        require(BuiltInRegistries.ITEM.keySet().stream().noneMatch(key ->
                key.getNamespace().equals(TFCM.MOD_ID) && key.getPath().contains("dirty_pile")),
                "Dirty Pile item is registered");
        require(helper.getLevel().getServer().getRecipeManager().getRecipeIds().noneMatch(key ->
                key.getPath().contains("dirty_pile")), "Dirty Pile recipe was loaded");
        var resources = helper.getLevel().getServer().getResourceManager().listResources("", id ->
                id.getNamespace().equals(TFCM.MOD_ID) && id.getPath().endsWith(".json"));
        for (var entry : resources.entrySet()) {
            require(!entry.getKey().getPath().contains("dirty_pile"), "Dirty Pile resource exists: " + entry.getKey());
            try (var stream = entry.getValue().open()) {
                require(!new String(stream.readAllBytes(), StandardCharsets.UTF_8).contains("dirty_pile"),
                        "Dirty Pile resource reference exists: " + entry.getKey());
            } catch (java.io.IOException exception) {
                throw new GameTestAssertException("Could not inspect " + entry.getKey() + ": " + exception.getMessage());
            }
        }
    }

    private static void checkExpected(ExpectedSeries expected) {
        ProcessingSeries actual = ProcessingSeries.valueOf(expected.id().toUpperCase());
        require(actual.sand().id().equals(expected.sand()) && actual.primaryRock().equals(expected.primaryRock())
                && actual.secondaryRock().equals(expected.secondaryRock())
                && Objects.equals(actual.gem(), expected.gem()) && actual.byproduct().equals(expected.byproduct())
                && Objects.equals(actual.recoveryFluid(), expected.recoveryFluid())
                && actual.graded() == expected.graded() && actual.nonMetal() == expected.nonMetal(),
                "Wrong processing definition for " + expected.id());
    }

    private static void checkPrimaryJson(ExpectedSeries expected) {
        JsonArray pools = primary(expected);
        require(pools.size() == (expected.gem() == null ? 3 : 4),
                "Unexpected primary wash pool count: " + expected.id());
        checkPool(pools, 0, "chunk", processingItem("chunks", expected.id()), null);
        checkPool(pools, 1, "primary_rock_30_percent", "tfc:rock/loose/" + expected.primaryRock(), 0.30f);
        checkPool(pools, 2, "secondary_rock_15_percent", "tfc:rock/loose/" + expected.secondaryRock(), 0.15f);
        if (expected.gem() != null) checkPool(pools, 3, "gem_0_02_percent",
                expected.gem().equals("quartz") ? "tfcm:ore/quartz" : "tfc:gem/" + expected.gem(), 0.0002f);
    }

    private static void checkSecondaryJson(ExpectedSeries expected) {
        JsonArray pools = secondary(expected);
        require(pools.size() == 4, "Unexpected secondary wash pool count: " + expected.id());
        checkPool(pools, 0, "powder", processingItem("powder", expected.id()), null);
        checkPool(pools, 1, "sand_pile_15_percent", processingItem("sand_pile", expected.sand()), 0.15f);
        checkPool(pools, 2, "same_dirty_dust_5_percent", processingItem("dirty_dust", expected.id()), 0.05f);
        checkPool(pools, 3, "byproduct_dirty_dust_1_percent",
                processingItem("dirty_dust", expected.byproduct()), 0.01f);
    }

    private static void checkPool(JsonArray pools, int index, String name, String item, Float chance) {
        JsonObject pool = pools.get(index).getAsJsonObject();
        require(pool.get("name").getAsString().equals(name), "Wrong loot pool name: " + name);
        require(pool.getAsJsonArray("entries").get(0).getAsJsonObject().get("name").getAsString().equals(item),
                "Wrong loot item in " + name);
        if (chance == null) {
            require(!pool.has("conditions"), "Guaranteed pool has a condition: " + name);
        } else {
            JsonArray conditions = pool.getAsJsonArray("conditions");
            JsonObject condition = conditions.get(0).getAsJsonObject();
            require(conditions.size() == 1
                    && condition.get("condition").getAsString().equals("minecraft:random_chance")
                    && Math.abs(condition.get("chance").getAsFloat() - chance) < 0.000001f,
                    "Wrong random chance in " + name);
        }
    }

    private static JsonArray primary(ExpectedSeries expected) {
        return resource("data/tfcm/loot_table/ore_processing/rocky_chunks/" + expected.id() + ".json")
                .getAsJsonArray("pools");
    }

    private static JsonArray secondary(ExpectedSeries expected) {
        return resource("data/tfcm/loot_table/ore_processing/dirty_dust/" + expected.id() + ".json")
                .getAsJsonArray("pools");
    }

    private static JsonObject resource(String path) {
        try (var stream = TFCMProcessingGameTests.class.getClassLoader().getResourceAsStream(path)) {
            require(stream != null, "Generated resource is missing: " + path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception exception) {
            throw new GameTestAssertException("Could not inspect " + path + ": " + exception.getMessage());
        }
    }

    private static void checkHammer(GameTestHelper helper, String series, String grade, int count, String namespace) {
        String suffix = grade == null ? series : grade + "_" + series;
        CraftingRecipe recipe = (CraftingRecipe) recipe(helper, "crafting/ore_processing/hammer/" + suffix);
        ItemStack hammer = BuiltInRegistries.ITEM.get(ResourceLocation.parse("tfc:metal/hammer/copper")).getDefaultInstance();
        String inputName = grade == null ? series : grade.equals("small") ? "small_" + series : suffix;
        ItemStack input = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(namespace,
                "ore/" + inputName)).getDefaultInstance();
        CraftingInput crafting = CraftingInput.of(2, 1, List.of(hammer, input));
        require(recipe.matches(crafting, helper.getLevel()), "Hammer recipe does not match " + suffix);
        ItemStack result = recipe.assemble(crafting, helper.getLevel().registryAccess());
        require(result.getCount() == count && result.is(BuiltInRegistries.ITEM.get(
                id("ore_processing/rocky_chunks/" + series))), "Wrong Rocky Chunk count for " + suffix);
        NonNullList<ItemStack> remainder = recipe.getRemainingItems(crafting);
        require(remainder.getFirst().is(hammer.getItem()) && remainder.getFirst().getDamageValue() == 1,
                "Hammer did not take one durability damage for " + suffix);
    }

    private static void checkCraft(GameTestHelper helper, String recipeName, ProcessingSeries series,
            ProcessingForm inputForm, int inputCount, ProcessingForm outputForm, int outputCount) {
        CraftingRecipe recipe = (CraftingRecipe) recipe(helper,
                "crafting/ore_processing/" + recipeName + "/" + series.id());
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < inputCount; i++) items.add(stack(series, inputForm));
        CraftingInput input = CraftingInput.of(inputCount == 4 ? 2 : 1, inputCount == 4 ? 2 : 1, items);
        require(recipe.matches(input, helper.getLevel()), "Compression recipe does not match: " + recipeName);
        ItemStack result = recipe.assemble(input, helper.getLevel().registryAccess());
        require(result.is(stack(series, outputForm).getItem()) && result.getCount() == outputCount,
                "Compression recipe has the wrong amount: " + recipeName);
    }

    private static void checkHeating(GameTestHelper helper, ProcessingSeries series, ProcessingForm form,
            int amount, String expectedFluid) {
        HeatingRecipe recipe = (HeatingRecipe) recipe(helper,
                "heating/ore_processing/" + form.id() + "/" + series.id());
        ItemStack input = stack(series, form);
        require(recipe.matches(input) && recipe.assembleFluid(input).getAmount() == amount
                && BuiltInRegistries.FLUID.getKey(recipe.assembleFluid(input).getFluid()).toString().equals(expectedFluid),
                "Wrong heating output for " + form.id() + "/" + series.id());
    }

    private static net.minecraft.world.item.crafting.Recipe<?> recipe(GameTestHelper helper, String path) {
        return helper.getLevel().getServer().getRecipeManager().byKey(id(path)).orElseThrow().value();
    }

    private static boolean recipePresent(GameTestHelper helper, String path) {
        return helper.getLevel().getServer().getRecipeManager().byKey(id(path)).isPresent();
    }

    private static ItemStack stack(ProcessingSeries series, ProcessingForm form) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(processingItem(form.id(), series.id()))).getDefaultInstance();
    }

    private static ExpectedSeries expected(String id) {
        return EXPECTED.stream().filter(expected -> expected.id().equals(id)).findFirst().orElseThrow();
    }

    private static String processingItem(String form, String name) {
        if (form.equals("powder")) {
            if (name.equals("cinnabar") || name.equals("cryolite")) return "minecraft:redstone";
            if (List.of("graphite", "sulfur").contains(name)) return "tfc:powder/" + name;
        }
        return "tfcm:ore_processing/" + form + "/" + name;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(TFCM.MOD_ID, path);
    }

    private static ExpectedSeries series(String id, String sand, String primaryRock, String secondaryRock,
            String gem, String byproduct, String recoveryFluid, boolean graded, boolean nonMetal,
            String oreNamespace) {
        return new ExpectedSeries(id, sand, primaryRock, secondaryRock, gem, byproduct, recoveryFluid,
                graded, nonMetal, oreNamespace);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new GameTestAssertException(message);
    }

    private record ExpectedSeries(String id, String sand, String primaryRock, String secondaryRock, String gem,
            String byproduct, String recoveryFluid, boolean graded, boolean nonMetal, String oreNamespace) {
    }
}
