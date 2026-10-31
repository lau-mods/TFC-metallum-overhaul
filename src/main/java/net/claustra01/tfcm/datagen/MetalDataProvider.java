package net.claustra01.tfcm.datagen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.claustra01.tfcm.common.metal.Metal;
import net.claustra01.tfcm.common.metal.MetalForm;
import net.claustra01.tfcm.common.metal.TfcMetal;
import net.claustra01.tfcm.common.tool.ToolMetal;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public final class MetalDataProvider implements DataProvider {
    private final Path data;

    public MetalDataProvider(PackOutput output) {
        data = output.getOutputFolder(PackOutput.Target.DATA_PACK);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> files = new ArrayList<>();
        for (Metal metal : Metal.values()) {
            files.add(save(output, "tfcm/tfc/fluid_heat/" + metal.id(), fluidHeat(metal)));
            files.add(save(output, "tfcm/recipe/casting/" + metal.id() + "_ingot", casting(metal, false)));
            files.add(save(output, "tfcm/recipe/casting/" + metal.id() + "_fire_ingot", casting(metal, true)));
            files.add(save(output, "tfcm/recipe/anvil/metal/sheet/" + metal.id(), anvil(metal, "sheet", "double_ingots", 1, forgeRules())));
            files.add(save(output, "tfcm/recipe/anvil/metal/rod/" + metal.id(), anvil(metal, "rod", "ingots", 2, forgeRules())));
            files.add(save(output, "tfcm/recipe/welding/metal/double_ingot/" + metal.id(), welding(metal, "double_ingot", "ingots", "ingots", doubleTier(metal))));
            files.add(save(output, "tfcm/recipe/welding/metal/double_sheet/" + metal.id(), welding(metal, "double_sheet", "sheets", "sheets", doubleTier(metal))));
            files.add(save(output, "tfcm/recipe/welding/metal/heavy_sheet/" + metal.id(), welding(metal, "heavy_sheet", "double_sheets", "sheets", metal.forgingTier())));
            for (MetalForm form : MetalForm.values()) {
                if (form.isAnvilForm() && form != MetalForm.PLATE)
                    files.add(save(output, "tfcm/recipe/anvil/metal/" + form.id + "/" + metal.id(), extendedAnvil(metal, form)));
            }
            files.add(save(output, "tfcm/recipe/anvil/metal/plate/" + metal.id(),
                    anvil(metal, "plate", "ingots", 1, forgeRules())));
            files.add(save(output, "tfcm/recipe/crafting/metal/nugget/" + metal.id(), nuggetCraft(metal, false)));
            files.add(save(output, "tfcm/recipe/crafting/metal/ingot_from_nuggets/" + metal.id(), nuggetCraft(metal, true)));
            files.add(save(output, "tfcm/recipe/quern/metal/dust/" + metal.id(), dust(metal)));
            for (MetalForm form : MetalForm.values()) {
                files.add(save(output, "tfcm/recipe/heating/metal/" + form.id + "/" + metal.id(), heating(metal, form)));
                files.add(save(output, "tfcm/tfc/item_heat/" + metal.id() + "/" + form.id, itemHeat(metal, form)));
                files.add(save(output, "tfcm/tfc/item_size/" + metal.id() + "/" + form.id, itemSize(metal, form)));
            }
            files.add(save(output, "c/tags/item/ingots/" + metal.id(), tag(metal, itemId(metal, "ingot"))));
            files.add(save(output, "c/tags/item/double_ingots/" + metal.id(), tag(metal, itemId(metal, "double_ingot"))));
            files.add(save(output, "c/tags/item/sheets/" + metal.id(), tag(metal, itemId(metal, "sheet"))));
            files.add(save(output, "c/tags/item/double_sheets/" + metal.id(), tag(metal, itemId(metal, "double_sheet"))));
            files.add(save(output, "c/tags/item/rods/" + metal.id(), tag(metal, itemId(metal, "rod"))));
            for (MetalForm form : MetalForm.values()) {
                if (form.isAddedForm()) files.add(save(output, "c/tags/item/" + form.tag() + "/" + metal.id(), tag(metal, form.item(metal))));
            }
            files.add(save(output, "c/tags/item/storage_blocks/" + metal.id(), tag(metal, blockId(metal, ""))));
            files.add(save(output, "c/tags/block/storage_blocks/" + metal.id(), tag(metal, blockId(metal, ""))));
            files.add(save(output, "c/tags/fluid/molten_" + metal.id(), tag(metal,
                    "tfcm:metal/" + metal.id(), "tfcm:metal/flowing_" + metal.id())));
            files.add(save(output, "tfcm/recipe/crafting/metal/block/" + metal.id(), blockCraft(metal)));
            files.add(save(output, "tfcm/recipe/crafting/metal/block/" + metal.id() + "_slab", slabCraft(metal)));
            files.add(save(output, "tfcm/recipe/crafting/metal/block/" + metal.id() + "_stairs", stairsCraft(metal)));
            files.add(save(output, "tfcm/loot_table/blocks/metal/block/" + metal.id(), blockLoot(metal, "")));
            files.add(save(output, "tfcm/loot_table/blocks/metal/block/" + metal.id() + "_slab", slabLoot(metal)));
            files.add(save(output, "tfcm/loot_table/blocks/metal/block/" + metal.id() + "_stairs", blockLoot(metal, "_stairs")));
        }
        for (TfcMetal metal : TfcMetal.values()) addTfcMetalForms(output, files, metal);
        addCollectionTags(output, files);
        addSpecialRecipes(output, files);
        return CompletableFuture.allOf(files.toArray(CompletableFuture[]::new));
    }

    // the added forms for the TFC metals, made with the same rules as the TFCM metals but TFC's tier and fluid
    private void addTfcMetalForms(CachedOutput output, List<CompletableFuture<?>> files, TfcMetal metal) {
        String recipe = "tfcm/recipe/";
        files.add(save(output, recipe + "anvil/metal/plate/" + metal.id(), tfcAnvil(metal, MetalForm.PLATE, "ingots",
                1, forgeRules())));
        JsonObject welding = tfcConditioned(metal);
        welding.addProperty("type", "tfc:welding");
        welding.add("first_input", tagIngredient("c:double_sheets/" + metal.id()));
        welding.add("second_input", tagIngredient("c:sheets/" + metal.id()));
        welding.add("result", item(metal.item(MetalForm.HEAVY_SHEET), 1));
        if (metal.tier() > 0) welding.addProperty("tier", metal.tier());
        files.add(save(output, recipe + "welding/metal/heavy_sheet/" + metal.id(), welding));
        JsonObject nugget = tfcConditioned(metal);
        nugget.addProperty("type", "minecraft:crafting_shapeless");
        JsonArray ingredients = new JsonArray();
        ingredients.add(tagIngredient("c:ingots/" + metal.id()));
        nugget.add("ingredients", ingredients);
        nugget.add("result", item(metal.item(MetalForm.NUGGET), 9));
        files.add(save(output, recipe + "crafting/metal/nugget/" + metal.id(), nugget));
        JsonObject ingot = tfcConditioned(metal);
        ingot.addProperty("type", "minecraft:crafting_shaped");
        JsonArray pattern = new JsonArray();
        for (int row = 0; row < 3; row++) pattern.add("NNN");
        ingot.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("N", tagIngredient("c:nuggets/" + metal.id()));
        ingot.add("key", key);
        ingot.add("result", item(metal.ingot(), 1));
        files.add(save(output, recipe + "crafting/metal/ingot_from_nuggets/" + metal.id(), ingot));
        JsonObject dust = tfcConditioned(metal);
        dust.addProperty("type", "tfc:quern");
        dust.add("ingredient", tagIngredient("c:ingots/" + metal.id()));
        dust.add("result", item(metal.item(MetalForm.DUST), 1));
        files.add(save(output, recipe + "quern/metal/dust/" + metal.id(), dust));

        for (MetalForm form : MetalForm.values()) {
            if (!form.isAddedForm()) continue;
            String item = metal.item(form);
            if (form.isAnvilForm() && form != MetalForm.PLATE)
                files.add(save(output, recipe + "anvil/metal/" + form.id + "/" + metal.id(), tfcAnvil(metal, form,
                        form.anvilInput().tag(), form.anvilCount(), forgeRules(form.anvilRules().toArray(String[]::new)))));
            files.add(save(output, recipe + "heating/metal/" + form.id + "/" + metal.id(),
                    tfcHeating(metal, item, form.amount)));
            // TFC's item data and tags ignore load conditions, so FirmaLife's forms stay valid through an optional tag
            JsonObject ingredient = metal.isFirmaLife()
                    ? tagIngredient("c:" + form.tag() + "/" + metal.id()) : itemIngredient(item);
            files.add(save(output, "tfcm/tfc/item_heat/" + metal.id() + "/" + form.id, tfcItemHeat(metal, ingredient, form.amount)));
            files.add(save(output, "tfcm/tfc/item_size/" + metal.id() + "/" + form.id, tfcItemSize(metal, ingredient, form.size, form.weight)));
            JsonObject tag = new JsonObject();
            JsonArray values = new JsonArray();
            if (metal.isFirmaLife()) values.add(ContentConditions.optional(item));
            else values.add(item);
            tag.add("values", values);
            files.add(save(output, "c/tags/item/" + form.tag() + "/" + metal.id(), tag));
        }
    }

    static JsonObject tfcHeating(TfcMetal metal, String item, int amount) {
        JsonObject json = tfcConditioned(metal);
        json.addProperty("type", "tfc:heating");
        json.add("ingredient", itemIngredient(item));
        json.add("result_fluid", fluid("id", metal.fluid(), amount));
        json.addProperty("temperature", metal.meltingPoint());
        return json;
    }

    static JsonObject tfcItemHeat(TfcMetal metal, JsonObject ingredient, int amount) {
        JsonObject json = tfcConditioned(metal);
        json.add("ingredient", ingredient);
        json.addProperty("heat_capacity", metal.heatCapacity() * amount / 100f);
        json.addProperty("forging_temperature", metal.forgingTemperature());
        json.addProperty("welding_temperature", metal.weldingTemperature());
        return json;
    }

    static JsonObject tfcItemSize(TfcMetal metal, JsonObject ingredient, String size, String weight) {
        JsonObject json = tfcConditioned(metal);
        json.add("ingredient", ingredient);
        json.addProperty("size", size);
        json.addProperty("weight", weight);
        return json;
    }

    private static JsonObject tfcConditioned(TfcMetal metal) {
        JsonObject json = new JsonObject();
        if (metal.isFirmaLife()) {
            JsonArray conditions = new JsonArray();
            conditions.add(ContentConditions.firmaLife());
            json.add("neoforge:conditions", conditions);
        }
        return json;
    }

    private static JsonObject tfcAnvil(TfcMetal metal, MetalForm result, String input, int count, JsonArray rules) {
        JsonObject json = tfcConditioned(metal);
        json.addProperty("type", "tfc:anvil");
        json.add("ingredient", tagIngredient("c:" + input + "/" + metal.id()));
        json.add("result", item(metal.item(result), count));
        json.add("rules", rules);
        if (metal.tier() > 0) json.addProperty("tier", metal.tier());
        return json;
    }

    private void addSpecialRecipes(CachedOutput output, List<CompletableFuture<?>> files) {
        files.add(save(output, "tfcm/recipe/anvil/metal/ingot/tungsten_steel", highCarbonAnvil()));
        files.add(save(output, "tfcm/recipe/welding/metal/ingot/high_carbon_tungsten_steel", highCarbonWelding()));
        files.add(save(output, "tfcm/recipe/welding/metal/ingot/netherite", netheriteWelding()));
        files.add(save(output, "tfcm/tfc/item_heat/netherite/scrap", netheriteScrapHeat()));
        files.add(save(output, "tfcm/recipe/heating/metal/ingot/high_carbon_tungsten_steel", highCarbonHeating()));
        files.add(save(output, "tfcm/tfc/item_heat/tungsten_steel/high_carbon_ingot", highCarbonHeat()));
        files.add(save(output, "tfcm/tfc/item_size/tungsten_steel/high_carbon_ingot", highCarbonSize()));
        files.add(save(output, "c/tags/item/ingots/high_carbon_tungsten_steel", tag(Metal.TUNGSTEN_STEEL,
                "tfcm:metal/ingot/high_carbon_tungsten_steel")));
        files.add(save(output, "tfcm/recipe/heating/loose_andesite", andesiteHeating()));
        files.add(save(output, "tfcm/tfc/item_heat/loose_andesite", andesiteItemHeat()));
        files.add(save(output, "tfcm/tfc/fluid_heat/molten_andesite", andesiteFluidHeat()));
        files.add(save(output, "tfcm/tags/fluid/molten_andesite", tag(Metal.ANDESITE_ALLOY,
                "tfcm:molten_andesite", "tfcm:flowing_molten_andesite")));

        files.add(save(output, "tfcm/recipe/alloy/constantan", alloy(Metal.CONSTANTAN,
                range("tfc:metal/copper", .4, .6), range("tfc:metal/nickel", .4, .6))));
        files.add(save(output, "tfcm/recipe/alloy/electrum", alloy(Metal.ELECTRUM,
                range("tfc:metal/gold", .4, .6), range("tfc:metal/silver", .4, .6))));
        files.add(save(output, "tfcm/recipe/alloy/invar", alloy(Metal.INVAR,
                range("tfc:metal/wrought_iron", .6, .7), range("tfc:metal/nickel", .3, .4))));
        files.add(save(output, "tfcm/recipe/alloy/osmiridium", alloy(Metal.OSMIRIDIUM,
                range("tfcm:metal/osmium", .6, .8), range("tfcm:metal/iridium", .2, .4))));
        files.add(save(output, "tfcm/recipe/alloy/solder", alloy(Metal.SOLDER,
                range("tfcm:metal/antimony", .4, .6), range("tfc:metal/bismuth", .2, .4), range("tfc:metal/tin", .2, .4))));
        files.add(save(output, "tfcm/recipe/alloy/andesite_alloy_zinc", alloy(Metal.ANDESITE_ALLOY,
                range("tfcm:molten_andesite", .89, .91), range("tfc:metal/zinc", .09, .11))));
        files.add(save(output, "tfcm/recipe/alloy/andesite_alloy_tin", alloy(Metal.ANDESITE_ALLOY,
                range("tfcm:molten_andesite", .89, .91), range("tfc:metal/tin", .09, .11))));
        files.add(save(output, "tfcm/recipe/alloy/ferroboron", alloy(Metal.FERROBORON,
                range("tfcm:metal/boron", .5, .5), range("tfc:metal/steel", .5, .5))));
        files.add(save(output, "tfcm/recipe/alloy/tough_alloy", alloy(Metal.TOUGH_ALLOY,
                range("tfcm:metal/ferroboron", .5, .5), range("tfcm:metal/lithium", .5, .5))));
        files.add(save(output, "tfcm/recipe/alloy/zircaloy", alloy(Metal.ZIRCALOY,
                range("tfcm:metal/zirconium", .875, .875), range("tfc:metal/tin", .125, .125))));
        files.add(save(output, "tfcm/recipe/alloy/hsla_steel", alloy(Metal.HSLA_STEEL,
                range("tfc:metal/cast_iron", .9375, .9375), range("tfc:metal/steel", .0625, .0625))));
        files.add(save(output, "tfcm/recipe/alloy/super_alloy", alloy(Metal.SUPER_ALLOY,
                range("tfcm:metal/tough_alloy", .45, .55), range("tfcm:metal/titanium", .45, .55))));
    }

    private CompletableFuture<?> save(CachedOutput output, String location, JsonObject json) {
        return DataProvider.saveStable(output, json, data.resolve(location + ".json"));
    }

    private static JsonObject casting(Metal metal, boolean fire) {
        JsonObject json = conditioned(metal);
        json.addProperty("type", "tfc:casting");
        json.addProperty("break_chance", fire ? .01 : .1);
        json.add("fluid", fluid("fluid", "tfcm:metal/" + metal.id(), 100));
        json.add("mold", itemIngredient(fire ? "tfc:ceramic/fire_ingot_mold" : "tfc:ceramic/ingot_mold"));
        json.add("result", item(itemId(metal, "ingot"), 1));
        return json;
    }

    private static JsonObject fluidHeat(Metal metal) {
        JsonObject json = conditioned(metal);
        json.addProperty("fluid", "tfcm:metal/" + metal.id());
        json.addProperty("melt_temperature", metal.meltingPoint());
        json.addProperty("specific_heat_capacity", metal.specificHeat());
        return json;
    }

    private static JsonObject anvil(Metal metal, String result, String input, int count, JsonArray rules) {
        JsonObject json = conditioned(metal);
        json.addProperty("type", "tfc:anvil");
        json.add("ingredient", tagIngredient("c:" + input + "/" + metal.id()));
        json.add("result", item(itemId(metal, result), count));
        json.add("rules", rules);
        json.addProperty("tier", metal.forgingTier());
        return json;
    }

    // like TFC one tier lower than the metal, so that its own anvil can be made on a lower one
    private static int doubleTier(Metal metal) {
        return Math.max(metal.forgingTier() - 1, 0);
    }

    private static JsonObject welding(Metal metal, String result, String first, String second, int tier) {
        JsonObject json = conditioned(metal);
        json.addProperty("type", "tfc:welding");
        json.add("first_input", tagIngredient("c:" + first + "/" + metal.id()));
        json.add("second_input", tagIngredient("c:" + second + "/" + metal.id()));
        json.add("result", item(itemId(metal, result), 1));
        json.addProperty("tier", tier);
        return json;
    }

    private static JsonObject extendedAnvil(Metal metal, MetalForm form) {
        return anvil(metal, form.id, form.anvilInput().tag(), form.anvilCount(),
                forgeRules(form.anvilRules().toArray(String[]::new)));
    }

    private static JsonObject nuggetCraft(Metal metal, boolean reverse) {
        JsonObject json = conditioned(metal);
        json.addProperty("type", reverse ? "minecraft:crafting_shaped" : "minecraft:crafting_shapeless");
        if (reverse) {
            JsonArray pattern = new JsonArray();
            pattern.add("NNN");
            pattern.add("NNN");
            pattern.add("NNN");
            json.add("pattern", pattern);
            JsonObject key = new JsonObject();
            key.add("N", tagIngredient("c:nuggets/" + metal.id()));
            json.add("key", key);
        } else {
            JsonArray ingredients = new JsonArray();
            ingredients.add(tagIngredient("c:ingots/" + metal.id()));
            json.add("ingredients", ingredients);
        }
        json.add("result", item(itemId(metal, reverse ? "ingot" : "nugget"), reverse ? 1 : 9));
        return json;
    }

    // the same recipes TFC gives its own metal blocks
    private static JsonObject blockCraft(Metal metal) {
        JsonObject json = conditioned(metal);
        json.addProperty("type", "tfc:advanced_shaped_crafting");
        json.addProperty("input_column", 2);
        json.add("pattern", forgeRules(" SH", "SWS", " S "));
        JsonObject key = new JsonObject();
        key.add("H", tagIngredient("c:tools/hammer"));
        key.add("S", tagIngredient("c:sheets/" + metal.id()));
        key.add("W", tagIngredient("minecraft:planks"));
        json.add("key", key);
        JsonObject modifier = new JsonObject();
        modifier.addProperty("type", "tfc:damage_crafting_remainder");
        JsonArray modifiers = new JsonArray();
        modifiers.add(modifier);
        JsonObject remainder = new JsonObject();
        remainder.add("modifiers", modifiers);
        json.add("remainder", remainder);
        json.add("result", item(blockId(metal, ""), 8));
        return json;
    }

    private static JsonObject slabCraft(Metal metal) {
        return blockShapeCraft(metal, forgeRules("BBB"), blockId(metal, "_slab"), 6);
    }

    private static JsonObject stairsCraft(Metal metal) {
        return blockShapeCraft(metal, forgeRules("B  ", "BB ", "BBB"), blockId(metal, "_stairs"), 8);
    }

    private static JsonObject blockShapeCraft(Metal metal, JsonArray pattern, String result, int count) {
        JsonObject json = conditioned(metal);
        json.addProperty("type", "minecraft:crafting_shaped");
        json.addProperty("category", "misc");
        json.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("B", tagIngredient("c:storage_blocks/" + metal.id()));
        json.add("key", key);
        json.add("result", item(result, count));
        return json;
    }

    private static JsonObject dust(Metal metal) {
        JsonObject json = conditioned(metal);
        json.addProperty("type", "tfc:quern");
        json.add("ingredient", tagIngredient("c:ingots/" + metal.id()));
        json.add("result", item(itemId(metal, "dust"), 1));
        return json;
    }

    private static JsonObject heating(Metal metal, MetalForm form) {
        JsonObject json = conditioned(metal);
        json.addProperty("type", "tfc:heating");
        json.add("ingredient", itemIngredient(form.item(metal)));
        json.add("result_fluid", fluid("id", "tfcm:metal/" + metal.id(), form.amount));
        json.addProperty("temperature", metal.meltingPoint());
        return json;
    }

    private static JsonObject itemHeat(Metal metal, MetalForm form) {
        JsonObject json = conditioned(metal);
        json.add("ingredient", itemIngredient(form.item(metal)));
        json.addProperty("heat_capacity", metal.specificHeat() * 333.33334f * form.amount / 100f);
        json.addProperty("forging_temperature", metal.forgingTemperature());
        json.addProperty("welding_temperature", metal.weldingTemperature());
        return json;
    }

    private static JsonObject itemSize(Metal metal, MetalForm form) {
        JsonObject json = conditioned(metal);
        json.add("ingredient", itemIngredient(form.item(metal)));
        json.addProperty("size", form.size);
        json.addProperty("weight", form.weight);
        return json;
    }

    private static JsonObject highCarbonAnvil() {
        JsonObject json = conditioned(Metal.TUNGSTEN_STEEL);
        json.addProperty("type", "tfc:anvil");
        json.add("ingredient", itemIngredient("tfcm:metal/ingot/high_carbon_tungsten_steel"));
        json.add("result", item("tfcm:metal/ingot/tungsten_steel", 1));
        json.add("rules", forgeRules());
        json.addProperty("tier", 7);
        return json;
    }

    private static JsonObject highCarbonWelding() {
        JsonObject json = conditioned(Metal.TUNGSTEN_STEEL);
        json.getAsJsonArray("neoforge:conditions").add(ContentConditions.metal(Metal.TUNGSTEN.id()));
        json.addProperty("type", "tfc:welding");
        json.add("first_input", tagIngredient("c:ingots/tungsten"));
        json.add("second_input", itemIngredient("tfc:metal/ingot/black_steel"));
        json.add("result", item("tfcm:metal/ingot/high_carbon_tungsten_steel", 1));
        json.addProperty("tier", 6);
        return json;
    }

    private static JsonObject netheriteWelding() {
        JsonObject json = conditioned(Metal.NETHERITE);
        json.addProperty("type", "tfc:welding");
        json.add("first_input", itemIngredient("minecraft:netherite_scrap"));
        json.add("second_input", itemIngredient("tfc:metal/ingot/black_steel"));
        json.add("result", item("tfcm:metal/ingot/netherite", 1));
        json.addProperty("tier", 6);
        return json;
    }

    // the scrap has to reach welding temperature; values of the Netherite ingot it becomes
    private static JsonObject netheriteScrapHeat() {
        return itemHeat(Metal.NETHERITE, MetalForm.INGOT, "minecraft:netherite_scrap");
    }

    private static JsonObject highCarbonHeating() {
        JsonObject json = conditioned(Metal.TUNGSTEN_STEEL);
        json.addProperty("type", "tfc:heating");
        json.add("ingredient", itemIngredient("tfcm:metal/ingot/high_carbon_tungsten_steel"));
        json.add("result_fluid", fluid("id", "tfcm:metal/tungsten_steel", 100));
        json.addProperty("temperature", Metal.TUNGSTEN_STEEL.meltingPoint());
        return json;
    }

    private static JsonObject highCarbonHeat() {
        return itemHeat(Metal.TUNGSTEN_STEEL, MetalForm.INGOT, "tfcm:metal/ingot/high_carbon_tungsten_steel");
    }

    private static JsonObject highCarbonSize() {
        JsonObject json = conditioned(Metal.TUNGSTEN_STEEL);
        json.add("ingredient", itemIngredient("tfcm:metal/ingot/high_carbon_tungsten_steel"));
        json.addProperty("size", "large");
        json.addProperty("weight", "medium");
        return json;
    }

    private static JsonObject itemHeat(Metal metal, MetalForm form, String id) {
        JsonObject json = itemHeat(metal, form);
        json.add("ingredient", itemIngredient(id));
        return json;
    }

    private static JsonObject andesiteHeating() {
        JsonObject json = conditioned(Metal.ANDESITE_ALLOY);
        json.addProperty("type", "tfc:heating");
        json.add("ingredient", itemIngredient("tfc:rock/loose/andesite"));
        json.add("result_fluid", fluid("id", "tfcm:molten_andesite", 90));
        json.addProperty("temperature", 1300);
        return json;
    }

    private static JsonObject andesiteFluidHeat() {
        JsonObject json = conditioned(Metal.ANDESITE_ALLOY);
        json.addProperty("fluid", "tfcm:molten_andesite");
        json.addProperty("melt_temperature", 1300);
        json.addProperty("specific_heat_capacity", .012);
        return json;
    }

    private static JsonObject andesiteItemHeat() {
        JsonObject json = conditioned(Metal.ANDESITE_ALLOY);
        json.add("ingredient", itemIngredient("tfc:rock/loose/andesite"));
        json.addProperty("heat_capacity", 1.2f);
        return json;
    }

    private void addCollectionTags(CachedOutput output, List<CompletableFuture<?>> files) {
        JsonObject mineable = allMetalBlocks("", "_slab", "_stairs");
        for (ToolMetal metal : ToolMetal.values()) {
            mineable.getAsJsonArray("values").add(ContentConditions.optional("tfcm:metal/anvil/" + metal.getSerializedName()));
        }
        mineable.getAsJsonArray("values").add("#tfcm:ores");
        mineable.getAsJsonArray("values").add("#tfcm:geode_blocks");
        files.add(save(output, "minecraft/tags/block/mineable/pickaxe", mineable));
        files.add(save(output, "minecraft/tags/block/slabs", allMetalBlocks("_slab")));
        files.add(save(output, "minecraft/tags/block/stairs", allMetalBlocks("_stairs")));
        files.add(save(output, "tfc/tags/fluid/molten_metals", allMetalFluids(false)));
        files.add(save(output, "tfc/tags/fluid/usable_in_ingot_mold", allMetalFluids(false)));
        // the old TFCM names of TFC's common form tags
        for (String form : List.of("sheets", "double_sheets", "double_ingots")) {
            JsonArray values = new JsonArray();
            values.add("#c:" + form);
            JsonObject alias = new JsonObject();
            alias.add("values", values);
            files.add(save(output, "tfc/tags/item/" + form, alias));
        }
        files.add(save(output, "c/tags/fluid/molten_metal", allMetalFluids(true)));
        for (MetalForm form : MetalForm.values()) {
            if (form != MetalForm.SLAB && form != MetalForm.STAIRS)
                files.add(save(output, "c/tags/item/" + form.tag(), allFormTags(form.tag())));
        }
    }

    private static JsonObject allMetalBlocks(String... suffixes) {
        JsonObject json = new JsonObject();
        JsonArray values = new JsonArray();
        for (Metal metal : Metal.values()) {
            for (String suffix : suffixes) values.add(ContentConditions.optional(blockId(metal, suffix)));
        }
        json.add("values", values);
        return json;
    }

    private static JsonObject allMetalFluids(boolean includeFlowing) {
        JsonObject json = new JsonObject();
        JsonArray values = new JsonArray();
        for (Metal metal : Metal.values()) {
            values.add(ContentConditions.optional("tfcm:metal/" + metal.id()));
            if (includeFlowing) values.add(ContentConditions.optional("tfcm:metal/flowing_" + metal.id()));
        }
        json.add("values", values);
        return json;
    }

    private static JsonObject allFormTags(String form) {
        JsonObject json = new JsonObject();
        JsonArray values = new JsonArray();
        for (Metal metal : Metal.values()) values.add(ContentConditions.optional("#c:" + form + "/" + metal.id()));
        boolean added = false;
        for (MetalForm metalForm : MetalForm.values()) added |= metalForm.isAddedForm() && metalForm.tag().equals(form);
        if (added) {
            for (TfcMetal metal : TfcMetal.values()) {
                if (metal.isFirmaLife()) values.add(ContentConditions.optional("#c:" + form + "/" + metal.id()));
                else values.add("#c:" + form + "/" + metal.id());
            }
        }
        json.add("values", values);
        return json;
    }

    private static JsonObject blockLoot(Metal metal, String suffix) {
        JsonObject json = conditioned(metal);
        json.addProperty("type", "minecraft:block");
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", blockId(metal, suffix));
        JsonArray entries = new JsonArray();
        entries.add(entry);
        json.add("pools", lootPools(entries));
        json.addProperty("random_sequence", "tfcm:blocks/metal/block/" + metal.id() + suffix);
        return json;
    }

    private static JsonObject slabLoot(Metal metal) {
        JsonObject json = conditioned(metal);
        json.addProperty("type", "minecraft:block");
        JsonObject count = new JsonObject();
        count.addProperty("function", "minecraft:set_count");
        count.addProperty("add", false);
        count.addProperty("count", 2);
        JsonObject properties = new JsonObject();
        JsonObject expected = new JsonObject();
        expected.addProperty("type", "double");
        JsonObject condition = new JsonObject();
        condition.addProperty("condition", "minecraft:block_state_property");
        condition.addProperty("block", blockId(metal, "_slab"));
        condition.add("properties", expected);
        JsonArray conditions = new JsonArray();
        conditions.add(condition);
        count.add("conditions", conditions);
        JsonArray functions = new JsonArray();
        functions.add(count);
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", blockId(metal, "_slab"));
        entry.add("functions", functions);
        JsonArray entries = new JsonArray();
        entries.add(entry);
        json.add("pools", lootPools(entries));
        json.addProperty("random_sequence", "tfcm:blocks/metal/block/" + metal.id() + "_slab");
        return json;
    }

    private static JsonArray lootPools(JsonArray entries) {
        JsonObject explosion = new JsonObject();
        explosion.addProperty("condition", "minecraft:survives_explosion");
        JsonArray conditions = new JsonArray();
        conditions.add(explosion);
        JsonObject pool = new JsonObject();
        pool.addProperty("bonus_rolls", 0);
        pool.addProperty("rolls", 1);
        pool.add("entries", entries);
        pool.add("conditions", conditions);
        JsonArray pools = new JsonArray();
        pools.add(pool);
        return pools;
    }

    private static JsonObject alloy(Metal result, JsonObject... ranges) {
        JsonObject json = conditioned(result);
        json.addProperty("type", "tfc:alloy");
        JsonArray contents = new JsonArray();
        for (JsonObject range : ranges) {
            contents.add(range);
            String fluid = range.get("fluid").getAsString();
            if (fluid.startsWith("tfcm:metal/")) {
                String input = fluid.substring("tfcm:metal/".length());
                if (!input.equals(result.id())) {
                    json.getAsJsonArray("neoforge:conditions").add(ContentConditions.metal(input));
                }
            }
        }
        json.add("contents", contents);
        json.addProperty("result", "tfcm:metal/" + result.id());
        return json;
    }

    private static JsonObject range(String fluid, double min, double max) {
        JsonObject json = new JsonObject();
        json.addProperty("fluid", fluid);
        json.addProperty("min", min);
        json.addProperty("max", max);
        return json;
    }

    private static JsonObject tag(Metal metal, String... values) {
        JsonObject json = conditioned(metal);
        JsonArray array = new JsonArray();
        for (String value : values) array.add(value);
        json.add("values", array);
        return json;
    }

    static JsonObject itemIngredient(String id) {
        JsonObject json = new JsonObject();
        json.addProperty("item", id);
        return json;
    }

    static JsonObject tagIngredient(String id) {
        JsonObject json = new JsonObject();
        json.addProperty("tag", id);
        return json;
    }

    static JsonObject item(String id, int count) {
        JsonObject json = new JsonObject();
        json.addProperty("count", count);
        json.addProperty("id", id);
        return json;
    }

    private static JsonObject fluid(String field, String id, int amount) {
        JsonObject json = new JsonObject();
        json.addProperty("amount", amount);
        json.addProperty(field, id);
        return json;
    }

    private static JsonArray forgeRules() {
        return forgeRules("hit_last", "hit_second_last", "hit_third_last");
    }

    static JsonArray forgeRules(String... steps) {
        JsonArray rules = new JsonArray();
        for (String step : steps) rules.add(step);
        return rules;
    }

    private static JsonObject conditioned(Metal metal) {
        JsonArray conditions = new JsonArray();
        conditions.add(ContentConditions.metal(metal.id()));
        JsonObject json = new JsonObject();
        json.add("neoforge:conditions", conditions);
        return json;
    }



    private static String itemId(Metal metal, String form) {
        return "tfcm:metal/" + form + "/" + metal.id();
    }

    private static String blockId(Metal metal, String suffix) {
        return "tfcm:metal/block/" + metal.id() + suffix;
    }

    @Override
    public String getName() {
        return "TFCM metals";
    }

}
