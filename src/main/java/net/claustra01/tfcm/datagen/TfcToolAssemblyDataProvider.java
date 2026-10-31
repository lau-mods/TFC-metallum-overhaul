package net.claustra01.tfcm.datagen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.claustra01.tfcm.common.metal.TfcMetal;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

/**
 * TFC Metal Tools 1.0.7 assembly for TFC's nine tool metals. The finished tools replace TFC's own Head and Stick
 * recipes under the same IDs; the sword parts and the Shears recipe are additions in the TFCM namespace.
 */
public final class TfcToolAssemblyDataProvider implements DataProvider {
    private static final List<String> TOOLS = List.of("axe", "chisel", "hammer", "hoe", "javelin", "knife", "mace",
            "pickaxe", "propick", "saw", "scythe", "shovel", "sword");
    private static final int CROSSGUARD_AMOUNT = 25;
    private static final int POMMEL_AMOUNT = 24;

    private final Path data;

    public TfcToolAssemblyDataProvider(PackOutput output) {
        data = output.getOutputFolder(PackOutput.Target.DATA_PACK);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> files = new ArrayList<>();
        for (TfcMetal metal : TfcMetal.values()) {
            if (!metal.hasTools()) continue;
            for (String tool : TOOLS) files.add(save(output, "tfc/recipe/crafting/metal/" + tool + "/" + metal.id(),
                    assembly(metal, tool)));
            files.add(save(output, "tfcm/recipe/crafting/metal/shears/" + metal.id(), shears(metal)));
            files.add(save(output, "tfcm/recipe/anvil/metal/crossguard/" + metal.id(), crossguard(metal)));
            files.add(save(output, "tfcm/recipe/welding/metal/pommel/" + metal.id(), pommel(metal)));
            addPart(output, files, metal, "crossguard", CROSSGUARD_AMOUNT);
            addPart(output, files, metal, "pommel", POMMEL_AMOUNT);
            JsonArray metalItems = new JsonArray();
            metalItems.add(part(metal, "crossguard"));
            metalItems.add(part(metal, "pommel"));
            JsonObject metalItem = new JsonObject();
            metalItem.add("values", metalItems);
            files.add(save(output, "tfc/tags/item/metal_item/" + metal.id(), metalItem));
        }
        return CompletableFuture.allOf(files.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "TFCM TFC tool assembly";
    }

    private CompletableFuture<?> save(CachedOutput output, String location, JsonObject json) {
        return DataProvider.saveStable(output, json, data.resolve(location + ".json"));
    }

    private void addPart(CachedOutput output, List<CompletableFuture<?>> files, TfcMetal metal, String part, int amount) {
        String item = part(metal, part);
        files.add(save(output, "tfcm/recipe/heating/metal/" + part + "/" + metal.id(),
                MetalDataProvider.tfcHeating(metal, item, amount)));
        files.add(save(output, "tfcm/tfc/item_heat/" + metal.id() + "/" + part,
                MetalDataProvider.tfcItemHeat(metal, item(item), amount)));
        files.add(save(output, "tfcm/tfc/item_size/" + metal.id() + "/" + part,
                MetalDataProvider.tfcItemSize(metal, item(item), "small", "light")));
    }

    private static JsonObject assembly(TfcMetal metal, String tool) {
        String id = metal.id();
        // Tier 3 and up need rings and leather; below that a rivet and glue do, or string for Copper's first tools.
        boolean simple = metal.tier() <= 2;
        boolean copper = metal == TfcMetal.COPPER;
        String head = tool + (List.of("knife", "saw", "scythe", "sword").contains(tool) ? "_blade" : "_head");
        Map<String, JsonObject> key = new LinkedHashMap<>();
        key.put("X", item("tfc:metal/" + head + "/" + id));
        String[] pattern;
        if (tool.equals("sword")) {
            key.put("R", tag("c:rivets/" + id));
            key.put("T", tag("c:rings/" + id));
            key.put("Y", item(part(metal, "crossguard")));
            if (simple) {
                key.put("H", tag("c:handle/short"));
                pattern = new String[] {"  X", "RY ", "HT "};
            } else {
                key.put("L", tag("c:leather/string"));
                key.put("H", item(part(metal, "pommel")));
                pattern = new String[] {"L X", "RY ", "HTL"};
            }
        } else if (simple) {
            key.put("H", tag("c:handle/" + handle(tool)));
            if (copper && (tool.equals("pickaxe") || tool.equals("propick"))) {
                key.put("N", tag("c:string/plant"));
                pattern = new String[] {"NX", "HN"};
            } else if (copper && tool.equals("saw")) {
                key.put("G", tag("c:string"));
                pattern = new String[] {"GX", "HG"};
            } else {
                key.put("N", tag(tool.equals("knife") ? "c:leather/string" : "c:rivets/" + id));
                key.put("G", tag(tool.equals("saw") ? "c:leather/string" : "c:glue"));
                pattern = new String[] {"NX", "HG"};
            }
        } else {
            key.put("H", tag("c:handle/" + handle(tool)));
            key.put("N", tag("c:rivets/" + id));
            key.put("G", tag("c:glue"));
            switch (tool) {
                case "knife" -> {
                    key.put("R", tag("c:rings/" + id));
                    pattern = new String[] {" NX", "NHR", "G  "};
                }
                case "saw" -> {
                    key.put("R", tag("c:wires/" + id));
                    key.put("L", tag("c:leather/string"));
                    pattern = new String[] {" NX", "GHN", "RL "};
                }
                case "scythe" -> {
                    key.put("R", tag("c:rings/" + id));
                    key.put("L", tag("c:leather/string"));
                    key.put("K", metal == TfcMetal.STEEL || metal == TfcMetal.WROUGHT_IRON
                            ? tag("c:handle/saw") : item("tfc:metal/hoe_head/" + id));
                    pattern = new String[] {" NX", "GHR", "RLK"};
                }
                default -> {
                    key.put("R", tag("c:rings/" + id));
                    key.put("L", tag(tool.equals("shovel") ? "c:leather/strip" : "c:leather/string"));
                    if (tool.equals("javelin") && metal == TfcMetal.BLACK_STEEL) key.put("G", tag("c:wires/" + id));
                    pattern = new String[] {" NX", "GHR", "RL "};
                }
            }
        }
        return shaped(pattern, key, 0, pattern[0].indexOf('X'), "tfc:metal/" + tool + "/" + id);
    }

    private static String handle(String tool) {
        return switch (tool) {
            case "hoe", "javelin", "scythe", "shovel" -> "long";
            case "knife" -> "short";
            case "saw" -> "saw";
            default -> "medium";
        };
    }

    private static JsonObject shears(TfcMetal metal) {
        Map<String, JsonObject> key = new LinkedHashMap<>();
        key.put("X", item("tfc:metal/knife_blade/" + metal.id()));
        key.put("N", tag("c:rivets/" + metal.id()));
        key.put("R", tag("c:rings/" + metal.id()));
        key.put("s", tag("c:leather/string"));
        key.put("S", tag("c:handle/short"));
        return shaped(new String[] {"sX ", "SNX", "RSs"}, key, 0, 1, "tfc:metal/shears/" + metal.id());
    }

    // the head at (row, column) is the input whose forging bonus the finished tool keeps
    private static JsonObject shaped(String[] pattern, Map<String, JsonObject> key, int row, int column, String result) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "tfc:advanced_shaped_crafting");
        JsonArray rows = new JsonArray();
        for (String line : pattern) rows.add(line);
        json.add("pattern", rows);
        JsonObject keys = new JsonObject();
        key.forEach(keys::add);
        json.add("key", keys);
        json.addProperty("input_row", row);
        json.addProperty("input_column", column);
        json.add("result", bonusResult(result));
        return json;
    }

    /** A crafted tool that keeps the forging bonus of its input. */
    static JsonObject bonusResult(String id) {
        JsonObject modifier = new JsonObject();
        modifier.addProperty("type", "tfc:copy_forging_bonus");
        JsonArray modifiers = new JsonArray();
        modifiers.add(modifier);
        JsonObject result = new JsonObject();
        result.add("stack", MetalDataProvider.item(id, 1));
        result.add("modifiers", modifiers);
        return result;
    }

    private static JsonObject crossguard(TfcMetal metal) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "tfc:anvil");
        json.addProperty("apply_bonus", true);
        json.add("ingredient", tag("c:rods/" + metal.id()));
        json.add("result", MetalDataProvider.item(part(metal, "crossguard"), 2));
        json.addProperty("tier", metal.tier());
        json.add("rules", MetalDataProvider.forgeRules("bend_last", "draw_second_last", "draw_third_last"));
        return json;
    }

    private static JsonObject pommel(TfcMetal metal) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "tfc:welding");
        json.add("first_input", tag("c:rings/" + metal.id()));
        json.add("second_input", tag("c:rings/" + metal.id()));
        json.addProperty("tier", 1);
        json.add("result", MetalDataProvider.item(part(metal, "pommel"), 1));
        return json;
    }

    private static String part(TfcMetal metal, String part) {
        return "tfcm:metal/" + part + "/" + metal.id();
    }

    private static JsonObject item(String id) {
        return MetalDataProvider.itemIngredient(id);
    }

    private static JsonObject tag(String id) {
        return MetalDataProvider.tagIngredient(id);
    }
}
