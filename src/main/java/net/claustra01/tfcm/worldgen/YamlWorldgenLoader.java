package net.claustra01.tfcm.worldgen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import net.claustra01.tfcm.worldgen.WorldgenDefinition.Dimension;
import net.claustra01.tfcm.worldgen.WorldgenDefinition.Geode;
import net.claustra01.tfcm.worldgen.WorldgenDefinition.Indicator;
import net.claustra01.tfcm.worldgen.WorldgenDefinition.Vein;
import net.claustra01.tfcm.worldgen.WorldgenDefinition.WeightedBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.neoforged.fml.loading.FMLPaths;

public final class YamlWorldgenLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> VEIN_TYPES = Set.of("tfc:cluster_vein", "tfc:disc_vein",
            "tfc:kaolin_disc_vein", "tfc:pipe_vein");

    private YamlWorldgenLoader() {
    }

    public static Map<String, Dimension> loadAll() {
        Path directory = FMLPaths.CONFIGDIR.get().resolve("tfcm");
        Map<String, Dimension> result = new LinkedHashMap<>();
        for (String name : List.of("overworld", "nether", "end")) {
            Path file = directory.resolve(name + ".yaml");
            try {
                result.put(name, Files.exists(file) ? parse(name, Files.readString(file)) : Dimension.EMPTY);
            } catch (IOException | RuntimeException exception) {
                LOGGER.warn("Skipping invalid TFCM worldgen file {}: {}", file, exception.getMessage());
                result.put(name, Dimension.EMPTY);
            }
        }
        return Map.copyOf(result);
    }

    public static Dimension parse(String dimension, String yaml) {
        return parse(dimension, yaml, warning -> LOGGER.warn("{}", warning));
    }

    static Dimension parse(String dimension, String yaml, Consumer<String> warnings) {
        Object rootValue = new Yaml().load(yaml);
        if (rootValue == null) return Dimension.EMPTY;
        if (!(rootValue instanceof Map<?, ?>)) {
            warn(warnings, dimension, "root", "expected a mapping");
            return Dimension.EMPTY;
        }
        Map<String, Object> root;
        try {
            root = mapping(rootValue);
        } catch (IllegalArgumentException exception) {
            warn(warnings, dimension, "root", exception.getMessage());
            return Dimension.EMPTY;
        }
        List<Vein> veins = new ArrayList<>();
        List<Geode> geodes = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        parseEntries(root.get("veins"), dimension, "veins", ids, warnings,
                (id, entry) -> veins.add(vein(dimension, id, entry)));
        parseEntries(root.get("geodes"), dimension, "geodes", ids, warnings,
                (id, entry) -> geodes.add(geode(entry)));
        return new Dimension(List.copyOf(veins), List.copyOf(geodes));
    }

    private static void parseEntries(Object value, String dimension, String section, Set<String> ids,
            Consumer<String> warnings, EntryParser parser) {
        if (value == null) return;
        if (!(value instanceof List<?> entries)) {
            warn(warnings, dimension, section, "expected a list");
            return;
        }
        for (Object raw : entries) {
            String id = "<invalid>";
            try {
                Map<String, Object> entry = mapping(raw);
                id = string(entry, "id", "");
                require(!id.isEmpty(), "missing id");
                require(ids.add(id), "duplicate id; first entry wins");
                parser.parse(id, entry);
            } catch (IllegalArgumentException exception) {
                warn(warnings, dimension, id.isEmpty() ? "<missing>" : id, exception.getMessage());
            }
        }
    }

    private static Vein vein(String dimension, String id, Map<String, Object> entry) {
        String type = string(entry, "type", "");
        require(VEIN_TYPES.contains(type), "unsupported vein type " + type);
        List<WeightedBlock> blocks = weightedBlocks(entry.get("blocks"));
        Map<String, Integer> tiers = integerMap(entry.get("tier"));
        List<String> rocks = strings(entry.get("rocks"));
        if (rocks.isEmpty()) {
            if (dimension.equals("nether")) rocks = List.of("minecraft:netherrack");
            else if (dimension.equals("end")) rocks = List.of("minecraft:end_stone");
            else throw new IllegalArgumentException("rocks is required in overworld");
        }
        Map<String, List<WeightedBlock>> blocksByRock = new LinkedHashMap<>();
        for (String rock : rocks) {
            String replacement = rockBlock(rock);
            block(replacement);
            List<WeightedBlock> expanded = expand(blocks, replacement, tiers);
            expanded.forEach(weighted -> block(weighted.block()));
            blocksByRock.put(replacement, expanded);
        }
        int minY = integer(entry, "ymin", Integer.MIN_VALUE);
        int maxY = integer(entry, "ymax", Integer.MIN_VALUE);
        int rarity = positiveInt(entry, "rarity", 1);
        float density = finiteFloat(entry, "density", 1);
        require(minY != Integer.MIN_VALUE && maxY != Integer.MIN_VALUE && minY <= maxY, "invalid ymin/ymax");
        require(density > 0 && density <= 1, "density must be in (0, 1]");
        int size = positiveInt(entry, "size", 1);
        int height = positiveInt(entry, "height", 1);
        int radius = positiveInt(entry, "radius", 1);
        int minSkew = integer(entry, "min_skew", 0);
        int maxSkew = integer(entry, "max_skew", 0);
        int minSlant = integer(entry, "min_slant", 0);
        int maxSlant = integer(entry, "max_slant", 0);
        require(minSkew <= maxSkew && minSlant <= maxSlant, "invalid skew or slant range");
        float sign = finiteFloat(entry, "sign", 0);
        require(sign >= -1 && sign <= 1, "sign must be in [-1, 1]");
        Indicator indicator = indicator(entry.get("indicator"), rocks, tiers);
        String randomName = string(entry, "random_name", id);
        long seed = entry.containsKey("seed") ? longValue(entry, "seed", 0) : hash(randomName);
        return new Vein(id, type, Map.copyOf(blocksByRock), minY, maxY, rarity, density, size,
                height, radius, minSkew, maxSkew, minSlant, maxSlant, sign, seed,
                bool(entry, "project", false), bool(entry, "project_offset", false),
                bool(entry, "near_lava", false), indicator);
    }

    private static Indicator indicator(Object value, List<String> rocks, Map<String, Integer> tiers) {
        if (value == null) return null;
        Map<String, Object> entry = mapping(value);
        String template = string(entry, "block", "");
        require(!template.isEmpty(), "indicator block is required");
        Map<String, WeightedBlock> unique = new LinkedHashMap<>();
        for (String rock : rocks) {
            for (WeightedBlock weighted : expand(List.of(new WeightedBlock(template, 1)), rockBlock(rock), tiers)) {
                unique.putIfAbsent(weighted.block(), weighted);
            }
        }
        unique.values().forEach(weighted -> block(weighted.block()));
        return new Indicator(List.copyOf(unique.values()), positiveInt(entry, "rarity", 1),
                nonNegativeInt(entry, "depth", 0), positiveInt(entry, "underground_rarity", 1),
                nonNegativeInt(entry, "underground_count", 0));
    }

    private static Geode geode(Map<String, Object> entry) {
        String type = string(entry, "type", "minecraft:geode");
        require(type.equals("minecraft:geode"), "unsupported geode type " + type);
        int minY = integer(entry, "ymin", Integer.MIN_VALUE);
        int maxY = integer(entry, "ymax", Integer.MIN_VALUE);
        require(minY != Integer.MIN_VALUE && maxY != Integer.MIN_VALUE && minY <= maxY, "invalid ymin/ymax");
        WeightedBlock outer = singleBlock(entry.get("outer"), "outer");
        WeightedBlock middle = singleBlock(entry.get("middle"), "middle");
        List<WeightedBlock> inner = weightedBlocks(entry.get("inner"));
        List<WeightedBlock> filling = weightedBlocks(entry.get("filling"));
        List<WeightedBlock> placements = weightedBlocks(entry.get("inner_placements"));
        block(outer.block());
        block(middle.block());
        inner.forEach(value -> block(value.block()));
        filling.forEach(value -> block(value.block()));
        placements.forEach(value -> block(value.block()));
        return new Geode(minY, maxY, positiveInt(entry, "rarity", 1), outer, middle,
                inner, filling, placements);
    }

    private static List<WeightedBlock> expand(List<WeightedBlock> blocks, String rock,
            Map<String, Integer> tiers) {
        String rockName = ResourceLocation.parse(rock).getPath();
        rockName = rockName.substring(rockName.lastIndexOf('/') + 1);
        List<WeightedBlock> result = new ArrayList<>();
        for (WeightedBlock weighted : blocks) {
            String withRock = weighted.block().replace("{rock}", rockName);
            if (withRock.contains("{tier}")) {
                require(!tiers.isEmpty(), "{tier} used without tier weights");
                tiers.forEach((tier, weight) -> result.add(new WeightedBlock(
                        withRock.replace("{tier}", tier), weighted.weight() * weight)));
            } else {
                result.add(new WeightedBlock(withRock, weighted.weight()));
            }
        }
        return List.copyOf(result);
    }

    static String rockBlock(String rock) {
        return rock.indexOf(':') >= 0 ? rock : "tfc:rock/raw/" + rock;
    }

    private static WeightedBlock singleBlock(Object value, String name) {
        if (value instanceof String string) return new WeightedBlock(string, 1);
        List<WeightedBlock> values = weightedBlocks(value);
        require(values.size() == 1, name + " must contain one block");
        return values.getFirst();
    }

    private static List<WeightedBlock> weightedBlocks(Object value) {
        List<WeightedBlock> result = new ArrayList<>();
        if (value instanceof String string) {
            result.add(new WeightedBlock(string, 1));
        } else if (value instanceof List<?> entries) {
            for (Object raw : entries) {
                Map<String, Object> entry = mapping(raw);
                String id = string(entry, "block", "");
                double weight = number(entry.getOrDefault("weight", 1)).doubleValue();
                require(!id.isEmpty() && Double.isFinite(weight) && weight > 0, "invalid block or weight");
                result.add(new WeightedBlock(id, weight));
            }
        }
        require(!result.isEmpty(), "at least one block is required");
        return List.copyOf(result);
    }

    private static Map<String, Integer> integerMap(Object value) {
        if (value == null) return Map.of();
        Map<String, Integer> result = new LinkedHashMap<>();
        mapping(value).forEach((key, raw) -> {
            Number number = number(raw);
            long exact = number.longValue();
            require(Double.isFinite(number.doubleValue()) && number.doubleValue() == exact
                    && exact > 0 && exact <= Integer.MAX_VALUE, "tier weights must be positive integers");
            result.put(key, (int) exact);
        });
        return Map.copyOf(result);
    }

    private static void block(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        require(location != null && BuiltInRegistries.BLOCK.containsKey(location), "unregistered block " + id);
    }

    private static int positiveInt(Map<String, Object> map, String key, int fallback) {
        int value = integer(map, key, fallback);
        require(value > 0, key + " must be positive");
        return value;
    }

    private static int nonNegativeInt(Map<String, Object> map, String key, int fallback) {
        int value = integer(map, key, fallback);
        require(value >= 0, key + " must not be negative");
        return value;
    }

    private static int integer(Map<String, Object> map, String key, int fallback) {
        if (!map.containsKey(key)) return fallback;
        Number number = number(map.get(key));
        long value = number.longValue();
        require(Double.isFinite(number.doubleValue()) && number.doubleValue() == value
                && value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE, key + " must be an integer");
        return (int) value;
    }

    private static long longValue(Map<String, Object> map, String key, long fallback) {
        if (!map.containsKey(key)) return fallback;
        Number number = number(map.get(key));
        long value = number.longValue();
        require(Double.isFinite(number.doubleValue()) && number.doubleValue() == value, key + " must be an integer");
        return value;
    }

    private static long hash(String value) {
        var seed = RandomSupport.seedFromHashOf(value);
        return seed.seedLo() ^ seed.seedHi();
    }

    private static float finiteFloat(Map<String, Object> map, String key, float fallback) {
        float value = map.containsKey(key) ? number(map.get(key)).floatValue() : fallback;
        require(Float.isFinite(value), key + " must be finite");
        return value;
    }

    private static boolean bool(Map<String, Object> map, String key, boolean fallback) {
        Object value = map.get(key);
        if (value == null) return fallback;
        require(value instanceof Boolean, key + " must be boolean");
        return (Boolean) value;
    }

    private static String string(Map<String, Object> map, String key, String fallback) {
        Object value = map.get(key);
        if (value == null) return fallback;
        require(value instanceof String, key + " must be a string");
        return (String) value;
    }

    private static List<String> strings(Object value) {
        if (value == null) return List.of();
        if (value instanceof String string) return List.of(string);
        require(value instanceof List<?>, "expected a list");
        List<String> result = new ArrayList<>();
        for (Object entry : (List<?>) value) {
            require(entry instanceof String, "expected a string");
            result.add((String) entry);
        }
        return List.copyOf(result);
    }

    private static Number number(Object value) {
        require(value instanceof Number, "expected a number");
        return (Number) value;
    }

    private static Map<String, Object> mapping(Object value) {
        require(value instanceof Map<?, ?>, "expected a mapping");
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
            require(entry.getKey() instanceof String, "mapping keys must be strings");
            result.put((String) entry.getKey(), entry.getValue());
        }
        return result;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }

    private static void warn(Consumer<String> warnings, String dimension, String id, String message) {
        warnings.accept("Skipping TFCM " + dimension + " worldgen entry " + id + ": " + message);
    }

    @FunctionalInterface
    private interface EntryParser {
        void parse(String id, Map<String, Object> entry);
    }
}
