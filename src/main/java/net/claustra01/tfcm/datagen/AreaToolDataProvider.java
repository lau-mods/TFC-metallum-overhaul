package net.claustra01.tfcm.datagen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.claustra01.tfcm.common.tool.area.AreaToolMetal;
import net.claustra01.tfcm.common.tool.area.AreaToolType;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public final class AreaToolDataProvider implements DataProvider {
    private final Path data;
    private final Path assets;

    public AreaToolDataProvider(PackOutput output) {
        data = output.getOutputFolder(PackOutput.Target.DATA_PACK);
        assets = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> files = new ArrayList<>();
        for (AreaToolType type : AreaToolType.values()) {
            files.add(save(output, data, "tfcm/recipe/knapping/ceramic/unfired_" + type.id() + "_head_mold",
                    knapping(type)));
            files.add(save(output, data, "tfcm/recipe/heating/ceramic/" + type.id() + "_head_mold",
                    moldHeating(type)));
            files.add(save(output, assets, "tfcm/models/item/ceramic/unfired_" + type.id() + "_head_mold",
                    moldModel(type, true)));
            files.add(save(output, assets, "tfcm/models/item/ceramic/" + type.id() + "_head_mold",
                    moldModel(type, false)));
            for (AreaToolMetal metal : AreaToolMetal.values()) {
                files.add(save(output, data, "tfcm/recipe/anvil/metal/" + type.id() + "_head/" + metal.id(),
                        anvil(metal, type)));
                if (metal.isCastable()) files.add(save(output, data,
                        "tfcm/recipe/casting/" + metal.id() + "_" + type.id() + "_head", casting(metal, type)));
                files.add(save(output, data, "tfcm/recipe/crafting/metal/" + type.id() + "/" + metal.id(),
                        assembly(metal, type)));
                files.add(save(output, data, "tfcm/recipe/heating/metal/" + type.id() + "_head/" + metal.id(),
                        heating(metal, type, true)));
                files.add(save(output, data, "tfcm/recipe/heating/metal/" + type.id() + "/" + metal.id(),
                        heating(metal, type, false)));
                files.add(save(output, data, "tfcm/tfc/item_heat/" + metal.id() + "/" + type.id(),
                        itemHeat(metal, type)));
                files.add(save(output, data, "tfcm/tfc/item_size/" + metal.id() + "/" + type.id(),
                        itemSize(metal, type, false)));
                files.add(save(output, data, "tfcm/tfc/item_size/" + metal.id() + "/" + type.id() + "_head",
                        itemSize(metal, type, true)));
                files.add(save(output, assets, "tfcm/models/item/metal/" + type.id() + "/" + metal.id(),
                        toolModel(metal, type)));
                files.add(save(output, assets, "tfcm/models/item/metal/" + type.id() + "_head/" + metal.id(),
                        headModel(metal, type)));
            }
        }
        files.add(save(output, data, "tfcm/tfc/item_size/area_tool_molds", moldSize()));
        files.add(save(output, data, "tfc/tags/item/unfired_molds", moldTag(true)));
        files.add(save(output, data, "tfc/tags/item/fired_molds", moldTag(false)));
        files.add(save(output, data, "c/tags/item/tools/mining_hammer", toolTag(AreaToolType.MINING_HAMMER)));
        files.add(save(output, data, "c/tags/item/tools/quarry_hammer", toolTag(AreaToolType.QUARRY_HAMMER)));
        files.add(save(output, data, "c/tags/item/tools/excavator", toolTag(AreaToolType.EXCAVATOR)));
        return CompletableFuture.allOf(files.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "TFCM M09 Area Mining Tools";
    }

    private static CompletableFuture<?> save(CachedOutput output, Path root, String location, JsonObject json) {
        return DataProvider.saveStable(output, json, root.resolve(location + ".json"));
    }

    private static JsonObject conditioned(AreaToolMetal metal) {
        JsonObject json = new JsonObject();
        if (metal.isTFCM()) {
            JsonArray conditions = new JsonArray();
            JsonObject condition = new JsonObject();
            condition.addProperty("type", "tfcm:metal_enabled");
            condition.addProperty("metal", metal.id());
            conditions.add(condition);
            json.add("neoforge:conditions", conditions);
        }
        return json;
    }

    private static JsonObject item(String id) {
        JsonObject json = new JsonObject();
        json.addProperty("item", id);
        return json;
    }

    private static JsonObject tag(String id) {
        JsonObject json = new JsonObject();
        json.addProperty("tag", id);
        return json;
    }

    private static JsonObject result(String id) {
        JsonObject json = new JsonObject();
        json.addProperty("count", 1);
        json.addProperty("id", id);
        return json;
    }

    private static String id(AreaToolMetal metal, AreaToolType type, boolean head) {
        return "tfcm:metal/" + type.id() + (head ? "_head/" : "/") + metal.id();
    }

    private static JsonObject knapping(AreaToolType type) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "tfc:knapping");
        json.addProperty("knapping_type", "tfc:clay");
        JsonArray pattern = new JsonArray();
        List<String> rows = switch (type) {
            case MINING_HAMMER -> List.of("#####", "     ", "     ", "## ##", "#####");
            case QUARRY_HAMMER -> List.of("#####", " # # ", "     ", "#   #", "#####");
            case EXCAVATOR -> List.of("#####", " ### ", "  #  ", "  #  ", "     ");
        };
        rows.forEach(pattern::add);
        json.add("pattern", pattern);
        json.add("result", result("tfcm:ceramic/unfired_" + type.id() + "_head_mold"));
        return json;
    }

    private static JsonObject moldHeating(AreaToolType type) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "tfc:heating");
        json.add("ingredient", item("tfcm:ceramic/unfired_" + type.id() + "_head_mold"));
        json.add("result_item", result("tfcm:ceramic/" + type.id() + "_head_mold"));
        json.addProperty("temperature", 1399);
        return json;
    }

    private static JsonObject anvil(AreaToolMetal metal, AreaToolType type) {
        JsonObject json = conditioned(metal);
        json.addProperty("type", "tfc:anvil");
        json.add("ingredient", tag("c:ingots/" + metal.id()));
        json.add("result", result(id(metal, type, true)));
        JsonArray rules = new JsonArray();
        if (type == AreaToolType.EXCAVATOR) {
            rules.add("punch_last");
            rules.add("hit_not_last");
        } else {
            rules.add("punch_last");
            rules.add("shrink_not_last");
        }
        json.add("rules", rules);
        json.addProperty("tier", metal.tierLevel());
        json.addProperty("apply_bonus", true);
        return json;
    }

    private static JsonObject casting(AreaToolMetal metal, AreaToolType type) {
        JsonObject json = conditioned(metal);
        json.addProperty("type", "tfc:casting");
        JsonObject fluid = new JsonObject();
        fluid.addProperty("amount", 100);
        fluid.addProperty("fluid", metal.namespace() + ":metal/" + metal.id());
        json.add("fluid", fluid);
        json.add("mold", item("tfcm:ceramic/" + type.id() + "_head_mold"));
        json.add("result", result(id(metal, type, true)));
        return json;
    }

    private static JsonObject assembly(AreaToolMetal metal, AreaToolType type) {
        JsonObject json = conditioned(metal);
        json.addProperty("type", "tfc:advanced_shaped_crafting");
        JsonArray pattern = new JsonArray();
        pattern.add("HBR");
        pattern.add(" T ");
        json.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("H", item(id(metal, type, true)));
        key.add("B", item("tfcm:tool_binding"));
        key.add("R", item("tfcm:tool_riveting"));
        key.add("T", item("tfcm:tool_handle"));
        json.add("key", key);
        JsonObject advancedResult = new JsonObject();
        advancedResult.add("stack", result(id(metal, type, false)));
        JsonArray modifiers = new JsonArray();
        JsonObject modifier = new JsonObject();
        modifier.addProperty("type", "tfc:copy_forging_bonus");
        modifiers.add(modifier);
        advancedResult.add("modifiers", modifiers);
        json.add("result", advancedResult);
        return json;
    }

    private static JsonObject heating(AreaToolMetal metal, AreaToolType type, boolean head) {
        JsonObject json = conditioned(metal);
        json.addProperty("type", "tfc:heating");
        json.add("ingredient", item(id(metal, type, head)));
        JsonObject fluid = new JsonObject();
        fluid.addProperty("amount", 100);
        fluid.addProperty("id", metal.namespace() + ":metal/" + meltMetal(metal));
        json.add("result_fluid", fluid);
        json.addProperty("temperature", metal.meltingPoint());
        return json;
    }

    private static JsonObject itemHeat(AreaToolMetal metal, AreaToolType type) {
        JsonObject json = conditioned(metal);
        JsonArray ingredients = new JsonArray();
        ingredients.add(item(id(metal, type, false)));
        ingredients.add(item(id(metal, type, true)));
        json.add("ingredient", ingredients);
        json.addProperty("heat_capacity", metal.heatCapacity());
        json.addProperty("forging_temperature", metal.forgingTemperature());
        json.addProperty("welding_temperature", metal.weldingTemperature());
        return json;
    }

    private static JsonObject itemSize(AreaToolMetal metal, AreaToolType type, boolean head) {
        JsonObject json = conditioned(metal);
        json.add("ingredient", item(id(metal, type, head)));
        json.addProperty("size", head ? "large" : "very_large");
        json.addProperty("weight", head ? "medium" : "very_heavy");
        return json;
    }

    private static JsonObject moldSize() {
        JsonObject json = new JsonObject();
        JsonArray ingredients = new JsonArray();
        for (AreaToolType type : AreaToolType.values()) {
            ingredients.add(item("tfcm:ceramic/unfired_" + type.id() + "_head_mold"));
            ingredients.add(item("tfcm:ceramic/" + type.id() + "_head_mold"));
        }
        json.add("ingredient", ingredients);
        json.addProperty("size", "small");
        json.addProperty("weight", "light");
        return json;
    }

    private static JsonObject moldTag(boolean unfired) {
        JsonObject json = new JsonObject();
        JsonArray values = new JsonArray();
        for (AreaToolType type : AreaToolType.values()) {
            values.add("tfcm:ceramic/" + (unfired ? "unfired_" : "") + type.id() + "_head_mold");
        }
        json.add("values", values);
        json.addProperty("replace", false);
        return json;
    }

    private static JsonObject toolTag(AreaToolType... types) {
        JsonObject json = new JsonObject();
        JsonArray values = new JsonArray();
        for (AreaToolMetal metal : AreaToolMetal.values()) {
            for (AreaToolType type : types) values.add(id(metal, type, false));
        }
        json.add("values", values);
        json.addProperty("replace", false);
        return json;
    }

    private static JsonObject moldModel(AreaToolType type, boolean unfired) {
        JsonObject json = new JsonObject();
        String reference = type == AreaToolType.EXCAVATOR ? "shovel" : "hammer";
        json.addProperty("parent", "tfc:item/ceramic/" + (unfired ? "unfired_" : "") + reference + "_head_mold");
        return json;
    }

    private static JsonObject toolModel(AreaToolMetal metal, AreaToolType type) {
        JsonObject json = new JsonObject();
        String reference = type == AreaToolType.EXCAVATOR ? "shovel" : type == AreaToolType.QUARRY_HAMMER ? "mace" : "hammer";
        json.addProperty("parent", "tfc:item/metal/" + reference + "/" + modelMetal(metal));
        return json;
    }

    private static JsonObject headModel(AreaToolMetal metal, AreaToolType type) {
        JsonObject json = new JsonObject();
        String reference = type == AreaToolType.EXCAVATOR ? "shovel_head" : type == AreaToolType.QUARRY_HAMMER ? "mace_head" : "hammer_head";
        json.addProperty("parent", "tfc:item/metal/" + reference + "/" + modelMetal(metal));
        return json;
    }

    private static String modelMetal(AreaToolMetal metal) {
        return switch (metal) {
            case INVAR -> "wrought_iron";
            case TITANIUM -> "black_steel";
            case NETHERITE -> "red_steel";
            case TUNGSTEN_STEEL -> "blue_steel";
            default -> metal.id();
        };
    }

    private static String meltMetal(AreaToolMetal metal) {
        return metal == AreaToolMetal.WROUGHT_IRON ? "cast_iron" : metal.id();
    }

}
