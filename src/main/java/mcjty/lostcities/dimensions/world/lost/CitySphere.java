package mcjty.lostcities.dimensions.world.lost;

public class CitySphere {
    private final boolean enabled;
    private final char glassBlock;
    private final char baseBlock;
    private final char sideBlock;

    public CitySphere(boolean enabled, char glassBlock, char baseBlock, char sideBlock) {
        this.enabled = enabled;
        this.glassBlock = glassBlock;
        this.baseBlock = baseBlock;
        this.sideBlock = sideBlock;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public char getGlassBlock() {
        return glassBlock;
    }

    public char getBaseBlock() {
        return baseBlock;
    }

    public char getSideBlock() {
        return sideBlock;
    }
}
