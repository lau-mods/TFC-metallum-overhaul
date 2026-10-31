package net.claustra01.tfcm.gametest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonParser;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.metal.Metal;
import net.claustra01.tfcm.common.metal.MetalForm;
import net.claustra01.tfcm.common.processing.ProcessingForm;
import net.claustra01.tfcm.config.TFCMConfig;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.size.ItemSizeManager;
import net.claustra01.tfcm.worldgen.YamlWorldgenLoader;
import net.dries007.tfc.util.data.Deposit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.JUnitLikeTestReporter;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("tfcm_m12")
@PrefixGameTestTemplate(false)
public final class TFCMContentConfigGameTests {
    private static final String BATCH = "tfcm_content_config";

    static {
        StructureUtils.testStructuresDir = System.getProperty("tfcm.gametest.structures");
    }

    private TFCMContentConfigGameTests() {
    }

    @BeforeBatch(batch = BATCH)
    public static void configureReport(net.minecraft.server.level.ServerLevel level) throws Exception {
        String reportPath = System.getProperty("tfcm.gametest.contentConfigReport");
        if (reportPath == null) return;
        Path report = Path.of(reportPath);
        Files.createDirectories(report.getParent());
        GlobalTestReporter.replaceWith(new JUnitLikeTestReporter(report.toFile()));
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void disabledMetalRemovesOnlyItsContent(GameTestHelper helper) {
        helper.succeedIf(() -> {
            for (MetalForm form : MetalForm.values()) {
                if (!form.isItem()) continue;
                String path = "metal/" + form.id + "/lead";
                require(!BuiltInRegistries.ITEM.containsKey(id(path)), "A disabled Lead form remains: " + path);
            }
            for (String path : new String[] {"metal/block/lead", "metal/block/lead_slab",
                    "metal/block/lead_stairs", "bucket/metal/lead"}) {
                require(!BuiltInRegistries.ITEM.containsKey(id(path)), "A disabled Lead item remains: " + path);
            }
            require(!BuiltInRegistries.BLOCK.containsKey(id("metal/block/lead")), "The Lead block remains");
            require(!BuiltInRegistries.FLUID.containsKey(id("metal/lead")), "The disabled Lead fluid remains");
            require(BuiltInRegistries.ITEM.containsKey(id("metal/ingot/platinum")),
                    "Disabling Lead removed another metal");
            require(BuiltInRegistries.ITEM.containsKey(id("metal/pickaxe/invar"))
                    && BuiltInRegistries.BLOCK.containsKey(id("metal/anvil/invar")),
                    "Other metal tools or anvils were removed");
            require(!BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM,
                    ResourceLocation.parse("c:ingots"))).orElseThrow().stream()
                    .anyMatch(holder -> BuiltInRegistries.ITEM.getKey(holder.value()).equals(id("metal/ingot/lead"))),
                    "A disabled Lead aggregate tag entry remains");
            require(BuiltInRegistries.ITEM.get(id("metal/ingot/platinum")).builtInRegistryHolder().is(
                    TagKey.create(Registries.ITEM,
                            ResourceLocation.fromNamespaceAndPath("c", "ingots/platinum"))),
                    "The aggregate tag lost another metal");
            ItemStack platinum = new ItemStack(BuiltInRegistries.ITEM.get(id("metal/ingot/platinum")));
            require(HeatCapability.getDefinition(platinum) != null
                    && ItemSizeManager.getDefinition(platinum) != null,
                    "Other metal heat or size data were removed");
            require(helper.getLevel().getServer().getRecipeManager().byKey(id("alloy/solder")).isPresent()
                    && helper.getLevel().getServer().getRecipeManager().byKey(id("alloy/osmiridium")).isPresent()
                    && helper.getLevel().getServer().getRecipeManager().byKey(id("heating/metal/ingot/platinum")).isPresent(),
                    "Unrelated alloy or heating recipe was removed");
            require(resource(helper, "tfcm:loot_table/blocks/metal/block/platinum.json")
                    .getAsJsonArray("pools").size() == 1,
                    "Other metal block loot was removed");
            require(helper.getLevel().getServer().getRecipeManager().getRecipes().stream()
                    .noneMatch(recipe -> recipe.id().getNamespace().equals(TFCM.MOD_ID)
                            && recipe.id().getPath().contains("lead")), "A Lead recipe remains loaded");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void disabledOreRemovesOnlyItsSeries(GameTestHelper helper) {
        helper.succeedIf(() -> {
            require(!BuiltInRegistries.ITEM.containsKey(id("ore/normal_galena")), "Galena ore remains registered");
            require(!BuiltInRegistries.ITEM.containsKey(id("ore/small_galena")), "Small Galena remains registered");
            require(!BuiltInRegistries.BLOCK.containsKey(id("ore/normal_galena/netherrack")),
                    "A Galena block remains registered");
            require(!BuiltInRegistries.BLOCK.containsKey(id("ore/normal_galena/end_stone")),
                    "An End Galena block remains registered");
            require(!BuiltInRegistries.BLOCK.getTag(TagKey.create(Registries.BLOCK,
                    ResourceLocation.parse("c:ores"))).orElseThrow().stream()
                    .anyMatch(holder -> BuiltInRegistries.BLOCK.getKey(holder.value()).getNamespace().equals(TFCM.MOD_ID)
                            && BuiltInRegistries.BLOCK.getKey(holder.value()).getPath().contains("galena")),
                    "A disabled Galena block remains in the ore tag");
            for (ProcessingForm form : ProcessingForm.values()) {
                require(!BuiltInRegistries.ITEM.containsKey(id("ore_processing/" + form.id() + "/galena")),
                        "A Galena processing form remains");
            }
            require(BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse("tfc:powder/sphalerite")),
                    "Disabling Galena removed Sphalerite");
            ItemStack sphalerite = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("tfc:powder/sphalerite")));
            require(HeatCapability.getDefinition(sphalerite) != null
                    && ItemSizeManager.getDefinition(sphalerite) != null,
                    "Other ore heat or size data were removed");
            require(helper.getLevel().getServer().getRecipeManager().byKey(
                    id("heating/ore_processing/powder/galena")).isEmpty(),
                    "Galena heating remains loaded");
            require(helper.getLevel().getServer().getRecipeManager().byKey(
                    id("heating/ore_processing/powder/sphalerite")).isPresent(),
                    "Sphalerite heating was removed");
            var pools = resource(helper, "tfcm:loot_table/ore_processing/dirty_dust/sphalerite.json")
                    .getAsJsonArray("pools");
            require(pools.size() == 4 && pools.get(3).getAsJsonObject().get("name").getAsString()
                    .equals("byproduct_dirty_dust_1_percent"), "Sphalerite wash pools changed");
            require(pools.get(3).getAsJsonObject().getAsJsonArray("neoforge:conditions").get(0)
                    .getAsJsonObject().get("ore").getAsString().equals("galena"),
                    "Sphalerite's Galena byproduct lacks its load condition");
            var sandPools = resource(helper, "tfcm:loot_table/surface_resource/sand/black.json")
                    .getAsJsonArray("pools");
            require(sandPools.get(2).getAsJsonObject().getAsJsonArray("neoforge:conditions").get(0)
                    .getAsJsonObject().get("ore").getAsString().equals("galena"),
                    "Black sand's Galena pool lacks its load condition");
            var lootData = helper.getLevel().getServer().reloadableRegistries();
            LootParams lootParams = new LootParams.Builder(helper.getLevel()).create(LootContextParamSets.EMPTY);
            LootTable sphaleriteWash = lootData.getLootTable(ResourceKey.create(Registries.LOOT_TABLE,
                    id("ore_processing/dirty_dust/sphalerite")));
            LootTable blackSand = lootData.getLootTable(ResourceKey.create(Registries.LOOT_TABLE,
                    id("surface_resource/sand/black")));
            for (int seed = 0; seed < 512; seed++) {
                var washDrops = sphaleriteWash.getRandomItems(lootParams, seed);
                require(washDrops.stream().anyMatch(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem())
                        .equals(ResourceLocation.parse("tfc:powder/sphalerite"))),
                        "Sphalerite's primary output was removed");
                require(washDrops.stream().noneMatch(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem())
                        .equals(id("ore_processing/dirty_dust/galena"))),
                        "Loaded Sphalerite loot still drops disabled Galena");
                require(blackSand.getRandomItems(lootParams, seed).stream().noneMatch(stack ->
                        BuiltInRegistries.ITEM.getKey(stack.getItem())
                                .equals(id("ore_processing/dirty_dust/galena"))),
                        "Loaded sand loot still drops disabled Galena");
            }
            require(helper.getLevel().getServer().getRecipeManager().getRecipes().stream()
                    .noneMatch(recipe -> recipe.id().getNamespace().equals(TFCM.MOD_ID)
                            && recipe.id().getPath().endsWith("/galena")), "A Galena recipe remains loaded");
            require(!Deposit.MANAGER.getElements().containsKey(id("ore_processing/rocky_chunks/galena")),
                    "The Galena primary wash deposit remains loaded");
            require(Deposit.MANAGER.getElements().containsKey(id("ore_processing/rocky_chunks/sphalerite")),
                    "Disabling Galena removed the Sphalerite wash deposit");
            String galena = """
                    veins:
                      - id: disabled_galena
                        type: tfc:cluster_vein
                        ymin: 0
                        ymax: 1
                        rocks: [minecraft:netherrack]
                        blocks:
                          - block: tfcm:ore/normal_galena/netherrack
                    """;
            String platinum = galena.replace("disabled_galena", "enabled_platinum")
                    .replace("normal_galena", "normal_native_platinum");
            require(YamlWorldgenLoader.parse("overworld", galena).veins().isEmpty(),
                    "A disabled Galena YAML candidate remains valid");
            require(YamlWorldgenLoader.parse("overworld", platinum).veins().size() == 1,
                    "Disabling Galena removed another YAML ore candidate");
            for (String dimension : new String[] {"nether", "end"}) {
                require(YamlWorldgenLoader.parse(dimension, galena).veins().isEmpty(),
                        "Disabled Galena remains in " + dimension + " worldgen");
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void unknownNamesAreIgnored(GameTestHelper helper) {
        helper.succeedIf(() -> {
            require(TFCMConfig.isMetalEnabled(Metal.PLATINUM), "An unknown metal disabled valid content");
            require(TFCMConfig.isOreEnabled("native_platinum"), "An unknown ore disabled valid content");
            require(BuiltInRegistries.ITEM.containsKey(id("metal/ingot/platinum")), "Valid metal content is missing");
            require(BuiltInRegistries.ITEM.containsKey(id("ore/normal_native_platinum")),
                    "Valid ore content is missing");
        });
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(TFCM.MOD_ID, path);
    }

    private static com.google.gson.JsonObject resource(GameTestHelper helper, String path) {
        try (var stream = helper.getLevel().getServer().getResourceManager().getResourceOrThrow(
                ResourceLocation.parse(path)).open()) {
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception exception) {
            throw new GameTestAssertException("Could not inspect " + path + ": " + exception.getMessage());
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new GameTestAssertException(message);
    }
}
