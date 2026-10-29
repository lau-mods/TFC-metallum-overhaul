package net.claustra01.tfcm.common.tool.area;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public enum AreaToolType {
    MINING_HAMMER("mining_hammer", BlockTags.MINEABLE_WITH_PICKAXE, 1),
    QUARRY_HAMMER("quarry_hammer", BlockTags.MINEABLE_WITH_PICKAXE, 2),
    EXCAVATOR("excavator", BlockTags.MINEABLE_WITH_SHOVEL, 1);

    private final String id;
    private final TagKey<Block> mineable;
    private final int radius;

    AreaToolType(String id, TagKey<Block> mineable, int radius) {
        this.id = id;
        this.mineable = mineable;
        this.radius = radius;
    }

    public String id() {
        return id;
    }

    public TagKey<Block> mineable() {
        return mineable;
    }

    public int radius() {
        return radius;
    }
}
