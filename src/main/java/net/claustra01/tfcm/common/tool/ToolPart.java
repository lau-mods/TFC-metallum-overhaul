package net.claustra01.tfcm.common.tool;

import java.util.Locale;

import net.dries007.tfc.util.Metal.ItemType;

public enum ToolPart {
    TUYERE(ItemType.TUYERE), FISH_HOOK(ItemType.FISH_HOOK), FISHING_ROD(ItemType.FISHING_ROD),
    PICKAXE(ItemType.PICKAXE), PICKAXE_HEAD(ItemType.PICKAXE_HEAD),
    PROPICK(ItemType.PROPICK), PROPICK_HEAD(ItemType.PROPICK_HEAD),
    AXE(ItemType.AXE), AXE_HEAD(ItemType.AXE_HEAD), SHOVEL(ItemType.SHOVEL), SHOVEL_HEAD(ItemType.SHOVEL_HEAD),
    HOE(ItemType.HOE), HOE_HEAD(ItemType.HOE_HEAD), CHISEL(ItemType.CHISEL), CHISEL_HEAD(ItemType.CHISEL_HEAD),
    HAMMER(ItemType.HAMMER), HAMMER_HEAD(ItemType.HAMMER_HEAD), SAW(ItemType.SAW), SAW_BLADE(ItemType.SAW_BLADE),
    JAVELIN(ItemType.JAVELIN), JAVELIN_HEAD(ItemType.JAVELIN_HEAD),
    SWORD(ItemType.SWORD), SWORD_BLADE(ItemType.SWORD_BLADE), MACE(ItemType.MACE), MACE_HEAD(ItemType.MACE_HEAD),
    KNIFE(ItemType.KNIFE), KNIFE_BLADE(ItemType.KNIFE_BLADE), SCYTHE(ItemType.SCYTHE), SCYTHE_BLADE(ItemType.SCYTHE_BLADE),
    SHEARS(ItemType.SHEARS), UNFINISHED_HELMET(ItemType.UNFINISHED_HELMET), HELMET(ItemType.HELMET),
    UNFINISHED_CHESTPLATE(ItemType.UNFINISHED_CHESTPLATE), CHESTPLATE(ItemType.CHESTPLATE),
    UNFINISHED_GREAVES(ItemType.UNFINISHED_GREAVES), GREAVES(ItemType.GREAVES),
    UNFINISHED_BOOTS(ItemType.UNFINISHED_BOOTS), BOOTS(ItemType.BOOTS),
    HORSE_ARMOR(ItemType.HORSE_ARMOR), SHIELD(ItemType.SHIELD);

    private final ItemType itemType;

    ToolPart(ItemType itemType) {
        this.itemType = itemType;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public ItemType itemType() {
        return itemType;
    }

    public boolean isFinishedTool() {
        return switch (this) {
            case PICKAXE, PROPICK, AXE, SHOVEL, HOE, CHISEL, HAMMER, SAW, JAVELIN, SWORD, MACE, KNIFE, SCYTHE,
                    SHEARS, FISHING_ROD, SHIELD -> true;
            default -> false;
        };
    }
}
