package net.claustra01.tfcm.datagen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.claustra01.tfcm.common.processing.ProcessingSeries;
import net.claustra01.tfcm.common.processing.ProcessingSeries.Byproduct;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

/** Optional machine routes share TFC's existing processing intermediates. */
public final class MachineProcessingDataProvider implements DataProvider {
    private final Path recipes;

    public MachineProcessingDataProvider(PackOutput output) {
        recipes = output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve("tfcm/recipe/compat");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> files = new ArrayList<>();
        for (ProcessingSeries series : ProcessingSeries.values()) {
            if (series.graded()) {
                addOre(output, files, series, "small", 1, .2f, 1);
                addOre(output, files, series, "poor", 3, .6f, 4);
                addOre(output, files, series, "normal", 6, 0, 7);
                addOre(output, files, series, "rich", 8, .4f, 10);
            } else {
                addOre(output, files, series, null, 8, .4f, 10);
            }

            JsonArray washed = new JsonArray();
            washed.add(stack(item("chunks", series), 1, 1));
            washed.add(stack("tfc:rock/loose/" + series.primaryRock(), 1, .4f));
            washed.add(stack("tfc:rock/loose/" + series.secondaryRock(), 1, .2f));
            if (series.gem() != null) washed.add(stack(ProcessingDataProvider.gem(series.gem()), 1, .001f));
            files.add(save(output, series, "create", "splashing/rocky_chunks/" + series.id(),
                    create("splashing", item("rocky_chunks", series), washed)));

            JsonArray dust = new JsonArray();
            dust.add(stack(item("dirty_dust", series), 1, 1));
            dust.add(stack(item("dirty_dust", series), 1, .25f));
            addByproductVariants(output, files, series, "create", "milling/chunks/" + series.id(),
                    create("milling", item("chunks", series), dust), byproductA(series), .005f);
            JsonObject crusher = crusher(item("chunks", series), item("dirty_dust", series), 1);
            crusher.getAsJsonArray("secondaries").add(secondary(item("dirty_dust", series), .5f));
            addByproductVariants(output, files, series, "immersiveengineering", "crusher/chunks/" + series.id(),
                    crusher, byproductA(series), .02f);

            JsonArray powder = new JsonArray();
            powder.add(stack(series.powderItem(), 1, 1));
            powder.add(stack("tfcm:ore_processing/sand_pile/" + series.sand().id(), 1, .25f));
            powder.add(stack(item("dirty_dust", series), 1, .25f));
            // The machine-route table specifies Native Silver for Mithril Matrix.
            List<Byproduct> byproducts = series == ProcessingSeries.MITHRIL_MATRIX
                    ? List.of(Byproduct.ore("native_silver")) : series.byproducts();
            addByproductVariants(output, files, series, "create", "splashing/dirty_dust/" + series.id(),
                    create("splashing", item("dirty_dust", series), powder), byproducts, .005f);
        }
        return CompletableFuture.allOf(files.toArray(CompletableFuture[]::new));
    }

    private void addOre(CachedOutput output, List<CompletableFuture<?>> files, ProcessingSeries series,
            String grade, int createCount, float createChance, int ieCount) {
        String input = ProcessingDataProvider.oreItem(series, grade);
        String suffix = grade == null ? series.id() : grade + "_" + series.id();
        String result = item("rocky_chunks", series);
        JsonArray outputs = new JsonArray();
        outputs.add(stack(result, createCount, 1));
        if (createChance > 0) outputs.add(stack(result, 1, createChance));
        files.add(save(output, series, "create", "crushing/ore/" + suffix, create("crushing", input, outputs)));
        JsonObject crusher = crusher(input, result, ieCount);
        crusher.getAsJsonArray("secondaries").add(secondary(result, .5f));
        files.add(save(output, series, "immersiveengineering", "crusher/ore/" + suffix, crusher));
    }

    // Mutually exclusive variants keep the main process available even when a
    // byproduct is disabled or requires an absent optional mod (Chromite).
    private void addByproductVariants(CachedOutput output, List<CompletableFuture<?>> files,
            ProcessingSeries series, String mod, String path, JsonObject base, List<Byproduct> byproducts,
            float chance) {
        addByproductVariants(output, files, series, mod, path, base, byproducts, 0, chance);
    }

