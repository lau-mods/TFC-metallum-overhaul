package net.claustra01.tfcm.datagen;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.claustra01.tfcm.common.metal.Metal;
import net.claustra01.tfcm.common.ore.Ore;
import net.claustra01.tfcm.common.processing.ProcessingForm;
import net.claustra01.tfcm.common.processing.ProcessingSeries;
import net.claustra01.tfcm.common.processing.SandPile;
import net.claustra01.tfcm.registry.TFCMItems;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public final class ProcessingDataProvider implements DataProvider {
    private final Path data;
    private final Path assets;
    private final List<GeneratedFile> generatedFiles = new ArrayList<>();

    public ProcessingDataProvider(PackOutput output) {
        data = output.getOutputFolder(PackOutput.Target.DATA_PACK);
        assets = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> files = new ArrayList<>();
        for (ProcessingSeries series : ProcessingSeries.values()) addSeries(output, files, series);
        addCommonTags(output, files);
        return CompletableFuture.allOf(files.toArray(CompletableFuture[]::new))
                .thenRun(() -> validateNoDirtyPile(generatedFiles));
    }

    @Override
    public String getName() {
        return "TFCM ore processing";
    }

    private void addCommonTags(CachedOutput output, List<CompletableFuture<?>> files) {
        for (ProcessingForm form : ProcessingForm.values()) {
            JsonArray collection = new JsonArray();
            for (ProcessingSeries series : ProcessingSeries.values()) {
                // non-metal Powder other than Borax is a foreign item, not one of the processing forms
                if (form == ProcessingForm.POWDER && series.nonMetal() && series != ProcessingSeries.BORAX) continue;
                String tag = "c:metal/" + form.id() + "/" + series.id();
                JsonArray values = new JsonArray();
                values.add(ContentConditions.optional(item(series, form)));
                JsonObject json = new JsonObject();
                json.add("values", values);
                files.add(save(output, data, "c/tags/item/metal/" + form.id() + "/" + series.id(), json));
                collection.add(ContentConditions.optional("#" + tag));
            }
            JsonObject json = new JsonObject();
            json.add("values", collection);
            files.add(save(output, data, "c/tags/item/metal/" + form.id(), json));
        }
        JsonArray cleanDusts = new JsonArray();
        cleanDusts.add("#c:metal/powder");
        JsonObject json = new JsonObject();
        json.add("values", cleanDusts);
        files.add(save(output, data, "c/tags/item/ores/clean_dusts", json));
    }

    private void addSeries(CachedOutput output, List<CompletableFuture<?>> files, ProcessingSeries series) {
        if (series.graded()) {
            addHammer(output, files, series, "small", 1);
            addHammer(output, files, series, "poor", 3);
            addHammer(output, files, series, "normal", 5);
            addHammer(output, files, series, "rich", 7);
            String namespace = oreNamespace(series);
            if (!namespace.equals("tfcm")) {
                for (String grade : List.of("poor", "normal", "rich")) {
                    files.add(save(output, data, namespace + "/recipe/heating/ore/" + grade + "_" + series.id(),
                            disabledRecipe()));
                }
            }
        } else {
            addHammer(output, files, series, null, series.nonMetal() ? 7 : 5);
        }
        addDeposit(output, files, series, ProcessingForm.ROCKY_CHUNKS);
        files.add(save(output, data, "tfcm/loot_table/ore_processing/rocky_chunks/" + series.id(),
                conditional(series, primaryWash(series))));
        files.add(save(output, data, "tfcm/recipe/quern/ore_processing/" + series.id(),
                conditional(series, quern(item(series, ProcessingForm.CHUNKS),
                        item(series, ProcessingForm.DIRTY_DUST)))));
        addDeposit(output, files, series, ProcessingForm.DIRTY_DUST);
        files.add(save(output, data, "tfcm/loot_table/ore_processing/dirty_dust/" + series.id(),
                conditional(series, secondaryWash(series))));
        addCompression(output, files, series);
        if (series.recoveryFluid() != null) addHeating(output, files, series);
    }

    private void addHammer(CachedOutput output, List<CompletableFuture<?>> files, ProcessingSeries series,
            String grade, int count) {
        String suffix = grade == null ? series.id() : grade + "_" + series.id();
        files.add(save(output, data, "tfcm/recipe/crafting/ore_processing/hammer/" + suffix,
                conditional(series, hammer(oreItem(series, grade), item(series, ProcessingForm.ROCKY_CHUNKS), count))));
    }

    private void addCompression(CachedOutput output, List<CompletableFuture<?>> files, ProcessingSeries series) {
        files.add(save(output, data, "tfcm/recipe/crafting/ore_processing/pellet/" + series.id(),
                conditional(series, shapeless(item(series, ProcessingForm.POWDER), 4,
                        item(series, ProcessingForm.PELLET), 1))));
        files.add(save(output, data, "tfcm/recipe/crafting/ore_processing/briquet/" + series.id(),
                conditional(series, shapeless(item(series, ProcessingForm.PELLET), 4,
                        item(series, ProcessingForm.BRIQUET), 1))));
        files.add(save(output, data, "tfcm/recipe/crafting/ore_processing/unpack_pellet/" + series.id(),
                conditional(series, shapeless(item(series, ProcessingForm.PELLET), 1,
                        item(series, ProcessingForm.POWDER), 4))));
        files.add(save(output, data, "tfcm/recipe/crafting/ore_processing/unpack_briquet/" + series.id(),
                conditional(series, shapeless(item(series, ProcessingForm.BRIQUET), 1,
                        item(series, ProcessingForm.PELLET), 4))));
    }

    private void addHeating(CachedOutput output, List<CompletableFuture<?>> files, ProcessingSeries series) {
        addHeating(output, files, series, ProcessingForm.POWDER, 5);
        addHeating(output, files, series, ProcessingForm.PELLET, 20);
        addHeating(output, files, series, ProcessingForm.BRIQUET, 80);
    }

    private void addHeating(CachedOutput output, List<CompletableFuture<?>> files, ProcessingSeries series,
            ProcessingForm form, int amount) {
        files.add(save(output, data, "tfcm/recipe/heating/ore_processing/" + form.id() + "/" + series.id(),
                conditionalRecovery(series, heating(item(series, form), series.recoveryFluid(), amount,
                        series.recoveryTemperature()))));
        files.add(save(output, data, "tfcm/tfc/item_heat/ore_processing/" + form.id() + "/" + series.id(),
                conditionalRecovery(series, itemHeat(foreignIngredient(series, form), amount, specificHeat(series)))));
    }

    private static JsonObject primaryWash(ProcessingSeries series) {
        JsonObject json = loot();
        JsonArray pools = json.getAsJsonArray("pools");
        pools.add(pool("chunk", item(series, ProcessingForm.CHUNKS), null));
        pools.add(pool("primary_rock_30_percent", "tfc:rock/loose/" + series.primaryRock(), 0.30f));
        pools.add(pool("secondary_rock_15_percent", "tfc:rock/loose/" + series.secondaryRock(), 0.15f));
        if (series.gem() != null) pools.add(pool("gem_0_02_percent", gem(series.gem()), 0.0002f));
        return json;
    }

    private static JsonObject secondaryWash(ProcessingSeries series) {
        JsonObject json = loot();
        JsonArray pools = json.getAsJsonArray("pools");
        pools.add(pool("powder", item(series, ProcessingForm.POWDER), null));
        pools.add(pool("sand_pile_15_percent", "tfcm:ore_processing/sand_pile/" + series.sand().id(), 0.15f));
        pools.add(pool("same_dirty_dust_5_percent", item(series, ProcessingForm.DIRTY_DUST), 0.05f));
        JsonObject byproduct = pool("byproduct_dirty_dust_1_percent",
                "tfcm:ore_processing/dirty_dust/" + series.byproduct(), 0.01f,
                ContentConditions.ore(series.byproduct()));
        if (series.byproduct().equals(ProcessingSeries.CHROMITE.id()))
            byproduct.getAsJsonArray("neoforge:conditions").add(ContentConditions.firmaLife());
        pools.add(byproduct);
        return json;
    }

    private static JsonObject loot() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:empty");
        json.add("pools", new JsonArray());
        return json;
    }

    private static JsonObject pool(String name, String item, Float chance) {
        return pool(name, item, chance, null);
    }

    private static JsonObject pool(String name, String item, Float chance, JsonObject loadCondition) {
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", item);
        JsonArray entries = new JsonArray();
        entries.add(entry);
        JsonObject pool = new JsonObject();
        pool.addProperty("name", name);
        pool.addProperty("rolls", 1);
        pool.add("entries", entries);
        if (loadCondition != null) {
            JsonArray loadConditions = new JsonArray();
            loadConditions.add(loadCondition);
            pool.add("neoforge:conditions", loadConditions);
        }
        if (chance != null) {
            JsonObject condition = new JsonObject();
            condition.addProperty("condition", "minecraft:random_chance");
            condition.addProperty("chance", chance);
            JsonArray conditions = new JsonArray();
            conditions.add(condition);
            pool.add("conditions", conditions);
        }
        return pool;
    }

    // Chromite's Deposits are added in code, see TFCM; its Pan models are the same.
    private void addDeposit(CachedOutput output, List<CompletableFuture<?>> files, ProcessingSeries series,
            ProcessingForm form) {
        String name = form.id() + "/" + series.id();
        JsonArray stages = SurfaceResourceDataProvider.panStages(output, files, assets, name,
                "tfcm:item/ore_processing/" + name);
        if (series.firmaLifeOnly()) return;
        JsonObject json = new JsonObject();
        json.add("ingredient", ingredient(item(series, form)));
        json.addProperty("loot_table", "tfcm:ore_processing/" + name);
        json.add("model_stages", stages);
        files.add(save(output, data, "tfcm/tfc/deposit/ore_processing/" + name, conditional(series, json)));
    }

    // TFC's item heat ignores load conditions, so Chromite's items are only reached through their optional tag
    private static JsonObject foreignIngredient(ProcessingSeries series, ProcessingForm form) {
        return series.firmaLifeOnly() ? tagIngredient("c:metal/" + form.id() + "/" + series.id())
                : ingredient(item(series, form));
    }

    private static JsonObject hammer(String input, String output, int count) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "tfc:advanced_shapeless_crafting");
        JsonArray ingredients = new JsonArray();
        ingredients.add(tagIngredient("c:tools/hammer"));
        ingredients.add(ingredient(input));
        json.add("ingredients", ingredients);
        json.add("primary_ingredient", tagIngredient("c:tools/hammer"));
        JsonObject modifier = new JsonObject();
        modifier.addProperty("type", "tfc:damage_crafting_remainder");
        JsonArray modifiers = new JsonArray();
        modifiers.add(modifier);
        JsonObject remainder = new JsonObject();
        remainder.add("modifiers", modifiers);
        json.add("remainder", remainder);
        json.add("result", result(output, count));
        return json;
    }

    private static JsonObject quern(String input, String output) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "tfc:quern");
        json.add("ingredient", ingredient(input));
        json.add("result", result(output, 1));
        return json;
    }

    private static JsonObject shapeless(String input, int inputCount, String output, int outputCount) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:crafting_shapeless");
        JsonArray ingredients = new JsonArray();
        for (int i = 0; i < inputCount; i++) ingredients.add(ingredient(input));
        json.add("ingredients", ingredients);
        json.add("result", result(output, outputCount));
        return json;
    }

    private static JsonObject heating(String input, String fluid, int amount, int temperature) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "tfc:heating");
        json.add("ingredient", ingredient(input));
        JsonObject result = new JsonObject();
        result.addProperty("id", fluid);
        result.addProperty("amount", amount);
        json.add("result_fluid", result);
        json.addProperty("temperature", temperature);
        return json;
    }

    private static JsonObject itemHeat(JsonObject ingredient, int amount, float specificHeat) {
        JsonObject json = new JsonObject();
        json.add("ingredient", ingredient);
        // TFC metal item heat scales the fluid specific heat by contained metal amount.
        json.addProperty("heat_capacity", specificHeat * 333.33334f * amount / 100f);
        return json;
    }

    private static JsonObject ingredient(String item) {
        JsonObject json = new JsonObject();
        json.addProperty("item", item);
        return json;
    }

    private static JsonObject tagIngredient(String tag) {
        JsonObject json = new JsonObject();
        json.addProperty("tag", tag);
        return json;
    }

    private static JsonObject result(String item, int count) {
        JsonObject json = new JsonObject();
        json.addProperty("id", item);
        json.addProperty("count", count);
        return json;
    }

    private static JsonObject conditional(ProcessingSeries series, JsonObject json) {
        JsonArray conditions = json.has("neoforge:conditions")
                ? json.getAsJsonArray("neoforge:conditions") : new JsonArray();
        conditions.add(ContentConditions.ore(series.id()));
        if (series.firmaLifeOnly()) conditions.add(ContentConditions.firmaLife());
        json.add("neoforge:conditions", conditions);
        return json;
    }

    private static JsonObject conditionalRecovery(ProcessingSeries series, JsonObject json) {
        conditional(series, json);
        if (series.recoveryFluid().startsWith("tfcm:metal/")) {
            String id = series.recoveryFluid().substring("tfcm:metal/".length());
            json.getAsJsonArray("neoforge:conditions").add(ContentConditions.metal(id));
        }
        return json;
    }

    static JsonObject disabledRecipe() {
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "neoforge:false");
        JsonArray conditions = new JsonArray();
        conditions.add(condition);
        JsonObject json = new JsonObject();
        json.add("neoforge:conditions", conditions);
        return json;
    }

    private CompletableFuture<?> save(CachedOutput output, Path root, String location, JsonObject json) {
        generatedFiles.add(new GeneratedFile(location, json.toString()));
        return DataProvider.saveStable(output, json, root.resolve(location + ".json"));
    }

    private static String item(ProcessingSeries series, ProcessingForm form) {
        return form == ProcessingForm.POWDER ? series.powderItem()
                : "tfcm:ore_processing/" + form.id() + "/" + series.id();
    }

    private static String oreItem(ProcessingSeries series, String grade) {
        String namespace = oreNamespace(series);
        String name = grade == null ? series.id()
                : grade.equals("small") ? "small_" + series.id() : grade + "_" + series.id();
        return namespace + ":ore/" + name;
    }

    private static String oreNamespace(ProcessingSeries series) {
        if (series.firmaLifeOnly()) return "firmalife";
        for (Ore ore : Ore.values()) if (ore.name().equals(series.name())) return "tfcm";
        return "tfc";
    }

    private static String gem(String gem) {
        return gem.equals("quartz") ? "tfcm:ore/quartz" : "tfc:gem/" + gem;
    }

    private static Ore ore(ProcessingSeries series) {
        for (Ore ore : Ore.values()) if (ore.name().equals(series.name())) return ore;
        return null;
    }

    private static float specificHeat(ProcessingSeries series) {
        ResourceLocation fluid = ResourceLocation.parse(series.recoveryFluid());
        if (fluid.getNamespace().equals("tfcm")) {
            for (Metal metal : Metal.values()) if (metal.id().equals(fluid.getPath().substring("metal/".length())))
                return metal.specificHeat();
        }
        return switch (fluid.getPath()) {
            case "metal/gold" -> 0.005f;
            case "metal/silver", "metal/nickel" -> 0.00625f;
            case "metal/tin", "metal/bismuth" -> 0.021428572f;
            case "metal/zinc" -> 0.014285715f;
            default -> 0.008571429f;
        };
    }

    private void validateNoDirtyPile(List<GeneratedFile> generatedFiles) {
        for (GeneratedFile file : generatedFiles) {
            if (file.path().contains("dirty_pile") || file.contents().contains("dirty_pile"))
                throw new IllegalStateException("Dirty Pile reference generated in " + file.path());
        }
        TFCMItems.ITEMS.getEntries().forEach(item -> {
            if (item.getId().getPath().contains("dirty_pile"))
                throw new IllegalStateException("Dirty Pile item is registered: " + item.getId());
        });
        for (Path root : List.of(data, assets)) {
            if (!Files.exists(root)) continue;
            try (var paths = Files.walk(root)) {
                paths.filter(Files::isRegularFile).forEach(path -> {
                    if (path.toString().contains("dirty_pile"))
                        throw new IllegalStateException("Dirty Pile resource exists: " + path);
                    if (path.toString().endsWith(".json")) {
                        try {
                            if (Files.readString(path, StandardCharsets.UTF_8).contains("dirty_pile"))
                                throw new IllegalStateException("Dirty Pile resource reference exists: " + path);
                        } catch (IOException exception) {
                            throw new UncheckedIOException(exception);
                        }
                    }
                });
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        }
    }

    private record GeneratedFile(String path, String contents) {
    }
}
