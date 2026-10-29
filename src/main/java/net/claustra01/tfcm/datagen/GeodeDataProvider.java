package net.claustra01.tfcm.datagen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.claustra01.tfcm.common.block.geode.GeodeMineral;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public final class GeodeDataProvider implements DataProvider {
    private final Path data;
    private final Path assets;

    public GeodeDataProvider(PackOutput output) {
        data = output.getOutputFolder(PackOutput.Target.DATA_PACK);
        assets = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> files = new ArrayList<>();
        List<String> geodeBlocks = new ArrayList<>();
        List<String> features = new ArrayList<>();
        for (GeodeMineral mineral : GeodeMineral.values()) {
            addAssets(output, files, mineral);
            addLoot(output, files, mineral);
            files.add(save(output, data, "tfcm/worldgen/configured_feature/" + mineral.id() + "_geode",
                    configuredFeature(mineral)));
            files.add(save(output, data, "tfcm/worldgen/placed_feature/" + mineral.id() + "_geode",
                    placedFeature(mineral)));
            geodeBlocks.add("tfcm:mineral/" + mineral.id() + "_block");
            geodeBlocks.add("tfcm:mineral/budding_" + mineral.id());
            geodeBlocks.add("tfcm:mineral/small_" + mineral.id() + "_bud");
            geodeBlocks.add("tfcm:mineral/medium_" + mineral.id() + "_bud");
            geodeBlocks.add("tfcm:mineral/large_" + mineral.id() + "_bud");
            geodeBlocks.add("tfcm:mineral/" + mineral.id() + "_cluster");
            features.add("tfcm:" + mineral.id() + "_geode");
        }
        files.add(save(output, data, "tfcm/tags/block/geode_blocks", tag(geodeBlocks)));
        files.add(save(output, data, "tfcm/tags/worldgen/biome/tfc_overworld", tfcBiomes()));
        files.add(save(output, data, "tfcm/neoforge/biome_modifier/add_geodes",
                biomeModifier(features)));
        return CompletableFuture.allOf(files.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "TFCM geodes";
    }

    private void addAssets(CachedOutput output, List<CompletableFuture<?>> files, GeodeMineral mineral) {
        String id = mineral.id();
        String blockTexture = texture(mineral, id + "_block", "amethyst_block");
        String buddingTexture = texture(mineral, "budding_" + id, "budding_amethyst");
        files.add(save(output, assets, "tfcm/models/block/mineral/" + id + "_block", cube(blockTexture)));
        files.add(save(output, assets, "tfcm/models/block/mineral/budding_" + id, cube(buddingTexture)));
        files.add(save(output, assets, "tfcm/blockstates/mineral/" + id + "_block",
                simpleBlockState("tfcm:block/mineral/" + id + "_block")));
        files.add(save(output, assets, "tfcm/blockstates/mineral/budding_" + id,
                simpleBlockState("tfcm:block/mineral/budding_" + id)));
        files.add(save(output, assets, "tfcm/models/item/mineral/" + id + "_block",
                parent("tfcm:block/mineral/" + id + "_block")));
        files.add(save(output, assets, "tfcm/models/item/mineral/budding_" + id,
                parent("tfcm:block/mineral/budding_" + id)));
        for (String stage : List.of("small", "medium", "large")) {
            String name = stage + "_" + id + "_bud";
            String texture = texture(mineral, name, stage + "_amethyst_bud");
            files.add(save(output, assets, "tfcm/models/block/mineral/" + name, cross(texture)));
            files.add(save(output, assets, "tfcm/blockstates/mineral/" + name,
                    directionalBlockState("tfcm:block/mineral/" + name)));
            files.add(save(output, assets, "tfcm/models/item/mineral/" + name, generated(texture)));
        }
        String cluster = id + "_cluster";
        String clusterTexture = texture(mineral, cluster, "amethyst_cluster");
        files.add(save(output, assets, "tfcm/models/block/mineral/" + cluster, cross(clusterTexture)));
        files.add(save(output, assets, "tfcm/blockstates/mineral/" + cluster,
                directionalBlockState("tfcm:block/mineral/" + cluster)));
        files.add(save(output, assets, "tfcm/models/item/mineral/" + cluster, generated(clusterTexture)));
    }

    private void addLoot(CachedOutput output, List<CompletableFuture<?>> files, GeodeMineral mineral) {
        String id = mineral.id();
        files.add(save(output, data, "tfcm/loot_table/blocks/mineral/" + id + "_block",
                blockLoot("tfcm:mineral/" + id + "_block", 1)));
        files.add(save(output, data, "tfcm/loot_table/blocks/mineral/budding_" + id,
                blockLoot("tfcm:mineral/budding_" + id, 1)));
        for (String stage : List.of("small", "medium", "large")) {
            String name = stage + "_" + id + "_bud";
            files.add(save(output, data, "tfcm/loot_table/blocks/mineral/" + name,
                    blockLoot("tfcm:mineral/" + name, 1)));
        }
        files.add(save(output, data, "tfcm/loot_table/blocks/mineral/" + id + "_cluster",
                blockLoot(mineral.clusterDrop(), 2)));
    }

    private CompletableFuture<?> save(CachedOutput output, Path root, String location, JsonObject json) {
        return DataProvider.saveStable(output, json, root.resolve(location + ".json"));
    }

    private static String texture(GeodeMineral mineral, String local, String vanilla) {
        return mineral == GeodeMineral.AMETHYST ? "minecraft:block/" + vanilla : "tfcm:block/mineral/" + local;
    }

    private static JsonObject cube(String texture) {
        JsonObject json = parent("minecraft:block/cube_all");
        JsonObject textures = new JsonObject();
        textures.addProperty("all", texture);
        json.add("textures", textures);
        return json;
    }

    private static JsonObject cross(String texture) {
        JsonObject json = parent("minecraft:block/cross");
        json.addProperty("render_type", "minecraft:cutout");
        JsonObject textures = new JsonObject();
        textures.addProperty("cross", texture);
        json.add("textures", textures);
        return json;
    }

    private static JsonObject generated(String texture) {
        JsonObject json = parent("minecraft:item/generated");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", texture);
        json.add("textures", textures);
        return json;
    }

    private static JsonObject parent(String parent) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", parent);
        return json;
    }

    private static JsonObject simpleBlockState(String model) {
        JsonObject variants = new JsonObject();
        variants.add("", model(model, 0, 0));
        JsonObject json = new JsonObject();
        json.add("variants", variants);
        return json;
    }

    private static JsonObject directionalBlockState(String model) {
        JsonObject variants = new JsonObject();
        variants.add("facing=down", model(model, 180, 0));
        variants.add("facing=east", model(model, 90, 90));
        variants.add("facing=north", model(model, 90, 0));
        variants.add("facing=south", model(model, 90, 180));
        variants.add("facing=up", model(model, 0, 0));
        variants.add("facing=west", model(model, 90, 270));
        JsonObject json = new JsonObject();
        json.add("variants", variants);
        return json;
    }

    private static JsonObject model(String model, int x, int y) {
        JsonObject json = new JsonObject();
        json.addProperty("model", model);
        if (x != 0) json.addProperty("x", x);
        if (y != 0) json.addProperty("y", y);
        return json;
    }

    private static JsonObject blockLoot(String item, int count) {
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", item);
        if (count != 1) {
            JsonObject function = new JsonObject();
            function.addProperty("function", "minecraft:set_count");
            function.addProperty("count", count);
            JsonArray functions = new JsonArray();
            functions.add(function);
            entry.add("functions", functions);
        }
        JsonArray entries = new JsonArray();
        entries.add(entry);
        JsonObject pool = new JsonObject();
        pool.addProperty("rolls", 1);
        pool.add("entries", entries);
        JsonObject survivesExplosion = new JsonObject();
        survivesExplosion.addProperty("condition", "minecraft:survives_explosion");
        JsonArray conditions = new JsonArray();
        conditions.add(survivesExplosion);
        pool.add("conditions", conditions);
        JsonArray pools = new JsonArray();
        pools.add(pool);
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:block");
        json.add("pools", pools);
        return json;
    }

    private static JsonObject configuredFeature(GeodeMineral mineral) {
        JsonObject blocks = new JsonObject();
        blocks.add("filling_provider", stateProvider("minecraft:air"));
        blocks.add("inner_layer_provider", weightedInnerProvider(mineral));
        blocks.add("alternate_inner_layer_provider", stateProvider("tfcm:mineral/budding_" + mineral.id()));
        blocks.add("middle_layer_provider", stateProvider("tfc:rock/raw/quartzite"));
        blocks.add("outer_layer_provider", stateProvider("tfc:rock/hardened/basalt"));
        JsonArray placements = new JsonArray();
        for (String name : List.of("small_" + mineral.id() + "_bud", "medium_" + mineral.id() + "_bud",
                "large_" + mineral.id() + "_bud", mineral.id() + "_cluster")) {
            JsonObject state = state("tfcm:mineral/" + name);
            JsonObject properties = new JsonObject();
            properties.addProperty("facing", "up");
            properties.addProperty("waterlogged", "false");
            state.add("Properties", properties);
            placements.add(state);
        }
        blocks.add("inner_placements", placements);
        blocks.addProperty("cannot_replace", "#minecraft:features_cannot_replace");
        blocks.addProperty("invalid_blocks", "#minecraft:geode_invalid_blocks");

        JsonObject config = new JsonObject();
        config.add("blocks", blocks);
        JsonObject layers = new JsonObject();
        layers.addProperty("filling", 1.7);
        layers.addProperty("inner_layer", 2.2);
        layers.addProperty("middle_layer", 3.2);
        layers.addProperty("outer_layer", 4.2);
        config.add("layers", layers);
        JsonObject crack = new JsonObject();
        crack.addProperty("generate_crack_chance", .95);
        crack.addProperty("base_crack_size", 2);
        crack.addProperty("crack_point_offset", 2);
        config.add("crack", crack);
        config.addProperty("use_potential_placements_chance", .35);
        config.addProperty("use_alternate_layer0_chance", 0);
        config.addProperty("placements_require_layer0_alternate", false);
        config.add("outer_wall_distance", uniform(4, 6));
        config.add("distribution_points", uniform(3, 4));
        config.add("point_offset", uniform(1, 2));
        config.addProperty("min_gen_offset", -16);
        config.addProperty("max_gen_offset", 16);
        config.addProperty("noise_multiplier", .05);
        config.addProperty("invalid_blocks_threshold", 1);
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:geode");
        json.add("config", config);
        return json;
    }

    private static JsonObject weightedInnerProvider(GeodeMineral mineral) {
        JsonArray entries = new JsonArray();
        entries.add(weightedState("tfcm:mineral/budding_" + mineral.id(), 2));
        entries.add(weightedState("tfcm:mineral/" + mineral.id() + "_block", 5));
        entries.add(weightedState("tfc:rock/raw/quartzite", 1));
        JsonObject provider = new JsonObject();
        provider.addProperty("type", "minecraft:weighted_state_provider");
        provider.add("entries", entries);
        return provider;
    }

    private static JsonObject weightedState(String block, int weight) {
        JsonObject json = new JsonObject();
        json.add("data", state(block));
        json.addProperty("weight", weight);
        return json;
    }

    private static JsonObject stateProvider(String block) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:simple_state_provider");
        json.add("state", state(block));
        return json;
    }

    private static JsonObject state(String block) {
        JsonObject json = new JsonObject();
        json.addProperty("Name", block);
        return json;
    }

    private static JsonObject uniform(int min, int max) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:uniform");
        json.addProperty("min_inclusive", min);
        json.addProperty("max_inclusive", max);
        return json;
    }

    private static JsonObject placedFeature(GeodeMineral mineral) {
        JsonArray placement = new JsonArray();
        JsonObject rarity = new JsonObject();
        rarity.addProperty("type", "minecraft:rarity_filter");
        rarity.addProperty("chance", 300);
        placement.add(rarity);
        JsonObject square = new JsonObject();
        square.addProperty("type", "minecraft:in_square");
        placement.add(square);
        JsonObject height = new JsonObject();
        height.addProperty("type", "minecraft:height_range");
        JsonObject range = new JsonObject();
        range.addProperty("type", "minecraft:uniform");
        range.add("min_inclusive", absoluteHeight(-48));
        range.add("max_inclusive", absoluteHeight(32));
        height.add("height", range);
        placement.add(height);
        JsonObject biome = new JsonObject();
        biome.addProperty("type", "minecraft:biome");
        placement.add(biome);
        JsonObject json = new JsonObject();
        json.addProperty("feature", "tfcm:" + mineral.id() + "_geode");
        json.add("placement", placement);
        return json;
    }

    private static JsonObject absoluteHeight(int value) {
        JsonObject json = new JsonObject();
        json.addProperty("absolute", value);
        return json;
    }

    private static JsonObject tag(List<String> values) {
        JsonObject json = new JsonObject();
        json.addProperty("replace", false);
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        json.add("values", array);
        return json;
    }

    private static JsonObject tfcBiomes() {
        List<String> biomes = TFCBiomes.REGISTRY.stream()
                .map(extension -> extension.key().location().toString()).sorted().toList();
        return tag(biomes);
    }

    private static JsonObject biomeModifier(List<String> features) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "neoforge:add_features");
        json.addProperty("biomes", "#tfcm:tfc_overworld");
        JsonArray values = new JsonArray();
        features.forEach(values::add);
        json.add("features", values);
        json.addProperty("step", "underground_ores");
        return json;
    }
}
