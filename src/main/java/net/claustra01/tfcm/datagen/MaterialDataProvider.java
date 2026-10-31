package net.claustra01.tfcm.datagen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.claustra01.tfcm.common.material.MaterialPart;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public final class MaterialDataProvider implements DataProvider {
    private final Path data;
    private final Path assets;

    public MaterialDataProvider(PackOutput output) {
        data = output.getOutputFolder(PackOutput.Target.DATA_PACK);
        assets = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> files = new ArrayList<>();
        for (MaterialPart part : MaterialPart.values()) {
            files.add(save(output, data, "tfcm/tfc/item_size/material/" + part.id(), size(part)));
        }
        // the assembled recipes in crafting/material replace these
        for (String recipe : List.of("minecraft/recipe/bow", "minecraft/recipe/crossbow", "minecraft/recipe/arrow",
                "tfc/recipe/crafting/arrow")) {
            files.add(save(output, data, recipe, ProcessingDataProvider.disabledRecipe()));
        }
        files.add(save(output, data, "tfcm/tfc/item_size/material/plant_block", size("tfcm:plant_block", "large", "medium")));
        files.add(save(output, assets, "tfcm/models/item/plant_block", blockItemModel()));
        files.add(save(output, assets, "tfcm/blockstates/plant_block", blockState()));
        files.add(save(output, data, "tfcm/loot_table/blocks/plant_block", plantBlockLoot()));

        return CompletableFuture.allOf(files.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "TFCM material parts";
    }

    private CompletableFuture<?> save(CachedOutput output, Path root, String location, JsonObject json) {
        return DataProvider.saveStable(output, json, root.resolve(location + ".json"));
    }

    private static JsonObject size(MaterialPart part) {
        return size("tfcm:" + part.id(), part.size(), part.weight());
    }

    private static JsonObject size(String id, String size, String weight) {
        JsonObject json = new JsonObject();
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", id);
        json.add("ingredient", ingredient);
        json.addProperty("size", size);
        json.addProperty("weight", weight);
        return json;
    }

    private static JsonObject blockItemModel() {
        JsonObject json = new JsonObject();
        json.addProperty("parent", "tfcm:block/plant_block");
        JsonObject thirdPerson = new JsonObject();
        thirdPerson.add("rotation", floats(10, -45, 170));
        thirdPerson.add("translation", floats(0, 1.5f, -2.75f));
        thirdPerson.add("scale", floats(.375f, .375f, .375f));
        JsonObject display = new JsonObject();
        display.add("thirdperson_righthand", thirdPerson);
        json.add("display", display);
        return json;
    }

    private static JsonArray floats(float x, float y, float z) {
        JsonArray array = new JsonArray();
        array.add(x);
        array.add(y);
        array.add(z);
        return array;
    }

    private static JsonObject blockState() {
        JsonObject json = new JsonObject();
        JsonObject variants = new JsonObject();
        JsonObject model = new JsonObject();
        model.addProperty("model", "tfcm:block/plant_block");
        variants.add("", model);
        json.add("variants", variants);
        return json;
    }

    private static JsonObject plantBlockLoot() {
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", "tfcm:plant_block");
        JsonArray entries = new JsonArray();
        entries.add(entry);
        JsonObject pool = new JsonObject();
        pool.add("entries", entries);
        pool.addProperty("rolls", 1);
        JsonArray pools = new JsonArray();
        pools.add(pool);
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:block");
        json.add("pools", pools);
        json.addProperty("random_sequence", "tfcm:blocks/plant_block");
        return json;
    }
}
