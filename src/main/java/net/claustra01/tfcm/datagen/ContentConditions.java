package net.claustra01.tfcm.datagen;

import com.google.gson.JsonObject;

final class ContentConditions {
    static JsonObject optional(String id) {
        JsonObject entry = new JsonObject();
        entry.addProperty("id", id);
        entry.addProperty("required", false);
        return entry;
    }

    static JsonObject ore(String id) {
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "tfcm:ore_enabled");
        condition.addProperty("ore", id);
        return condition;
    }

    static JsonObject metal(String id) {
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "tfcm:metal_enabled");
        condition.addProperty("metal", id);
        return condition;
    }

    static JsonObject firmaLife() {
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "neoforge:mod_loaded");
        condition.addProperty("modid", "firmalife");
        return condition;
    }

    private ContentConditions() {
    }
}
