package net.claustra01.tfcm.common.processing;

public enum ProcessingForm {
    ROCKY_CHUNKS("rocky_chunks"),
    CHUNKS("chunks"),
    DIRTY_DUST("dirty_dust"),
    POWDER("powder"),
    PELLET("pellet"),
    BRIQUET("briquet");

    private final String id;

    ProcessingForm(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
