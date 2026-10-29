package net.claustra01.tfcm.common.block.geode;

public enum GeodeMineral {
    QUARTZ("quartz", "tfcm:gem/cut_quartz"),
    AMETHYST("amethyst", "tfc:gem/amethyst"),
    CERTUS_QUARTZ("certus_quartz", "tfcm:gem/certus_quartz");

    private final String id;
    private final String clusterDrop;

    GeodeMineral(String id, String clusterDrop) {
        this.id = id;
        this.clusterDrop = clusterDrop;
    }

    public String id() {
        return id;
    }

    public String clusterDrop() {
        return clusterDrop;
    }
}
