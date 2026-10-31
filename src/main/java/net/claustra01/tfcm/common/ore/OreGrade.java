package net.claustra01.tfcm.common.ore;

import java.util.Locale;

public enum OreGrade {
    POOR,
    NORMAL,
    RICH;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }
}
