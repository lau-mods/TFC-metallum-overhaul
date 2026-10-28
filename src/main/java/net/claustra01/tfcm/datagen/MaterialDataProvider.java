package net.claustra01.tfcm.datagen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.claustra01.tfcm.common.material.MaterialPart;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public final class MaterialDataProvider implements DataProvider {
    private static final Map<MaterialPart, String> TEXTURES = Map.ofEntries(
            Map.entry(MaterialPart.LEATHER_STRING, "minecraft:item/string"),
            Map.entry(MaterialPart.LEATHER_STRAP, "minecraft:item/leather"),
            Map.entry(MaterialPart.LEATHER_STRIP, "minecraft:item/leather"),
            Map.entry(MaterialPart.PLANT_FIBER, "tfc:item/jute_fiber"),
            Map.entry(MaterialPart.PLANT_STRING, "tfc:item/rope"),
            Map.entry(MaterialPart.PLANT_FABRIC, "tfc:item/burlap_cloth"),
            Map.entry(MaterialPart.PLANT_CLOTHING, "minecraft:item/leather_chestplate"),
            Map.entry(MaterialPart.BAMBOO_FIBER, "tfc:item/jute_fiber"),
            Map.entry(MaterialPart.BAMBOO_STRING, "tfc:item/rope"),
            Map.entry(MaterialPart.BOW_LIMB, "minecraft:item/stick"),
            Map.entry(MaterialPart.BOW_CHORD, "minecraft:item/string"),
            Map.entry(MaterialPart.BOW_GRIP, "minecraft:item/leather"),
            Map.entry(MaterialPart.CROSSBOW_LIMB, "minecraft:item/stick"),
            Map.entry(MaterialPart.CROSSBOW_CHORD, "minecraft:item/string"),
            Map.entry(MaterialPart.CROSSBOW_STOCK, "minecraft:item/stick"),
            Map.entry(MaterialPart.CROSSBOW_TRIGGER, "minecraft:item/tripwire_hook"),
            Map.entry(MaterialPart.ARROW_TREAD, "minecraft:item/flint"),
            Map.entry(MaterialPart.ARROW_FLETCH, "minecraft:item/feather"),
            Map.entry(MaterialPart.ARROW_SHAFT, "minecraft:item/stick"),
            Map.entry(MaterialPart.TOOL_HANDLE, "minecraft:item/stick"),
            Map.entry(MaterialPart.TOOL_BINDING, "minecraft:item/string"),
            Map.entry(MaterialPart.TOOL_RIVETING, "minecraft:item/iron_nugget"),
            Map.entry(MaterialPart.SWORD_CROSSGUARD, "minecraft:item/iron_nugget"),
            Map.entry(MaterialPart.SWORD_POMMEL, "minecraft:item/iron_nugget"));

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
            files.add(save(output, assets, "tfcm/models/item/" + part.id(), itemModel(part)));
        }
        files.add(save(output, data, "tfcm/tfc/item_size/material/plant_block", size("tfcm:plant_block", "large", "medium")));
        files.add(save(output, assets, "tfcm/models/item/plant_block", blockItemModel()));
        files.add(save(output, assets, "tfcm/models/block/plant_block", blockModel()));
        files.add(save(output, assets, "tfcm/blockstates/plant_block", blockState()));
        files.add(save(output, data, "tfcm/loot_table/blocks/plant_block", plantBlockLoot()));
        files.add(save(output, data, "neoforge/loot_modifiers/global_loot_modifiers", globalLootModifiers()));
        files.add(save(output, data, "tfcm/loot_modifiers/plant_fiber_from_grass", grassLootModifier()));

        addRecipes(output, files);
        return CompletableFuture.allOf(files.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "TFCM material parts";
    }

    private void addRecipes(CachedOutput output, List<CompletableFuture<?>> files) {
        files.add(recipe(output, "leather_strip", advanced("tfcm:leather_strip", 4, "minecraft:leather")));
        files.add(recipe(output, "leather_string", advanced("tfcm:leather_string", 2, "tfcm:leather_strip")));
        files.add(recipe(output, "leather_strap", shapeless("tfcm:leather_strap", 1, "tfcm:leather_strip", "tfcm:leather_strip")));
        files.add(recipe(output, "plant_string", shapeless("tfcm:plant_string", 1, "tfcm:plant_fiber", "tfcm:plant_fiber", "tfcm:plant_fiber")));
        files.add(recipe(output, "plant_fabric", shapeless("tfcm:plant_fabric", 1, "tfcm:plant_string", "tfcm:plant_string", "tfcm:plant_string", "tfcm:plant_string")));
        files.add(recipe(output, "plant_block", shapeless("tfcm:plant_block", 1, "tfcm:plant_fabric", "tfcm:plant_fabric", "tfcm:plant_fabric", "tfcm:plant_fabric")));
        files.add(recipe(output, "plant_clothing", shapeless("tfcm:plant_clothing", 1, "tfcm:plant_fabric", "tfcm:plant_fabric", "tfcm:plant_fabric")));
        files.add(recipe(output, "bamboo_fiber", advanced("tfcm:bamboo_fiber", 4, "minecraft:bamboo")));
        files.add(recipe(output, "bamboo_string", shapeless("tfcm:bamboo_string", 1, "tfcm:bamboo_fiber", "tfcm:bamboo_fiber", "tfcm:bamboo_fiber")));
        files.add(recipe(output, "bow_limb", advanced("tfcm:bow_limb", 1, "#c:rods/wooden", "#c:rods/wooden")));
        files.add(recipe(output, "bow_chord", shapeless("tfcm:bow_chord", 1, "tfcm:plant_string", "tfcm:plant_string", "tfcm:plant_string")));
        files.add(recipe(output, "bow_grip", advanced("tfcm:bow_grip", 1, "tfcm:leather_strap")));
        files.add(recipe(output, "bow", shapeless("minecraft:bow", 1, "tfcm:bow_limb", "tfcm:bow_limb", "tfcm:bow_chord", "tfcm:bow_grip")));
        files.add(recipe(output, "crossbow_limb", advanced("tfcm:crossbow_limb", 1, "#c:rods/wooden", "#c:rods/wooden", "#c:rods/wooden")));
        files.add(recipe(output, "crossbow_chord", shapeless("tfcm:crossbow_chord", 1, "tfcm:bamboo_string", "tfcm:bamboo_string", "tfcm:bamboo_string")));
        files.add(recipe(output, "crossbow_stock", advanced("tfcm:crossbow_stock", 1, "#c:rods/wooden", "#c:rods/wooden")));
        files.add(recipe(output, "crossbow_trigger", shapeless("tfcm:crossbow_trigger", 1, "minecraft:tripwire_hook")));
        files.add(recipe(output, "crossbow", shapeless("minecraft:crossbow", 1, "tfcm:crossbow_limb", "tfcm:crossbow_chord", "tfcm:crossbow_stock", "tfcm:crossbow_trigger")));
        files.add(recipe(output, "arrow_tread", advanced("tfcm:arrow_tread", 2, "minecraft:flint")));
        files.add(recipe(output, "arrow_fletch", advanced("tfcm:arrow_fletch", 2, "#c:feathers")));
        files.add(recipe(output, "arrow_shaft", advanced("tfcm:arrow_shaft", 2, "#c:rods/wooden")));
        files.add(recipe(output, "arrow", shapeless("minecraft:arrow", 2, "tfcm:arrow_tread", "tfcm:arrow_fletch", "tfcm:arrow_shaft", "tfcm:plant_string")));
        files.add(recipe(output, "tool_handle", advanced("tfcm:tool_handle", 1, "#c:rods/wooden", "tfcm:leather_strap")));
        files.add(recipe(output, "tool_binding", shapeless("tfcm:tool_binding", 1, "tfcm:leather_string", "tfcm:leather_strap")));
        files.add(recipe(output, "tool_riveting", shapeless("tfcm:tool_riveting", 1, "#c:rivets", "tfcm:leather_strap")));
        files.add(recipe(output, "sword_crossguard", shapeless("tfcm:sword_crossguard", 1, "tfcm:tool_riveting")));
        files.add(recipe(output, "sword_pommel", shapeless("tfcm:sword_pommel", 1, "tfcm:tool_riveting")));
    }

    private CompletableFuture<?> recipe(CachedOutput output, String name, JsonObject json) {
        return save(output, data, "tfcm/recipe/crafting/material/" + name, json);
    }

    private CompletableFuture<?> save(CachedOutput output, Path root, String location, JsonObject json) {
        return DataProvider.saveStable(output, json, root.resolve(location + ".json"));
    }

    private static JsonObject advanced(String result, int count, String... inputs) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "tfc:advanced_shapeless_crafting");
        JsonArray ingredients = ingredients(inputs);
        JsonObject knife = new JsonObject();
        knife.addProperty("tag", "c:tools/knife");
        ingredients.add(knife);
        json.add("ingredients", ingredients);
        json.add("primary_ingredient", knife);
        JsonObject damage = new JsonObject();
        damage.addProperty("type", "tfc:damage_crafting_remainder");
        JsonArray modifiers = new JsonArray();
        modifiers.add(damage);
        JsonObject remainder = new JsonObject();
        remainder.add("modifiers", modifiers);
        json.add("remainder", remainder);
        json.add("result", result(result, count));
        return json;
    }

    private static JsonObject shapeless(String result, int count, String... inputs) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:crafting_shapeless");
        json.add("ingredients", ingredients(inputs));
        json.add("result", result(result, count));
        return json;
    }

    private static JsonArray ingredients(String... inputs) {
        JsonArray ingredients = new JsonArray();
        for (String input : inputs) {
            JsonObject ingredient = new JsonObject();
            ingredient.addProperty(input.startsWith("#") ? "tag" : "item", input.startsWith("#") ? input.substring(1) : input);
            ingredients.add(ingredient);
        }
        return ingredients;
    }

    private static JsonObject result(String id, int count) {
        JsonObject result = new JsonObject();
        result.addProperty("id", id);
        result.addProperty("count", count);
        return result;
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

    private static JsonObject itemModel(MaterialPart part) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", "minecraft:item/generated");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", TEXTURES.get(part));
        json.add("textures", textures);
        return json;
    }

    private static JsonObject blockModel() {
        JsonObject json = new JsonObject();
        json.addProperty("parent", "minecraft:block/cube_all");
        JsonObject textures = new JsonObject();
        textures.addProperty("all", "tfc:block/thatch");
        json.add("textures", textures);
        return json;
    }

    private static JsonObject blockItemModel() {
        JsonObject json = new JsonObject();
        json.addProperty("parent", "tfcm:block/plant_block");
        return json;
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

    private static JsonObject globalLootModifiers() {
        JsonObject json = new JsonObject();
        json.addProperty("replace", false);
        JsonArray entries = new JsonArray();
        entries.add("tfcm:plant_fiber_from_grass");
        json.add("entries", entries);
        return json;
    }

    private static JsonObject grassLootModifier() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "tfcm:grass_plant_fiber");
        json.add("conditions", new JsonArray());
        return json;
    }

}
