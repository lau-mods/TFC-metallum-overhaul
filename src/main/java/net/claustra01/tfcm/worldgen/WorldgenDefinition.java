package net.claustra01.tfcm.worldgen;

import java.util.List;
import java.util.Map;

public final class WorldgenDefinition {
    public record Dimension(List<Vein> veins, List<Geode> geodes) {
        public static final Dimension EMPTY = new Dimension(List.of(), List.of());
    }

    public record Vein(String id, String type, Map<String, List<WeightedBlock>> blocksByRock,
            int minY, int maxY, int rarity, float density, int size, int height,
            int radius, int minSkew, int maxSkew, int minSlant, int maxSlant, float sign,
            long seed, boolean project, boolean projectOffset, boolean nearLava,
            Indicator indicator) {
    }

    public record Indicator(List<WeightedBlock> blocks, int rarity, int depth, int undergroundRarity,
            int undergroundCount) {
    }

    public record Geode(int minY, int maxY, int rarity, WeightedBlock outer,
            WeightedBlock middle, List<WeightedBlock> inner, List<WeightedBlock> filling,
            List<WeightedBlock> innerPlacements) {
    }

    public record WeightedBlock(String block, double weight) {
    }

    private WorldgenDefinition() {
    }
}
