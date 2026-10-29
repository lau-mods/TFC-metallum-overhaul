package net.claustra01.tfcm.compat.firmalife;

import net.claustra01.tfcm.common.ore.OreGrade;
import net.claustra01.tfcm.registry.TFCMBlocks;

public final class FirmaLifeCompat {
    private FirmaLifeCompat() {
    }

    public static void register() {
        for (OreGrade grade : OreGrade.values()) {
            TFCMBlocks.registerDimensionOre(TFCMBlocks.CHROMITE_ORES, grade.id() + "_chromite");
        }
    }
}
