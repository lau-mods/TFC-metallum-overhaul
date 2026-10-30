package net.claustra01.tfcm.gametest;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.registry.TFCMItems;
import net.dries007.tfc.util.data.Deposit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TFCM.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TFCMSurfaceResourceGameTests {
    private static final String BATCH = "tfcm_foundation";
    private static final List<String> ORES = List.of("native_copper", "native_gold", "native_silver", "cassiterite");
    private static final List<RockOutput> ROCKS = List.of(
            new RockOutput("andesite", "tfc:groundcover/pumice", "tfc:ore/pyrite"),
            new RockOutput("basalt", "tfc:rock/loose/basalt", "tfc:ore/pyrite"),
            new RockOutput("chalk", "tfc:rock/loose/chalk", "tfc:gem/sapphire"),
            new RockOutput("chert", "tfc:rock/loose/chert", "tfc:gem/ruby"),
            new RockOutput("claystone", "tfc:rock/loose/claystone", "tfc:gem/amethyst"),
            new RockOutput("conglomerate", "tfc:rock/loose/conglomerate", "tfc:ore/lignite"),
            new RockOutput("dacite", "tfc:groundcover/pumice", "tfc:ore/pyrite"),
            new RockOutput("diorite", "tfc:rock/loose/diorite", "tfc:gem/emerald"),
            new RockOutput("dolomite", "tfc:rock/loose/dolomite", "tfc:gem/amethyst"),
            new RockOutput("gabbro", "tfc:rock/loose/gabbro", "tfc:gem/diamond"),
            new RockOutput("gneiss", "tfc:rock/loose/gneiss", "tfc:ore/gypsum"),
            new RockOutput("granite", "tfc:rock/loose/granite", "tfc:gem/topaz"),
            new RockOutput("limestone", "tfc:rock/loose/limestone", "tfc:gem/lapis_lazuli"),
            new RockOutput("marble", "tfc:rock/loose/marble", "tfc:gem/lapis_lazuli"),
            new RockOutput("phyllite", "tfc:rock/loose/phyllite", "tfc:ore/pyrite"),
            new RockOutput("quartzite", "tfc:rock/loose/quartzite", "tfc:gem/opal"),
            new RockOutput("rhyolite", "tfc:groundcover/pumice", "tfc:ore/pyrite"),
            new RockOutput("schist", "tfc:rock/loose/schist", "tfc:ore/pyrite"),
            new RockOutput("shale", "tfc:rock/loose/shale", "tfc:ore/borax"),
            new RockOutput("slate", "tfc:rock/loose/slate", "tfc:ore/pyrite"),
            new RockOutput("tuff", "tfc:rock/loose/tuff", "tfc:ore/pyrite"));

    private TFCMSurfaceResourceGameTests() {
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void allDepositsUseFiveIndependentSpecifiedPools(GameTestHelper helper) {
        helper.succeedIf(() -> {
            for (String ore : ORES) {
                for (RockOutput rock : ROCKS) {
                    checkDeposit("tfc:deposit/" + ore + "/" + rock.id(), "tfc:deposit/" + ore + "_" + rock.id(),
                            false);
                    JsonArray pools = resource(helper, "tfc", "loot_table/deposit/" + ore + "_" + rock.id() + ".json");
                    require(pools.size() == 5, "Deposit must have five independent pools: " + ore + "/" + rock.id());
                    checkPool(pools, 0, "tfc:ore/small_" + ore, 0.50f);
                    checkPool(pools, 1, "tfc:ore/small_" + ore, 0.10f);
                    checkPool(pools, 2, "tfc:rock/loose/" + rock.id(), 0.50f);
                    checkPool(pools, 3, rock.first(), 0.25f);
                    checkPool(pools, 4, rock.second(), 0.0533f);
                }
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void allRockGravelUsesSpecifiedPoolsWithoutGoldNuggets(GameTestHelper helper) {
        helper.succeedIf(() -> {
            for (RockOutput rock : ROCKS) {
                checkDeposit("tfc:rock/gravel/" + rock.id(), "tfcm:surface_resource/gravel/" + rock.id(), true);
                JsonArray pools = resource(helper, "tfcm",
                        "loot_table/surface_resource/gravel/" + rock.id() + ".json");
                require(pools.size() == 5, "Gravel must have five independent pools: " + rock.id());
                checkPool(pools, 0, "tfcm:ore_processing/powder/native_copper", 0.50f);
                checkPool(pools, 1, "tfcm:ore_processing/powder/native_copper", 0.25f);
                checkPool(pools, 2, "tfc:rock/loose/" + rock.id(), 0.50f);
                checkPool(pools, 3, "minecraft:flint", 0.15f);
                checkPool(pools, 4, "minecraft:bone", 0.10f);
                require(containsItem(pools, "minecraft:gold_nugget") == false, "Gravel produces a Gold Nugget");
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void sandAndRockPowderUseSpecifiedPoolsWithoutDirtyPile(GameTestHelper helper) {
        helper.succeedIf(() -> {
            checkSand(helper, "black", items("tfcm:ore_processing/sand_pile/white", "tfcm:ore_processing/sand_pile/black",
                    "tfcm:ore_processing/dirty_dust/galena", "tfc:powder/charcoal", "minecraft:bone",
                    "tfc:powder/sulfur", "tfc:gem/sapphire"),
                    chances(1f, 0.30f, 0.05f, 0.05f, 0.03f, 0.01f, 0.001f));
            checkSand(helper, "brown", items("tfcm:ore_processing/sand_pile/white", "tfcm:ore_processing/sand_pile/brown",
                    "tfc:ore/gypsum", "tfc:ore/sylvite", "minecraft:bone", "tfc:gem/topaz"),
                    chances(1f, 0.30f, 0.05f, 0.05f, 0.03f, 0.001f));
            checkSand(helper, "green", items("tfcm:ore_processing/sand_pile/white", "tfcm:ore_processing/sand_pile/green",
                    "tfcm:ore_processing/powder/malachite", "tfcm:ore_processing/powder/bismuthinite",
                    "tfcm:ore_processing/powder/garnierite", "minecraft:bone", "tfc:gem/emerald"),
                    chances(1f, 0.30f, 0.05f, 0.05f, 0.02f, 0.03f, 0.001f));
            checkSand(helper, "pink", items("tfcm:ore_processing/sand_pile/white", "tfcm:ore_processing/sand_pile/pink",
                    "minecraft:redstone", "tfcm:ore_processing/powder/bauxite", "minecraft:bone", "tfc:gem/ruby"),
                    chances(1f, 0.30f, 0.04f, 0.04f, 0.03f, 0.001f));
            checkSand(helper, "red", items("tfcm:ore_processing/sand_pile/white", "tfcm:ore_processing/sand_pile/red",
                    "minecraft:redstone", "tfcm:ore_processing/powder/hematite", "minecraft:bone", "tfc:gem/ruby"),
                    chances(1f, 0.30f, 0.04f, 0.04f, 0.03f, 0.001f));
            checkSand(helper, "white", items("tfcm:ore_processing/sand_pile/white", "minecraft:bone_meal",
                    "tfcm:ore_processing/powder/sphalerite", "tfcm:ore_processing/powder/native_silver",
                    "tfcm:ore/quartz", "minecraft:bone", "tfc:gem/opal"),
                    chances(1f, 0.15f, 0.015f, 0.01f, 0.01f, 0.025f, 0.0005f));
            checkSand(helper, "yellow", items("tfcm:ore_processing/sand_pile/white", "tfcm:ore_processing/sand_pile/yellow",
                    "tfcm:ore_processing/powder/limonite", "tfcm:ore_processing/powder/native_gold",
                    "minecraft:bone", "tfc:ore/pyrite"), chances(1f, 0.30f, 0.08f, 0.03f, 0.03f, 0.001f));
            for (String color : List.of("black", "brown", "green", "pink", "red", "white", "yellow")) {
                ResourceLocation sandPile = ResourceLocation.fromNamespaceAndPath(TFCM.MOD_ID,
                        "ore_processing/sand_pile/" + color);
                require(BuiltInRegistries.ITEM.containsKey(sandPile), "Sand Pile is not registered: " + color);
                checkDeposit("tfc:sand/" + color, "tfcm:surface_resource/sand/" + color, true);
            }

            JsonArray rockPowder = resource(helper, "tfcm", "loot_table/surface_resource/rock_powder.json");
            List<String> outputs = items("tfcm:ore_processing/sand_pile/black", "minecraft:flint",
                    "tfcm:ore_processing/dirty_dust/native_copper", "tfcm:ore_processing/dirty_dust/sphalerite",
                    "tfcm:ore_processing/dirty_dust/native_gold", "tfcm:ore_processing/dirty_dust/native_silver",
                    "tfcm:ore_processing/dirty_dust/magnetite");
            List<Float> chances = chances(0.15f, 0.05f, 0.03f, 0.01f, 0.01f, 0.01f, 0.005f);
            require(rockPowder.size() == outputs.size(), "Rock Powder has the wrong pool count");
            for (int index = 0; index < outputs.size(); index++)
                checkPool(rockPowder, index, outputs.get(index), chances.get(index));
            require(TFCMItems.SAND_PILES.size() == 7, "Seven Sand Piles are not registered");
            checkDeposit("tfcm:ore_processing/rock_powder", "tfcm:surface_resource/rock_powder", true);
        });
    }

    private static void checkSand(GameTestHelper helper, String color, List<String> items, List<Float> chances) {
        JsonArray pools = resource(helper, "tfcm", "loot_table/surface_resource/sand/" + color + ".json");
        require(pools.size() == items.size(), "Sand has the wrong pool count: " + color);
        for (int index = 0; index < items.size(); index++) checkPool(pools, index, items.get(index), chances.get(index));
        require(containsDirtyPile(pools) == false, "Sand produces Dirty Pile: " + color);
    }

    private static void checkPool(JsonArray pools, int index, String item, float chance) {
        JsonObject pool = pools.get(index).getAsJsonObject();
        require(pool.getAsJsonArray("entries").size() == 1, "Pool is not an independent item draw: " + index);
        String actualItem = pool.getAsJsonArray("entries").get(0).getAsJsonObject().get("name").getAsString();
        require(actualItem.equals(item),
                "Wrong item in pool " + index + ": " + item);
        require(BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(actualItem)),
                "Pool item is not registered: " + actualItem);
        require(ResourceLocation.parse(actualItem).getPath().contains("dirty_pile") == false,
                "Pool produces Dirty Pile: " + actualItem);
        if (chance == 1f) {
            require(pool.has("conditions") == false, "Guaranteed pool has a chance condition: " + item);
        } else {
            JsonArray conditions = pool.getAsJsonArray("conditions");
            require(conditions.size() == 1, "Pool has combined chance conditions: " + item);
            JsonObject condition = conditions.get(0).getAsJsonObject();
            require(condition.get("condition").getAsString().equals("minecraft:random_chance")
                    && Math.abs(condition.get("chance").getAsFloat() - chance) < 0.000001f,
                    "Wrong chance for " + item);
        }
    }

    private static JsonArray resource(GameTestHelper helper, String namespace, String path) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(namespace, path);
        try (var stream = helper.getLevel().getServer().getResourceManager().getResourceOrThrow(location).open()) {
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject()
                    .getAsJsonArray("pools");
        } catch (Exception exception) {
            throw new GameTestAssertException("Could not inspect " + location + ": " + exception.getMessage());
        }
    }

    private static List<String> items(String... items) {
        return List.of(items);
    }

    private static List<Float> chances(Float... chances) {
        return List.of(chances);
    }

    private static ItemStack item(String id) {
        ResourceLocation location = ResourceLocation.parse(id);
        require(BuiltInRegistries.ITEM.containsKey(location), "Deposit input is not registered: " + id);
        return BuiltInRegistries.ITEM.get(location).getDefaultInstance();
    }

    private static void checkDeposit(String input, String lootTable, boolean genericModels) {
        Deposit deposit = Deposit.get(item(input));
        require(deposit != null, "TFC Deposit is not loaded: " + input);
        require(deposit.lootTable().location().equals(ResourceLocation.parse(lootTable)),
                "TFC Deposit has the wrong loot table: " + input);
        if (genericModels) {
            List<ResourceLocation> models = List.of(ResourceLocation.parse("tfc:item/pan/full"),
                    ResourceLocation.parse("tfc:item/pan/half"), ResourceLocation.parse("tfc:item/pan/empty"));
            require(deposit.modelStages().equals(models), "TFC Deposit has the wrong generic model stages: " + input);
            for (ResourceLocation model : models)
                require(resourceExists("assets/" + model.getNamespace() + "/models/" + model.getPath() + ".json"),
                        "TFC Deposit model is missing: " + model);
        }
    }

    private static boolean containsItem(JsonArray pools, String item) {
        for (var element : pools)
            if (element.getAsJsonObject().getAsJsonArray("entries").get(0).getAsJsonObject().get("name")
                    .getAsString().equals(item)) return true;
        return false;
    }

    private static boolean containsDirtyPile(JsonArray pools) {
        for (var element : pools) {
            String item = element.getAsJsonObject().getAsJsonArray("entries").get(0).getAsJsonObject().get("name")
                    .getAsString();
            if (ResourceLocation.parse(item).getPath().contains("dirty_pile")) return true;
        }
        return false;
    }

    private static boolean resourceExists(String path) {
        return TFCMSurfaceResourceGameTests.class.getClassLoader().getResource(path) != null;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new GameTestAssertException(message);
    }

    private record RockOutput(String id, String first, String second) {
    }
}
