package mcjty.lostcities.api;

public class LostChunkCharacteristics {
    public boolean isCity;
    public boolean couldHaveBuilding;   // True if this chunk could contain a building
    public MultiPos multiPos;           // Equal to SINGLE if a single building
    public int cityLevel;               // 0 is lowest city level
    public ILostCityCityStyle cityStyle;
    public ILostCityMultiBuilding multiBuilding;
    public ILostCityBuilding buildingType;

    /**
     * A section of a multibuilding
     */
    public record MultiPos(int x, int z, int w, int h) {
        public static final MultiPos SINGLE = new MultiPos(-1, -1, 1, 1);
        public static final MultiPos C00 = new MultiPos(0, 0, 2, 2);
        public static final MultiPos C10 = new MultiPos(1, 0, 2, 2);
        public static final MultiPos C01 = new MultiPos(0, 1, 2, 2);
        public static final MultiPos C11 = new MultiPos(1, 1, 2, 2);

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
            return x == w-1;
        }

        public boolean isBottomSide() {
            return z == h-1;
        }
    }
}
