package mcjty.lostcities.worldgen.lost.regassets.data;

import java.util.Optional;

/**
 * Represents a condition
 */
public class ConditionTest {
    private final Boolean top;
    private final Boolean ground;
    private final Boolean cellar;
    private final Boolean isbuilding;
    private final Boolean issphere;
    private final Integer floor;
    private final Integer chunkx;
    private final Integer chunkz;
    private final String inpart;
    private final String inbuilding;
    private final String inbiome;
    private final String range;

    public Boolean getTop() {
        return top;
    }

    public Boolean getGround() {
        return ground;
    }

    public Boolean getCellar() {
        return cellar;
    }

    public Boolean getIsbuilding() {
        return isbuilding;
    }

    public Boolean getIssphere() {
        return issphere;
    }

    public Integer getFloor() {
        return floor;
    }

    public Integer getChunkx() {
        return chunkx;
    }

    public Integer getChunkz() {
        return chunkz;
    }

    public String getInpart() {
        return inpart;
    }

    public String getInbuilding() {
        return inbuilding;
    }

    public String getInbiome() {
        return inbiome;
    }

    public String getRange() {
        return range;
    }

    public ConditionTest(
            Optional<Boolean> top,
            Optional<Boolean> ground,
            Optional<Boolean> cellar,
            Optional<Boolean> isbuilding,
            Optional<Boolean> issphere,
            Optional<Integer> floor,
            Optional<Integer> chunkx,
            Optional<Integer> chunkz,
            Optional<String> inpart,
            Optional<String> inbuilding,
            Optional<String> inbiome,
            Optional<String> range) {
        this.top = top.orElse(null);
        this.ground = ground.orElse(null);
        this.cellar = cellar.orElse(null);
        this.isbuilding = isbuilding.orElse(null);
        this.issphere = issphere.orElse(null);
        this.floor = floor.orElse(null);
        this.chunkx = chunkx.orElse(null);
        this.chunkz = chunkz.orElse(null);
        this.inpart = inpart.orElse(null);
        this.inbuilding = inbuilding.orElse(null);
        this.inbiome = inbiome.orElse(null);
        this.range = range.orElse(null);
    }
}
