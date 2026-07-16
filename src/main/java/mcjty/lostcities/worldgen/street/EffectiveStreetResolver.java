package mcjty.lostcities.worldgen.street;

/** Pure final-combination rules shared by worldgen and focused tests. */
public final class EffectiveStreetResolver {

    private EffectiveStreetResolver() {
    }

    public static PlannedRoadType resolve(PlannedRoadType rawRoadType, boolean currentChunkIsCity,
                                          boolean hasConnectedCityNeighbor, boolean overriddenByHigherPrecedenceContent) {
        if (!currentChunkIsCity || !hasConnectedCityNeighbor || overriddenByHigherPrecedenceContent) {
            return PlannedRoadType.NONE;
        }
        return rawRoadType;
    }
}
