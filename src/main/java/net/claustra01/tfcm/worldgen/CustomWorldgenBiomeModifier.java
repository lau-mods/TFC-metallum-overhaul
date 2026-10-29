package net.claustra01.tfcm.worldgen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.config.TFCMConfig;
import net.claustra01.tfcm.registry.TFCMBiomeModifiers;
import net.claustra01.tfcm.worldgen.WorldgenDefinition.Dimension;
import net.claustra01.tfcm.worldgen.WorldgenDefinition.Geode;
import net.claustra01.tfcm.worldgen.WorldgenDefinition.Vein;
import net.claustra01.tfcm.worldgen.WorldgenDefinition.WeightedBlock;
import net.dries007.tfc.util.collections.IWeighted;
import net.dries007.tfc.util.collections.Weighted;
import net.dries007.tfc.world.feature.TFCFeatures;
import net.dries007.tfc.world.feature.vein.ClusterVeinConfig;
import net.dries007.tfc.world.feature.vein.DiscVeinConfig;
import net.dries007.tfc.world.feature.vein.Indicator;
import net.dries007.tfc.world.feature.vein.PipeVeinConfig;
import net.dries007.tfc.world.feature.vein.VeinConfig;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo.BiomeInfo.Builder;

public final class CustomWorldgenBiomeModifier implements BiomeModifier {
    public static final MapCodec<CustomWorldgenBiomeModifier> CODEC =
            MapCodec.unit(CustomWorldgenBiomeModifier::new);
    private static final TagKey<Biome> TFC_OVERWORLD = TagKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(TFCM.MOD_ID, "tfc_overworld"));

    private final Map<String, Dimension> definitions;
    private final Map<String, List<Holder<PlacedFeature>>> features;

    private CustomWorldgenBiomeModifier() {
        this(YamlWorldgenLoader.loadAll());
    }

    CustomWorldgenBiomeModifier(Map<String, Dimension> definitions) {
        this.definitions = Map.copyOf(definitions);
        Map<String, List<Holder<PlacedFeature>>> built = new LinkedHashMap<>();
        definitions.forEach((dimension, definition) -> {
            List<Holder<PlacedFeature>> dimensionFeatures = new ArrayList<>();
            definition.veins().forEach(vein -> dimensionFeatures.add(Holder.direct(placedVein(vein))));
            definition.geodes().forEach(geode -> dimensionFeatures.add(Holder.direct(placedGeode(geode))));
            built.put(dimension, List.copyOf(dimensionFeatures));
        });
        this.features = Map.copyOf(built);
    }

    @Override
    public void modify(Holder<Biome> biome, Phase phase, Builder builder) {
        if (phase != Phase.AFTER_EVERYTHING || !TFCMConfig.ENABLE_CUSTOM_VEIN_GENERATION.get()) return;
        String dimension = dimension(biome);
        if (dimension != null) modify(dimension, builder);
    }

    void modify(String dimension, Builder builder) {
        Dimension definition = definitions.getOrDefault(dimension, Dimension.EMPTY);
        List<Holder<PlacedFeature>> undergroundOres = builder.getGenerationSettings()
                .getFeatures(GenerationStep.Decoration.UNDERGROUND_ORES);
        if (dimension.equals("overworld")) {
            undergroundOres.removeIf(CustomWorldgenBiomeModifier::isStandardTFCMGeode);
            if (!definition.veins().isEmpty()) undergroundOres.removeIf(CustomWorldgenBiomeModifier::isTFCVein);
        }
        undergroundOres.addAll(features.getOrDefault(dimension, List.of()));
    }

    List<Holder<PlacedFeature>> features(String dimension) {
        return features.getOrDefault(dimension, List.of());
    }

    private static String dimension(Holder<Biome> biome) {
        if (biome.is(BiomeTags.IS_NETHER)) return "nether";
        if (biome.is(BiomeTags.IS_END)) return "end";
        if (biome.is(BiomeTags.IS_OVERWORLD) || biome.is(TFC_OVERWORLD)) return "overworld";
        return null;
    }

    static boolean isStandardTFCMGeode(Holder<PlacedFeature> holder) {
        return holder.unwrapKey().map(key -> key.location().getNamespace().equals(TFCM.MOD_ID)
                && key.location().getPath().endsWith("_geode")).orElse(false);
    }

    static boolean isTFCVein(Holder<PlacedFeature> holder) {
        return holder.unwrapKey().map(key -> {
            ResourceLocation id = key.location();
            String path = id.getPath();
            return id.getNamespace().equals("tfc") && path.startsWith("vein/")
                    && !path.endsWith("_dike") && !path.equals("vein/gravel");
        }).orElse(false);
    }

    static PlacedFeature placedVein(Vein vein) {
        Map<Block, IWeighted<BlockState>> states = new LinkedHashMap<>();
        vein.blocksByRock().forEach((rock, outputs) -> states.put(block(rock), new Weighted<>(outputs.stream()
                .map(output -> Pair.of(block(output.block()).defaultBlockState(), output.weight())).toList())));
        Optional<Indicator> indicator = Optional.ofNullable(vein.indicator()).map(value -> new Indicator(
                value.depth(), value.rarity(), value.undergroundRarity(), value.undergroundCount(),
                new Weighted<>(value.blocks().stream().map(entry -> Pair.of(
                        block(entry.block()).defaultBlockState(), entry.weight())).toList())));
        VeinConfig base = new VeinConfig(states, indicator, vein.rarity(), vein.density(), vein.minY(), vein.maxY(),
                vein.project(), vein.projectOffset(), vein.seed(), vein.nearLava());
        return switch (vein.type()) {
            case "tfc:cluster_vein" -> new PlacedFeature(Holder.direct(new ConfiguredFeature<>(
                    TFCFeatures.CLUSTER_VEIN.get(), new ClusterVeinConfig(base, vein.size()))), List.of());
            case "tfc:disc_vein" -> new PlacedFeature(Holder.direct(new ConfiguredFeature<>(
                    TFCFeatures.DISC_VEIN.get(), new DiscVeinConfig(base, vein.size(), vein.height()))), List.of());
            case "tfc:kaolin_disc_vein" -> new PlacedFeature(Holder.direct(new ConfiguredFeature<>(
                    TFCFeatures.KAOLIN_DISC_VEIN.get(), new DiscVeinConfig(base, vein.size(), vein.height()))), List.of());
            case "tfc:pipe_vein" -> new PlacedFeature(Holder.direct(new ConfiguredFeature<>(
                    TFCFeatures.PIPE_VEIN.get(), new PipeVeinConfig(base, vein.height(), vein.radius(),
                            vein.minSkew(), vein.maxSkew(), vein.minSlant(), vein.maxSlant(), vein.sign()))), List.of());
            default -> throw new IllegalStateException("Validated vein has unknown type " + vein.type());
        };
    }

    private static PlacedFeature placedGeode(Geode geode) {
        List<BlockState> placements = new ArrayList<>();
        List<Integer> placementWeights = integerWeights(geode.innerPlacements());
        for (int index = 0; index < geode.innerPlacements().size(); index++) {
            WeightedBlock value = geode.innerPlacements().get(index);
            BlockState state = block(value.block()).defaultBlockState();
            if (state.hasProperty(AmethystClusterBlock.FACING))
                state = state.setValue(AmethystClusterBlock.FACING, Direction.UP);
            if (state.hasProperty(AmethystClusterBlock.WATERLOGGED))
                state = state.setValue(AmethystClusterBlock.WATERLOGGED, false);
            for (int count = 0; count < placementWeights.get(index); count++) placements.add(state);
        }
        GeodeBlockSettings blocks = new GeodeBlockSettings(provider(geode.filling()), provider(geode.inner()),
                provider(geode.inner()), provider(List.of(geode.middle())), provider(List.of(geode.outer())),
                placements, BlockTags.FEATURES_CANNOT_REPLACE, BlockTags.GEODE_INVALID_BLOCKS);
        GeodeConfiguration config = new GeodeConfiguration(blocks, new GeodeLayerSettings(1.7, 2.2, 3.2, 4.2),
                new GeodeCrackSettings(.95, 2, 2), .35, 0, false, UniformInt.of(4, 6),
                UniformInt.of(3, 4), UniformInt.of(1, 2), -16, 16, .05, 1);
        return new PlacedFeature(Holder.direct(new ConfiguredFeature<>(Feature.GEODE, config)),
                List.of(RarityFilter.onAverageOnceEvery(geode.rarity()), InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(geode.minY()),
                                VerticalAnchor.absolute(geode.maxY())), BiomeFilter.biome()));
    }

    private static BlockStateProvider provider(List<WeightedBlock> values) {
        if (values.size() == 1) return SimpleStateProvider.simple(block(values.getFirst().block()));
        SimpleWeightedRandomList.Builder<BlockState> builder = SimpleWeightedRandomList.builder();
        List<Integer> weights = integerWeights(values);
        for (int index = 0; index < values.size(); index++) {
            builder.add(block(values.get(index).block()).defaultBlockState(), weights.get(index));
        }
        return new WeightedStateProvider(builder);
    }

    private static List<Integer> integerWeights(List<WeightedBlock> values) {
        List<Integer> weights = values.stream()
                .map(value -> Math.max(1, (int) Math.round(value.weight() * 1000))).toList();
        int divisor = weights.getFirst();
        for (int weight : weights) {
            int left = divisor;
            int right = weight;
            while (right != 0) {
                int remainder = left % right;
                left = right;
                right = remainder;
            }
            divisor = left;
        }
        int commonDivisor = divisor;
        // Vanilla geode providers use integer weights; three decimals preserve normal YAML ratios.
        return weights.stream().map(weight -> weight / commonDivisor).toList();
    }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id));
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return TFCMBiomeModifiers.CUSTOM_WORLDGEN.get();
    }
}
