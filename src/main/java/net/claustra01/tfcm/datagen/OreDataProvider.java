package net.claustra01.tfcm.datagen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.claustra01.tfcm.common.metal.Metal;
import net.claustra01.tfcm.common.ore.Ore;
import net.claustra01.tfcm.common.ore.OreGrade;
import net.claustra01.tfcm.common.ore.TfcOre;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public final class OreDataProvider implements DataProvider {
    private static final Map<Ore, Metal> ORE_METALS = Map.ofEntries(
            Map.entry(Ore.NATIVE_PLATINUM, Metal.PLATINUM), Map.entry(Ore.NATIVE_NAQUADAH, Metal.NAQUADAH),
            Map.entry(Ore.NATIVE_IRIDIUM, Metal.IRIDIUM), Map.entry(Ore.NATIVE_OSMIUM, Metal.OSMIUM),
            Map.entry(Ore.RUTILE, Metal.TITANIUM), Map.entry(Ore.COBALTITE, Metal.COBALT),
            Map.entry(Ore.SPODUMENE, Metal.LITHIUM), Map.entry(Ore.BAUXITE, Metal.ALUMINUM),
            Map.entry(Ore.GALENA, Metal.LEAD), Map.entry(Ore.URANINITE, Metal.URANIUM),
            Map.entry(Ore.MITHRIL_MATRIX, Metal.MITHRIL), Map.entry(Ore.STIBNITE, Metal.ANTIMONY),
            Map.entry(Ore.WOLFRAMITE, Metal.TUNGSTEN), Map.entry(Ore.THORIANITE, Metal.THORIUM),
            Map.entry(Ore.MAGNESITE, Metal.MAGNESIUM), Map.entry(Ore.ZIRCON, Metal.ZIRCONIUM));
    private final Path data;
    private final Path assets;

    public OreDataProvider(PackOutput output) {
        data = output.getOutputFolder(PackOutput.Target.DATA_PACK);
        assets = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> files = new ArrayList<>();
        List<String> oreBlocks = new ArrayList<>();
        List<String> pickaxeBlocks = new ArrayList<>();
        for (Ore ore : Ore.values()) addCustomOre(output, files, oreBlocks, pickaxeBlocks, ore);
        for (String ore : TfcOre.GRADED) {
            for (OreGrade grade : OreGrade.values()) addForeignOre(output, files, oreBlocks, pickaxeBlocks,
                    grade.id() + "_" + ore, "tfc", false);
        }
        for (String ore : TfcOre.UNGRADED) addForeignOre(output, files, oreBlocks, pickaxeBlocks, ore, "tfc", false);
        for (OreGrade grade : OreGrade.values()) addForeignOre(output, files, oreBlocks, pickaxeBlocks,
                grade.id() + "_chromite", "firmalife", true);
        List<String> chromiteBlocks = new ArrayList<>();
        for (OreGrade grade : OreGrade.values()) {
            chromiteBlocks.add("tfcm:ore/" + grade.id() + "_chromite/netherrack");
            chromiteBlocks.add("tfcm:ore/" + grade.id() + "_chromite/end_stone");
        }
        for (String ore : TfcOre.samples()) addGroundcover(output, files, ore, TfcOre.sampleTexture(ore), TfcOre.sampleDrop(ore));
        addItemTags(output, files);
        files.add(save(output, data, "tfcm/tags/block/firmalife_ores", tag(chromiteBlocks)));
        files.add(save(output, data, "c/tags/block/ores", tag(oreBlocks)));
        files.add(save(output, data, "tfcm/tags/block/ores", tag(pickaxeBlocks)));
        files.add(save(output, data, "tfc/tags/block/prospectable", tag(List.of("#tfcm:ores"))));
        files.add(save(output, data, "tfc/tags/block/mineable/propick", tag(List.of("#tfcm:ores"))));
        files.add(save(output, data, "tfc/tags/item/gem_powders", tag(List.of("tfcm:powder/quartz", "tfcm:powder/fluorite"))));
        files.add(save(output, data, "tfc/tags/item/bowl_powders", tag(List.of("tfcm:powder/quartz", "tfcm:powder/fluorite"))));
        files.add(save(output, data, "c/tags/item/gems/quartz", tag(List.of("tfcm:gem/cut_quartz", "tfcm:gem/certus_quartz"))));
        files.add(save(output, data, "c/tags/item/gems", tag(List.of("#c:gems/quartz"))));
        files.add(save(output, data, "tfcm/recipe/quern/quartz", conditioned("quartz", null, quern("quartz"))));
        files.add(save(output, data, "tfcm/recipe/quern/fluorite", conditioned("fluorite", null, quern("fluorite"))));
        files.add(save(output, data, "tfcm/recipe/crafting/cut_quartz", conditioned("quartz", null, polishing())));
        files.add(save(output, assets, "tfcm/models/item/powder/quartz", generatedModel("tfcm:item/powder/quartz")));
        files.add(save(output, assets, "tfcm/models/item/powder/fluorite", generatedModel("tfcm:item/powder/fluorite")));
        files.add(save(output, assets, "tfcm/models/item/gem/cut_quartz", generatedModel("tfcm:item/gem/cut_quartz")));
        files.add(save(output, assets, "tfcm/models/item/gem/certus_quartz", generatedModel("tfcm:item/gem/certus_quartz")));
        return CompletableFuture.allOf(files.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "TFCM ores";
    }

    private void addCustomOre(CachedOutput output, List<CompletableFuture<?>> files, List<String> ores,
            List<String> pickaxes, Ore ore) {
        if (ore.graded()) {
            for (OreGrade grade : OreGrade.values()) {
                String name = ore.itemName(grade);
                files.add(save(output, assets, "tfcm/models/item/ore/" + name,
                        generatedModel("tfcm:item/ore/" + name)));
                addHosts(output, files, ores, pickaxes, name, "tfcm:block/ore/" + name,
                        "tfcm:ore/" + name, ore.id(), false);
            }
            String small = "small_" + ore.id();
            files.add(save(output, assets, "tfcm/models/item/ore/" + small,
                    generatedModel("tfcm:item/ore/" + small)));
            files.add(save(output, assets, "tfcm/models/block/groundcover/" + ore.id(),
                    groundcoverModel("tfcm:item/ore/small_" + ore.id())));
            files.add(save(output, assets, "tfcm/blockstates/ore/" + small, smallBlockState(ore.id())));
            files.add(save(output, data, "tfcm/loot_table/blocks/ore/" + small,
                    blockLoot("tfcm:ore/" + small, ore.id(), false)));
            Metal metal = ORE_METALS.get(ore);
            files.add(save(output, data, "tfcm/recipe/heating/ore/" + small,
                    conditioned(ore.id(), metal, smallOreHeating(ore, metal))));
            files.add(save(output, data, "tfcm/tfc/item_heat/ore/" + small,
                    conditioned(ore.id(), metal, smallOreHeat(ore, metal))));
        } else {
            files.add(save(output, assets, "tfcm/models/item/ore/" + ore.id(),
                    generatedModel("tfcm:item/ore/" + ore.id())));
            addHosts(output, files, ores, pickaxes, ore.id(), "tfcm:block/ore/" + ore.id(),
                    "tfcm:ore/" + ore.id(), ore.id(), false);
            addGroundcover(output, files, ore.id(), "tfcm:item/ore/" + ore.id(), "tfcm:ore/" + ore.id());
        }
    }

    // Surface samples of ungraded ores: placed like a small ore, but only ever yield the raw item.
    private void addGroundcover(CachedOutput output, List<CompletableFuture<?>> files, String ore, String texture,
            String drop) {
        files.add(save(output, assets, "tfcm/models/block/groundcover/" + ore, groundcoverModel(texture)));
        files.add(save(output, assets, "tfcm/blockstates/ore/small_" + ore, smallBlockState(ore)));
        files.add(save(output, data, "tfcm/loot_table/blocks/ore/small_" + ore, blockLoot(drop, ore, false)));
    }

    private void addItemTags(CachedOutput output, List<CompletableFuture<?>> files) {
        List<String> metalOres = new ArrayList<>();
        List<String> orePieces = new ArrayList<>();
        List<String> smallOres = new ArrayList<>();
        List<String> commonOres = new ArrayList<>();
        for (Ore ore : Ore.values()) {
            if (!ore.graded()) {
                String item = "tfcm:ore/" + ore.id();
                if (ore == Ore.CAROBBIITE) {
                    metalOres.add(item);
                    files.add(save(output, data, "c/tags/item/ores/beryllium", tag(List.of(item))));
                    commonOres.add("#c:ores/beryllium");
                }
                orePieces.add(item);
                files.add(save(output, data, "c/tags/item/ores/" + ore.id(), tag(List.of(item))));
                commonOres.add("#c:ores/" + ore.id());
                continue;
            }
            smallOres.add("tfcm:ore/small_" + ore.id());
            List<String> grades = new ArrayList<>();
            for (OreGrade grade : OreGrade.values()) {
                String item = "tfcm:ore/" + ore.itemName(grade);
                metalOres.add(item);
                orePieces.add(item);
                grades.add("#c:ores/" + ore.id() + "/" + grade.id());
                files.add(save(output, data, "c/tags/item/ores/" + ore.id() + "/" + grade.id(), tag(List.of(item))));
                String metal = ORE_METALS.get(ore).id();
                files.add(save(output, data, "c/tags/item/ores/" + metal + "/" + grade.id(), tag(List.of(item))));
                commonOres.add("#c:ores/" + metal + "/" + grade.id());
            }
            files.add(save(output, data, "c/tags/item/ores/" + ore.id(), tag(grades)));
        }
        files.add(save(output, data, "tfc/tags/item/metal_ores", tag(metalOres)));
        files.add(save(output, data, "tfc/tags/item/ore_pieces", tag(orePieces)));
        files.add(save(output, data, "tfc/tags/item/small_ore_pieces", tag(smallOres)));
        files.add(save(output, data, "c/tags/item/ores", tag(commonOres)));
    }

    private void addHosts(CachedOutput output, List<CompletableFuture<?>> files, List<String> ores,
            List<String> pickaxes, String name, String overlay, String drop, String oreId, boolean conditional) {
        for (Rock rock : Rock.values()) addHost(output, files, ores, pickaxes, name, rock.getSerializedName(),
                "tfc:block/rock/raw/" + rock.getSerializedName(), overlay, drop, oreId, conditional);
        addHost(output, files, ores, pickaxes, name, "netherrack", "minecraft:block/netherrack", overlay, drop, oreId, conditional);
        addHost(output, files, ores, pickaxes, name, "end_stone", "minecraft:block/end_stone", overlay, drop, oreId, conditional);
    }

    private void addForeignOre(CachedOutput output, List<CompletableFuture<?>> files, List<String> ores,
            List<String> pickaxes, String name, String namespace, boolean conditional) {
        String overlay = namespace + ":block/ore/" + name;
        String drop = namespace + ":ore/" + name;
        String oreId = name.replaceFirst("^(poor|normal|rich)_", "");
        addHost(output, files, ores, pickaxes, name, "netherrack", "minecraft:block/netherrack", overlay, drop, oreId, conditional);
        addHost(output, files, ores, pickaxes, name, "end_stone", "minecraft:block/end_stone", overlay, drop, oreId, conditional);
    }

    private void addHost(CachedOutput output, List<CompletableFuture<?>> files, List<String> ores,
            List<String> pickaxes, String name, String host, String base, String overlay, String drop,
            String oreId, boolean conditional) {
        String id = "tfcm:ore/" + name + "/" + host;
        String tagEntry = conditional ? "#tfcm:firmalife_ores" : id;
        if (!ores.contains(tagEntry)) ores.add(tagEntry);
        if (!pickaxes.contains(tagEntry)) pickaxes.add(tagEntry);
        files.add(save(output, assets, "tfcm/models/block/ore/" + name + "/" + host, oreModel(base, overlay)));
        files.add(save(output, assets, "tfcm/models/item/ore/" + name + "/" + host,
                parentModel("tfcm:block/ore/" + name + "/" + host)));
        files.add(save(output, assets, "tfcm/blockstates/ore/" + name + "/" + host,
                blockState("tfcm:block/ore/" + name + "/" + host)));
        files.add(save(output, data, "tfcm/loot_table/blocks/ore/" + name + "/" + host,
                blockLoot(drop, oreId, conditional)));
    }

    private CompletableFuture<?> save(CachedOutput output, Path root, String location, JsonObject json) {
        return DataProvider.saveStable(output, json, root.resolve(location + ".json"));
    }

    private static JsonObject generatedModel(String texture) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", "minecraft:item/generated");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", texture);
        json.add("textures", textures);
        return json;
    }

    private static JsonObject parentModel(String parent) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", parent);
        return json;
    }

    private static JsonObject oreModel(String base, String overlay) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", "tfc:block/ore");
        JsonObject textures = new JsonObject();
        textures.addProperty("all", base);
        textures.addProperty("particle", base);
        textures.addProperty("overlay", overlay);
        json.add("textures", textures);
        return json;
    }

    private static JsonObject blockState(String model) {
        JsonObject json = new JsonObject();
        JsonObject variants = new JsonObject();
        JsonObject value = new JsonObject();
        value.addProperty("model", model);
        variants.add("", value);
        json.add("variants", variants);
        return json;
    }

    private static JsonObject smallBlockState(String ore) {
        JsonObject json = new JsonObject();
        JsonObject variants = new JsonObject();
        JsonArray models = new JsonArray();
        for (int rotation : new int[] {0, 90, 180, 270}) {
            JsonObject model = new JsonObject();
            model.addProperty("model", "tfcm:block/groundcover/" + ore);
            if (rotation != 0) model.addProperty("y", rotation);
            models.add(model);
        }
        variants.add("", models);
        json.add("variants", variants);
        return json;
    }

    private static JsonObject groundcoverModel(String texture) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", "tfc:block/groundcover/native_copper");
        JsonObject textures = new JsonObject();
        textures.addProperty("0", texture);
        textures.addProperty("particle", texture);
        json.add("textures", textures);
        return json;
    }

    private static JsonObject blockLoot(String drop, String ore, boolean conditional) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:block");
        json.add("neoforge:conditions", conditions(ore, conditional));
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", drop);
        JsonArray entries = new JsonArray();
        entries.add(entry);
        JsonObject pool = new JsonObject();
        pool.addProperty("rolls", 1);
        pool.add("entries", entries);
        JsonObject survivesExplosion = new JsonObject();
        survivesExplosion.addProperty("condition", "minecraft:survives_explosion");
        JsonArray poolConditions = new JsonArray();
        poolConditions.add(survivesExplosion);
        pool.add("conditions", poolConditions);
        JsonArray pools = new JsonArray();
        pools.add(pool);
        json.add("pools", pools);
        return json;
    }

    private static JsonObject tag(List<String> values) {
        JsonObject json = new JsonObject();
        json.addProperty("replace", false);
        JsonArray array = new JsonArray();
        for (String value : values) {
            array.add(ContentConditions.optional(value));
        }
        json.add("values", array);
        return json;
    }

    private static JsonArray conditions(String ore, boolean modLoaded) {
        JsonArray conditions = new JsonArray();
        conditions.add(ContentConditions.ore(ore));
        if (modLoaded) {
            JsonObject condition = new JsonObject();
            condition.addProperty("type", "neoforge:mod_loaded");
            condition.addProperty("modid", "firmalife");
            conditions.add(condition);
        }
        return conditions;
    }

    private static JsonObject conditioned(String ore, Metal metal, JsonObject json) {
        JsonArray conditions = conditions(ore, false);
        if (metal != null) {
            conditions.add(ContentConditions.metal(metal.id()));
        }
        json.add("neoforge:conditions", conditions);
        return json;
    }

    private static JsonObject smallOreHeating(Ore ore, Metal metal) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "tfc:heating");
        json.add("ingredient", ingredient("tfcm:ore/small_" + ore.id()));
        JsonObject fluid = new JsonObject();
        fluid.addProperty("id", "tfcm:metal/" + metal.id());
        fluid.addProperty("amount", 10);
        json.add("result_fluid", fluid);
        json.addProperty("temperature", metal.meltingPoint());
        return json;
    }

    private static JsonObject smallOreHeat(Ore ore, Metal metal) {
        JsonObject json = new JsonObject();
        json.add("ingredient", ingredient("tfcm:ore/small_" + ore.id()));
        json.addProperty("heat_capacity", metal.specificHeat() * 33.333334f);
        json.addProperty("forging_temperature", metal.forgingTemperature());
        json.addProperty("welding_temperature", metal.weldingTemperature());
        return json;
    }

    private static JsonObject quern(String ore) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "tfc:quern");
        json.add("ingredient", ingredient("tfcm:ore/" + ore));
        JsonObject result = new JsonObject();
        result.addProperty("id", "tfcm:powder/" + ore);
        result.addProperty("count", 4);
        json.add("result", result);
        return json;
    }

    private static JsonObject polishing() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "tfc:advanced_shapeless_crafting");
        JsonArray ingredients = new JsonArray();
        ingredients.add(ingredient("tfc:sandpaper"));
        ingredients.add(ingredient("tfcm:ore/quartz"));
        json.add("ingredients", ingredients);
        json.add("primary_ingredient", ingredient("tfc:sandpaper"));
        JsonObject modifier = new JsonObject();
        modifier.addProperty("type", "tfc:damage_crafting_remainder");
        JsonArray modifiers = new JsonArray();
        modifiers.add(modifier);
        JsonObject remainder = new JsonObject();
        remainder.add("modifiers", modifiers);
        json.add("remainder", remainder);
        JsonObject result = new JsonObject();
        result.addProperty("id", "tfcm:gem/cut_quartz");
        result.addProperty("count", 1);
        json.add("result", result);
        return json;
    }

    private static JsonObject ingredient(String item) {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", item);
        return ingredient;
    }
}
