package net.claustra01.tfcm.datagen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.claustra01.tfcm.common.tool.ToolMetal;
import net.claustra01.tfcm.common.tool.ToolPart;
import net.claustra01.tfcm.common.tool.area.AreaToolMetal;
import net.claustra01.tfcm.common.tool.area.AreaToolType;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public final class EquipmentDataProvider implements DataProvider {
    private final Path data;
    private final Path assets;

    public EquipmentDataProvider(PackOutput output) {
        data = output.getOutputFolder(PackOutput.Target.DATA_PACK);
        assets = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> files = new ArrayList<>();
        for (ToolMetal metal : ToolMetal.values()) {
            for (ToolPart part : ToolPart.values()) {
                String id = part.id();
                files.add(save(output, data, "tfcm/recipe/heating/metal/" + id + "/" + metal.getSerializedName(),
                        heating(metal, part)));
                files.add(save(output, data, "tfcm/tfc/item_heat/" + metal.getSerializedName() + "/" + id,
                        itemHeat(metal, part)));
                files.add(save(output, data, "tfcm/tfc/item_size/" + metal.getSerializedName() + "/" + id,
                        itemSize(metal, part)));
                files.add(save(output, assets, "tfcm/models/item/metal/" + id + "/" + metal.getSerializedName(),
                        model(metal, part)));
                if (isForged(part)) {
                    files.add(save(output, data, "tfcm/recipe/anvil/metal/" + id + "/" + metal.getSerializedName(),
                            anvilPart(metal, part)));
                }
                if (hasAssembly(part)) {
                    files.add(save(output, data, "tfcm/recipe/crafting/metal/" + id + "/" + metal.getSerializedName(),
                            assembly(metal, part)));
                }
            }
            files.add(save(output, data, "tfcm/recipe/welding/metal/shears/" + metal.getSerializedName(), shears(metal)));
            files.add(save(output, data, "tfcm/recipe/crafting/metal/fishing_rod/" + metal.getSerializedName(), fishingRod(metal)));
            for (String armor : List.of("helmet", "chestplate", "greaves", "boots")) {
                files.add(save(output, data, "tfcm/recipe/welding/metal/" + armor + "/" + metal.getSerializedName(),
                        armorWelding(metal, armor)));
            }
            files.add(save(output, data, "tfcm/recipe/crafting/metal/horse_armor/" + metal.getSerializedName(), horseArmor(metal)));
            files.add(save(output, data, "tfcm/recipe/crafting/metal/anvil/" + metal.getSerializedName(), anvilCraft(metal)));
            files.add(save(output, data, "tfcm/recipe/heating/metal/anvil/" + metal.getSerializedName(), anvilHeating(metal)));
            files.add(save(output, data, "tfcm/tfc/item_heat/" + metal.getSerializedName() + "/anvil", anvilItemHeat(metal)));
            files.add(save(output, data, "tfcm/tfc/item_size/" + metal.getSerializedName() + "/anvil", anvilSize(metal)));
            files.add(save(output, data, "tfcm/loot_table/blocks/metal/anvil/" + metal.getSerializedName(), anvilLoot(metal)));
            files.add(save(output, assets, "tfcm/blockstates/metal/anvil/" + metal.getSerializedName(), anvilBlockstate(metal)));
            files.add(save(output, assets, "tfcm/models/block/metal/anvil/" + metal.getSerializedName(), anvilBlockModel(metal)));
            files.add(save(output, assets, "tfcm/models/item/metal/anvil/" + metal.getSerializedName(), anvilItemModel(metal)));
        }
        files.add(save(output, data, "tfc/tags/block/anvils", anvilTag()));
        files.add(save(output, data, "tfc/tags/item/usable_on_tool_rack", toolRack()));
        files.add(save(output, data, "minecraft/tags/item/pickaxes", toolTagWithArea(ToolPart.PICKAXE,
                AreaToolType.MINING_HAMMER, AreaToolType.QUARRY_HAMMER)));
        files.add(save(output, data, "minecraft/tags/item/axes", toolTag(ToolPart.AXE)));
        files.add(save(output, data, "minecraft/tags/item/shovels", toolTagWithArea(ToolPart.SHOVEL,
                AreaToolType.EXCAVATOR)));
        files.add(save(output, data, "minecraft/tags/item/hoes", toolTag(ToolPart.HOE)));
        files.add(save(output, data, "minecraft/tags/item/swords", toolTag(ToolPart.SWORD)));
        files.add(save(output, data, "c/tags/item/tools/propick", toolTag(ToolPart.PROPICK)));
        files.add(save(output, data, "c/tags/item/tools/hammer", toolTag(ToolPart.HAMMER)));
        files.add(save(output, data, "c/tags/item/tools/shield", toolTag(ToolPart.SHIELD)));
        files.add(save(output, data, "c/tags/item/tools/scythe", toolTag(ToolPart.SCYTHE)));
        files.add(save(output, data, "c/tags/item/tools/fishing_rod", toolTag(ToolPart.FISHING_ROD)));
        files.add(save(output, data, "c/tags/item/tools/chisel", toolTag(ToolPart.CHISEL)));
        files.add(save(output, data, "c/tags/item/tools/saw", toolTag(ToolPart.SAW)));
        files.add(save(output, data, "c/tags/item/tools/mace", toolTag(ToolPart.MACE)));
        files.add(save(output, data, "c/tags/item/tools/knife", toolTag(ToolPart.KNIFE)));
        files.add(save(output, data, "c/tags/item/tools/mining_tool", toolTag(ToolPart.PICKAXE)));
        files.add(save(output, data, "c/tags/item/tools/shear", toolTag(ToolPart.SHEARS)));
        files.add(save(output, data, "c/tags/item/tools/spear", toolTag(ToolPart.JAVELIN)));
        files.add(save(output, data, "c/tags/item/tools/melee_weapon",
                toolTag(ToolPart.SWORD, ToolPart.AXE, ToolPart.MACE)));
        files.add(save(output, data, "c/tags/item/tools/ranged_weapon", toolTag(ToolPart.JAVELIN)));
        files.add(save(output, data, "tfchotornot/tags/item/tongs", hotOrNotTongsTag()));
        for (ToolMetal metal : ToolMetal.values()) {
            files.add(save(output, data, "tfcm/tags/item/hotornot/tong_part/" + metal.getSerializedName(),
                    optionalItemTag(metal, "tong_part")));
            files.add(save(output, data, "tfcm/tags/item/hotornot/tongs/" + metal.getSerializedName(),
                    optionalItemTag(metal, "tongs")));
            files.add(save(output, data, "tfcm/recipe/anvil/metal/tong_part/" + metal.getSerializedName(), tongPart(metal)));
            files.add(save(output, data, "tfcm/recipe/crafting/metal/tongs/" + metal.getSerializedName(), tongs(metal)));
            files.add(save(output, data, "tfcm/recipe/heating/metal/tong_part/" + metal.getSerializedName(),
                    heating(metal, "tong_part", 100, true)));
            files.add(save(output, data, "tfcm/recipe/heating/metal/tongs/" + metal.getSerializedName(),
                    heating(metal, "tongs", 200, true)));
            files.add(save(output, data, "tfcm/tfc/item_heat/" + metal.getSerializedName() + "/tong_part",
                    itemHeat(metal, "tong_part", 100, true)));
            files.add(save(output, data, "tfcm/tfc/item_heat/" + metal.getSerializedName() + "/tongs",
                    itemHeat(metal, "tongs", 200, true)));
            files.add(save(output, data, "tfcm/tfc/item_size/" + metal.getSerializedName() + "/tong_part",
                    itemSize(metal, "tong_part", "large", "medium", true)));
            files.add(save(output, data, "tfcm/tfc/item_size/" + metal.getSerializedName() + "/tongs",
                    itemSize(metal, "tongs", "large", "medium", true)));
            files.add(save(output, assets, "tfcm/models/item/metal/tong_part/" + metal.getSerializedName(),
                    hotOrNotModel(metal, "tong_part")));
            files.add(save(output, assets, "tfcm/models/item/metal/tongs/" + metal.getSerializedName(),
                    hotOrNotModel(metal, "tongs")));
        }
        return CompletableFuture.allOf(files.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "TFCM M05 Equipment";
    }

    private static CompletableFuture<?> save(CachedOutput output, Path root, String location, JsonObject json) {
        return DataProvider.saveStable(output, json, root.resolve(location + ".json"));
    }

    private static JsonObject conditioned(ToolMetal metal) {
        JsonObject json = new JsonObject();
        JsonArray conditions = new JsonArray();
        conditions.add(ContentConditions.metal(metal.getSerializedName()));
        json.add("neoforge:conditions", conditions);
        return json;
    }

    private static JsonObject conditionedHotOrNot(ToolMetal metal) {
        JsonObject json = conditioned(metal);
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "neoforge:mod_loaded");
        condition.addProperty("modid", "tfchotornot");
        json.getAsJsonArray("neoforge:conditions").add(condition);
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

    private static String id(ToolMetal metal, ToolPart part) {
        return "tfcm:metal/" + part.id() + "/" + metal.getSerializedName();
    }

    private static boolean isForged(ToolPart part) {
        return part.name().endsWith("_HEAD") || part == ToolPart.SAW_BLADE || part == ToolPart.SWORD_BLADE
                || part == ToolPart.KNIFE_BLADE || part == ToolPart.SCYTHE_BLADE || part == ToolPart.TUYERE
                || part == ToolPart.FISH_HOOK || part == ToolPart.SHIELD || part.name().startsWith("UNFINISHED_");
    }

    private static boolean hasAssembly(ToolPart part) {
        return part.isFinishedTool() && part != ToolPart.SHEARS && part != ToolPart.FISHING_ROD
                && part != ToolPart.SHIELD;
    }

    private static JsonObject anvilPart(ToolMetal metal, ToolPart part) {
        JsonObject json = conditioned(metal);
        json.addProperty("type", "tfc:anvil");
        String input = switch (part) {
            case TUYERE, SHIELD, UNFINISHED_HELMET, UNFINISHED_CHESTPLATE, UNFINISHED_GREAVES ->
                    "c:double_sheets/";
            case FISH_HOOK, UNFINISHED_BOOTS -> "c:sheets/";
            case SWORD_BLADE, MACE_HEAD -> "c:double_ingots/";
            default -> "c:ingots/";
        };
        json.add("ingredient", tag(input + metal.getSerializedName()));
        json.add("result", result(id(metal, part)));
        JsonArray rules = new JsonArray();
        for (String rule : anvilRules(part)) rules.add(rule);
        json.add("rules", rules);
        json.addProperty("tier", metal.toolTier().level());
        if (part != ToolPart.TUYERE) json.addProperty("apply_bonus", true);
        return json;
    }

    private static List<String> anvilRules(ToolPart part) {
        return switch (part) {
            case AXE_HEAD -> List.of("punch_last", "hit_second_last", "upset_third_last");
            case PROPICK_HEAD -> List.of("punch_last", "bend_not_last", "draw_not_last");
            case CHISEL_HEAD -> List.of("hit_last", "draw_not_last", "hit_not_last");
            case KNIFE_BLADE -> List.of("hit_last", "draw_second_last", "draw_third_last");
            case JAVELIN_HEAD -> List.of("hit_last", "hit_second_last", "draw_third_last");
            case HOE_HEAD -> List.of("punch_last", "hit_not_last", "bend_not_last");
            case SHOVEL_HEAD -> List.of("punch_last", "hit_not_last");
            case SAW_BLADE -> List.of("hit_last", "hit_second_last");
            case PICKAXE_HEAD -> List.of("punch_last", "draw_not_last", "bend_not_last");
            case MACE_HEAD -> List.of("hit_last", "bend_not_last", "shrink_not_last");
            case HAMMER_HEAD -> List.of("punch_last", "shrink_not_last");
            case SWORD_BLADE, UNFINISHED_HELMET -> List.of("hit_last", "bend_second_last", "bend_third_last");
            case SCYTHE_BLADE -> List.of("hit_last", "draw_second_last", "bend_third_last");
            case FISH_HOOK -> List.of("hit_any", "bend_any", "draw_not_last");
            case TUYERE -> List.of("bend_last", "bend_second_last");
            case SHIELD -> List.of("upset_last", "bend_second_last", "bend_third_last");
            case UNFINISHED_CHESTPLATE -> List.of("hit_last", "hit_second_last", "upset_third_last");
            case UNFINISHED_GREAVES -> List.of("hit_any", "draw_any", "bend_any");
            case UNFINISHED_BOOTS -> List.of("bend_last", "bend_second_last", "shrink_third_last");
            default -> throw new IllegalArgumentException("No anvil rules for " + part);
        };
    }

    private static JsonObject tongPart(ToolMetal metal) {
        JsonObject json = conditionedHotOrNot(metal);
        json.addProperty("type", "tfc:anvil");
        json.add("ingredient", tag("c:ingots/" + metal.getSerializedName()));
        json.add("result", result("tfcm:metal/tong_part/" + metal.getSerializedName()));
        JsonArray rules = new JsonArray();
        rules.add("bend_last");
        rules.add("draw_second_last");
        rules.add("draw_third_last");
        json.add("rules", rules);
        json.addProperty("tier", metal.toolTier().level());
        return json;
    }

    private static JsonObject tongs(ToolMetal metal) {
        JsonObject json = conditionedHotOrNot(metal);
        json.addProperty("type", "minecraft:crafting_shaped");
        JsonArray pattern = new JsonArray();
        pattern.add("PP");
        pattern.add(" R");
        json.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("P", item("tfcm:metal/tong_part/" + metal.getSerializedName()));
        key.add("R", tag("c:rivets/" + metal.getSerializedName()));
        json.add("key", key);
        json.add("result", result("tfcm:metal/tongs/" + metal.getSerializedName()));
        return json;
    }

    private static JsonObject assembly(ToolMetal metal, ToolPart part) {
        JsonObject json = conditioned(metal);
        json.addProperty("type", "tfc:advanced_shaped_crafting");
        JsonArray pattern = new JsonArray();
        JsonObject key = new JsonObject();
        ToolPart head = switch (part) {
            case SAW -> ToolPart.SAW_BLADE;
            case SWORD -> ToolPart.SWORD_BLADE;
            case KNIFE -> ToolPart.KNIFE_BLADE;
            case SCYTHE -> ToolPart.SCYTHE_BLADE;
            case JAVELIN -> ToolPart.JAVELIN_HEAD;
            default -> ToolPart.valueOf(part.name() + "_HEAD");
        };
        if (part == ToolPart.SWORD) {
            pattern.add(" B ");
            pattern.add("CHP");
            key.add("B", item(id(metal, head)));
            key.add("C", item("tfcm:sword_crossguard"));
            key.add("H", item("tfcm:tool_handle"));
            key.add("P", item("tfcm:sword_pommel"));
        } else {
            pattern.add("XBR");
            pattern.add(" H ");
            key.add("X", item(id(metal, head)));
            key.add("B", item("tfcm:tool_binding"));
            key.add("R", tag("c:rings/" + metal.getSerializedName()));
            key.add("H", item("tfcm:tool_handle"));
        }
        json.add("pattern", pattern);
        json.add("key", key);
        JsonObject stack = result(id(metal, part));
        JsonArray modifiers = new JsonArray();
        JsonObject modifier = new JsonObject();
        modifier.addProperty("type", "tfc:copy_forging_bonus");
        modifiers.add(modifier);
        JsonObject advancedResult = new JsonObject();
        advancedResult.add("stack", stack);
        advancedResult.add("modifiers", modifiers);
        json.add("result", advancedResult);
        return json;
    }

    private static JsonObject shears(ToolMetal metal) {
        JsonObject json = conditioned(metal);
        json.addProperty("type", "tfc:welding");
        json.add("first_input", item(id(metal, ToolPart.KNIFE_BLADE)));
        json.add("second_input", item(id(metal, ToolPart.KNIFE_BLADE)));
        json.add("result", result(id(metal, ToolPart.SHEARS)));
        json.addProperty("tier", metal.toolTier().level() - 1);
        json.addProperty("bonus", "copy_worst");
        return json;
    }

    private static JsonObject fishingRod(ToolMetal metal) {
        JsonObject json = conditioned(metal);
        json.addProperty("type", "tfc:advanced_shaped_crafting");
        JsonArray pattern = new JsonArray();
        pattern.add("  S");
        pattern.add(" SW");
        pattern.add("SHW");
        json.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("S", tag("c:rods/wooden"));
        key.add("W", tag("c:wires/" + metal.getSerializedName()));
        key.add("H", item(id(metal, ToolPart.FISH_HOOK)));
        json.add("key", key);
        JsonObject advancedResult = new JsonObject();
        advancedResult.add("stack", result(id(metal, ToolPart.FISHING_ROD)));
        JsonArray modifiers = new JsonArray();
        JsonObject modifier = new JsonObject();
        modifier.addProperty("type", "tfc:copy_forging_bonus");
        modifiers.add(modifier);
        advancedResult.add("modifiers", modifiers);
        json.add("result", advancedResult);
        json.addProperty("input_row", 2);
        json.addProperty("input_column", 1);
        return json;
    }

    private static JsonObject armorWelding(ToolMetal metal, String armor) {
        JsonObject json = conditioned(metal);
        json.addProperty("type", "tfc:welding");
        json.add("first_input", item("tfcm:metal/unfinished_" + armor + "/" + metal.getSerializedName()));
        json.add("second_input", tag("c:" + (armor.equals("chestplate") ? "double_sheets/" : "sheets/")
                + metal.getSerializedName()));
        json.add("result", result("tfcm:metal/" + armor + "/" + metal.getSerializedName()));
        json.addProperty("tier", metal.toolTier().level() - 1);
        json.addProperty("bonus", "copy_best");
        return json;
    }

    private static JsonObject horseArmor(ToolMetal metal) {
        JsonObject json = conditioned(metal);
        json.addProperty("type", "minecraft:crafting_shaped");
        JsonArray pattern = new JsonArray();
        pattern.add("RHR");
        pattern.add("SSS");
        json.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("R", tag("c:rivets/" + metal.getSerializedName()));
        key.add("H", item("minecraft:leather_horse_armor"));
        key.add("S", tag("c:double_sheets/" + metal.getSerializedName()));
        json.add("key", key);
        json.add("result", result(id(metal, ToolPart.HORSE_ARMOR)));
        return json;
    }

    private static JsonObject anvilCraft(ToolMetal metal) {
        JsonObject json = conditioned(metal);
        json.addProperty("type", "minecraft:crafting_shaped");
        JsonArray pattern = new JsonArray();
        pattern.add("###");
        pattern.add(" # ");
        pattern.add("###");
        json.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("#", tag("c:double_ingots/" + metal.getSerializedName()));
        json.add("key", key);
        json.add("result", result("tfcm:metal/anvil/" + metal.getSerializedName()));
        return json;
    }

    private static JsonObject heating(ToolMetal metal, ToolPart part) {
        return heating(metal, part.id(), metalAmount(part), false);
    }

    private static JsonObject heating(ToolMetal metal, String part, int amount, boolean hotOrNot) {
        JsonObject json = hotOrNot ? conditionedHotOrNot(metal) : conditioned(metal);
        json.addProperty("type", "tfc:heating");
        json.add("ingredient", item("tfcm:metal/" + part + "/" + metal.getSerializedName()));
        JsonObject fluid = new JsonObject();
        fluid.addProperty("id", "tfcm:metal/" + metal.getSerializedName());
        fluid.addProperty("amount", amount);
        json.add("result_fluid", fluid);
        json.addProperty("temperature", metal.metal().meltingPoint());
        return json;
    }

    private static JsonObject anvilHeating(ToolMetal metal) {
        JsonObject json = conditioned(metal);
        json.addProperty("type", "tfc:heating");
        json.add("ingredient", item("tfcm:metal/anvil/" + metal.getSerializedName()));
        JsonObject fluid = new JsonObject();
        fluid.addProperty("id", "tfcm:metal/" + metal.getSerializedName());
        fluid.addProperty("amount", 1400);
        json.add("result_fluid", fluid);
        json.addProperty("temperature", metal.metal().meltingPoint());
        return json;
    }

    private static JsonObject itemHeat(ToolMetal metal, ToolPart part) {
        return itemHeat(metal, part.id(), metalAmount(part), false);
    }

    private static JsonObject itemHeat(ToolMetal metal, String part, int amount, boolean hotOrNot) {
        JsonObject json = hotOrNot ? conditionedHotOrNot(metal) : conditioned(metal);
        json.add("ingredient", hotOrNot
                ? tag("tfcm:hotornot/" + part + "/" + metal.getSerializedName())
                : item("tfcm:metal/" + part + "/" + metal.getSerializedName()));
        json.addProperty("heat_capacity", metal.metal().specificHeat() * 333.33334f * amount / 100f);
        json.addProperty("forging_temperature", metal.metal().forgingTemperature());
        json.addProperty("welding_temperature", metal.metal().weldingTemperature());
        return json;
    }

    private static JsonObject anvilItemHeat(ToolMetal metal) {
        JsonObject json = itemHeat(metal, ToolPart.TUYERE);
        json.add("ingredient", item("tfcm:metal/anvil/" + metal.getSerializedName()));
        json.addProperty("heat_capacity", metal.metal().specificHeat() * 4666.6665f);
        return json;
    }

    private static JsonObject itemSize(ToolMetal metal, ToolPart part) {
        String size;
        String weight;
        if (part == ToolPart.CHISEL || part == ToolPart.KNIFE || part == ToolPart.SHEARS) {
            size = "large";
            weight = "medium";
        } else if (part.isFinishedTool()) {
            size = "very_large";
            weight = "very_heavy";
        } else if (part == ToolPart.HELMET || part == ToolPart.CHESTPLATE || part == ToolPart.GREAVES
                || part == ToolPart.BOOTS || part == ToolPart.HORSE_ARMOR) {
            size = "large";
            weight = "very_heavy";
        } else if (part == ToolPart.TUYERE) {
            size = "large";
            weight = "heavy";
        } else {
            size = "large";
            weight = "medium";
        }
        return itemSize(metal, part.id(), size, weight, false);
    }

    private static JsonObject itemSize(ToolMetal metal, String part, String size, String weight, boolean hotOrNot) {
        JsonObject json = hotOrNot ? conditionedHotOrNot(metal) : conditioned(metal);
        json.add("ingredient", hotOrNot
                ? tag("tfcm:hotornot/" + part + "/" + metal.getSerializedName())
                : item("tfcm:metal/" + part + "/" + metal.getSerializedName()));
        json.addProperty("size", size);
        json.addProperty("weight", weight);
        return json;
    }

    private static JsonObject anvilSize(ToolMetal metal) {
        JsonObject json = conditioned(metal);
        json.add("ingredient", item("tfcm:metal/anvil/" + metal.getSerializedName()));
        json.addProperty("size", "huge");
        json.addProperty("weight", "very_heavy");
        return json;
    }

    private static int metalAmount(ToolPart part) {
        return switch (part) {
            case TUYERE, SHIELD, BOOTS, UNFINISHED_HELMET, UNFINISHED_CHESTPLATE, UNFINISHED_GREAVES -> 400;
            case FISH_HOOK, FISHING_ROD, SWORD, SWORD_BLADE, MACE, MACE_HEAD, UNFINISHED_BOOTS, SHEARS -> 200;
            case HELMET, GREAVES -> 600;
            case CHESTPLATE -> 800;
            case HORSE_ARMOR -> 1200;
            default -> 100;
        };
    }

    private static JsonObject model(ToolMetal metal, ToolPart part) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", "tfc:item/metal/" + part.id() + "/" + reference(metal));
        return json;
    }

    private static String reference(ToolMetal metal) {
        return switch (metal) {
            case INVAR -> "wrought_iron";
            case TITANIUM -> "black_steel";
            case NETHERITE -> "red_steel";
            case TUNGSTEN_STEEL -> "blue_steel";
        };
    }

    private static JsonObject hotOrNotModel(ToolMetal metal, String part) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", "tfchotornot:item/" + part + "/" + reference(metal));
        return json;
    }

    private static JsonObject hotOrNotTongsTag() {
        JsonObject json = new JsonObject();
        JsonArray values = new JsonArray();
        for (ToolMetal metal : ToolMetal.values()) {
            values.add(ContentConditions.optional("tfcm:metal/tongs/" + metal.getSerializedName()));
        }
        json.add("values", values);
        json.addProperty("replace", false);
        return json;
    }

    private static JsonObject optionalItemTag(ToolMetal metal, String part) {
        JsonObject json = conditioned(metal);
        JsonArray values = new JsonArray();
        values.add(ContentConditions.optional("tfcm:metal/" + part + "/" + metal.getSerializedName()));
        json.add("values", values);
        json.addProperty("replace", false);
        return json;
    }

    private static JsonObject anvilTag() {
        JsonObject json = new JsonObject();
        JsonArray values = new JsonArray();
        for (ToolMetal metal : ToolMetal.values()) values.add(ContentConditions.optional("tfcm:metal/anvil/" + metal.getSerializedName()));
        json.add("values", values);
        json.addProperty("replace", false);
        return json;
    }

    private static JsonObject toolRack() {
        JsonObject json = new JsonObject();
        JsonArray values = new JsonArray();
        for (ToolMetal metal : ToolMetal.values()) for (ToolPart part : ToolPart.values())
            if (part.isFinishedTool()) values.add(ContentConditions.optional(id(metal, part)));
        for (AreaToolMetal metal : AreaToolMetal.values()) for (AreaToolType type : AreaToolType.values())
            values.add(ContentConditions.optional("tfcm:metal/" + type.id() + "/" + metal.id()));
        json.add("values", values);
        json.addProperty("replace", false);
        return json;
    }

    private static JsonObject toolTag(ToolPart... parts) {
        JsonObject json = new JsonObject();
        JsonArray values = new JsonArray();
        for (ToolMetal metal : ToolMetal.values()) for (ToolPart part : parts) values.add(ContentConditions.optional(id(metal, part)));
        json.add("values", values);
        json.addProperty("replace", false);
        return json;
    }

    private static JsonObject toolTagWithArea(ToolPart part, AreaToolType... types) {
        JsonObject json = toolTag(part);
        JsonArray values = json.getAsJsonArray("values");
        for (AreaToolMetal metal : AreaToolMetal.values()) for (AreaToolType type : types)
            values.add(ContentConditions.optional("tfcm:metal/" + type.id() + "/" + metal.id()));
        return json;
    }



    private static JsonObject anvilLoot(ToolMetal metal) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:block");
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", "tfcm:metal/anvil/" + metal.getSerializedName());
        JsonArray entries = new JsonArray();
        entries.add(entry);
        JsonObject pool = new JsonObject();
        pool.addProperty("rolls", 1);
        JsonObject survivesExplosion = new JsonObject();
        survivesExplosion.addProperty("condition", "minecraft:survives_explosion");
        JsonArray conditions = new JsonArray();
        conditions.add(survivesExplosion);
        pool.add("conditions", conditions);
        pool.add("entries", entries);
        JsonArray pools = new JsonArray();
        pools.add(pool);
        json.add("pools", pools);
        return json;
    }

    private static JsonObject anvilBlockstate(ToolMetal metal) {
        JsonObject json = new JsonObject();
        JsonObject variants = new JsonObject();
        JsonObject north = new JsonObject();
        north.addProperty("model", "tfcm:block/metal/anvil/" + metal.getSerializedName());
        variants.add("facing=north", north);
        JsonObject south = north.deepCopy();
        south.addProperty("y", 180);
        variants.add("facing=south", south);
        JsonObject west = north.deepCopy();
        west.addProperty("y", 270);
        variants.add("facing=west", west);
        JsonObject east = north.deepCopy();
        east.addProperty("y", 90);
        variants.add("facing=east", east);
        json.add("variants", variants);
        return json;
    }

    private static JsonObject anvilBlockModel(ToolMetal metal) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", "tfc:block/metal/anvil/" + reference(metal));
        return json;
    }

    private static JsonObject anvilItemModel(ToolMetal metal) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", "tfcm:block/metal/anvil/" + metal.getSerializedName());
        return json;
    }
}
