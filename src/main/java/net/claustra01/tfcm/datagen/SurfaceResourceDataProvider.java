package net.claustra01.tfcm.datagen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public final class SurfaceResourceDataProvider implements DataProvider {
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
    private static final List<SandOutput> SANDS = List.of(
            new SandOutput("black", new Output("tfcm:ore_processing/sand_pile/white", 1f),
                    new Output("tfcm:ore_processing/sand_pile/black", 0.30f),
                    new Output("tfcm:ore_processing/dirty_dust/galena", 0.05f),
                    new Output("tfc:powder/charcoal", 0.05f), new Output("minecraft:bone", 0.03f),
                    new Output("tfcm:ore_processing/powder/sulfur", 0.01f),
                    new Output("tfc:gem/sapphire", 0.001f)),
            new SandOutput("brown", new Output("tfcm:ore_processing/sand_pile/white", 1f),
                    new Output("tfcm:ore_processing/sand_pile/brown", 0.30f),
                    new Output("tfc:ore/gypsum", 0.05f), new Output("tfc:ore/sylvite", 0.05f),
                    new Output("minecraft:bone", 0.03f), new Output("tfc:gem/topaz", 0.001f)),
            new SandOutput("green", new Output("tfcm:ore_processing/sand_pile/white", 1f),
                    new Output("tfcm:ore_processing/sand_pile/green", 0.30f),
                    new Output("tfcm:ore_processing/powder/malachite", 0.05f),
                    new Output("tfcm:ore_processing/powder/bismuthinite", 0.05f),
                    new Output("tfcm:ore_processing/powder/garnierite", 0.02f),
                    new Output("minecraft:bone", 0.03f), new Output("tfc:gem/emerald", 0.001f)),
            new SandOutput("pink", new Output("tfcm:ore_processing/sand_pile/white", 1f),
                    new Output("tfcm:ore_processing/sand_pile/pink", 0.30f),
                    new Output("minecraft:redstone", 0.04f),
                    new Output("tfcm:ore_processing/powder/bauxite", 0.04f),
                    new Output("minecraft:bone", 0.03f), new Output("tfc:gem/ruby", 0.001f)),
            new SandOutput("red", new Output("tfcm:ore_processing/sand_pile/white", 1f),
                    new Output("tfcm:ore_processing/sand_pile/red", 0.30f),
                    new Output("minecraft:redstone", 0.04f),
                    new Output("tfcm:ore_processing/powder/hematite", 0.04f),
                    new Output("minecraft:bone", 0.03f), new Output("tfc:gem/ruby", 0.001f)),
            new SandOutput("white", new Output("tfcm:ore_processing/sand_pile/white", 1f),
                    new Output("minecraft:bone_meal", 0.15f),
                    new Output("tfcm:ore_processing/powder/sphalerite", 0.015f),
                    new Output("tfcm:ore_processing/powder/native_silver", 0.01f),
                    new Output("tfcm:ore/quartz", 0.01f), new Output("minecraft:bone", 0.025f),
                    new Output("tfc:gem/opal", 0.0005f)),
            new SandOutput("yellow", new Output("tfcm:ore_processing/sand_pile/white", 1f),
                    new Output("tfcm:ore_processing/sand_pile/yellow", 0.30f),
                    new Output("tfcm:ore_processing/powder/limonite", 0.08f),
                    new Output("tfcm:ore_processing/powder/native_gold", 0.03f),
                    new Output("minecraft:bone", 0.03f), new Output("tfc:ore/pyrite", 0.001f)));

    private final Path data;
    private final Path assets;

    public SurfaceResourceDataProvider(PackOutput output) {
        data = output.getOutputFolder(PackOutput.Target.DATA_PACK);
        assets = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> files = new ArrayList<>();
        for (RockOutput rock : ROCKS) {
            for (String ore : ORES) {
                JsonObject loot = loot();
                JsonArray pools = loot.getAsJsonArray("pools");
                pools.add(pool("small_ore_50_percent", "tfc:ore/small_" + ore, 0.50f));
                pools.add(pool("small_ore_10_percent", "tfc:ore/small_" + ore, 0.10f));
                pools.add(pool("loose_rock_50_percent", "tfc:rock/loose/" + rock.id(), 0.50f));
                pools.add(pool("primary_material_25_percent", rock.first(), 0.25f));
                pools.add(pool("secondary_material_5_33_percent", rock.second(), 0.0533f));
                files.add(save(output, data, "tfc/loot_table/deposit/" + ore + "_" + rock.id(), loot));
            }

            String lootTable = "tfcm:surface_resource/gravel/" + rock.id();
            files.add(save(output, data, "tfcm/tfc/deposit/surface_resource/gravel/" + rock.id(),
                    deposit("tfc:rock/gravel/" + rock.id(), lootTable)));
            JsonObject gravel = loot();
            JsonArray gravelPools = gravel.getAsJsonArray("pools");
            gravelPools.add(pool("copper_powder_50_percent", powder("native_copper"), 0.50f));
            gravelPools.add(pool("copper_powder_25_percent", powder("native_copper"), 0.25f));
            gravelPools.add(pool("loose_rock_50_percent", "tfc:rock/loose/" + rock.id(), 0.50f));
            gravelPools.add(pool("flint_15_percent", "minecraft:flint", 0.15f));
            gravelPools.add(pool("bone_10_percent", "minecraft:bone", 0.10f));
            files.add(save(output, data, "tfcm/loot_table/surface_resource/gravel/" + rock.id(), gravel));
        }

        for (SandOutput sand : SANDS) {
            String lootTable = "tfcm:surface_resource/sand/" + sand.id();
            files.add(save(output, data, "tfcm/tfc/deposit/surface_resource/sand/" + sand.id(),
                    deposit("tfc:sand/" + sand.id(), lootTable)));
            JsonObject loot = loot();
            JsonArray pools = loot.getAsJsonArray("pools");
            for (int index = 0; index < sand.outputs().size(); index++) {
                Output value = sand.outputs().get(index);
                pools.add(pool("output_" + index, value.item(), value.chance()));
            }
            files.add(save(output, data, "tfcm/loot_table/surface_resource/sand/" + sand.id(), loot));
        }

        files.add(save(output, assets, "tfcm/models/item/ore_processing/rock_powder",
                model("tfc:item/powder/flux")));
        files.add(save(output, data, "tfcm/tfc/deposit/surface_resource/rock_powder",
                deposit("tfcm:ore_processing/rock_powder", "tfcm:surface_resource/rock_powder")));
        JsonObject rockPowder = loot();
        JsonArray pools = rockPowder.getAsJsonArray("pools");
        pools.add(pool("black_sand_pile_15_percent", "tfcm:ore_processing/sand_pile/black", 0.15f));
        pools.add(pool("flint_5_percent", "minecraft:flint", 0.05f));
        pools.add(pool("native_copper_dirty_dust_3_percent", dirtyDust("native_copper"), 0.03f));
        pools.add(pool("sphalerite_dirty_dust_1_percent", dirtyDust("sphalerite"), 0.01f));
        pools.add(pool("native_gold_dirty_dust_1_percent", dirtyDust("native_gold"), 0.01f));
        pools.add(pool("native_silver_dirty_dust_1_percent", dirtyDust("native_silver"), 0.01f));
        pools.add(pool("magnetite_dirty_dust_0_5_percent", dirtyDust("magnetite"), 0.005f));
        files.add(save(output, data, "tfcm/loot_table/surface_resource/rock_powder", rockPowder));
        return CompletableFuture.allOf(files.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "TFCM Pan and Sluice surface resources";
    }

    private static JsonObject loot() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:empty");
        json.add("pools", new JsonArray());
        return json;
    }

    private static JsonObject pool(String poolName, String item, float chance) {
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", item);
        JsonArray entries = new JsonArray();
        entries.add(entry);
        JsonObject pool = new JsonObject();
        pool.addProperty("name", poolName);
        pool.addProperty("rolls", 1);
        pool.add("entries", entries);
        String ore = referencedOre(item);
        if (ore != null) {
            JsonObject loadCondition = ContentConditions.ore(ore);
            JsonArray loadConditions = new JsonArray();
            loadConditions.add(loadCondition);
            pool.add("neoforge:conditions", loadConditions);
        }
        if (chance < 1f) {
            JsonObject condition = new JsonObject();
            condition.addProperty("condition", "minecraft:random_chance");
            condition.addProperty("chance", chance);
            JsonArray conditions = new JsonArray();
            conditions.add(condition);
            pool.add("conditions", conditions);
        }
        return pool;
    }

    private static String referencedOre(String item) {
        if (item.startsWith("tfcm:ore_processing/powder/")
                || item.startsWith("tfcm:ore_processing/dirty_dust/")) {
            return item.substring(item.lastIndexOf('/') + 1);
        }
        if (item.startsWith("tfc:ore/small_")) return item.substring("tfc:ore/small_".length());
        if (item.equals("tfcm:ore/quartz")) return "quartz";
        return null;
    }

    private static JsonObject deposit(String ingredientItem, String lootTable) {
        JsonObject json = new JsonObject();
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", ingredientItem);
        json.add("ingredient", ingredient);
        json.addProperty("loot_table", lootTable);
        JsonArray stages = new JsonArray();
        stages.add("tfc:item/pan/full");
        stages.add("tfc:item/pan/half");
        stages.add("tfc:item/pan/empty");
        json.add("model_stages", stages);
        return json;
    }

    private static JsonObject model(String texture) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", "minecraft:item/generated");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", texture);
        json.add("textures", textures);
        return json;
    }

    private CompletableFuture<?> save(CachedOutput output, Path root, String location, JsonObject json) {
        return DataProvider.saveStable(output, json, root.resolve(location + ".json"));
    }

    private static String powder(String series) {
        return "tfcm:ore_processing/powder/" + series;
    }

    private static String dirtyDust(String series) {
        return "tfcm:ore_processing/dirty_dust/" + series;
    }

    private record RockOutput(String id, String first, String second) {
    }

    private record SandOutput(String id, List<Output> outputs) {
        private SandOutput(String id, Output... outputs) {
            this(id, List.of(outputs));
        }
    }

    private record Output(String item, float chance) {
    }
}
