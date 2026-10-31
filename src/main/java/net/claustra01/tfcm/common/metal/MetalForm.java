package net.claustra01.tfcm.common.metal;

import java.util.List;

public enum MetalForm {
    INGOT("ingot", 100, "large", "medium"), DOUBLE_INGOT("double_ingot", 200, "large", "heavy"),
    SHEET("sheet", 200, "large", "medium"), DOUBLE_SHEET("double_sheet", 400, "large", "heavy"),
    ROD("rod", 50, "normal", "light"), BLOCK("block", 100, "large", "medium"),
    SLAB("slab", 50, "small", "very_light"), STAIRS("stairs", 75, "small", "light"),
    FOIL("foil", 100, "large", "medium"), GEAR("gear", 400, "large", "heavy"),
    HEAVY_SHEET("heavy_sheet", 600, "large", "heavy"), NAIL("nail", 5, "very_small", "very_light"),
    RING("ring", 12, "small", "light"), RIVET("rivet", 5, "very_small", "very_light"),
    SCREW("screw", 5, "very_small", "very_light"), STAMEN("stamen", 25, "small", "light"),
    WIRE("wire", 25, "small", "light"), PLATE("plate", 100, "normal", "medium"),
    NUGGET("nugget", 10, "tiny", "very_light"), DUST("dust", 100, "small", "light");

    public final String id;
    public final int amount;
    public final String size;
    public final String weight;

    MetalForm(String id, int amount, String size, String weight) {
        this.id = id;
        this.amount = amount;
        this.size = size;
        this.weight = weight;
    }

    public boolean isAddedForm() {
        return ordinal() >= FOIL.ordinal();
    }

    public boolean isAnvilForm() {
        return isAddedForm() && this != HEAVY_SHEET && this != NUGGET && this != DUST;
    }

    public boolean isItem() {
        return this != BLOCK && this != SLAB && this != STAIRS;
    }

    public String tag() {
        return switch (this) {
            case BLOCK, SLAB, STAIRS -> "storage_blocks";
            case HEAVY_SHEET -> "heavy_sheets";
            default -> id + "s";
        };
    }

    public MetalForm anvilInput() {
        return switch (this) {
            case FOIL -> SHEET;
            case GEAR -> DOUBLE_SHEET;
            case NAIL, RING, RIVET, SCREW -> STAMEN;
            case STAMEN, WIRE -> ROD;
            default -> INGOT;
        };
    }

    public int anvilCount() {
        return switch (this) {
            case FOIL, RING, STAMEN, WIRE -> 2;
            case NAIL, RIVET, SCREW -> 5;
            default -> 1;
        };
    }

    public List<String> anvilRules() {
        return switch (this) {
            case GEAR -> List.of("punch_last", "hit_second_last", "upset_third_last");
            case NAIL -> List.of("punch_last", "hit_not_last", "bend_not_last");
            case RING -> List.of("hit_last", "draw_second_last", "draw_third_last");
            case RIVET -> List.of("draw_not_last", "bend_any", "hit_any");
            case SCREW -> List.of("hit_any", "hit_any", "draw_last");
            case STAMEN -> List.of("bend_last", "draw_second_last", "draw_third_last");
            default -> List.of("hit_last", "hit_second_last", "hit_third_last");
        };
    }

    public String item(Metal metal) {
        return switch (this) {
            case BLOCK -> "tfcm:metal/block/" + metal.id();
            case SLAB, STAIRS -> "tfcm:metal/block/" + metal.id() + "_" + id;
            default -> "tfcm:metal/" + id + "/" + metal.id();
        };
    }
}
