package net.claustra01.tfcm.common.processing;

import java.util.Locale;

public enum SandPile {
    BLACK,
    BROWN,
    GREEN,
    PINK,
    RED,
    WHITE,
    YELLOW;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }
}
