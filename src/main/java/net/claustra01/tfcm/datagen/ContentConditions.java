package net.claustra01.tfcm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.claustra01.tfcm.common.processing.ProcessingSeries.Byproduct;

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
        return modLoaded("firmalife");
    }

    static JsonObject modLoaded(String mod) {
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "neoforge:mod_loaded");
        condition.addProperty("modid", mod);
        return condition;
    }

    static JsonObject byproduct(Byproduct byproduct) {
        if (byproduct.metalDust()) return metal(byproduct.id());
        JsonObject ore = ore(byproduct.id());
        if (!byproduct.id().equals("chromite")) return ore;
        JsonArray all = new JsonArray();
        all.add(ore);
        all.add(firmaLife());
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "neoforge:and");
        condition.add("values", all);
        return condition;
    }

    private ContentConditions() {
    }
}
