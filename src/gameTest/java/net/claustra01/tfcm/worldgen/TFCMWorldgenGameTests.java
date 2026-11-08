package net.claustra01.tfcm.worldgen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mojang.datafixers.util.Pair;

import io.netty.buffer.Unpooled;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.config.TFCMConfig;
import net.dries007.tfc.world.feature.TFCFeatures;
import net.dries007.tfc.world.feature.vein.ClusterVeinConfig;
import net.dries007.tfc.world.feature.vein.DiscVeinConfig;
import net.dries007.tfc.world.feature.vein.PipeVeinConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo.BiomeInfo;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo.BiomeInfo.Builder;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TFCM.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TFCMWorldgenGameTests {
    private static final String BATCH = "tfcm_worldgen";

    private TFCMWorldgenGameTests() {
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void threeDimensionFilesKeepTheirOwnDefinitions(GameTestHelper helper) {
        try {
            var overworld = YamlWorldgenLoader.parse("overworld", yaml("overworld_vein",
                    "tfc:cluster_vein", "minecraft:stone", "minecraft:iron_ore") + geode("minecraft:stone"));
            var nether = YamlWorldgenLoader.parse("nether", yaml("nether_vein",
                    "tfc:disc_vein", null, "minecraft:nether_quartz_ore"));
            var end = YamlWorldgenLoader.parse("end", yaml("end_vein",
                    "tfc:pipe_vein", null, "minecraft:end_stone"));
            var modifier = new CustomWorldgenBiomeModifier(Map.of(
                    "overworld", overworld, "nether", nether, "end", end));

            Builder overworldBuilder = builder(helper, Biomes.PLAINS, List.of());
            Builder secondOverworldBuilder = builder(helper, Biomes.FOREST, List.of());
            Builder netherBuilder = builder(helper, Biomes.NETHER_WASTES, List.of());
            Builder endBuilder = builder(helper, Biomes.END_HIGHLANDS, List.of());
            modifier.modify(biome(helper, Biomes.PLAINS), BiomeModifier.Phase.AFTER_EVERYTHING, overworldBuilder);
            modifier.modify(biome(helper, Biomes.FOREST), BiomeModifier.Phase.AFTER_EVERYTHING,
                    secondOverworldBuilder);
            modifier.modify(biome(helper, Biomes.NETHER_WASTES), BiomeModifier.Phase.AFTER_EVERYTHING, netherBuilder);
            modifier.modify(biome(helper, Biomes.END_HIGHLANDS), BiomeModifier.Phase.AFTER_EVERYTHING, endBuilder);

            List<Holder<PlacedFeature>> overworldFeatures = features(overworldBuilder);
            List<Holder<PlacedFeature>> netherFeatures = features(netherBuilder);
            List<Holder<PlacedFeature>> endFeatures = features(endBuilder);
            require(overworldFeatures.size() == 2
                    && overworldFeatures.get(0).value().feature().value().config() instanceof ClusterVeinConfig
                    && overworldFeatures.get(1).value().feature().value().config() instanceof GeodeConfiguration,
                    "Overworld did not receive only its vein and geode");
            GeodeConfiguration geode = (GeodeConfiguration) overworldFeatures.get(1).value().feature().value().config();
            var placements = geode.geodeBlockSettings.innerPlacements;
            require(placements.size() == 100001
                    && placements.get(99999).is(net.minecraft.world.level.block.Blocks.AMETHYST_CLUSTER)
                    && placements.get(100000).is(net.minecraft.world.level.block.Blocks.SMALL_AMETHYST_BUD),
                    "Small fractional geode weights lost their configured ratio");
            require(netherFeatures.size() == 1
                    && netherFeatures.getFirst().value().feature().value().config() instanceof DiscVeinConfig,
                    "Nether did not receive only its disc vein");
            require(endFeatures.size() == 1
                    && endFeatures.getFirst().value().feature().value().config() instanceof PipeVeinConfig,
                    "End did not receive only its pipe vein");
            require(((DiscVeinConfig) netherFeatures.getFirst().value().feature().value().config()).config()
                    .states().containsKey(BuiltInRegistries.BLOCK.get(ResourceLocation.parse("minecraft:netherrack")))
                    && ((PipeVeinConfig) endFeatures.getFirst().value().feature().value().config()).config()
                    .states().containsKey(BuiltInRegistries.BLOCK.get(ResourceLocation.parse("minecraft:end_stone"))),
                    "Nether and End did not use their default replacement rocks");
            require(overworldFeatures.getFirst() == features(secondOverworldBuilder).getFirst(),
                    "Biomes did not share the dimension's placed feature holders");
            helper.succeed();
        } catch (Throwable throwable) {
            helper.fail(throwable.getMessage());
        }
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void templatesWeightsRocksTiersAndIndicatorsReachTfcConfig(GameTestHelper helper) {
        try {
            String yaml = """
                    veins:
                      - id: graded
                        type: tfc:cluster_vein
                        ymin: -32
                        ymax: 48
                        rarity: 20
                        density: 0.4
                        size: 12
                        rocks: [basalt, granite]
                        tier: {poor: 7, normal: 3}
                        blocks:
                          - block: tfcm:ore/{tier}_native_platinum/{rock}
                            weight: 2
                        indicator:
                          block: tfcm:ore/small_native_platinum
                          rarity: 14
                          depth: 35
                          underground_rarity: 2
                          underground_count: 3
                      - id: disc
                        type: tfc:disc_vein
                        ymin: 1
                        ymax: 20
                        rocks: [minecraft:stone]
                        blocks: [{block: minecraft:iron_ore, weight: 1}]
                        size: 9
                        height: 4
                      - id: kaolin
                        type: tfc:kaolin_disc_vein
                        ymin: 2
                        ymax: 21
                        rocks: [minecraft:stone]
                        blocks: [{block: minecraft:clay, weight: 1}]
                        size: 8
                        height: 3
                      - id: pipe
                        type: tfc:pipe_vein
                        ymin: 3
                        ymax: 22
                        rocks: [minecraft:stone]
                        blocks: [{block: minecraft:gold_ore, weight: 1}]
                        height: 15
                        radius: 6
                        min_skew: -2
                        max_skew: 3
                        min_slant: 1
                        max_slant: 4
                        sign: -0.5
                    """;
            List<WorldgenDefinition.Vein> definitions = YamlWorldgenLoader.parse("overworld", yaml).veins();
            require(definitions.size() == 4, "Not all four TFC vein types were parsed");
            var cluster = (ClusterVeinConfig) CustomWorldgenBiomeModifier.placedVein(definitions.get(0))
                    .feature().value().config();
            var basalt = BuiltInRegistries.BLOCK.get(ResourceLocation.parse("tfc:rock/raw/basalt"));
            var granite = BuiltInRegistries.BLOCK.get(ResourceLocation.parse("tfc:rock/raw/granite"));
            require(cluster.config().states().size() == 2
                    && cluster.config().states().keySet().containsAll(List.of(basalt, granite)),
                    "Both rock conditions were not retained or an unrelated rock condition matched");
            for (var outputs : cluster.config().states().values()) {
                double previous = 0;
                List<Double> weights = new ArrayList<>();
                for (Pair<BlockState, Double> output : outputs.weightedValues()) {
                    weights.add(output.getSecond() - previous);
                    previous = output.getSecond();
                }
                require(weights.equals(List.of(14.0, 6.0)),
                        "Block and tier weights were not multiplied");
            }
            require(cluster.config().indicator().isPresent()
                    && cluster.config().indicator().get().rarity() == 14
                    && cluster.config().indicator().get().depth() == 35
                    && cluster.config().indicator().get().undergroundRarity() == 2
                    && cluster.config().indicator().get().undergroundCount() == 3,
                    "Surface and underground indicator settings were not retained");
            var disc = (DiscVeinConfig) CustomWorldgenBiomeModifier.placedVein(definitions.get(1))
                    .feature().value().config();
            var kaolin = (DiscVeinConfig) CustomWorldgenBiomeModifier.placedVein(definitions.get(2))
                    .feature().value().config();
            var pipe = (PipeVeinConfig) CustomWorldgenBiomeModifier.placedVein(definitions.get(3))
                    .feature().value().config();
            require(disc.size() == 9 && disc.height() == 4 && kaolin.size() == 8 && kaolin.height() == 3,
                    "Disc or kaolin disc parameters were not retained");
            require(CustomWorldgenBiomeModifier.placedVein(definitions.get(1)).feature().value().feature()
                    == TFCFeatures.DISC_VEIN.get()
                    && CustomWorldgenBiomeModifier.placedVein(definitions.get(2)).feature().value().feature()
                    == TFCFeatures.KAOLIN_DISC_VEIN.get(), "Disc definitions used the wrong TFC feature");
            require(pipe.height() == 15 && pipe.radius() == 6 && pipe.minSkew() == -2 && pipe.maxSkew() == 3
                    && pipe.minSlant() == 1 && pipe.maxSlant() == 4 && pipe.sign() == -0.5F,
                    "Pipe parameters were not retained");
            helper.succeed();
        } catch (Throwable throwable) {
            helper.fail(throwable.getMessage());
        }
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void veinDisplaysAlignRocksOresWeightsAndIndicators(GameTestHelper helper) {
        try {
            String overworldYaml = """
                    veins:
                      - id: graded
                        type: tfc:cluster_vein
                        ymin: -32
                        ymax: 48
                        rarity: 20
                        density: 0.4
                        size: 12
                        rocks: [basalt, granite]
                        tier: {poor: 7, normal: 3}
                        blocks:
                          - block: tfcm:ore/{tier}_native_platinum/{rock}
                        indicator:
                          block: tfcm:ore/small_native_platinum
                          rarity: 14
                          depth: 35
                          underground_rarity: 2
                          underground_count: 3
                    """;
            String netherYaml = """
                    veins:
                      - id: itemless
                        type: tfc:pipe_vein
                        ymin: 3
                        ymax: 22
                        height: 15
                        radius: 6
                        blocks: [{block: minecraft:nether_gold_ore, weight: 3}, {block: minecraft:water, weight: 1}]
                        indicator:
                          block: tfcm:ore/small_fluorite
                    """;
            var definitions = new LinkedHashMap<String, WorldgenDefinition.Dimension>();
            definitions.put("overworld", YamlWorldgenLoader.parse("overworld", overworldYaml));
            definitions.put("nether", YamlWorldgenLoader.parse("nether", netherYaml));
            var server = helper.getLevel().getServer();
            List<VeinDisplay> veins = VeinDisplay.build(definitions, server);
            require(veins.size() == 2, "Each valid YAML vein did not become one display");

            VeinDisplay graded = veins.get(0);
            require(graded.dimension().equals("overworld") && graded.id().equals("graded")
                    && graded.type().equals("tfc:cluster_vein") && graded.minY() == -32 && graded.maxY() == 48
                    && graded.rarity() == 20 && graded.density() == 0.4F && graded.size() == 12,
                    "Vein values were not taken from the YAML");
            require(items(graded.rocks()).equals(List.of("tfc:rock/raw/basalt", "tfc:rock/raw/granite")),
                    "Rocks were not shown in YAML order");
            require(graded.ores().size() == 2
                    && items(graded.ores().get(0).stacks()).equals(List.of(
                            "tfcm:ore/poor_native_platinum/basalt", "tfcm:ore/poor_native_platinum/granite"))
                    && items(graded.ores().get(1).stacks()).equals(List.of(
                            "tfcm:ore/normal_native_platinum/basalt", "tfcm:ore/normal_native_platinum/granite")),
                    "Ores were not aligned with the rocks they replace");
            require(Math.abs(graded.ores().get(0).chance() - .7F) < 1e-6
                    && Math.abs(graded.ores().get(1).chance() - .3F) < 1e-6, "Tier weights were not normalized");
            require(graded.indicator().isPresent()
                    && items(graded.indicator().get().stacks()).equals(List.of("tfcm:ore/small_native_platinum"))
                    && graded.indicator().get().rarity() == 14 && graded.indicator().get().depth() == 35
                    && graded.indicator().get().undergroundRarity() == 2
                    && graded.indicator().get().undergroundCount() == 3, "Indicator values were not kept");

            VeinDisplay itemless = veins.get(1);
            require(itemless.dimension().equals("nether")
                    && items(itemless.rocks()).equals(List.of("minecraft:netherrack"))
                    && itemless.ores().size() == 1
                    && items(itemless.ores().getFirst().stacks()).equals(List.of("minecraft:nether_gold_ore"))
                    && Math.abs(itemless.ores().getFirst().chance() - .75F) < 1e-6
                    && itemless.height() == 15 && itemless.radius() == 6,
                    "A block without an item was shown or changed the other blocks' share");
            require(itemless.indicator().isPresent() && items(itemless.indicator().get().stacks())
                    .equals(List.of("tfcm:ore/fluorite")), "An item-less sample indicator did not show its drop");

            var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(),
                    helper.getLevel().registryAccess(), ConnectionType.NEOFORGE);
            VeinDisplaySync.Payload.STREAM_CODEC.encode(buffer, new VeinDisplaySync.Payload(veins));
            List<VeinDisplay> decoded = VeinDisplaySync.Payload.STREAM_CODEC.decode(buffer).veins();
            require(decoded.size() == 2 && decoded.get(0).id().equals("graded") && decoded.get(1).minY() == 3
                    && items(decoded.get(0).ores().get(1).stacks()).equals(items(graded.ores().get(1).stacks()))
                    && decoded.get(0).ores().get(0).chance() == graded.ores().get(0).chance()
                    && decoded.get(0).indicator().orElseThrow().undergroundCount() == 3
                    && items(decoded.get(0).indicator().orElseThrow().stacks()).equals(
                            items(graded.indicator().orElseThrow().stacks()))
                    && items(decoded.get(1).indicator().orElseThrow().stacks()).equals(List.of("tfcm:ore/fluorite")),
                    "Vein displays did not survive the payload codec");

            var registered = VeinDisplaySync.definitions(server);
            require(registered.keySet().containsAll(List.of("overworld", "nether", "end")),
                    "The registered TFCM biome modifier's definitions were not found");
            require(VeinDisplaySync.build(server).size() == (TFCMConfig.ENABLE_CUSTOM_VEIN_GENERATION.get()
                    ? VeinDisplay.build(registered, server).size() : 0),
                    "Sent veins did not follow enableCustomVeinGeneration");
            helper.succeed();
        } catch (Throwable throwable) {
            helper.fail(throwable.getMessage());
        }
    }

    private static List<String> items(List<ItemStack> stacks) {
        return stacks.stream().map(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()).toList();
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void invalidDuplicateAndEmptyDefinitionsUseDimensionFallbackRules(GameTestHelper helper) {
        try {
            List<String> warnings = new ArrayList<>();
            String invalid = """
                    veins:
                      - id: duplicate
                        type: tfc:cluster_vein
                        ymin: 0
                        ymax: 20
                        rocks: [minecraft:stone]
                        blocks: [{block: minecraft:iron_ore}]
                      - id: duplicate
                        type: tfc:cluster_vein
                        ymin: 0
                        ymax: 20
                        rocks: [minecraft:stone]
                        blocks: [{block: minecraft:gold_ore}]
                      - malformed
                      - id: missing_block
                        type: tfc:cluster_vein
                        ymin: 0
                        ymax: 20
                        rocks: [minecraft:stone]
                        blocks: [{block: missing:ore}]
                    geodes: invalid_section
                    """;
            var parsed = YamlWorldgenLoader.parse("overworld", invalid, warnings::add);
            require(parsed.veins().size() == 1 && parsed.veins().getFirst().id().equals("duplicate"),
                    "The first valid duplicate entry did not win");
            require(parsed.veins().getFirst().blocksByRock().values().iterator().next().getFirst().block()
                    .equals("minecraft:iron_ore"), "The later duplicate replaced the first entry");
            require(warnings.size() == 4 && warnings.stream().allMatch(message -> message.contains("Skipping TFCM")),
                    "Invalid entries and sections did not each emit a warning");

            Holder<PlacedFeature> vein = placed(helper, "tfc", "vein/surface_native_copper");
            Holder<PlacedFeature> gravel = placed(helper, "tfc", "vein/gravel");
            Holder<PlacedFeature> dike = placed(helper, "tfc", "vein/gabbro_dike");
            Holder<PlacedFeature> standardGeode = placed(helper, TFCM.MOD_ID, "quartz_geode");
            List<Holder<PlacedFeature>> original = List.of(vein, gravel, dike, standardGeode);

            var emptyModifier = new CustomWorldgenBiomeModifier(Map.of(
                    "overworld", WorldgenDefinition.Dimension.EMPTY,
                    "nether", WorldgenDefinition.Dimension.EMPTY,
                    "end", WorldgenDefinition.Dimension.EMPTY));
            Builder fallback = builder(helper, Biomes.PLAINS, original);
            emptyModifier.modify("overworld", fallback);
            require(features(fallback).containsAll(List.of(vein, gravel, dike))
                    && !features(fallback).contains(standardGeode),
                    "Empty Overworld did not keep TFC veins, gravel and dikes while removing standard geodes");
            Builder emptyNether = builder(helper, Biomes.NETHER_WASTES, List.of());
            Builder emptyEnd = builder(helper, Biomes.END_HIGHLANDS, List.of());
            emptyModifier.modify("nether", emptyNether);
            emptyModifier.modify("end", emptyEnd);
            require(features(emptyNether).isEmpty() && features(emptyEnd).isEmpty(),
                    "Empty Nether or End generated features");

            var customModifier = new CustomWorldgenBiomeModifier(Map.of("overworld", parsed));
            Builder replaced = builder(helper, Biomes.PLAINS, original);
            customModifier.modify("overworld", replaced);
            require(!features(replaced).contains(vein) && !features(replaced).contains(standardGeode)
                    && features(replaced).containsAll(List.of(gravel, dike))
                    && features(replaced).contains(customModifier.features("overworld").getFirst()),
                    "Valid custom veins did not replace only TFC ore veins");
            helper.succeed();
        } catch (Throwable throwable) {
            helper.fail(throwable.getMessage());
        }
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void bundledDefaultsLoadAndNeverReplaceExistingFiles(GameTestHelper helper) {
        helper.succeedIf(() -> {
            try {
                List<String> warnings = new ArrayList<>();
                var overworld = YamlWorldgenLoader.parse("overworld", bundled("overworld"), warnings::add);
                boolean firmaLife = ModList.get().isLoaded("firmalife");
                require(warnings.stream().allMatch(warning -> !firmaLife && warning.contains("firmalife")),
                        "Bundled overworld.yaml has invalid entries: " + warnings);
                require(overworld.veins().size() == (firmaLife ? 45 : 43) && overworld.geodes().size() == 2,
                        "Bundled overworld.yaml lost entries");
                require(overworld.veins().stream().anyMatch(vein -> vein.id().equals("tfcm:vein/normal_uraninite")
                        && vein.indicator().rarity() == 0), "An indicator rarity of 0 was rejected");
                overworld.veins().forEach(CustomWorldgenBiomeModifier::placedVein);
                var nether = YamlWorldgenLoader.parse("nether", bundled("nether"));
                var end = YamlWorldgenLoader.parse("end", bundled("end"));
                require(nether.veins().isEmpty() && nether.geodes().isEmpty()
                        && end.veins().isEmpty() && end.geodes().isEmpty(),
                        "Nether and End must not generate anything by default");

                Path directory = Files.createTempDirectory("tfcm-worldgen");
                Path existing = directory.resolve("nether.yaml");
                Files.writeString(existing, "veins: []\n# mine\n");
                YamlWorldgenLoader.installDefaults(directory);
                require(Files.readString(existing).equals("veins: []\n# mine\n"), "An existing file was overwritten");
                require(Files.readString(directory.resolve("overworld.yaml")).equals(bundled("overworld"))
                        && Files.readString(directory.resolve("end.yaml")).equals(bundled("end")),
                        "Missing files did not get the bundled defaults");
            } catch (IOException exception) {
                throw new GameTestAssertException("Cannot read worldgen defaults: " + exception.getMessage());
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void veinReplacementKeepsNonOreVeins(GameTestHelper helper) {
        helper.succeedIf(() -> {
            for (String kept : List.of("vein/gravel", "vein/kaolin_disc", "vein/granite_dike")) {
                require(!CustomWorldgenBiomeModifier.isTFCVein(placed(helper, "tfc", kept)), "Replaced " + kept);
            }
            require(CustomWorldgenBiomeModifier.isTFCVein(placed(helper, "tfc", "vein/normal_malachite")),
                    "An ore vein was kept");
            if (ModList.get().isLoaded("firmalife")) {
                require(CustomWorldgenBiomeModifier.isTFCVein(placed(helper, "firmalife", "vein/normal_chromite")),
                        "FirmaLife's vein would generate twice");
            }
        });
    }

    private static String bundled(String name) throws IOException {
        try (var stream = YamlWorldgenLoader.class.getResourceAsStream(YamlWorldgenLoader.defaultResource(name))) {
            require(stream != null, "Missing bundled " + name);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String yaml(String id, String type, String rock, String block) {
        String rocks = rock == null ? "" : "    rocks: [" + rock + "]\n";
        return "veins:\n  - id: " + id + "\n    type: " + type + "\n    ymin: 0\n"
                + "    ymax: 20\n    rarity: 10\n    density: 0.5\n    size: 8\n    height: 8\n"
                + "    radius: 3\n" + rocks + "    blocks:\n      - block: " + block + "\n";
    }

    private static String geode(String outer) {
        return """
                geodes:
                  - id: overworld_geode
                    type: minecraft:geode
                    ymin: -20
                    ymax: 20
                    rarity: 30
                    outer: %s
                    middle: minecraft:calcite
                    inner: [{block: minecraft:amethyst_block, weight: 2}]
                    filling: [{block: minecraft:air, weight: 1}]
                    inner_placements: [{block: minecraft:amethyst_cluster, weight: 1}, {block: minecraft:small_amethyst_bud, weight: 0.00001}]
                """.formatted(outer);
    }

    private static Builder builder(GameTestHelper helper, ResourceKey<Biome> biomeKey,
            List<Holder<PlacedFeature>> placedFeatures) {
        Biome biome = biome(helper, biomeKey).value();
        BiomeGenerationSettings.PlainBuilder generation = new BiomeGenerationSettings.PlainBuilder();
        placedFeatures.forEach(feature -> generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, feature));
        return Builder.copyOf(new BiomeInfo(biome.getModifiedClimateSettings(), biome.getModifiedSpecialEffects(),
                generation.build(), biome.getMobSettings()));
    }

    private static Holder<Biome> biome(GameTestHelper helper, ResourceKey<Biome> key) {
        return helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(key);
    }

    private static Holder<PlacedFeature> placed(GameTestHelper helper, String namespace, String path) {
        Registry<PlacedFeature> registry = helper.getLevel().registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        return registry.getHolderOrThrow(ResourceKey.create(Registries.PLACED_FEATURE,
                ResourceLocation.fromNamespaceAndPath(namespace, path)));
    }

    private static List<Holder<PlacedFeature>> features(Builder builder) {
        return builder.getGenerationSettings().getFeatures(GenerationStep.Decoration.UNDERGROUND_ORES);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new GameTestAssertException(message);
    }
}
