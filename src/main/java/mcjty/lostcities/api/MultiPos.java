package mcjty.lostcities.api;

/**
 * A section of a multibuilding
 */
public record MultiPos(int x, int z, int w, int h) {
    public static final MultiPos SINGLE = new MultiPos(-1, -1, 1, 1);

    public boolean isSingle() {
        return x == -1;
    }

    public boolean isMulti() {
        return x != -1;
    }

    public boolean isTopLeft() {
        return x == 0 && z == 0;
    }

    public boolean isRightSide() {
        return x == w - 1;
    }

    public boolean isBottomSide() {
        return z == h - 1;
    }
}
