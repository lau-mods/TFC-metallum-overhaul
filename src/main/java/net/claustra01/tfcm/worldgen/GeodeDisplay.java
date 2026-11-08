package net.claustra01.tfcm.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.claustra01.tfcm.worldgen.WorldgenDefinition.Dimension;
import net.claustra01.tfcm.worldgen.WorldgenDefinition.Geode;
import net.claustra01.tfcm.worldgen.WorldgenDefinition.WeightedBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** What recipe viewers show of one YAML geode: the clusters grown inside it and where it generates. */
public record GeodeDisplay(String dimension, int minY, int maxY, int rarity, List<ItemStack> clusters) {
    public static final StreamCodec<RegistryFriendlyByteBuf, GeodeDisplay> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, GeodeDisplay::dimension,
            ByteBufCodecs.VAR_INT, GeodeDisplay::minY,
            ByteBufCodecs.VAR_INT, GeodeDisplay::maxY,
            ByteBufCodecs.VAR_INT, GeodeDisplay::rarity,
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), GeodeDisplay::clusters,
            GeodeDisplay::new);

    /** Every geode of every dimension in file order; geodes without a cluster are left out. */
    public static List<GeodeDisplay> build(Map<String, Dimension> definitions) {
        List<GeodeDisplay> result = new ArrayList<>();
        definitions.forEach((dimension, definition) -> definition.geodes().forEach(geode -> {
            List<ItemStack> clusters = clusters(geode);
            if (!clusters.isEmpty())
                result.add(new GeodeDisplay(dimension, geode.minY(), geode.maxY(), geode.rarity(), clusters));
        }));
        return List.copyOf(result);
    }

    /** The inner placements named as clusters, such as tfcm:mineral/amethyst_cluster; buds are not shown. */
    private static List<ItemStack> clusters(Geode geode) {
        return geode.innerPlacements().stream().map(WeightedBlock::block).distinct()
                .filter(block -> ResourceLocation.parse(block).getPath().endsWith("cluster"))
                .map(block -> new ItemStack(BuiltInRegistries.BLOCK.get(ResourceLocation.parse(block)).asItem()))
                .filter(stack -> !stack.isEmpty()).toList();
    }
}
