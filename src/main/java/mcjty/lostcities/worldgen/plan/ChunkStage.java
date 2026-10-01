package mcjty.lostcities.worldgen.plan;

// TODO
public enum ChunkStage {
    BASE,
    /** Highway and railway networks. */
    INFRASTRUCTURE,
    /** City membership, multibuilding footprint, and city level. */
    CHARACTERISTICS,
    /** This chunk's own building, street, and palette decisions. */
    CHUNK
}