    private void addByproductVariants(CachedOutput output, List<CompletableFuture<?>> files,
            ProcessingSeries series, String mod, String path, JsonObject base, List<Byproduct> byproducts,
            int index, float chance) {
        if (index == byproducts.size()) {
            files.add(save(output, series, mod, path, base));
            return;
        }
        Byproduct byproduct = byproducts.get(index);
        JsonObject available = ContentConditions.byproduct(byproduct);
        JsonObject with = base.deepCopy();
        if (mod.equals("create")) {
            with.getAsJsonArray("results").add(stack(byproduct.item(), 1, chance));
        } else {
            with.getAsJsonArray("secondaries").add(secondary(byproduct.item(), chance));
        }
        conditions(with).add(available);
        addByproductVariants(output, files, series, mod, path, with, byproducts, index + 1, chance);
        JsonObject not = new JsonObject();
        not.addProperty("type", "neoforge:not");
        not.add("value", available.deepCopy());
        conditions(base).add(not);
        String suffix = byproducts.size() == 1 ? "_without_byproduct" : "_without_" + byproduct.id();
        addByproductVariants(output, files, series, mod, path + suffix, base, byproducts, index + 1, chance);
    }

    private static JsonArray conditions(JsonObject recipe) {
        if (!recipe.has("neoforge:conditions")) recipe.add("neoforge:conditions", new JsonArray());
        return recipe.getAsJsonArray("neoforge:conditions");
    }

    private CompletableFuture<?> save(CachedOutput output, ProcessingSeries series, String mod,
            String path, JsonObject recipe) {
        JsonArray conditions = conditions(recipe);
        JsonObject loaded = new JsonObject();
        loaded.addProperty("type", "neoforge:mod_loaded");
        loaded.addProperty("modid", mod);
        conditions.add(loaded);
        conditions.add(ContentConditions.ore(series.id()));
        if (series.firmaLifeOnly()) conditions.add(ContentConditions.firmaLife());
        return DataProvider.saveStable(output, recipe, recipes.resolve(mod + "/" + path + ".json"));
    }

    private static JsonObject create(String type, String input, JsonArray results) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "create:" + type);
        JsonArray ingredients = new JsonArray();
        ingredients.add(ingredient(input));
        recipe.add("ingredients", ingredients);
        recipe.add("results", results);
        // Create 6.0.10: raw-ore crushing and granite milling baseline values.
        // Splashing must omit duration and uses Create's fan configuration.
        if (type.equals("crushing")) recipe.addProperty("processing_time", 400);
        if (type.equals("milling")) recipe.addProperty("processing_time", 200);
        return recipe;
    }

    private static JsonObject crusher(String input, String result, int count) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "immersiveengineering:crusher");
        recipe.add("input", ingredient(input));
        recipe.add("result", stack(result, count, 1));
        // IE 12.4.2 uses 6000 FE for both ore and raw-ore crushing.
        recipe.addProperty("energy", 6000);
        recipe.add("secondaries", new JsonArray());
        return recipe;
    }

    private static JsonObject secondary(String item, float chance) {
        JsonObject result = new JsonObject();
        result.add("output", stack(item, 1, 1));
        result.addProperty("chance", chance);
        return result;
    }

    private static JsonObject stack(String item, int count, float chance) {
        JsonObject result = new JsonObject();
        result.addProperty("id", item);
        result.addProperty("count", count);
        if (chance < 1) result.addProperty("chance", chance);
        return result;
    }

    private static JsonObject ingredient(String item) {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", item);
        return ingredient;
    }

    private static String item(String form, ProcessingSeries series) {
        return "tfcm:ore_processing/" + form + "/" + series.id();
    }

    private static List<Byproduct> byproductA(ProcessingSeries series) {
        String ore = switch (series) {
            case NATIVE_COPPER -> "malachite";
            case NATIVE_GOLD, MITHRIL_MATRIX -> "native_platinum";
            case HEMATITE, GRAPHITE -> "magnetite";
            case NATIVE_SILVER -> "native_gold";
            case CASSITERITE -> "cryolite";
            case BISMUTHINITE, TETRAHEDRITE, CINNABAR -> "stibnite";
            case GARNIERITE, MAGNETITE -> "chromite";
            case MALACHITE -> "native_copper";
            case LIMONITE, MAGNESITE, CHROMITE -> "garnierite";
            case SPHALERITE -> "tetrahedrite";
            case GALENA -> "native_silver";
            case PYROLUSITE -> "hematite";
            case MONAZITE, BAUXITE, ZIRCON -> "rutile";
            case URANINITE, COBALTITE, WOLFRAMITE -> "bismuthinite";
            case RUTILE, SPODUMENE -> "zircon";
            case STIBNITE, SULFUR -> "cinnabar";
            case NATIVE_PLATINUM, NATIVE_OSMIUM, NATIVE_NAQUADAH -> "native_iridium";
            case THORIANITE -> "uraninite";
            case NATIVE_IRIDIUM -> "native_osmium";
            case CAROBBIITE -> "borax";
            case CRYOLITE -> "cassiterite";
            case BORAX -> "carobbiite";
            default -> null;
        };
        if (ore != null) return List.of(Byproduct.ore(ore));
        return series.byproducts().stream().limit(1).toList();
    }

    @Override
    public String getName() {
        return "TFCM optional Create and IE ore processing";
    }
}
