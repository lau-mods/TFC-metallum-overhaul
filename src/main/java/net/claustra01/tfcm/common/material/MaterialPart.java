package net.claustra01.tfcm.common.material;

public enum MaterialPart {
    LEATHER_STRING("leather_string", "tiny", "very_light"),
    LEATHER_STRAP("leather_strap", "small", "light"),
    LEATHER_STRIP("leather_strip", "tiny", "very_light"),
    PLANT_FIBER("plant_fiber", "tiny", "very_light"),
    PLANT_STRING("plant_string", "tiny", "very_light"),
    PLANT_FABRIC("plant_fabric", "small", "light"),
    PLANT_CLOTHING("plant_clothing", "normal", "light"),
    BAMBOO_FIBER("bamboo_fiber", "tiny", "very_light"),
    BAMBOO_STRING("bamboo_string", "tiny", "very_light"),
    BOW_LIMB("bow_limb", "large", "light"),
    BOW_CHORD("bow_chord", "small", "very_light"),
    BOW_GRIP("bow_grip", "small", "light"),
    CROSSBOW_LIMB("crossbow_limb", "large", "light"),
    CROSSBOW_CHORD("crossbow_chord", "small", "very_light"),
    CROSSBOW_STOCK("crossbow_stock", "large", "medium"),
    CROSSBOW_TRIGGER("crossbow_trigger", "tiny", "very_light"),
    ARROW_TREAD("arrow_tread", "tiny", "very_light"),
    ARROW_FLETCH("arrow_fletch", "tiny", "very_light"),
    ARROW_SHAFT("arrow_shaft", "small", "very_light"),
    TOOL_HANDLE("tool_handle", "large", "light"),
    TOOL_BINDING("tool_binding", "small", "very_light"),
    TOOL_RIVETING("tool_riveting", "tiny", "very_light"),
    SWORD_CROSSGUARD("sword_crossguard", "small", "light"),
    SWORD_POMMEL("sword_pommel", "small", "light");

    private final String id;
    private final String size;
    private final String weight;

    MaterialPart(String id, String size, String weight) {
        this.id = id;
        this.size = size;
        this.weight = weight;
    }

    public String id() {
        return id;
    }

    public String size() {
        return size;
    }

    public String weight() {
        return weight;
    }
}
